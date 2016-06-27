/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.edit;

import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel;

/**
 * Defines an observable view model for the edit draft screen.
 */
public interface PurchaseEditDraftViewModel extends PurchaseAddEditViewModel {

    void onDeleteDraftMenuClick();
}
