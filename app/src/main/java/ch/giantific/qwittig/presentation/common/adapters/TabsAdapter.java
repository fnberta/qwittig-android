/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.adapters;

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

    private final List<Fragment> mFragments = new ArrayList<>();
    private final List<String> mFragmentTitles = new ArrayList<>();

    /**
     * Constructs a new {@link TabsAdapter}.
     *
     * @param fm the {@link FragmentManager} to use in the adapter
     */
    public TabsAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    /**
     * Adds a fragment to the adapter.
     *
     * @param fragment the fragment to add
     * @param title    the title of the fragment to be displayed in the tab
     */
    public void addFragment(@NonNull Fragment fragment, @NonNull String title) {
        mFragments.add(fragment);
        mFragmentTitles.add(title);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitles.get(position);
    }
}
