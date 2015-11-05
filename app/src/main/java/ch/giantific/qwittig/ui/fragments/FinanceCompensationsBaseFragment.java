/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v7.widget.RecyclerView;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.ParseCompensationRepository;
import ch.giantific.qwittig.data.helpers.query.CompensationQueryHelper;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.utils.HelperUtils;
import ch.giantific.qwittig.ParseErrorHandler;
import ch.giantific.qwittig.utils.Utils;

/**
 * Provides an abstract base class for screens displaying a list of compensations using a
 * {@link RecyclerView}.
 * <p/>
 * Subclass of {@link BaseRecyclerViewOnlineFragment}.
 */
public abstract class FinanceCompensationsBaseFragment extends BaseRecyclerViewOnlineFragment {

    private static final String LOG_TAG = FinanceCompensationsBaseFragment.class.getSimpleName();
    FragmentInteractionListener mListener;
    CompensationRepository mCompsRepo;


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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCompsRepo = new ParseCompensationRepository();
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
     * Tells the adapter of the {@link RecyclerView} to re-query its data.
     */
    @CallSuper
    public void onCompensationsUpdated() {
        updateAdapter();
    }

    /**
     * Passes the error code to the generic error handler, shows the user an error message and
     * removes the retained helper fragment and loading indicators.
     *
     * @param errorCode the error code of the exception thrown in the process
     */
    public void onCompensationUpdateFailed(int errorCode) {
        final Activity context = getActivity();
        ParseErrorHandler.handleParseError(context, errorCode);
        showOnlineQueryErrorSnackbar(ParseErrorHandler.getErrorMessage(context, errorCode));
        HelperUtils.removeHelper(getFragmentManager(), getQueryHelperTag());

        setLoading(false);
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
