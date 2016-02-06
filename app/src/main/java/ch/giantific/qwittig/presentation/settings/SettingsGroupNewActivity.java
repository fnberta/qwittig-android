/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings;

import android.annotation.TargetApi;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.transition.Transition;
import android.view.MenuItem;
import android.view.View;

import ch.berta.fabio.fabprogress.FabProgress;
import ch.berta.fabio.fabprogress.ProgressFinalAnimationListener;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.listeners.TransitionListenerAdapter;
import ch.giantific.qwittig.utils.Utils;

/**
 * Hosts {@link SettingsGroupNewFragment} that allows to user to create a new group.
 * <p/>
 * Mostly handles transition animations and communication between dialogs and fragments. Handles
 * the circle loading animation of the {@link FloatingActionButton}.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class SettingsGroupNewActivity extends BaseActivity implements
        SettingsGroupNewFragment.FragmentInteractionListener,
        CreateGroupWorkerListener {

    public static final String RESULT_DATA_GROUP = "RESULT_DATA_GROUP";
    private static final String STATE_GROUP_NEW_FRAGMENT = "STATE_GROUP_NEW_FRAGMENT";
    private SettingsGroupNewFragment mSettingsGroupNewFragment;
    private FabProgress mFabProgress;
    private String mNewGroupName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_group_new);

        mFabProgress = (FabProgress) findViewById(R.id.fab_group_new);
        mFabProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingsGroupNewFragment.addNewGroup();
            }
        });
        final SettingsGroupNewActivity activity = this;
        mFabProgress.setProgressFinalAnimationListener(new ProgressFinalAnimationListener() {
            @Override
            public void onProgressFinalAnimationComplete() {
                mSettingsGroupNewFragment.setIsCreatingNew(false);

                Intent intentNewGroupName = new Intent();
                intentNewGroupName.putExtra(RESULT_DATA_GROUP, mNewGroupName);
                setResult(RESULT_OK, intentNewGroupName);
                ActivityCompat.finishAfterTransition(activity);
            }
        });

        FragmentManager fragmentManager = getFragmentManager();
        if (savedInstanceState == null) {
            if (Utils.isRunningLollipopAndHigher()) {
                addActivityTransitionListener();
            } else {
                mFabProgress.show();
            }

            mSettingsGroupNewFragment = new SettingsGroupNewFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.container, mSettingsGroupNewFragment)
                    .commit();
        } else {
            mFabProgress.show();

            mSettingsGroupNewFragment = (SettingsGroupNewFragment) fragmentManager
                    .getFragment(savedInstanceState, STATE_GROUP_NEW_FRAGMENT);
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

        getFragmentManager().putFragment(outState, STATE_GROUP_NEW_FRAGMENT, mSettingsGroupNewFragment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if (id == android.R.id.home) {
            return checkIfCreatingNew() || super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean checkIfCreatingNew() {
        if (mSettingsGroupNewFragment.isCreatingNew()) {
            Snackbar.make(mToolbar, R.string.toast_creating_group, Snackbar.LENGTH_LONG).show();
            return true;
        }

        return false;
    }

    @Override
    public void startProgressAnim() {
        mFabProgress.startProgress();
    }

    @Override
    public void stopProgressAnim() {
        mFabProgress.stopProgress();
    }

    @Override
    public void onNewGroupCreated(@NonNull Group newGroup, boolean invitingUser) {
        mSettingsGroupNewFragment.onNewGroupCreated(newGroup, invitingUser);
    }

    @Override
    public void onCreateNewGroupFailed(@StringRes int errorMessage) {
        mSettingsGroupNewFragment.onCreateNewGroupFailed(errorMessage);
    }

    @Override
    public void onUsersInvited() {
        mSettingsGroupNewFragment.onUsersInvited();
    }

    @Override
    public void onInviteUsersFailed(@StringRes int errorMessage) {
        mSettingsGroupNewFragment.onInviteUsersFailed(errorMessage);
    }

    @Override
    public void finishGroupCreation(@NonNull String newGroupName) {
        mNewGroupName = newGroupName;
        mFabProgress.startProgressFinalAnimation();
    }

    @Override
    public void onBackPressed() {
        if (!checkIfCreatingNew()) {
            super.onBackPressed();
        }
    }
}
