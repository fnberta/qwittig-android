package ch.giantific.qwittig.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.helpers.CompensationQueryHelper;
import ch.giantific.qwittig.utils.MessageUtils;
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
            showErrorSnackbar(getString(R.string.toast_no_connection));
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        CompensationQueryHelper compensationQueryHelper = findQueryHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (compensationQueryHelper == null) {
            compensationQueryHelper = CompensationQueryHelper.newInstance(queryPaid);

            fragmentManager.beginTransaction()
                    .add(compensationQueryHelper, getQueryHelperTag())
                    .commit();
        }
    }

    private CompensationQueryHelper findQueryHelper(FragmentManager fragmentManager) {
        return (CompensationQueryHelper) fragmentManager.findFragmentByTag(getQueryHelperTag());
    }

    protected abstract String getQueryHelperTag();

    /**
     * Called from activity when compensation query failed
     * @param e the ParseException thrown
     */
    public void onCompensationsPinFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        showErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), e));
        removeQueryHelper();

        setLoading(false);
    }

    private void showErrorSnackbar(String errorMessage) {
        Snackbar snackbar = MessageUtils.getBasicSnackbar(mRecyclerView, errorMessage);
        snackbar.setAction(R.string.action_retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoading(true);
                onlineQuery();
            }
        });
        snackbar.show();
    }

    private void removeQueryHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        CompensationQueryHelper compensationQueryHelper = findQueryHelper(fragmentManager);

        if (compensationQueryHelper != null) {
            fragmentManager.beginTransaction().remove(compensationQueryHelper).commitAllowingStateLoss();
        }
    }

    /**
     * Called from activity when all compensations queries are finished
     */
    public void onAllCompensationQueriesFinished() {
        removeQueryHelper();
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

    public interface FragmentInteractionListener {
        void showAccountCreateDialog();
    }
}
