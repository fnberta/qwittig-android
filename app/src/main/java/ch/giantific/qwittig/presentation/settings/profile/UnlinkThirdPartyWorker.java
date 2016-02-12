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

import ch.giantific.qwittig.di.components.WorkerComponent;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import rx.Observable;

/**
 * Handles the unlinking of the user's account from this Facebook or Google profile.
 */
public class UnlinkThirdPartyWorker extends BaseWorker<User, UnlinkThirdPartyWorkerListener> {

    private static final String WORKER_TAG = UnlinkThirdPartyWorker.class.getCanonicalName();
    private static final String KEY_UNLINK_ACTION = "UNLINK_ACTION";
    @Inject
    Application mAppContext;

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
    public static UnlinkThirdPartyWorker attach(@NonNull FragmentManager fm,
                                                @UnlinkAction int unlinkAction) {
        UnlinkThirdPartyWorker worker = (UnlinkThirdPartyWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new UnlinkThirdPartyWorker();
            final Bundle args = new Bundle();
            args.putInt(KEY_UNLINK_ACTION, unlinkAction);
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

    @Nullable
    @Override
    protected Observable<User> getObservable(@NonNull Bundle args) {
        final int type = args.getInt(KEY_UNLINK_ACTION, 0);
        final User user = mUserRepo.getCurrentUser();
        if (user != null) {
            switch (type) {
                case UnlinkAction.UNLINK_FACEBOOK: {
                    return mUserRepo.unlinkFacebook(user).toObservable();
                }
                case UnlinkAction.UNLINK_GOOGLE: {
                    return mUserRepo.unlinkGoogle(mAppContext, user)
                            .toObservable();
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
        mActivity.setUnlinkStream(observable.toSingle(), WORKER_TAG);
    }

    @IntDef({UnlinkAction.UNLINK_FACEBOOK, UnlinkAction.UNLINK_GOOGLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface UnlinkAction {
        int UNLINK_FACEBOOK = 1;
        int UNLINK_GOOGLE = 2;
    }
}
