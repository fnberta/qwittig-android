package ch.giantific.qwittig.ui.fragments;

import android.app.Fragment;
import android.app.FragmentManager;

/**
 * Created by fabio on 05.06.15.
 */
public abstract class BaseFragment extends Fragment {

    final void removeHelper(String tag) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = findHelper(fragmentManager, tag);

        if (fragment != null) {
            fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
    }

    final Fragment findHelper(FragmentManager fragmentManager, String tag) {
        return fragmentManager.findFragmentByTag(tag);
    }

    public interface BaseFragmentInteractionListener {
        void showAccountCreateDialog();
    }
}
