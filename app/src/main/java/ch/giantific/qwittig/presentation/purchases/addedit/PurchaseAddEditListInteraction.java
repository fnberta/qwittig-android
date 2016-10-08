package ch.giantific.qwittig.presentation.purchases.addedit;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.listadapters.interactions.ListInteraction;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditArticleIdentityItemViewModel;

/**
 * Created by fabio on 08.10.16.
 */

public interface PurchaseAddEditListInteraction extends ListInteraction {

    void notifyItemIdentityChanged(int identityRowPos,
                                   @NonNull PurchaseAddEditArticleIdentityItemViewModel itemViewModel);
}
