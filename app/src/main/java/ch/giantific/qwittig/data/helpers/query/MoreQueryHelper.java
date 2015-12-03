/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.helpers.query;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.text.TextUtils;

import com.parse.ParseObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.data.repositories.ParseCompensationRepository;
import ch.giantific.qwittig.data.repositories.ParsePurchaseRepository;
import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;

/**
 * Queries for more items of one of the types defined in {@link ClassName} and pins them to the
 * local data store.
 * <p/>
 * Subclass of {@link BaseQueryHelper}.
 */
public class MoreQueryHelper extends BaseQueryHelper {

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
            className = args.getString(BUNDLE_CLASS_NAME, "");
            skip = args.getInt(BUNDLE_SKIP, 0);
        }

        if (TextUtils.isEmpty(className)) {
            failEarly();
            return;
        }

        if (!setCurrentGroups()) {
            failEarly();
            return;
        }

        switch (className) {
            case Purchase.CLASS: {
                PurchaseRepository repo = new ParsePurchaseRepository();
                repo.getPurchasesOnlineAsync(mCurrentUser, mCurrentGroup, skip, new PurchaseRepository.GetPurchasesOnlineListener() {
                    @Override
                    public void onPurchasesOnlineLoaded(@NonNull List<ParseObject> purchases) {
                        onLoaded(purchases);
                    }

                    @Override
                    public void onPurchaseOnlineLoadFailed(int errorCode) {
                        onLoadFailed(errorCode);
                    }
                });
                break;
            }
            case Compensation.CLASS: {
                CompensationRepository repo = new ParseCompensationRepository();
                repo.getCompensationsPaidOnlineAsync(mCurrentGroup, skip, new CompensationRepository.GetCompensationsOnlineListener() {
                    @Override
                    public void onCompensationsPaidOnlineLoaded(@NonNull List<ParseObject> purchases) {
                        onLoaded(purchases);
                    }

                    @Override
                    public void onCompensationsPaidOnlineLoadFailed(int errorCode) {
                        onLoadFailed(errorCode);
                    }
                });
                break;
            }
        }
    }

    private void failEarly() {
        if (mListener != null) {
            List<ParseObject> emptyList = new ArrayList<>();
            mListener.onMoreObjectsLoaded(emptyList);
        }
    }

    private void onLoaded(List<ParseObject> objects) {
        if (mListener != null) {
            mListener.onMoreObjectsLoaded(objects);
        }
    }

    private void onLoadFailed(int errorCode) {
        if (mListener != null) {
            mListener.onMoreObjectsLoadFailed(errorCode);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @StringDef({Purchase.CLASS, Compensation.CLASS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ClassName {
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
        void onMoreObjectsLoaded(@NonNull List<ParseObject> objects);

        /**
         * Handles the failed query or pin of new objects.
         *
         * @param errorCode the error code of the exception thrown during the process
         */
        void onMoreObjectsLoadFailed(int errorCode);
    }
}
