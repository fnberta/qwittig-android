/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v7.widget.RecyclerView;

import com.parse.ParseException;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.helpers.CompensationQueryHelper;
import ch.giantific.qwittig.utils.HelperUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.Utils;

/**
 * Provides an abstract base class for screens displaying a list of compensations using a
 * {@link RecyclerView}.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
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
                    + " must implement DialogInteractionListener");
        }
    }

    final void onlineQuery(boolean queryPaid) {
        if (!Utils.isConnected(getActivity())) {
            setLoading(false);
            showOnlineQueryErrorSnackbar(getString(R.string.toast_no_connection));
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        Fragment queryHelper = HelperUtils.findHelper(fragmentManager, getQueryHelperTag());

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
     * Passes the {@link ParseException} to the generic error handler, shows the user an error
     * message and removes the retained helper fragment and loading indicators.
     *
     * @param e the {@link ParseException} thrown in the process
     */
    public void onCompensationsPinFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        showOnlineQueryErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), e));
        HelperUtils.removeHelper(getFragmentManager(), getQueryHelperTag());

        setLoading(false);
    }

    /**
     * Removes the retained helper fragment and and loading indicators.
     */
    public void onAllCompensationsQueried() {
        HelperUtils.removeHelper(getFragmentManager(), getQueryHelperTag());
        setLoading(false);
    }

    /**
     * Tells the adapter of the {@link RecyclerView} to re-query its data.
     */
    public void onCompensationsPinned() {
        updateAdapter();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     * <p/>
     * Currently a stub.
     * <p/>
     * Extends {@link BaseFragmentInteractionListener}.
     */
    public interface FragmentInteractionListener extends BaseFragmentInteractionListener {
    }
}
