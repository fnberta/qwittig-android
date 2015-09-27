package ch.giantific.qwittig.ui;

import android.app.Fragment;

/**
 * Created by fabio on 05.06.15.
 */
public abstract class BaseFragment extends Fragment {

    public interface BaseFragmentInteractionListener {
        void showAccountCreateDialog();
    }
}
