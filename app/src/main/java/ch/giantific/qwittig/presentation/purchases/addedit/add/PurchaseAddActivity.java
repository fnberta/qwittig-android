/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.add;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.data.push.FcmMessagingService;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.purchases.addedit.BasePurchaseAddEditActivity;
import ch.giantific.qwittig.presentation.purchases.addedit.BasePurchaseAddEditFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.BasePurchaseAddEditReceiptFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.di.DaggerPurchaseAddComponent;
import ch.giantific.qwittig.presentation.purchases.addedit.di.PurchaseAddComponent;
import ch.giantific.qwittig.presentation.purchases.addedit.di.PurchaseAddViewModelModule;
import ch.giantific.qwittig.utils.Utils;

/**
 * Hosts {@link PurchaseAddFragment} that handles the creation of a new purchase.
 * <p/>
 * Asks the user if he wants to discard the new purchase when dismissing the activity.
 */
public class PurchaseAddActivity extends BasePurchaseAddEditActivity<PurchaseAddComponent> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            if (Utils.isRunningLollipopAndHigher()) {
                // check if activity was started from push notification, we have no activity
                // transition then and need to show the fab
                if (getIntent().hasExtra(FcmMessagingService.PUSH_OCR_DATA_ID)) {
                    showFab();
                }
            }
        }
    }

    @Override
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        component = DaggerPurchaseAddComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .purchaseAddViewModelModule(new PurchaseAddViewModelModule(savedInstanceState))
                .build();
        component.inject(this);
        final String ocrDataId = getOcrDataId();
        if (!TextUtils.isEmpty(ocrDataId)) {
            addEditViewModel = component.getAddOcrViewModel();
            ((PurchaseAddOcrViewModel) addEditViewModel).setOcrDataId(ocrDataId);
        } else {
            addEditViewModel = component.getAddViewModel();
        }
        addEditViewModel.attachView(this);
    }

    @NonNull
    protected BasePurchaseAddEditFragment getPurchaseAddEditFragment() {
        return TextUtils.isEmpty(getOcrDataId())
                ? new PurchaseAddFragment()
                : new PurchaseAddOcrFragment();
    }

    @Nullable
    private String getOcrDataId() {
        final Intent intent = getIntent();
        String ocrDataId = intent.getStringExtra(Navigator.INTENT_OCR_PURCHASE_ID);
        if (TextUtils.isEmpty(ocrDataId)) {
            ocrDataId = intent.getStringExtra(FcmMessagingService.PUSH_OCR_DATA_ID);
        }

        return ocrDataId;
    }

    @Override
    protected BasePurchaseAddEditReceiptFragment getReceiptFragment() {
        return TextUtils.isEmpty(getOcrDataId())
                ? new PurchaseAddReceiptFragment()
                : new PurchaseAddOcrReceiptFragment();
    }
}
