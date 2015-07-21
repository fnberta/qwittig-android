package ch.giantific.qwittig.ui;

import android.os.Bundle;

import ch.giantific.qwittig.R;

public class PurchaseDraftsActivity extends BaseNavDrawerActivity implements
        PurchaseDraftsFragment.FragmentInteractionListener {

    private static final String DRAFTS_FRAGMENT = "drafts_fragment";
    private PurchaseDraftsFragment mDraftsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_drafts);

        // check item in NavDrawer
        checkNavDrawerItem(R.id.nav_drafts);

        if (savedInstanceState == null && mUserIsLoggedIn) {
            addDraftsFragment();
        }
    }

    private void addDraftsFragment() {
        getFragmentManager().beginTransaction()
                .add(R.id.container, new PurchaseDraftsFragment(), DRAFTS_FRAGMENT)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mUserIsLoggedIn) {
            findDraftsFragment();
        }
    }

    private void findDraftsFragment() {
        mDraftsFragment = (PurchaseDraftsFragment) getFragmentManager()
                .findFragmentByTag(DRAFTS_FRAGMENT);
    }

    @Override
    void afterLoginSetup() {
        super.afterLoginSetup();

        addDraftsFragment();
        getFragmentManager().executePendingTransactions();
        findDraftsFragment();
    }

    @Override
    protected void onNewGroupSet() {
        updateFragmentAdapter();
    }

    private void updateFragmentAdapter() {
        mDraftsFragment.updateAdapter();
    }

    @Override
    int getSelfNavDrawerItem() {
        return R.id.nav_drafts;
    }
}
