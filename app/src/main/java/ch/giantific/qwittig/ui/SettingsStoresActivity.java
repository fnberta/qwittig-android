package ch.giantific.qwittig.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.transition.Explode;
import android.transition.Transition;
import android.view.View;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.ui.dialogs.StoreAddDialogFragment;
import ch.giantific.qwittig.ui.widgets.TransitionListenerAdapter;
import ch.giantific.qwittig.utils.Utils;

public class SettingsStoresActivity extends BaseActivity implements
        SettingsStoresFragment.FragmentInteractionListener,
        StoreAddDialogFragment.DialogInteractionListener {

    private static final String SETTINGS_STORE_FRAGMENT = "settings_store_fragment";
    private SettingsStoresFragment mSettingsStoresFragment;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_stores);

        mFab = (FloatingActionButton) findViewById(R.id.fab_add_store);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStoreAddDialog();
            }
        });

        if (Utils.isRunningLollipopAndHigher()) {
            setActivityTransition();

            if (savedInstanceState == null) {
                addActivityTransitionListener();
            } else if (ViewCompat.isLaidOut(mFab)) {
                circularRevealFab();
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
                    .add(R.id.container, new SettingsStoresFragment(), SETTINGS_STORE_FRAGMENT)
                    .commit();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setActivityTransition() {
        Transition transitionEnter = new Explode();
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void circularHideFab() {
        Animator hide = Utils.getCircularHideAnimator(mFab);
        hide.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mFab.setVisibility(View.INVISIBLE);
            }
        });
        hide.start();
    }

    private void showStoreAddDialog() {
        StoreAddDialogFragment storeAddDialogFragment = new StoreAddDialogFragment();
        storeAddDialogFragment.show(getFragmentManager(), "add_store");
    }

    @Override
    public void addStore(String store) {
        mSettingsStoresFragment.addStoreToList(store);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mSettingsStoresFragment = (SettingsStoresFragment) getFragmentManager()
                .findFragmentByTag(SETTINGS_STORE_FRAGMENT);
    }

    @Override
    public void toggleFabVisibility() {
        if (mFab.getVisibility() == View.VISIBLE) {
            if (Utils.isRunningLollipopAndHigher()) {
                circularHideFab();
            } else {
                mFab.setVisibility(View.INVISIBLE);
            }
        } else {
            if (Utils.isRunningLollipopAndHigher()) {
                circularRevealFab();
            } else {
                mFab.setVisibility(View.VISIBLE);
            }
        }
    }
}
