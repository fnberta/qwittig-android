/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModelBaseImpl;
import ch.giantific.qwittig.utils.MessageAction;
import rx.Observable;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link PurchasesViewModel}.
 */
public class PurchasesViewModelImpl extends OnlineListViewModelBaseImpl<Purchase, PurchasesViewModel.ViewListener>
        implements PurchasesViewModel {

    private static final String STATE_LOADING_MORE = "STATE_LOADING_MORE";
    private final PurchaseRepository mPurchaseRepo;
    private boolean mLoadingMore;

    public PurchasesViewModelImpl(@Nullable Bundle savedState,
                                  @NonNull PurchasesViewModel.ViewListener view,
                                  @NonNull RxBus<Object> eventBus,
                                  @NonNull UserRepository userRepository,
                                  @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, view, eventBus, userRepository);

        mPurchaseRepo = purchaseRepo;

        if (savedState != null) {
            mItems = new ArrayList<>();
            mLoadingMore = savedState.getBoolean(STATE_LOADING_MORE, false);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_LOADING_MORE, mLoadingMore);
    }

    @Override
    public void loadData() {
        getSubscriptions().add(
                mUserRepo.fetchIdentityData(mCurrentIdentity)
                        .flatMapObservable(new Func1<Identity, Observable<Purchase>>() {
                            @Override
                            public Observable<Purchase> call(Identity identity) {
                                return mPurchaseRepo.getPurchases(identity, false);
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

                                if (mLoadingMore) {
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
    protected void refreshItems() {
        if (!mView.isNetworkAvailable()) {
            setRefreshing(false);
            mView.showMessageWithAction(R.string.toast_no_connection, getRefreshAction());
            return;
        }

        mView.startUpdatePurchasesService();
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
    public void onDataUpdated(boolean successful) {
        setRefreshing(false);
        if (successful) {
            loadData();
        } else {
            mView.showMessageWithAction(R.string.toast_error_purchases_update, getRefreshAction());
        }
    }

    @Override
    public void onPurchaseRowItemClick(int position) {
        final Purchase purchase = getItemAtPosition(position);
        mView.startPurchaseDetailsActivity(purchase);
    }

    @Override
    public void setPurchasesQueryMoreStream(@NonNull Observable<Purchase> observable,
                                            @NonNull final String workerTag) {
        getSubscriptions().add(observable
                .toList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<Purchase>>() {
                    @Override
                    public void onSuccess(List<Purchase> purchases) {
                        mView.removeWorker(workerTag);
                        mLoadingMore = false;

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
                        mLoadingMore = false;

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
        mLoadingMore = true;

        addLoadMore();
        mView.loadQueryMorePurchasesWorker(mItems.size());
    }

    @Override
    public boolean isLoadingMore() {
        return !mView.isNetworkAvailable() || mLoadingMore || isRefreshing();
    }

    @Override
    public int getItemViewType(int position) {
        if (mItems.get(position) == null) {
            return TYPE_PROGRESS;
        }

        return TYPE_ITEM;
    }
}
