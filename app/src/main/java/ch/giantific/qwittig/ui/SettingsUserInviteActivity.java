package ch.giantific.qwittig.ui;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.transition.Explode;
import android.transition.Transition;
import android.view.View;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.parse.ParseException;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.constants.AppConstants;
import ch.giantific.qwittig.helpers.InviteUsersHelper;
import ch.giantific.qwittig.ui.listeners.TransitionListenerAdapter;
import ch.giantific.qwittig.utils.Utils;

public class SettingsUserInviteActivity extends BaseActivity implements
        SettingsUserInviteFragment.FragmentInteractionListener,
        FABProgressListener,
        InviteUsersHelper.HelperInteractionListener {

    private static final String USER_INVITE_FRAGMENT = "user_invite_fragment";
    private SettingsUserInviteFragment mSettingsUserInviteFragment;
    private FloatingActionButton mFab;
    private FABProgressCircle mFabProgressCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Utils.isRunningLollipopAndHigher()) {
            setActivityTransition();
        }
        setContentView(R.layout.activity_settings_user_invite);

        mFab = (FloatingActionButton) findViewById(R.id.fab_user_invite);
        mFab.setImageAlpha(AppConstants.ICON_BLACK_ALPHA_RGB);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingsUserInviteFragment.startInvitation();
            }
        });
        mFabProgressCircle = (FABProgressCircle) findViewById(R.id.fab_user_invite_circle);
        mFabProgressCircle.attachListener(this);

        if (savedInstanceState == null) {
            if (Utils.isRunningLollipopAndHigher()) {
                addActivityTransitionListener();
            } else {
                mFab.show();
            }

            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsUserInviteFragment(), USER_INVITE_FRAGMENT)
                    .commit();
        } else {
            mFab.show();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setActivityTransition() {
        Transition transitionEnter = new Explode();
        transitionEnter.excludeTarget(android.R.id.statusBarBackground, true);
        transitionEnter.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setEnterTransition(transitionEnter);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void addActivityTransitionListener() {
        Transition enter = getWindow().getEnterTransition();
        enter.addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                super.onTransitionEnd(transition);
                transition.removeListener(this);

                mFab.show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mSettingsUserInviteFragment = (SettingsUserInviteFragment)
                getFragmentManager().findFragmentByTag(USER_INVITE_FRAGMENT);
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
    public void onInviteUsersFailed(ParseException e) {
        mSettingsUserInviteFragment.onInviteUsersFailed(e);
    }
}
