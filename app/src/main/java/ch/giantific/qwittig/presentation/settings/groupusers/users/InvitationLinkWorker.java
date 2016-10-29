/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.users;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.rest.exchangerates.ExchangeRates;
import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import rx.Observable;

/**
 * Fetches the newest currency exchange rates online using {@link ExchangeRates}.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class InvitationLinkWorker extends BaseWorker<String, InvitationLinkWorkerListener> {

    private static final String WORKER_TAG = InvitationLinkWorker.class.getCanonicalName();
    private static final String KEY_IDENTITY_ID = "IDENTITY_ID";
    private static final String KEY_GROUP_NAME = "GROUP_NAME";
    private static final String KEY_INVITER_NICKNAME = "INVITER_NICKNAME";
    @Inject
    GroupRepository groupRepo;

    public InvitationLinkWorker() {
        // empty default constructor
    }

    /**
     * Attaches a new instance of {@link InvitationLinkWorker}.
     *
     * @param fm              the fragment manager to use for the transaction
     * @param identityId      the id of the invited identity
     * @param groupName       the name of the group the identity belongs to
     * @param inviterNickname the nickname of the identity sending the invitation
     * @return a new instance of {@link InvitationLinkWorker}
     */
    public static InvitationLinkWorker attach(@NonNull FragmentManager fm, @NonNull String identityId,
                                              @NonNull String groupName, @NonNull String inviterNickname) {
        InvitationLinkWorker worker = (InvitationLinkWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new InvitationLinkWorker();
            final Bundle args = new Bundle();
            args.putString(KEY_IDENTITY_ID, identityId);
            args.putString(KEY_GROUP_NAME, groupName);
            args.putString(KEY_INVITER_NICKNAME, inviterNickname);
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
    protected Observable<String> getObservable(@NonNull Bundle args) {
        final String identityId = args.getString(KEY_IDENTITY_ID, "");
        final String groupName = args.getString(KEY_GROUP_NAME, "");
        final String inviterNickname = args.getString(KEY_INVITER_NICKNAME, "");
        if (!TextUtils.isEmpty(identityId)
                && !TextUtils.isEmpty(groupName)
                && !TextUtils.isEmpty(inviterNickname)) {
            final String apiKey = getString(R.string.google_api_key);
            return groupRepo.getInvitationLink(identityId, groupName, inviterNickname, apiKey).toObservable();
        }

        return null;
    }

    @Override
    protected void onError() {
        activity.onWorkerError(WORKER_TAG);
    }

    @Override
    protected void setStream(@NonNull Observable<String> observable) {
        activity.setInvitationLinkStream(observable.toSingle(), WORKER_TAG);
    }
}
