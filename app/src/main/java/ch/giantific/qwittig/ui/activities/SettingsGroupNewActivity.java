/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.activities;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.transition.Transition;
import android.view.View;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.data.helpers.group.CreateGroupHelper;
import ch.giantific.qwittig.ui.fragments.SettingsGroupNewFragment;
import ch.giantific.qwittig.ui.listeners.TransitionListenerAdapter;
import ch.giantific.qwittig.utils.Utils;

/**
 * Hosts {@link SettingsGroupNewFragment} that allows to user to create a new group.
 * <p/>
 * Mostly handles transition animations and communication between dialogs and fragments. Handles
 * the circle loading animation of the {@link FloatingActionButton}.
 * <p/>
 * Subclass of {@link BaseActivity}.
 *
 * @see FABProgressCircle
 */
public class SettingsGroupNewActivity extends BaseActivity implements
        SettingsGroupNewFragment.FragmentInteractionListener,
        FABProgressListener,
        CreateGroupHelper.HelperInteractionListener {

    public static final String RESULT_DATA_GROUP = "RESULT_DATA_GROUP";
    private static final String STATE_GROUP_NEW_FRAGMENT = "STATE_GROUP_NEW_FRAGMENT";
    private static final String LOG_TAG = SettingsGroupNewActivity.class.getSimpleName();
    private SettingsGroupNewFragment mSettingsGroupNewFragment;
    private FloatingActionButton mFab;
    private FABProgressCircle mFabProgressCircle;
    private String mNewGroupName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_group_new);

        mFab = (FloatingActionButton) findViewById(R.id.fab_group_new);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingsGroupNewFragment.addNewGroup();
            }
        });
        mFabProgressCircle = (FABProgressCircle) findViewById(R.id.fab_group_new_circle);
        mFabProgressCircle.attachListener(this);

        FragmentManager fragmentManager = getFragmentManager();
        if (savedInstanceState == null) {
            if (Utils.isRunningLollipopAndHigher()) {
                addActivityTransitionListener();
            } else {
                mFab.show();
            }

            mSettingsGroupNewFragment = new SettingsGroupNewFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.container, mSettingsGroupNewFragment)
                    .commit();
        } else {
            mSettingsGroupNewFragment = (SettingsGroupNewFragment) fragmentManager
                    .getFragment(savedInstanceState, STATE_GROUP_NEW_FRAGMENT);

            mFab.show();
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

                mFab.show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getFragmentManager().putFragment(outState, STATE_GROUP_NEW_FRAGMENT, mSettingsGroupNewFragment);
    }

    @Override
    public void progressCircleShow() {
        mFabProgressCircle.show();
    }

    @Override
    public void progressCircleHide() {
        mFabProgressCircle.hide();
    }

    @Override
    public void onFABProgressAnimationEnd() {
        mSettingsGroupNewFragment.setIsCreatingNew(false);

        Intent intentNewGroupName = new Intent();
        intentNewGroupName.putExtra(RESULT_DATA_GROUP, mNewGroupName);
        setResult(RESULT_OK, intentNewGroupName);
        ActivityCompat.finishAfterTransition(this);
    }

    @Override
    public void onNewGroupCreated(@NonNull Group newGroup, boolean invitingUser) {
        mSettingsGroupNewFragment.onNewGroupCreated(newGroup, invitingUser);
    }

    @Override
    public void onCreateNewGroupFailed(int errorCode) {
        mSettingsGroupNewFragment.onCreateNewGroupFailed(errorCode);
    }

    @Override
    public void onUsersInvited() {
        mSettingsGroupNewFragment.onUsersInvited();
    }

    @Override
    public void onInviteUsersFailed(int errorCode) {
        mSettingsGroupNewFragment.onInviteUsersFailed(errorCode);
    }

    @Override
    public void finishGroupCreation(@NonNull String newGroupName) {
        mNewGroupName = newGroupName;
        mFabProgressCircle.beginFinalAnimation();
    }
}
