package ch.giantific.qwittig.helper;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.data.parse.OnlineQuery;
import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.Purchase;

/**
 * Created by fabio on 10.12.14.
 */
public class MoreQueryHelper extends BaseQueryHelper {

    public static final String MORE_QUERY_HELPER = "more_query_helper";
    private static final String CLASS_NAME = "class_name";
    private static final String SKIP = "skip";
    private static final String LOG_TAG = MoreQueryHelper.class.getSimpleName();
    private HelperInteractionListener mListener;

    public MoreQueryHelper() {
        // empty default constructor
    }

    public static MoreQueryHelper newInstance(String className, int skip) {
        MoreQueryHelper fragment = new MoreQueryHelper();
        Bundle args = new Bundle();
        args.putString(CLASS_NAME, className);
        args.putInt(SKIP, skip);
        fragment.setArguments(args);
        return fragment;
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

        String className = "";
        int skip = 0;
        Bundle args = getArguments();
        if (args != null) {
            className = args.getString(CLASS_NAME);
            skip = args.getInt(SKIP);
        }

        if (TextUtils.isEmpty(className) || mCurrentGroup == null) {
            finish();
            return;
        }

        switch (className) {
            case Purchase.CLASS:
                queryPurchasesMore(skip);
                break;
            case Compensation.CLASS:
                queryCompensationsPaidMore(skip);
                break;
        }
    }

    private void queryPurchasesMore(int skip) {
        ParseQuery<ParseObject> query = OnlineQuery.getPurchasesQuery();
        query.setSkip(skip);
        query.whereEqualTo(Purchase.GROUP, mCurrentGroup);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> parseObjects, ParseException e) {
                if (e != null) {
                    onParseError(e);
                    return;
                }

                // Add the latest results for this query to the cache
                ParseObject.pinAllInBackground(Purchase.PIN_LABEL + mCurrentGroup.getObjectId(),
                        parseObjects, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null && mListener != null) {
                            mListener.onMoreObjectsPinned(parseObjects);
                        }
                    }
                });
            }
        });
    }

    private void queryCompensationsPaidMore(int skip) {
        ParseQuery<ParseObject> query = OnlineQuery.getCompensationsQuery();
        query.setSkip(skip);
        query.whereEqualTo(Compensation.GROUP, mCurrentGroup);
        query.whereEqualTo(Compensation.IS_PAID, true);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> parseObjects, ParseException e) {
                if (e != null) {
                    onParseError(e);
                    return;
                }

                // Add the latest results for this query to the cache.
                ParseObject.pinAllInBackground(Compensation.PIN_LABEL_PAID + mCurrentGroup.getObjectId(),
                        parseObjects, new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null && mListener != null) {
                                    mListener.onMoreObjectsPinned(parseObjects);
                                }
                            }
                        });
            }
        });
    }

    @Override
    protected void onParseError(ParseException e) {
        if (mListener != null) {
            mListener.onMoreObjectsPinFailed(e);
        }
    }

    @Override
    protected void finish() {
        if (mListener != null) {
            List<ParseObject> emptyList = new ArrayList<>();
            mListener.onMoreObjectsPinned(emptyList);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface HelperInteractionListener {
        void onMoreObjectsPinned(List<ParseObject> objects);

        void onMoreObjectsPinFailed(ParseException e);
    }
}
