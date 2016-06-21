/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details.di;

import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.purchases.details.PurchaseDetailsActivity;
import ch.giantific.qwittig.presentation.purchases.details.PurchaseDetailsFragment;
import ch.giantific.qwittig.presentation.purchases.details.PurchaseDetailsReceiptFragment;
import dagger.Subcomponent;

/**
 * Provides the dependencies for the purchase details screen.
 */
@PerActivity
@Subcomponent(modules = {PurchaseDetailsViewModelModule.class})
public interface PurchaseDetailsSubcomponent {

    void inject(PurchaseDetailsActivity purchaseDetailsActivity);

    void inject(PurchaseDetailsFragment purchaseDetailsFragment);

    void inject(PurchaseDetailsReceiptFragment purchaseDetailsReceiptFragment);
}
