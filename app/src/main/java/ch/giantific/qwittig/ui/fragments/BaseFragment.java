package ch.giantific.qwittig.ui.fragments;

import android.app.Fragment;

/**
 * Created by fabio on 05.06.15.
 */
public abstract class BaseFragment extends Fragment {

    // placeholder

    public interface BaseFragmentInteractionListener {
        void showAccountCreateDialog();
    }
}
