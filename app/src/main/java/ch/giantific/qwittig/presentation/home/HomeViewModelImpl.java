/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.MenuItem;

import org.stringtemplate.v4.ST;

import ch.berta.fabio.fabspeeddial.FabMenu;
import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Provides an implementation of the {@link HomeViewModel}.
 */
public class HomeViewModelImpl extends ViewModelBaseImpl<HomeViewModel.ViewListener>
        implements HomeViewModel {

    private static final String STATE_INVITATION_ID = "STATE_INVITATION_ID";
    private final PurchaseRepository mPurchaseRepo;
    private boolean mDraftsAvailable;
    private String mInvitationIdentityId;

    public HomeViewModelImpl(@Nullable Bundle savedState,
                             @NonNull HomeViewModel.ViewListener view,
                             @NonNull UserRepository userRepository,
                             @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, view, userRepository);

        mPurchaseRepo = purchaseRepo;
        if (savedState != null) {
            mInvitationIdentityId = savedState.getString(STATE_INVITATION_ID);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        if (!TextUtils.isEmpty(mInvitationIdentityId)) {
            outState.putString(STATE_INVITATION_ID, mInvitationIdentityId);
        }
    }

    @Override
    public void onLoginSuccessful() {
        mCurrentUser = mUserRepo.getCurrentUser();
        setCurrentIdentity();
        mDraftsAvailable = mPurchaseRepo.isDraftsAvailable(mCurrentIdentity);
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
        final boolean draftsAvailable = mPurchaseRepo.isDraftsAvailable(mCurrentIdentity);
        setDraftsAvailable(draftsAvailable);

        return draftsAvailable;
    }

    @Override
    public FabMenu.FabMenuItemClickListener getFabMenuItemClickListener() {
        return new FabMenu.FabMenuItemClickListener() {
            @Override
            public void onFabMenuItemClicked(@NonNull MenuItem menuItem) {
                final int id = menuItem.getItemId();
                switch (id) {
                    case R.id.action_fab_home_auto:
                        mView.startPurchaseAddActivity(true);
                        break;
                    case R.id.action_fab_home_manual:
                        mView.startPurchaseAddActivity(false);
                        break;
                }
            }
        };
    }

    @Override
    public void handleInvitation(@NonNull String identityId, @NonNull String groupName,
                                 @NonNull String inviterNickname) {
        mInvitationIdentityId = identityId;
        mView.showGroupJoinDialog(groupName, inviterNickname);
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
    public void setJoinGroupStream(@NonNull Single<Identity> single, @NonNull final String workerTag) {
        getSubscriptions().add(single.subscribe(new SingleSubscriber<Identity>() {
            @Override
            public void onSuccess(Identity identity) {
                mView.removeWorker(workerTag);
                mView.hideProgressDialog();

                mView.showMessage(R.string.toast_group_joined);
                mView.onGroupJoined();
            }

            @Override
            public void onError(Throwable error) {
                mView.removeWorker(workerTag);
                mView.hideProgressDialog();

                mView.showMessage(R.string.toast_error_unknown);
                // TODO: show proper error message, define error codes in cloud code
            }
        }));
    }
}
