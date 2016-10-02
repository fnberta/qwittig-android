/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.di;

import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.di.GoogleApiClientDelegateModule;
import ch.giantific.qwittig.presentation.purchases.list.HomeActivity;
import ch.giantific.qwittig.presentation.purchases.list.drafts.DraftsFragment;
import ch.giantific.qwittig.presentation.purchases.list.drafts.DraftsContract;
import ch.giantific.qwittig.presentation.purchases.list.purchases.PurchasesFragment;
import ch.giantific.qwittig.presentation.purchases.list.purchases.PurchasesContract;
import dagger.Subcomponent;

/**
 * Provides the dependencies for the home screen.
 */
@PerActivity
@Subcomponent(modules = {HomePresenterModule.class, GoogleApiClientDelegateModule.class,
        PurchasesPresenterModule.class, DraftsPresenterModule.class})
public interface HomeSubcomponent {

    void inject(HomeActivity homeActivity);

    void inject(PurchasesFragment purchasesFragment);

    void inject(DraftsFragment draftsFragment);

    PurchasesContract.Presenter getPurchasesPresenter();

    DraftsContract.Presenter getDraftsPresenter();
}
