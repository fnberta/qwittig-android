/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import ch.giantific.qwittig.presentation.common.listadapters.TabsAdapter;

/**
 * Handles fragments as tabs in a {@link ViewPager}.
 * <p/>
 * Subclass of {@link FragmentPagerAdapter}.
 */
public class HomeTabsAdapter extends TabsAdapter {

    private final FragmentManager mFragmentManager;

    /**
     * Constructs a new {@link HomeTabsAdapter}.
     *
     * @param fm the {@link FragmentManager} to use in the adapter
     */
    public HomeTabsAdapter(@NonNull FragmentManager fm) {
        super(fm);

        mFragmentManager = fm;
    }

    /**
     * Adds an additional fragment to the adapter.
     *
     * @param fragment the fragment to addItemAtPosition
     * @param title    the title of the fragment to be displayed in the tab
     */
    public void addFragment(@NonNull Fragment fragment, @NonNull String title) {
        fragments.add(fragment);
        fragmentTitles.add(title);
        notifyDataSetChanged();
    }

    public void removeFragment(@NonNull Fragment fragment) {
        final int pos = fragments.indexOf(fragment);
        fragments.remove(pos);
        fragmentTitles.remove(pos);
        notifyDataSetChanged();
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public int getItemPosition(Object object) {
        if (!fragments.contains(object)) {
            return POSITION_NONE;
        }

        return super.getItemPosition(object);

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);

        if (position >= getCount()) {
            mFragmentManager
                    .beginTransaction()
                    .remove((Fragment) object)
                    .commit();
        }
    }
}