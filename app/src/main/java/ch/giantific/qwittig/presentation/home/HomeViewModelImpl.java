/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by fabio on 22.01.16.
 */
public class HomeViewModelImpl extends ViewModelBaseImpl<HomeViewModel.ViewListener>
        implements HomeViewModel {

    private PurchaseRepository mPurchaseRepo;
    private boolean mDraftsAvailable;
    private String mInvitationIdentityId;

    public HomeViewModelImpl(@Nullable Bundle savedState,
                             @NonNull HomeViewModel.ViewListener view,
                             @NonNull UserRepository userRepository,
                             @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, view, userRepository);

        mPurchaseRepo = purchaseRepo;
        mDraftsAvailable = mPurchaseRepo.isDraftsAvailable();
    }

    @Override
    @Bindable
    public boolean isDraftsAvailable() {
        return mDraftsAvailable;
    }

    @Override
    public void setDraftsAvailable(boolean available) {
        mDraftsAvailable = available;
        notifyPropertyChanged(BR.draftsAvailable);
    }

    @Override
    public boolean updateDraftsAvailable() {
        final boolean draftsAvailable = mPurchaseRepo.isDraftsAvailable();
        setDraftsAvailable(draftsAvailable);

        return draftsAvailable;
    }

    @Override
    public void onFabAddPurchaseManualClick(View view) {
        mView.startPurchaseAddActivity(false);
    }

    @Override
    public void onFabAddPurchaseAutoClick(View view) {
        mView.startPurchaseAddActivity(true);
    }

    @Override
    public void handleInvitation(@NonNull String identityId, @NonNull String groupName) {
        mInvitationIdentityId = identityId;
        mView.showGroupJoinDialog(groupName);
    }

    @Override
    public void onJoinInvitedGroupSelected() {
        mView.showProgressDialog(R.string.progress_joining_group);
        mView.loadJoinGroupWorker(mInvitationIdentityId);
    }

    @Override
    public void onDiscardInvitationSelected() {
        // do nothing
    }

    @Override
    public void setJoinGroupStream(@NonNull Single<User> single, @NonNull final String workerTag) {
        mSubscriptions.add(single.subscribe(new SingleSubscriber<User>() {
            @Override
            public void onSuccess(User value) {
                mView.removeWorker(workerTag);
                mView.hideProgressDialog();

                mView.showMessage(R.string.toast_group_joined);
                mView.onGroupJoined();
            }

            @Override
            public void onError(Throwable error) {
                mView.removeWorker(workerTag);
                mView.hideProgressDialog();

                mView.showMessage(R.string.toast_unknown_error);
                // TODO: show proper error message
            }
        }));
    }
}
