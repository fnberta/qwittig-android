package ch.giantific.qwittig.ui;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.ui.adapter.TabsAdapter;

public class StatsActivity extends BaseNavDrawerActivity implements
        StatsBaseFragment.FragmentInteractionListener {

    private static final String STATS_COSTS_FRAGMENT = "stats_costs_fragment";
    private static final String STATS_STORES_FRAGMENT = "stats_stores_fragment";
    private static final String STATS_CURRENCIES_FRAGMENT = "stats_currencies_fragment";

    private StatsSpendingFragment mStatsSpendingFragment;
    private StatsStoresFragment mStatsStoresFragment;
    private StatsCurrenciesFragment mStatsCurrenciesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // check item in NavDrawer
        checkNavDrawerItem(R.id.nav_stats);

        if (mUserIsLoggedIn) {
            if (savedInstanceState == null) {
                addViewPagerFragments();
            } else {
                FragmentManager fragmentManager = getFragmentManager();
                mStatsSpendingFragment = (StatsSpendingFragment) fragmentManager
                        .getFragment(savedInstanceState, STATS_COSTS_FRAGMENT);
                mStatsStoresFragment = (StatsStoresFragment) fragmentManager
                        .getFragment(savedInstanceState, STATS_STORES_FRAGMENT);
                mStatsCurrenciesFragment = (StatsCurrenciesFragment) fragmentManager
                        .getFragment(savedInstanceState, STATS_CURRENCIES_FRAGMENT);
                setupTabs();
            }
        }
    }

    private void addViewPagerFragments() {
        mStatsSpendingFragment = new StatsSpendingFragment();
        mStatsStoresFragment = new StatsStoresFragment();
        mStatsCurrenciesFragment = new StatsCurrenciesFragment();

        setupTabs();
    }

    private void setupTabs() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabsAdapter tabsAdapter = new TabsAdapter(getFragmentManager());
        tabsAdapter.addFragment(mStatsSpendingFragment, getString(R.string.tab_stats_spending));
        tabsAdapter.addFragment(mStatsStoresFragment, getString(R.string.tab_stats_stores));
        tabsAdapter.addFragment(mStatsCurrenciesFragment, getString(R.string.tab_stats_currencies));
        viewPager.setAdapter(tabsAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    void afterLoginSetup() {
        addViewPagerFragments();

        super.afterLoginSetup();
    }

    @Override
    protected void onNewGroupSet() {
        if (mStatsSpendingFragment.isAdded()) {
            mStatsSpendingFragment.updateData();
        }
        if (mStatsStoresFragment.isAdded()) {
            mStatsStoresFragment.updateData();
        }
        if (mStatsCurrenciesFragment.isAdded()) {
            mStatsCurrenciesFragment.updateData();
        }
    }
}
