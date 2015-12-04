/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.activities;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.transition.Transition;
import android.view.MenuItem;
import android.view.View;

import ch.berta.fabio.fabprogress.FabProgress;
import ch.berta.fabio.fabprogress.ProgressFinalAnimationListener;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.workerfragments.group.InviteUsersWorker;
import ch.giantific.qwittig.ui.fragments.SettingsUserInviteFragment;
import ch.giantific.qwittig.ui.listeners.TransitionListenerAdapter;
import ch.giantific.qwittig.utils.Utils;

/**
 * Hosts {@link SettingsUserInviteFragment} that allows the user to invite users to his/her current
 * group.
 * <p/>
 * Handles the circle loading animation of the {@link FloatingActionButton}.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class SettingsUserInviteActivity extends BaseActivity implements
        SettingsUserInviteFragment.FragmentInteractionListener,
        InviteUsersWorker.WorkerInteractionListener {

    private static final String STATE_USER_INVITE_FRAGMENT = "STATE_USER_INVITE_FRAGMENT";
    private SettingsUserInviteFragment mSettingsUserInviteFragment;
    private FabProgress mFabProgress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_user_invite);

        mFabProgress = (FabProgress) findViewById(R.id.fab_user_invite);
        mFabProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingsUserInviteFragment.startInvitation();
            }
        });
        mFabProgress.setProgressFinalAnimationListener(new ProgressFinalAnimationListener() {
            @Override
            public void onProgressFinalAnimationComplete() {
                mSettingsUserInviteFragment.finishInvitations();
            }
        });

        FragmentManager fragmentManager = getFragmentManager();
        if (savedInstanceState == null) {
            if (Utils.isRunningLollipopAndHigher()) {
                addActivityTransitionListener();
            } else {
                mFabProgress.show();
            }

            mSettingsUserInviteFragment = new SettingsUserInviteFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.container, mSettingsUserInviteFragment)
                    .commit();
        } else {
            mFabProgress.show();

            mSettingsUserInviteFragment = (SettingsUserInviteFragment) fragmentManager
                    .getFragment(savedInstanceState, STATE_USER_INVITE_FRAGMENT);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void addActivityTransitionListener() {
        Transition enter = getWindow().getEnterTransition();
        enter.addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                super.onTransitionEnd(transition);
                transition.removeListener(this);

                mFabProgress.show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getFragmentManager().putFragment(outState, STATE_USER_INVITE_FRAGMENT,
                mSettingsUserInviteFragment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if (id == android.R.id.home) {
            return checkIfInviting() || super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean checkIfInviting() {
        if (mSettingsUserInviteFragment.isInviting()) {
            Snackbar.make(mToolbar, R.string.toast_inviting_user, Snackbar.LENGTH_LONG).show();
            return true;
        }

        return false;
    }

    @Override
    public void startProgressAnim() {
        mFabProgress.startProgress();
    }

    @Override
    public void startFinalProgressAnim() {
        mFabProgress.beginProgressFinalAnimation();
    }

    @Override
    public void stopProgressAnim() {
        mFabProgress.startProgress();
    }

    @Override
    public void onUsersInvited() {
        mSettingsUserInviteFragment.onUsersInvited();
    }

    @Override
    public void onInviteUsersFailed(int errorCode) {
        mSettingsUserInviteFragment.onInviteUsersFailed(errorCode);
    }

    @Override
    public void onBackPressed() {
        if (!checkIfInviting()) {
            super.onBackPressed();
        }
    }
}
