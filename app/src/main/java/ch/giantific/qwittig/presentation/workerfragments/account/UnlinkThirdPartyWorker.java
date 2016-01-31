/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.account;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.ParseErrorHandler;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.di.components.WorkerComponent;
import ch.giantific.qwittig.domain.models.parse.User;
import rx.Observable;

/**
 * Handles the unlinking of the user's account from this Facebook or Google profile.
 */
public class UnlinkThirdPartyWorker extends BaseGoogleApiLoginWorker {

    private static final String BUNDLE_UNLINK_ACTION = "BUNDLE_UNLINK_ACTION";
    @Nullable
    private WorkerInteractionListener mListener;
    private User mCurrentUser;

    public UnlinkThirdPartyWorker() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link UnlinkThirdPartyWorker} that will unlink the user's
     * account from Facebook or Google.
     *
     * @param unlinkAction the unlink action to take, either unlink from Facebook or from Google
     * @return a new instance of {@link UnlinkThirdPartyWorker} that will unlink the user's account
     * from Facebook or Google
     */
    @NonNull
    public static UnlinkThirdPartyWorker newInstance(@UnlinkAction int unlinkAction) {
        UnlinkThirdPartyWorker fragment = new UnlinkThirdPartyWorker();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_UNLINK_ACTION, unlinkAction);
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


        mCurrentUser = (User) ParseUser.getCurrentUser();
        int type = 0;
        Bundle args = getArguments();
        if (args != null) {
            type = args.getInt(BUNDLE_UNLINK_ACTION, 0);
        }
        switch (type) {
            case UnlinkAction.UNLINK_FACEBOOK: {
                unlinkFacebook();
                break;
            }
            case UnlinkAction.UNLINK_GOOGLE: {
                setupGoogleApiClient();
                break;
            }
            default:
                if (mListener != null) {
                    mListener.onThirdPartyUnlinkFailed(R.string.toast_unknown_error);
                }
        }
    }

    private void unlinkFacebook() {
        ParseFacebookUtils.unlinkInBackground(mCurrentUser, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                onUserSaved(e);
            }
        });
    }

    private void onUserSaved(ParseException e) {
        if (e != null) {
            if (mListener != null) {
                mListener.onThirdPartyUnlinkFailed(ParseErrorHandler.handleParseError(getActivity(), e));
            }
        }

        if (mListener != null) {
            mListener.onThirdPartyUnlinked();
        }
    }

    @Override
    protected void onGoogleClientConnected() {
        unlinkGoogle();
    }

    @Override
    protected void onGoogleClientConnectionFailed() {
        if (mListener != null) {
            mListener.onThirdPartyUnlinkFailed(R.string.toast_no_connection);
        }
    }

    @Override
    protected void onGoogleUnlinkSuccessful() {
        mCurrentUser.removeGoogleId();
        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                onUserSaved(e);
            }
        });
    }

    @Override
    protected void onGoogleUnlinkFailed() {
        if (mListener != null) {
            mListener.onThirdPartyUnlinkFailed(R.string.toast_unlink_failed);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @IntDef({UnlinkAction.UNLINK_FACEBOOK, UnlinkAction.UNLINK_GOOGLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface UnlinkAction {
        int UNLINK_FACEBOOK = 1;
        int UNLINK_GOOGLE = 2;
    }

    /**
     * Defines the actions to take after a successful login, a failed login or the reset of a
     * password.
     */
    public interface WorkerInteractionListener {
        /**
         * Handles successful login of a user
         */
        void onThirdPartyUnlinked();

        /**
         * Handles the failure to log in a user
         *
         * @param errorMessage the error message from the exception thrown during the process
         */
        void onThirdPartyUnlinkFailed(@StringRes int errorMessage);
    }
}
