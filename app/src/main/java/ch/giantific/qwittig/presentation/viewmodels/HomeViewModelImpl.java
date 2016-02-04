/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;

/**
 * Created by fabio on 22.01.16.
 */
public class HomeViewModelImpl extends ViewModelBaseImpl<HomeViewModel.ViewListener>
        implements HomeViewModel {

    private PurchaseRepository mPurchaseRepo;
    private boolean mDraftsAvailable;

    public HomeViewModelImpl(@Nullable Bundle savedState,
                             @NonNull HomeViewModel.ViewListener view,
                             @NonNull UserRepository userRepository,
                             @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, view, userRepository);

        mPurchaseRepo = purchaseRepo;
        mDraftsAvailable = mPurchaseRepo.isPurchaseDraftsAvailable();

        if (mCurrentUser != null) {
//            checkForInvitations();
        }
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
        final boolean draftsAvailable = mPurchaseRepo.isPurchaseDraftsAvailable();
        setDraftsAvailable(draftsAvailable);

        return draftsAvailable;
    }

    @Override
    public void onFabAddPurchaseManualClick(View view) {
        if (isUserInGroup()) {
            mView.startPurchaseAddActivity(false);
        } else {
            mView.showCreateGroupDialog(R.string.dialog_group_create_purchases);
        }
    }

    @Override
    public void onFabAddPurchaseAutoClick(View view) {
        if (isUserInGroup()) {
            mView.startPurchaseAddActivity(true);
        } else {
            mView.showCreateGroupDialog(R.string.dialog_group_create_purchases);
        }
    }

    //    private void checkForInvitations() {
//        Intent intent = getIntent();
//        if (intent == null) {
//            return;
//        }
//
//        String intentAction = intent.getAction();
//        if (Intent.ACTION_VIEW.equals(intentAction)) {
//            Uri uri = intent.getData();
//            if (uri != null) {
//                String email = uri.getQueryParameter(URI_INVITED_EMAIL);
//                mInvitedGroupId = uri.getQueryParameter(URI_INVITED_GROUP_ID);
//
//                if (!email.equals(mCurrentUser.getUsername())) {
//                    Snackbar.make(mFabMenu, R.string.toast_emails_no_match, Snackbar.LENGTH_LONG).show();
//                    return;
//                }
//            }
//        } else if (intent.hasExtra(PushBroadcastReceiver.KEY_PUSH_DATA)) {
//            try {
//                JSONObject jsonExtras = PushBroadcastReceiver.getData(intent);
//                String notificationType = jsonExtras.optString(PushBroadcastReceiver.NOTIFICATION_TYPE);
//                if (PushBroadcastReceiver.TYPE_USER_INVITED.equals(notificationType)) {
//                    mInvitedGroupId = jsonExtras.optString(PushBroadcastReceiver.PUSH_PARAM_GROUP_ID);
//                    mInviteInitiator = jsonExtras.optString(PushBroadcastReceiver.PUSH_PARAM_USER);
//                    mInvitationAction = intent.getIntExtra(
//                            PushBroadcastReceiver.INTENT_ACTION_INVITATION, 0);
//                }
//            } catch (JSONException e) {
//                return;
//            }
//        } else {
//            return;
//        }
//
//        if (!TextUtils.isEmpty(mInvitedGroupId)) {
//            if (!Utils.isNetworkAvailable(this)) {
//                Snackbar.make(mFabMenu, R.string.toast_no_connection, Snackbar.LENGTH_LONG).show();
//                return;
//            }
//
//            // add currentUser to the ACL and Role of the group he is invited to, otherwise we
//            // won't be able to query the group
//            getInvitedGroupWithWorker();
//        }
//    }
//
//    private void getInvitedGroupWithWorker() {
//        final FragmentManager fragmentManager = getFragmentManager();
//        Fragment fragment = WorkerUtils.findWorker(fragmentManager, INVITED_GROUP_WORKER);
//        if (fragment == null) {
//            fragment = InvitedGroupWorker.newInstance(mInvitedGroupId);
//
//            fragmentManager.beginTransaction()
//                    .add(fragment, INVITED_GROUP_WORKER)
//                    .commit();
//        }
//    }
//
//    @Override
//    public void onInvitedGroupQueryFailed(@StringRes int errorMessage) {
//        Snackbar.make(mFabMenu, errorMessage, Snackbar.LENGTH_LONG).show();
//        WorkerUtils.removeWorker(getFragmentManager(), INVITED_GROUP_WORKER);
//    }
//
//    @Override
//    public void onEmailNotValid() {
//        Snackbar.make(mFabMenu, getString(R.string.toast_group_invite_not_valid),
//                Snackbar.LENGTH_LONG).show();
//        WorkerUtils.removeWorker(getFragmentManager(), INVITED_GROUP_WORKER);
//    }
//
//    @Override
//    public void onInvitedGroupQueried(@NonNull ParseObject parseObject) {
//        mInvitedGroup = (Group) parseObject;
//
//        switch (mInvitationAction) {
//            case PushBroadcastReceiver.ACTION_INVITATION_ACCEPTED:
//                onJoinInvitedGroupSelected();
//                break;
//            case PushBroadcastReceiver.ACTION_INVITATION_DISCARDED:
//                onDiscardInvitationSelected();
//                break;
//            default:
//                String groupName = mInvitedGroup.getName();
//                showGroupJoinDialog(groupName);
//        }
//    }
//
//    private void showGroupJoinDialog(String groupName) {
//        final GroupJoinDialogFragment dialog =
//                GroupJoinDialogFragment.newInstance(groupName, mInviteInitiator);
//        dialog.show(getFragmentManager(), GROUP_JOIN_DIALOG);
//    }
//
//    @Override
//    public void onJoinInvitedGroupSelected() {
//        showProgressDialog(getString(R.string.progress_switch_groups));
//
//        final InvitedGroupWorker invitedGroupWorker = (InvitedGroupWorker)
//                WorkerUtils.findWorker(getFragmentManager(), INVITED_GROUP_WORKER);
//        invitedGroupWorker.joinInvitedGroup(mInvitedGroup);
//    }
//
//    private void showProgressDialog(@NonNull String message) {
//        mProgressDialog = ProgressDialog.show(this, null, message);
//    }
//
//    @Override
//    public void onUserJoinedGroup() {
//        WorkerUtils.removeWorker(getFragmentManager(), INVITED_GROUP_WORKER);
//        dismissProgressDialog();
//        Snackbar.make(mFabMenu, getString(R.string.toast_group_added, mInvitedGroup.getName()),
//                Snackbar.LENGTH_LONG).show();
//
//        // register for notifications for the new group
//        ParsePush.subscribeInBackground(mInvitedGroup.getObjectId());
//
//        // remove user from invited list
//        mInvitedGroup.removeUserInvited(mCurrentUser.getUsername());
//        mInvitedGroup.saveEventually();
//
//        mCurrentGroup = mCurrentUser.getCurrentGroup();
//        updateGroupSpinner();
//        // TODO: set query in progress in fragment
//        ParseQueryService.startQueryAll(this);
//    }
//
//    private void dismissProgressDialog() {
//        if (mProgressDialog != null) {
//            mProgressDialog.dismiss();
//        }
//    }
//
//    @Override
//    public void onUserJoinGroupFailed(@StringRes int errorMessage) {
//        Snackbar.make(mFabMenu, errorMessage, Snackbar.LENGTH_LONG).show();
//        WorkerUtils.removeWorker(getFragmentManager(), INVITED_GROUP_WORKER);
//
//        dismissProgressDialog();
//    }
//
//    @Override
//    public void onDiscardInvitationSelected() {
//        WorkerUtils.removeWorker(getFragmentManager(), INVITED_GROUP_WORKER);
//
//        mInvitedGroup.removeUserInvited(mCurrentUser.getUsername());
//        mInvitedGroup.saveEventually();
//
//        Snackbar.make(mFabMenu, R.string.toast_invitation_discarded, Snackbar.LENGTH_LONG).show();
//    }
}
