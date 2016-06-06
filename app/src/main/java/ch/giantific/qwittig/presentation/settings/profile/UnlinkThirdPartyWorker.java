/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile;

import android.app.Application;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;

import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import rx.Observable;

/**
 * Handles the unlinking of the user's account from this Facebook or Google profile.
 */
public class UnlinkThirdPartyWorker extends BaseWorker<User, UnlinkThirdPartyWorkerListener> {

    private static final String WORKER_TAG = UnlinkThirdPartyWorker.class.getCanonicalName();
    private static final String KEY_PROFILE_ACTION = "PROFILE_ACTION";
    @Inject
    Application mAppContext;
    @ProfileAction
    private int mType;

    public UnlinkThirdPartyWorker() {
        // empty default constructor
    }

    /**
     * Attaches a new instance of {@link UnlinkThirdPartyWorker} that will unlink the user's
     * account from Facebook or Google.
     *
     * @param fm           the fragment manager to user for the transaction
     * @param unlinkAction the unlink action to take, either unlink from Facebook or from Google
     * @return a new instance of {@link UnlinkThirdPartyWorker} that will unlink the user's account
     * from Facebook or Google
     */
    @NonNull
    public static UnlinkThirdPartyWorker attachUnlink(@NonNull FragmentManager fm,
                                                      @ProfileAction int unlinkAction) {
        UnlinkThirdPartyWorker worker = (UnlinkThirdPartyWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new UnlinkThirdPartyWorker();
            final Bundle args = new Bundle();
            args.putInt(KEY_PROFILE_ACTION, unlinkAction);
            worker.setArguments(args);

            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();
        }

        return worker;
    }

    @Override
    protected void injectWorkerDependencies(@NonNull WorkerComponent component) {
        component.inject(this);
    }

    @SuppressWarnings("WrongConstant")
    @Nullable
    @Override
    protected Observable<User> getObservable(@NonNull Bundle args) {
        mType = args.getInt(KEY_PROFILE_ACTION, 0);
        final User user = mUserRepo.getCurrentUser();
        if (user != null) {
            switch (mType) {
                case ProfileAction.UNLINK_FACEBOOK: {
                    return mUserRepo.unlinkFacebook(user, false).toObservable();
                }
                case ProfileAction.UNLINK_GOOGLE: {
                    return mUserRepo.unlinkGoogle(mAppContext, user, false).toObservable();
                }
            }
        }

        return null;
    }

    @Override
    protected void onError() {
        mActivity.onWorkerError(WORKER_TAG);
    }

    @Override
    protected void setStream(@NonNull Observable<User> observable) {
        mActivity.setUnlinkActionStream(observable.toSingle(), WORKER_TAG, mType);
    }

    @IntDef({ProfileAction.UNLINK_FACEBOOK, ProfileAction.UNLINK_GOOGLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ProfileAction {
        int UNLINK_FACEBOOK = 1;
        int UNLINK_GOOGLE = 2;
    }
}
