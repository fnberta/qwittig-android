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
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import rx.Observable;
import rx.functions.Func1;

/**
 * Handles the unlinking of the user's account from this Facebook or Google profile.
 */
public class SettingsProfileWorker extends BaseWorker<User, SettingsProfileWorkerListener> {

    private static final String WORKER_TAG = SettingsProfileWorker.class.getCanonicalName();
    private static final String KEY_PROFILE_ACTION = "PROFILE_ACTION";
    private static final String KEY_NICKNAME = "NICKNAME";
    private static final String KEY_AVATAR = "AVATAR";
    @Inject
    Application mAppContext;
    @ProfileAction
    private int mType;

    public SettingsProfileWorker() {
        // empty default constructor
    }

    /**
     * Attaches a new instance of {@link SettingsProfileWorker} that will update the user's
     * identities with a new nickname and avatar
     *
     * @param fm       the fragment manager to user for the transaction
     * @param nickname the nickname to set to all identities
     * @param avatar   the avatar to set for all identities
     * @return a new instance of {@link SettingsProfileWorker}
     */
    @NonNull
    public static SettingsProfileWorker attachSaveAvatar(@NonNull FragmentManager fm,
                                                         @NonNull String nickname,
                                                         @NonNull byte[] avatar) {
        SettingsProfileWorker worker = (SettingsProfileWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new SettingsProfileWorker();
            final Bundle args = new Bundle();
            args.putInt(KEY_PROFILE_ACTION, ProfileAction.SAVE_AVATAR);
            args.putString(KEY_NICKNAME, nickname);
            args.putByteArray(KEY_AVATAR, avatar);
            worker.setArguments(args);

            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();
        }

        return worker;
    }

    /**
     * Attaches a new instance of {@link SettingsProfileWorker} that will unlink the user's
     * account from Facebook or Google.
     *
     * @param fm           the fragment manager to user for the transaction
     * @param unlinkAction the unlink action to take, either unlink from Facebook or from Google
     * @return a new instance of {@link SettingsProfileWorker} that will unlink the user's account
     * from Facebook or Google
     */
    @NonNull
    public static SettingsProfileWorker attachUnlink(@NonNull FragmentManager fm,
                                                     @ProfileAction int unlinkAction) {
        SettingsProfileWorker worker = (SettingsProfileWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new SettingsProfileWorker();
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
                case ProfileAction.SAVE_AVATAR:
                    final String nickname = args.getString(KEY_NICKNAME, "");
                    final byte[] avatar = args.getByteArray(KEY_AVATAR);
                    return mUserRepo.saveIdentitiesWithAvatar(user.getIdentities(), nickname, avatar)
                            .toList()
                            .map(new Func1<List<Identity>, User>() {
                                @Override
                                public User call(List<Identity> identities) {
                                    return user;
                                }
                            });
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
        mActivity.setProfileActionStream(observable.toSingle(), WORKER_TAG, mType);
    }

    @IntDef({ProfileAction.SAVE_AVATAR, ProfileAction.UNLINK_FACEBOOK, ProfileAction.UNLINK_GOOGLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ProfileAction {
        int SAVE_AVATAR = 0;
        int UNLINK_FACEBOOK = 1;
        int UNLINK_GOOGLE = 2;
    }
}
