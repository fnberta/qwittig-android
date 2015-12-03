/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.helpers.account;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.domain.models.parse.User;

/**
 * Handles the unlinking of the user's account from this Facebook or Google profile.
 */
public class UnlinkThirdPartyHelper extends BaseGoogleApiLoginHelper {

    @IntDef({UNLINK_FACEBOOK, UNLINK_GOOGLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface UnlinkAction {}
    public static final int UNLINK_FACEBOOK = 1;
    public static final int UNLINK_GOOGLE = 2;
    private static final String BUNDLE_UNLINK_ACTION = "BUNDLE_UNLINK_ACTION";
    private static final String LOG_TAG = UnlinkThirdPartyHelper.class.getSimpleName();
    @Nullable
    private HelperInteractionListener mListener;
    private User mCurrentUser;

    public UnlinkThirdPartyHelper() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link UnlinkThirdPartyHelper} that will unlink the user's
     * account from Facebook or Google.
     *
     * @param unlinkAction the unlink action to take, either unlink from Facebook or from Google
     * @return a new instance of {@link UnlinkThirdPartyHelper} that will unlink the user's account
     * from Facebook or Google
     */
    @NonNull
    public static UnlinkThirdPartyHelper newInstance(@UnlinkAction int unlinkAction) {
        UnlinkThirdPartyHelper fragment = new UnlinkThirdPartyHelper();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_UNLINK_ACTION, unlinkAction);
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

        Bundle args = getArguments();
        if (args == null) {
            if (mListener != null) {
                mListener.onThirdPartyUnlinkFailed(0);
            }

            return;
        }

        mCurrentUser = (User) ParseUser.getCurrentUser();
        final int type = args.getInt(BUNDLE_UNLINK_ACTION);
        switch (type) {
            case UNLINK_FACEBOOK: {
                unlinkFacebook();
                break;
            }
            case UNLINK_GOOGLE: {
                setupGoogleApiClient();
                break;
            }
            default:
                if (mListener != null) {
                    mListener.onThirdPartyUnlinkFailed(0);
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
                mListener.onThirdPartyUnlinkFailed(e.getCode());
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
            mListener.onThirdPartyUnlinkFailed(0);
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
    protected void onGoogleUnlinkFailed(int errorCode) {
        if (mListener != null) {
            mListener.onThirdPartyUnlinkFailed(errorCode);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the actions to take after a successful login, a failed login or the reset of a
     * password.
     */
    public interface HelperInteractionListener {
        /**
         * Handles successful login of a user
         */
        void onThirdPartyUnlinked();

        /**
         * Handles the failure to log in a user
         *
         * @param errorCode the error code of the exception thrown during the process
         */
        void onThirdPartyUnlinkFailed(int errorCode);
    }
}
