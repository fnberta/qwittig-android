/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.workerfragments.save;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import ch.giantific.qwittig.ParseErrorHandler;
import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.workerfragments.BaseWorker;

/**
 * Saves a {@link Compensation} object to the online Parse.com database.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class CompensationSaveWorker extends BaseWorker {

    private static final String LOG_TAG = CompensationSaveWorker.class.getSimpleName();
    @Nullable
    private WorkerInteractionListener mListener;
    private Compensation mCompensation;

    public CompensationSaveWorker() {
        // empty default constructor
    }

    /**
     * Constructs a new {@link CompensationSaveWorker} with a {@link Compensation} object as a
     * parameter.
     * <p/>
     * Using a non empty constructor to be able to pass a {@link ParseObject}. Because the fragment
     * is retained across configuration changes, there is no risk that the system will recreate it
     * with the default empty constructor.
     *
     * @param compensation the {@link Compensation} to save
     */
    @SuppressLint("ValidFragment")
    public CompensationSaveWorker(@NonNull ParseObject compensation) {
        mCompensation = (Compensation) compensation;
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

        saveCompensation();
    }

    private void saveCompensation() {
        mCompensation.saveInBackground(new SaveCallback() {
            @Override
            public void done(@Nullable ParseException e) {
                if (e != null) {
                    mCompensation.setPaid(false);

                    if (mListener != null) {
                        mListener.onCompensationSaveFailed(mCompensation,
                                ParseErrorHandler.handleParseError(getActivity(), e));
                    }

                    return;
                }

                if (mListener != null) {
                    mListener.onCompensationSaved(mCompensation);
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the actions to take after the compensation was saved or the action failed.
     */
    public interface WorkerInteractionListener {
        /**
         * Handles the successful save of the {@link Compensation} object.
         *
         * @param compensation the saved {@link Compensation} object
         */
        void onCompensationSaved(@NonNull ParseObject compensation);

        /**
         * Handles the failure of saving the {@link Compensation} object.
         *
         * @param compensation the {@link Compensation} object that failed to save
         * @param errorMessage the error message from the exception thrown during the save process
         */
        void onCompensationSaveFailed(@NonNull ParseObject compensation, @StringRes int errorMessage);
    }
}
