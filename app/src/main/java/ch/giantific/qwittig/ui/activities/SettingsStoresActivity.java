package ch.giantific.qwittig.ui.activities;

import android.annotation.TargetApi;
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

        if (savedInstanceState == null) {
            if (Utils.isRunningLollipopAndHigher()) {
                addActivityTransitionListener();
            } else {
                mFab.show();
            }

            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsStoresFragment(), SETTINGS_STORE_FRAGMENT)
                    .commit();
        } else {
            mFab.show();
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
            mFab.hide();
        } else {
            mFab.show();
        }
    }
}
