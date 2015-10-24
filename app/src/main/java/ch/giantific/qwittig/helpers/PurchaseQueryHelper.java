package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;

import com.parse.ParseException;

/**
 * Created by fabio on 10.12.14.
 */
public class PurchaseQueryHelper extends BaseQueryHelper {

    private static final String LOG_TAG = PurchaseQueryHelper.class.getSimpleName();
    private HelperInteractionListener mListener;

    public PurchaseQueryHelper() {
        // empty default constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (HelperInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mCurrentGroup == null || mCurrentUserGroups == null) {
            mListener.onAllPurchasesQueried();
            return;
        }

        // purchases for each group
        mTotalNumberOfQueries = mCurrentUserGroups.size();
        queryPurchases();
    }

    @Override
    protected void onParseError(ParseException e) {
        if (mListener != null) {
            mListener.onPurchasesPinFailed(e);
        }
    }

    @Override
    protected void finish() {
        if (mListener != null) {
            mListener.onAllPurchasesQueried();
        }
    }

    @Override
    void onPurchasesPinned(String groupId) {
        super.onPurchasesPinned(groupId);

        if (mListener != null && mCurrentGroup != null &&
                groupId.equals(mCurrentGroup.getObjectId())) {
            mListener.onPurchasesPinned();
        }

        checkQueryCount();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface HelperInteractionListener {
        void onPurchasesPinned();

        void onPurchasesPinFailed(ParseException e);

        void onAllPurchasesQueried();
    }
}
