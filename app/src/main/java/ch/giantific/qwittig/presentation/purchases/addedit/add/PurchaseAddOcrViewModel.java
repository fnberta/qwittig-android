package ch.giantific.qwittig.presentation.purchases.addedit.add;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel;

/**
 * Created by fabio on 18.06.16.
 */
public interface PurchaseAddOcrViewModel extends PurchaseAddEditViewModel {

    void setOcrDataId(@NonNull String ocrDataId);
}
