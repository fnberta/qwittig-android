package ch.giantific.qwittig.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.transition.Explode;
import android.transition.Transition;
import android.view.View;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.ui.widgets.TransitionListenerAdapter;
import ch.giantific.qwittig.utils.Utils;

public class SettingsUserInviteActivity extends BaseActivity implements
        SettingsUserInviteFragment.FragmentInteractionListener,
        FABProgressListener {

    private static final String USER_INVITE_FRAGMENT = "user_invite_fragment";
    private SettingsUserInviteFragment mSettingsUserInviteFragment;
    private FloatingActionButton mFab;
    private FABProgressCircle mFabProgressCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_user_invite);

        if (Utils.isRunningLollipopAndHigher()) {
            setActivityTransition();
        }

        mFab = (FloatingActionButton) findViewById(R.id.fab_user_invite);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingsUserInviteFragment.startInvitation();
            }
        });
        mFabProgressCircle = (FABProgressCircle) findViewById(R.id.fab_user_invite_circle);
        mFabProgressCircle.attachListener(this);

        if (Utils.isRunningLollipopAndHigher()) {
            if (savedInstanceState == null) {
                addActivityTransitionListener();
            } else {
                mFab.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        v.removeOnLayoutChangeListener(this);
                        circularRevealFab();
                    }
                });
            }
        } else {
            mFab.setVisibility(View.VISIBLE);
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsUserInviteFragment(), USER_INVITE_FRAGMENT)
                    .commit();
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
                circularRevealFab();
            }
        });
    }

    private void circularRevealFab() {
        Animator reveal = Utils.getCircularRevealAnimator(mFab);
        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mFab.setVisibility(View.VISIBLE);
            }
        });
        reveal.start();
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
}
