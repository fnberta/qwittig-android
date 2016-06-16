/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.paid;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.bus.events.EventCompensationConfirmed;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModelBaseImpl;
import ch.giantific.qwittig.utils.MessageAction;
import rx.Observable;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link CompsPaidViewModel}.
 */
public class CompsPaidViewModelImpl
        extends OnlineListViewModelBaseImpl<Compensation, CompsPaidViewModel.ViewListener>
        implements CompsPaidViewModel {

    private static final String STATE_IS_LOADING_MORE = "STATE_IS_LOADING_MORE";
    private final CompensationRepository mCompsRepo;
    private boolean mIsLoadingMore;

    public CompsPaidViewModelImpl(@Nullable Bundle savedState,
                                  @NonNull CompsPaidViewModel.ViewListener view,
                                  @NonNull RxBus<Object> eventBus,
                                  @NonNull UserRepository userRepository,
                                  @NonNull CompensationRepository compsRepo) {
        super(savedState, view, eventBus, userRepository);

        mCompsRepo = compsRepo;
        if (savedState != null) {
            mItems = new ArrayList<>();
            mIsLoadingMore = savedState.getBoolean(STATE_IS_LOADING_MORE, false);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_IS_LOADING_MORE, mIsLoadingMore);
    }

    @Override
    public void onViewVisible() {
        super.onViewVisible();

        getSubscriptions().add(mEventBus.observeEvents(EventCompensationConfirmed.class)
                .subscribe(new Action1<EventCompensationConfirmed>() {
                    @Override
                    public void call(EventCompensationConfirmed eventCompensationConfirmed) {
                        loadData();
                    }
                })
        );
    }

    @Override
    public void loadData() {
        getSubscriptions().add(mUserRepo.fetchIdentityData(mCurrentIdentity)
                .flatMapObservable(new Func1<Identity, Observable<Compensation>>() {
                    @Override
                    public Observable<Compensation> call(Identity identity) {
                        return mCompsRepo.getCompensationsPaid(identity);
                    }
                }).subscribe(new Subscriber<Compensation>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        mItems.clear();
                    }

                    @Override
                    public void onCompleted() {
                        setLoading(false);
                        mView.notifyDataSetChanged();

                        if (mIsLoadingMore) {
                            addLoadMore();
                            mView.scrollToPosition(getLastPosition());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        setLoading(false);
                        mView.showMessage(R.string.toast_error_comps_load);
                    }

                    @Override
                    public void onNext(Compensation compensation) {
                        mItems.add(compensation);
                    }
                })
        );
    }

    private void addLoadMore() {
        mItems.add(null);
        final int lastPosition = getLastPosition();
        mView.notifyItemInserted(lastPosition);
    }

    @Override
    public void onDataUpdated(boolean successful) {
        setRefreshing(false);
        if (successful) {
            loadData();
        } else {
            mView.showMessageWithAction(R.string.toast_error_comps_update, getRefreshAction());
        }
    }

    @Override
    protected void refreshItems() {
        if (!mView.isNetworkAvailable()) {
            setRefreshing(false);
            mView.showMessageWithAction(R.string.toast_no_connection, getRefreshAction());
            return;
        }

        mView.startUpdateCompensationsPaidService();
    }

    @NonNull
    private MessageAction getRefreshAction() {
        return new MessageAction(R.string.action_retry) {
            @Override
            public void onClick(View v) {
                refreshItems();
            }
        };
    }

    @Override
    public int getItemViewType(int position) {
        if (mItems.get(position) == null) {
            return TYPE_PROGRESS;
        }

        return TYPE_ITEM;
    }

    @Override
    public void setCompensationsQueryMoreStream(@NonNull Observable<Compensation> observable,
                                                @NonNull final String workerTag) {
        getSubscriptions().add(observable
                .toList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<Compensation>>() {
                    @Override
                    public void onSuccess(List<Compensation> compensations) {
                        mView.removeWorker(workerTag);
                        mIsLoadingMore = false;

                        removeLoadMore();
                        mItems.addAll(compensations);
                        mView.notifyItemRangeInserted(getItemCount(), compensations.size());
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        mView.showMessageWithAction(mCompsRepo.getErrorMessage(error),
                                new MessageAction(R.string.action_retry) {
                                    @Override
                                    public void onClick(View v) {
                                        onLoadMore();
                                    }
                                });
                        mIsLoadingMore = false;

                        removeLoadMore();
                    }
                })
        );
    }

    private void removeLoadMore() {
        final int finalPosition = getLastPosition();
        mItems.remove(finalPosition);
        mView.notifyItemRemoved(finalPosition);
    }

    @Override
    public void onLoadMore() {
        mIsLoadingMore = true;

        addLoadMore();
        final int skip = mItems.size();
        mView.loadQueryMoreCompensationsPaidWorker(skip);
    }

    @Override
    public boolean isLoadingMore() {
        return !mView.isNetworkAvailable() || mIsLoadingMore || isRefreshing();
    }
}
