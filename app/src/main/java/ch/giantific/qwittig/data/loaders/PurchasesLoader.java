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
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import rx.Observable;
import rx.functions.Func1;

/**
 * Gets the purchases from the local data store and sends them as the result.
 * <p/>
 * Subclass of {@link BaseRxLoader}.
 */
public class PurchasesLoader extends BaseRxLoader<Purchase> {

    private final UserRepository mUserRepo;
    private final PurchaseRepository mPurchaseRepo;

    @Inject
    public PurchasesLoader(@NonNull Application context, @NonNull UserRepository userRepository,
                           @NonNull PurchaseRepository purchaseRepository) {
        super(context);

        mUserRepo = userRepository;
        mPurchaseRepo = purchaseRepository;
    }

    @Override
    @Nullable
    protected Observable<Purchase> getObservable() {
        final User currentUser = mUserRepo.getCurrentUser();
        if (currentUser != null) {
            return mUserRepo.fetchIdentityDataAsync(currentUser.getCurrentIdentity())
                    .flatMapObservable(new Func1<Identity, Observable<Purchase>>() {
                        @Override
                        public Observable<Purchase> call(Identity identity) {
                            return mPurchaseRepo.getPurchasesLocalAsync(identity, false);
                        }
                    });
        }

        return null;
    }
}
