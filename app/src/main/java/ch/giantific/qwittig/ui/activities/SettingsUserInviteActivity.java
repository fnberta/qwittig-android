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
import android.transition.Transition;
import android.view.View;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.helpers.group.InviteUsersHelper;
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
 *
 * @see FABProgressCircle
 */
public class SettingsUserInviteActivity extends BaseActivity implements
        SettingsUserInviteFragment.FragmentInteractionListener,
        FABProgressListener,
        InviteUsersHelper.HelperInteractionListener {

    private static final String STATE_USER_INVITE_FRAGMENT = "STATE_USER_INVITE_FRAGMENT";
    private SettingsUserInviteFragment mSettingsUserInviteFragment;
    private FloatingActionButton mFab;
    private FABProgressCircle mFabProgressCircle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_user_invite);

        mFab = (FloatingActionButton) findViewById(R.id.fab_user_invite);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingsUserInviteFragment.startInvitation();
            }
        });
        mFabProgressCircle = (FABProgressCircle) findViewById(R.id.fab_user_invite_circle);
        mFabProgressCircle.attachListener(this);

        FragmentManager fragmentManager = getFragmentManager();
        if (savedInstanceState == null) {
            if (Utils.isRunningLollipopAndHigher()) {
                addActivityTransitionListener();
            } else {
                mFab.show();
            }

            mSettingsUserInviteFragment = new SettingsUserInviteFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.container, mSettingsUserInviteFragment)
                    .commit();
        } else {
            mFab.show();

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

                mFab.show();
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
    public void onFABProgressAnimationEnd() {
        mSettingsUserInviteFragment.finishInvitations();
    }

    @Override
    public void progressCircleShow() {
        mFabProgressCircle.show();
    }

    @Override
    public void progressCircleStartFinal() {
        mFabProgressCircle.beginFinalAnimation();
    }

    @Override
    public void progressCircleHide() {
        mFabProgressCircle.hide();
    }

    @Override
    public void onUsersInvited() {
        mSettingsUserInviteFragment.onUsersInvited();
    }

    @Override
    public void onInviteUsersFailed(int errorCode) {
        mSettingsUserInviteFragment.onInviteUsersFailed(errorCode);
    }
}
