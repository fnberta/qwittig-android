package ch.giantific.qwittig.utils;

import android.app.Fragment;
import android.app.FragmentManager;

/**
 * Created by fabio on 22.10.15.
 */
public class HelperUtils {

    private HelperUtils() {
        // class cannot be instantiated
    }

    public static void removeHelper(FragmentManager fragmentManager, String tag) {
        Fragment fragment = findHelper(fragmentManager, tag);

        if (fragment != null) {
            fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
    }

    public static Fragment findHelper(FragmentManager fragmentManager, String tag) {
        return fragmentManager.findFragmentByTag(tag);
    }
}
