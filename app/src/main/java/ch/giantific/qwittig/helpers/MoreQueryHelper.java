/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.text.TextUtils;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.data.parse.OnlineQuery;
import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.Purchase;

/**
 * Queries for more items of one of the types defined in {@link ClassName} and pins them to the
 * local data store.
 * <p/>
 * Subclass of {@link BaseQueryHelper}.
 */
public class MoreQueryHelper extends BaseQueryHelper {

    @StringDef({Purchase.CLASS, Compensation.CLASS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ClassName {}
    public static final String MORE_QUERY_HELPER = "MORE_QUERY_HELPER";
    private static final String BUNDLE_CLASS_NAME = "BUNDLE_CLASS_NAME";
    private static final String BUNDLE_SKIP = "BUNDLE_SKIP";
    private static final String LOG_TAG = MoreQueryHelper.class.getSimpleName();
    @Nullable
    private HelperInteractionListener mListener;

    public MoreQueryHelper() {
        // empty default constructor
    }

    /**
     * Return a new instance of {@link MoreQueryHelper} with the class name of the items to query
     * and number of items to skip as arguments.
     *
     * @param className the class name of the items to query, must be defined in {@link ClassName}
     * @param skip      the number of items to skip
     * @return a new instance of {@link MoreQueryHelper}
     */
    @NonNull
    public static MoreQueryHelper newInstance(@NonNull @ClassName String className, int skip) {
        MoreQueryHelper fragment = new MoreQueryHelper();
        Bundle args = new Bundle();
        args.putString(BUNDLE_CLASS_NAME, className);
        args.putInt(BUNDLE_SKIP, skip);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (HelperInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String className = "";
        int skip = 0;
        Bundle args = getArguments();
        if (args != null) {
            className = args.getString(BUNDLE_CLASS_NAME);
            skip = args.getInt(BUNDLE_SKIP);
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
            public void done(@NonNull final List<ParseObject> parseObjects, @Nullable ParseException e) {
                if (e != null) {
                    onParseError(e);
                    return;
                }

                ParseObject.pinAllInBackground(Purchase.PIN_LABEL + mCurrentGroup.getObjectId(),
                        parseObjects, new SaveCallback() {
                            @Override
                            public void done(@Nullable ParseException e) {
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
            public void done(@NonNull final List<ParseObject> parseObjects, @Nullable ParseException e) {
                if (e != null) {
                    onParseError(e);
                    return;
                }

                ParseObject.pinAllInBackground(Compensation.PIN_LABEL_PAID + mCurrentGroup.getObjectId(),
                        parseObjects, new SaveCallback() {
                            @Override
                            public void done(@Nullable ParseException e) {
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

    /**
     * Defines the actions to take after more objects were queried and pinned to the local data
     * store or after the query failed.
     */
    public interface HelperInteractionListener {
        /**
         * Handles the successful pin of new objects.
         *
         * @param objects the newly pinned objects
         */
        void onMoreObjectsPinned(@NonNull List<ParseObject> objects);

        /**
         * Handles the failed query or pin of new objects.
         *
         * @param e the {@link ParseException} thrown during the process
         */
        void onMoreObjectsPinFailed(@NonNull ParseException e);
    }
}
