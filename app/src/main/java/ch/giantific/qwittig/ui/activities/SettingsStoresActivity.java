package ch.giantific.qwittig.ui.activities;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.transition.Transition;
import android.view.View;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.ui.fragments.SettingsStoresFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.StoreAddDialogFragment;
import ch.giantific.qwittig.ui.listeners.TransitionListenerAdapter;
import ch.giantific.qwittig.utils.Utils;

public class SettingsStoresActivity extends BaseActivity implements
        SettingsStoresFragment.FragmentInteractionListener,
        StoreAddDialogFragment.DialogInteractionListener {

    private static final String STATE_SETTINGS_STORE_FRAGMENT = "STATE_SETTINGS_STORE_FRAGMENT";
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

        FragmentManager fragmentManager = getFragmentManager();
        if (savedInstanceState == null) {
            if (Utils.isRunningLollipopAndHigher()) {
                addActivityTransitionListener();
            } else {
                mFab.show();
            }

            mSettingsStoresFragment = new SettingsStoresFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.container, mSettingsStoresFragment)
                    .commit();
        } else {
            mFab.show();

            mSettingsStoresFragment = (SettingsStoresFragment) fragmentManager
                    .getFragment(savedInstanceState, STATE_SETTINGS_STORE_FRAGMENT);
        }
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getFragmentManager().putFragment(outState, STATE_SETTINGS_STORE_FRAGMENT,
                mSettingsStoresFragment);
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
    public void toggleFabVisibility() {
        if (mFab.getVisibility() == View.VISIBLE) {
            mFab.hide();
        } else {
            mFab.show();
        }
    }
}
