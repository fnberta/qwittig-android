/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.loaders;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by fabio on 15.02.16.
 */
public class PurchasesLoader extends BaseRxLoader<Purchase> {

    private UserRepository mUserRepo;
    private IdentityRepository mIdentityRepo;
    private PurchaseRepository mPurchaseRepo;

    @Inject
    public PurchasesLoader(@NonNull Application context, @NonNull UserRepository userRepository,
                           @NonNull IdentityRepository identityRepository,
                           @NonNull PurchaseRepository purchaseRepository) {
        super(context);

        mUserRepo = userRepository;
        mIdentityRepo = identityRepository;
        mPurchaseRepo = purchaseRepository;
    }

    @Override
    @Nullable
    protected Observable<Purchase> getObservable() {
        final User currentUser = mUserRepo.getCurrentUser();
        if (currentUser != null) {
            return mIdentityRepo.fetchIdentityDataAsync(currentUser.getCurrentIdentity())
                    .flatMap(new Func1<Identity, Observable<Purchase>>() {
                        @Override
                        public Observable<Purchase> call(Identity identity) {
                            return mPurchaseRepo.getPurchasesLocalAsync(identity, false);
                        }
                    });
        }

        return null;
    }
}
