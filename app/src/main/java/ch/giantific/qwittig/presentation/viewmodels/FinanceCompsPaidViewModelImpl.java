/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.MessageAction;
import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import rx.Observable;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by fabio on 18.01.16.
 */
public class FinanceCompsPaidViewModelImpl extends OnlineListViewModelBaseImpl<Compensation, FinanceCompsPaidViewModel.ViewListener>
        implements FinanceCompsPaidViewModel {

    private static final String STATE_IS_LOADING_MORE = "STATE_IS_LOADING_MORE";
    private CompensationRepository mCompsRepo;
    private boolean mIsLoadingMore;

    public FinanceCompsPaidViewModelImpl(@Nullable Bundle savedState,
                                         @NonNull GroupRepository groupRepo,
                                         @NonNull UserRepository userRepository,
                                         @NonNull CompensationRepository compsRepo) {
        super(savedState, groupRepo, userRepository);

        mCompsRepo = compsRepo;
        if (savedState != null) {
            mIsLoadingMore = savedState.getBoolean(STATE_IS_LOADING_MORE, false);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_IS_LOADING_MORE, mIsLoadingMore);
    }

    @Override
    public void updateList() {
        mSubscriptions.add(mGroupRepo.fetchGroupDataAsync(mCurrentGroup)
                .toObservable()
                .flatMap(new Func1<Group, Observable<Compensation>>() {
                    @Override
                    public Observable<Compensation> call(Group group) {
                        return mCompsRepo.getCompensationsLocalPaidAsync(mCurrentUser, group);
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
    protected void refreshItems() {
        if (!mView.isNetworkAvailable()) {
            setRefreshing(false);
            mView.showMessageWithAction(R.string.toast_no_connection, getRefreshAction());
            return;
        }

        mView.loadUpdateCompensationsPaidWorker();
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
        mSubscriptions.add(observable
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
    public void setCompensationsUpdateStream(@NonNull Observable<Compensation> observable,
                                             boolean paid, @NonNull final String workerTag) {
        mSubscriptions.add(observable
                .toList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<Compensation>>() {
                    @Override
                    public void onSuccess(List<Compensation> compensations) {
                        mView.removeWorker(workerTag);
                        updateList();
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        setRefreshing(false);
                        mView.showMessageWithAction(mCompsRepo.getErrorMessage(error),
                                getRefreshAction());
                    }
                })
        );
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

    @Override
    public void onNewGroupSet() {
        super.onNewGroupSet();

        updateList();
    }
}
