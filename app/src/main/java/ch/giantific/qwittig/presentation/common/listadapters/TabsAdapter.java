/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.listadapters;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles fragments as tabs in a {@link ViewPager}.
 * <p/>
 * Subclass of {@link FragmentPagerAdapter}.
 */
public class TabsAdapter extends FragmentPagerAdapter {

    protected final List<Fragment> fragments = new ArrayList<>();
    protected final List<String> fragmentTitles = new ArrayList<>();

    /**
     * Constructs a new {@link TabsAdapter}.
     *
     * @param fm the {@link FragmentManager} to use in the adapter
     */
    public TabsAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    /**
     * Adds an initial fragment to the adapter.
     *
     * @param fragment the fragment to addItemAtPosition
     * @param title    the title of the fragment to be displayed in the tab
     */
    public void addInitialFragment(@NonNull Fragment fragment, @NonNull String title) {
        fragments.add(fragment);
        fragmentTitles.add(title);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTitles.get(position);
    }
}
