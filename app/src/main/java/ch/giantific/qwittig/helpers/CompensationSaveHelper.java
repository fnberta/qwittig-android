/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import ch.giantific.qwittig.data.parse.models.Compensation;

/**
 * Saves a {@link Compensation} object to the online Parse.com database.
 * <p/>
 * Subclass of {@link BaseHelper}.
 */
public class CompensationSaveHelper extends BaseHelper {

    private static final String LOG_TAG = CompensationSaveHelper.class.getSimpleName();
    @Nullable
    private HelperInteractionListener mListener;
    private Compensation mCompensation;

    public CompensationSaveHelper() {
        // empty default constructor
    }

    /**
     * Constructs a new {@link CompensationSaveHelper} with a {@link Compensation} object as a
     * parameter.
     *
     * Using a non empty constructor to be able to pass a {@link ParseObject}. Because the fragment
     * is retained across configuration changes, there is no risk that the system will recreate it
     * with the default empty constructor.
     *
     * @param compensation the {@link Compensation} to save
     */
    @SuppressLint("ValidFragment")
    public CompensationSaveHelper(@NonNull ParseObject compensation) {
        mCompensation = (Compensation) compensation;
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

        saveCompensation();
    }

    private void saveCompensation() {
        mCompensation.saveInBackground(new SaveCallback() {
            @Override
            public void done(@Nullable ParseException e) {
                if (e != null) {
                    mCompensation.setPaid(false);

                    if (mListener != null) {
                        mListener.onCompensationSaveFailed(mCompensation, e);
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
     * Defines the actions to take after the {@link Compensation} was saved or the action failed.
     */
    public interface HelperInteractionListener {
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
         * @param e            the {@link ParseException} thrown during the save process
         */
        void onCompensationSaveFailed(@NonNull ParseObject compensation, @NonNull ParseException e);
    }
}
