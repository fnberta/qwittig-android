/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.workerfragments.query;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.annotation.StringRes;
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
 * Subclass of {@link BaseQueryWorker}.
 */
public class MoreQueryWorker extends BaseQueryWorker {

    public static final String MORE_QUERY_WORKER = "MORE_QUERY_WORKER";
    private static final String BUNDLE_CLASS_NAME = "BUNDLE_CLASS_NAME";
    private static final String BUNDLE_SKIP = "BUNDLE_SKIP";
    private static final String LOG_TAG = MoreQueryWorker.class.getSimpleName();
    @Nullable
    private WorkerInteractionListener mListener;

    public MoreQueryWorker() {
        // empty default constructor
    }

    /**
     * Return a new instance of {@link MoreQueryWorker} with the class name of the items to query
     * and number of items to skip as arguments.
     *
     * @param className the class name of the items to query, must be defined in {@link ClassName}
     * @param skip      the number of items to skip
     * @return a new instance of {@link MoreQueryWorker}
     */
    @NonNull
    public static MoreQueryWorker newInstance(@NonNull @ClassName String className, int skip) {
        MoreQueryWorker fragment = new MoreQueryWorker();
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
            mListener = (WorkerInteractionListener) activity;
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
                PurchaseRepository repo = new ParsePurchaseRepository(getActivity());
                repo.getPurchasesOnlineAsync(mCurrentUser, mCurrentGroup, skip, new PurchaseRepository.GetPurchasesOnlineListener() {
                    @Override
                    public void onPurchasesOnlineLoaded(@NonNull List<ParseObject> purchases) {
                        onLoaded(purchases);
                    }

                    @Override
                    public void onPurchaseOnlineLoadFailed(@StringRes int errorMessage) {
                        onLoadFailed(errorMessage);
                    }
                });
                break;
            }
            case Compensation.CLASS: {
                CompensationRepository repo = new ParseCompensationRepository(getActivity());
                repo.getCompensationsPaidOnlineAsync(mCurrentGroup, skip, new CompensationRepository.GetCompensationsOnlineListener() {
                    @Override
                    public void onCompensationsPaidOnlineLoaded(@NonNull List<ParseObject> purchases) {
                        onLoaded(purchases);
                    }

                    @Override
                    public void onCompensationsPaidOnlineLoadFailed(int errorMessage) {
                        onLoadFailed(errorMessage);
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

    private void onLoadFailed(int errorMessage) {
        if (mListener != null) {
            mListener.onMoreObjectsLoadFailed(errorMessage);
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
    public interface WorkerInteractionListener {
        /**
         * Handles the successful pin of new objects.
         *
         * @param objects the newly pinned objects
         */
        void onMoreObjectsLoaded(@NonNull List<ParseObject> objects);

        /**
         * Handles the failed query or pin of new objects.
         *
         * @param errorMessage the error message from the exception thrown during the process
         */
        void onMoreObjectsLoadFailed(@StringRes int errorMessage);
    }
}
