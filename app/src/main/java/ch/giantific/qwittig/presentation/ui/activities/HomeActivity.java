/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.view.ActionMode;

import ch.giantific.qwittig.LocalBroadcastImpl;
import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.ActivityHomeBinding;
import ch.giantific.qwittig.di.components.DaggerHomeComponent;
import ch.giantific.qwittig.di.components.HomeComponent;
import ch.giantific.qwittig.di.components.NavDrawerComponent;
import ch.giantific.qwittig.di.modules.HomeViewModelModule;
import ch.giantific.qwittig.domain.models.MessageAction;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.presentation.ui.adapters.TabsAdapter;
import ch.giantific.qwittig.presentation.ui.fragments.HomeDraftsFragment;
import ch.giantific.qwittig.presentation.ui.fragments.HomePurchasesFragment;
import ch.giantific.qwittig.presentation.ui.fragments.dialogs.GroupCreateDialogFragment;
import ch.giantific.qwittig.presentation.viewmodels.HomeDraftsViewModel;
import ch.giantific.qwittig.presentation.viewmodels.HomePurchasesViewModel;
import ch.giantific.qwittig.presentation.viewmodels.HomeViewModel;
import ch.giantific.qwittig.presentation.viewmodels.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.workerfragments.query.PurchasesQueryMoreListener;
import ch.giantific.qwittig.presentation.workerfragments.query.PurchasesUpdateListener;
import ch.giantific.qwittig.utils.ViewUtils;
import rx.Observable;

/**
 * Provides the launcher activity for {@link Qwittig}, hosts a viewpager with
 * {@link HomePurchasesFragment} and {@link HomeDraftsFragment} that display lists of recent
 * purchases and open drafts. Only loads the fragments if the  user is logged in.
 * <p/>
 * Handles the case when a user is invited to a group and he/she wants to join it or declines the
 * invitation.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class HomeActivity extends BaseNavDrawerActivity<HomeViewModel> implements
        HomeViewModel.ViewListener,
        HomePurchasesFragment.ActivityListener,
        HomeDraftsFragment.ActivityListener,
        PurchasesUpdateListener,
        PurchasesQueryMoreListener,
//        GroupJoinDialogFragment.DialogInteractionListener,
        GroupCreateDialogFragment.DialogInteractionListener
//        InvitedGroupWorker.WorkerInteractionListener
{

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();
    private static final String INVITED_GROUP_WORKER = "INVITED_GROUP_WORKER";
    private static final String URI_INVITED_GROUP_ID = "group";
    private static final String GROUP_JOIN_DIALOG = "GROUP_JOIN_DIALOG";
    private ActivityHomeBinding mBinding;
    private HomePurchasesViewModel mPurchasesViewModel;
    private HomeDraftsViewModel mDraftsViewModel;
    private HomeDraftsFragment mDraftsFragment;
    private Group mInvitedGroup;
    private String mInviteInitiator;
    private String mInvitedGroupId;
    private ProgressDialog mProgressDialog;
    private int mInvitationAction;

    @Override
    void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);

        if (dataType == LocalBroadcastImpl.DATA_TYPE_PURCHASES_UPDATED) {
            mPurchasesViewModel.updateList();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        // check item in NavDrawer
        checkNavDrawerItem(R.id.nav_home);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_activity_home);
        }

        mBinding.fabMenu.hideMenuButton(false);
        mBinding.fabMenu.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBinding.fabMenu.showMenuButton(true);
            }
        }, ViewUtils.FAB_CIRCULAR_REVEAL_DELAY * 4);

        final HomeComponent comp = DaggerHomeComponent.builder()
                .homeViewModelModule(new HomeViewModelModule(savedInstanceState))
                .build();
        mViewModel = comp.getHomeViewModel();
        mBinding.setViewModel(mViewModel);

        if (isUserLoggedIn()) {
            if (savedInstanceState == null) {
                addViewPagerFragments();
                //            checkForInvitations();
            } else {
                // TODO: get drafts fragment reference
            }
        }
    }

    private void addViewPagerFragments() {
        mDraftsFragment = new HomeDraftsFragment();

        final TabsAdapter tabsAdapter = new TabsAdapter(getFragmentManager());
        tabsAdapter.addFragment(new HomePurchasesFragment(), getString(R.string.tab_purchases));
        tabsAdapter.addFragment(mDraftsFragment, getString(R.string.title_activity_purchase_drafts));
        mBinding.viewpager.setAdapter(tabsAdapter);

        mBinding.tabs.setupWithViewPager(mBinding.viewpager);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // TODO: save drafts fragment reference
    }

    @Override
    protected void injectNavDrawerDependencies(@NonNull NavDrawerComponent navComp) {
        navComp.inject(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mViewModel.attachView(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mViewModel.detachView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_PURCHASE_MODIFY:
                switch (resultCode) {
                    case PurchaseAddEditViewModel.RESULT_PURCHASE_SAVED:
                        showMessage(R.string.toast_purchase_added);
                        break;
                    case PurchaseAddEditViewModel.RESULT_PURCHASE_SAVED_AUTO:
                        showMessage(R.string.toast_purchase_added);
                        break;
                    case PurchaseAddEditViewModel.RESULT_PURCHASE_DRAFT:
                        showMessage(R.string.toast_purchase_added_draft);
                        break;
                    case PurchaseAddEditViewModel.RESULT_PURCHASE_DISCARDED:
                        showMessage(R.string.toast_purchase_discarded);
                        break;
                    case PurchaseAddEditViewModel.RESULT_PURCHASE_ERROR:
                        showMessage(R.string.toast_create_image_file_failed);
                        break;
                }
                break;
        }
    }

    @Override
    public void setPurchasesViewModel(@NonNull HomePurchasesViewModel viewModel) {
        mPurchasesViewModel = viewModel;
    }

    @Override
    public void setDraftsViewModel(@NonNull HomeDraftsViewModel viewModel) {
        mDraftsViewModel = viewModel;
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

    @Override
    void onLoginSuccessful() {
        super.onLoginSuccessful();

        // TODO: fix setLoading(true) because online query is still happening
        addViewPagerFragments();
    }

    @Override
    public void onNewGroupSet() {
        super.onNewGroupSet();

        mPurchasesViewModel.onNewGroupSet();
        mDraftsViewModel.onNewGroupSet();
    }

    @Override
    public void updateNavDrawerSelectedGroup() {
        mNavDrawerViewModel.notifySelectedGroupChanged();
    }

    @Override
    public ActionMode startActionMode() {
        return mToolbar.startActionMode(mDraftsFragment);
    }

    @Override
    public void showCreateGroupDialog(@StringRes int message) {

    }

    @Override
    public void onCreateGroupSelected() {
        final Intent intent = new Intent(this, SettingsGroupNewActivity.class);
        startActivity(intent);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startPurchaseAddActivity(boolean autoMode) {
        final Intent intent = new Intent(this, PurchaseAddActivity.class);
        if (autoMode) {
            intent.putExtra(PurchaseAddActivity.INTENT_PURCHASE_NEW_AUTO, true);
            startActivityForResult(intent, HomeActivity.INTENT_REQUEST_PURCHASE_MODIFY);
        } else {
            intent.putExtra(PurchaseAddActivity.INTENT_PURCHASE_NEW_AUTO, false);
            final ActivityOptionsCompat activityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this);
            startActivityForResult(intent, HomeActivity.INTENT_REQUEST_PURCHASE_MODIFY,
                    activityOptionsCompat.toBundle());
        }

        mBinding.fabMenu.close();
    }

    @Override
    public void setPurchasesUpdateStream(@NonNull Observable<Purchase> observable, @NonNull String workerTag) {
        mPurchasesViewModel.setPurchasesUpdateStream(observable, workerTag);
    }

    @Override
    public void setPurchasesQueryMoreStream(@NonNull Observable<Purchase> observable, @NonNull String workerTag) {
        mPurchasesViewModel.setPurchasesQueryMoreStream(observable, workerTag);
    }

    @Override
    int getSelfNavDrawerItem() {
        return R.id.nav_home;
    }

    @Override
    public boolean isNetworkAvailable() {
        return false;
    }

    @Override
    public void showMessage(@StringRes int resId, @NonNull String... args) {

    }

    @Override
    public void showMessageWithAction(@StringRes int resId, @NonNull MessageAction action) {

    }

    @Override
    public void removeWorker(@NonNull String workerTag) {

    }
}

