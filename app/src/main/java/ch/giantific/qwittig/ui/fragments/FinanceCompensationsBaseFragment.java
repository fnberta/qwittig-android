package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;

import com.parse.ParseException;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.helpers.CompensationQueryHelper;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.Utils;

/**
 * A placeholder fragment containing a simple view.
 */
public abstract class FinanceCompensationsBaseFragment extends BaseRecyclerViewFragment implements
    LocalQuery.ObjectLocalFetchListener {

    private static final String LOG_TAG = FinanceCompensationsBaseFragment.class.getSimpleName();
    FragmentInteractionListener mListener;

    public FinanceCompensationsBaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    final void onlineQuery(boolean queryPaid) {
        if (!Utils.isConnected(getActivity())) {
            setLoading(false);
            showOnlineQueryErrorSnackbar(getString(R.string.toast_no_connection));
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        Fragment queryHelper = findHelper(fragmentManager, getQueryHelperTag());

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (queryHelper == null) {
            queryHelper = CompensationQueryHelper.newInstance(queryPaid);

            fragmentManager.beginTransaction()
                    .add(queryHelper, getQueryHelperTag())
                    .commit();
        }
    }

    protected abstract String getQueryHelperTag();

    /**
     * Called from activity when compensation query failed
     * @param e the ParseException thrown
     */
    public void onCompensationsPinFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        showOnlineQueryErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), e));
        removeHelper(getQueryHelperTag());

        setLoading(false);
    }

    /**
     * Called from activity when all compensations queries are finished
     */
    public void onAllCompensationQueriesFinished() {
        removeHelper(getQueryHelperTag());
        setLoading(false);
    }

    /**
     * Called from activity when helper finished pinning new compensations
     */
    public void onCompensationsPinned() {
        updateAdapter();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentInteractionListener extends BaseFragmentInteractionListener {
    }
}
