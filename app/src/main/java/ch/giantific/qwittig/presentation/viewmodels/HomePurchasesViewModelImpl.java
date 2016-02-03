/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.MessageAction;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import rx.Observable;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by fabio on 21.01.16.
 */
public class HomePurchasesViewModelImpl extends OnlineListViewModelBaseImpl<Purchase, HomePurchasesViewModel.ViewListener>
        implements HomePurchasesViewModel {

    private static final String STATE_IS_LOADING_MORE = "STATE_IS_LOADING_MORE";
    private PurchaseRepository mPurchaseRepo;
    private boolean mIsLoadingMore;

    public HomePurchasesViewModelImpl(@Nullable Bundle savedState,
                                      @NonNull GroupRepository groupRepo,
                                      @NonNull UserRepository userRepository,
                                      @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, groupRepo, userRepository);

        mPurchaseRepo = purchaseRepo;

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
    public void updateList() {
        mSubscriptions.add(mGroupRepo.fetchGroupDataAsync(mCurrentGroup)
                .toObservable()
                .flatMap(new Func1<Group, Observable<Purchase>>() {
                    @Override
                    public Observable<Purchase> call(Group group) {
                        return mPurchaseRepo.getPurchasesLocalAsync(mCurrentUser, group, false);
                    }
                })
                .subscribe(new Subscriber<Purchase>() {
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
                        mView.showMessage(R.string.toast_error_purchases_load);
                    }

                    @Override
                    public void onNext(Purchase purchase) {
                        mItems.add(purchase);
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
    public int getItemViewType(int position) {
        if (mItems.get(position) == null) {
            return TYPE_PROGRESS;
        }

        return TYPE_ITEM;
    }

    @Override
    protected void refreshItems() {
        if (!mView.isNetworkAvailable()) {
            setRefreshing(false);
            mView.showMessageWithAction(R.string.toast_no_connection, getRefreshAction());
            return;
        }

        mView.loadUpdatePurchasesWorker();
    }

    @NonNull
    private MessageAction getRefreshAction() {
        return new MessageAction(R.string.action_retry) {
            @Override
            public void onClick(View v) {
                setRefreshing(true);
                refreshItems();
            }
        };
    }

    @Override
    public void onPurchaseRowItemClick(int position) {
        final Purchase purchase = getItemAtPosition(position);
        mView.startPurchaseDetailsActivity(purchase);
    }

    @Override
    public void setPurchasesUpdateStream(@NonNull Observable<Purchase> observable,
                                         @NonNull final String workerTag) {
        mSubscriptions.add(observable
                .toList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<Purchase>>() {
                    @Override
                    public void onSuccess(List<Purchase> purchases) {
                        mView.removeWorker(workerTag);
                        setRefreshing(false);

                        updateList();
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        setRefreshing(false);

                        mView.showMessageWithAction(mPurchaseRepo.getErrorMessage(error),
                                getRefreshAction());
                    }
                })
        );
    }

    @Override
    public void setPurchasesQueryMoreStream(@NonNull Observable<Purchase> observable,
                                            @NonNull final String workerTag) {
        mSubscriptions.add(observable
                .toList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<Purchase>>() {
                    @Override
                    public void onSuccess(List<Purchase> purchases) {
                        mView.removeWorker(workerTag);
                        mIsLoadingMore = false;

                        removeLoadMore();
                        mItems.addAll(purchases);
                        mView.notifyItemRangeInserted(getItemCount(), purchases.size());
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        mView.showMessageWithAction(mPurchaseRepo.getErrorMessage(error),
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
        mView.loadQueryMorePurchasesWorker(skip);
    }

    @Override
    public boolean isLoadingMore() {
        return !mView.isNetworkAvailable() || mIsLoadingMore || isRefreshing();
    }
}
