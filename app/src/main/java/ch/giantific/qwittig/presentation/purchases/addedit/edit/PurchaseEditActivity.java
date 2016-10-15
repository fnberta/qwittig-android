/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.edit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.di.PersistentViewModelsModule;
import ch.giantific.qwittig.presentation.common.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.BasePurchaseAddEditActivity;
import ch.giantific.qwittig.presentation.purchases.addedit.BasePurchaseAddEditFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.BasePurchaseAddEditReceiptFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddActivity;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.di.DaggerPurchaseEditComponent;
import ch.giantific.qwittig.presentation.purchases.addedit.di.PurchaseEditComponent;
import ch.giantific.qwittig.presentation.purchases.addedit.di.PurchaseEditPresenterModule;
import ch.giantific.qwittig.utils.Utils;
import ch.giantific.qwittig.utils.rxwrapper.android.RxAndroidViews;

/**
 * Hosts {@link PurchaseAddFragment} or {@link PurchaseEditDraftFragment} that handle the
 * editing of a purchase or draft..
 * <p/>
 * Asks the user if he wants to discard the possible changes when dismissing the activity.
 * <p/>
 * Subclass of {@link PurchaseAddActivity}.
 */
public class PurchaseEditActivity extends BasePurchaseAddEditActivity<PurchaseEditComponent> implements
        DiscardChangesDialogFragment.DialogInteractionListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }
    }

    @Override
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        final String editPurchaseId = getIntent().getStringExtra(Navigator.EXTRA_PURCHASE_ID);
        component = DaggerPurchaseEditComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .purchaseEditPresenterModule(new PurchaseEditPresenterModule(editPurchaseId))
                .persistentViewModelsModule(new PersistentViewModelsModule(savedInstanceState))
                .build();
        component.inject(this);

        presenter = isDraft() ? component.getEditDraftPresenter() : component.getEditPresenter();
        presenter.attachView(this);
    }

    private boolean isDraft() {
        return getIntent().getBooleanExtra(Navigator.EXTRA_PURCHASE_EDIT_DRAFT, false);
    }

    @Override
    protected void handleEnterTransition(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (Utils.isRunningLollipopAndHigher()) {
                subscriptions.add(RxAndroidViews.observeTransition(getWindow().getEnterTransition())
                        .subscribe(transitionSubject));
            } else {
                dispatchFakeEnterTransitionEnd();
            }
        } else {
            dispatchFakeEnterTransitionEnd();
        }
    }

    @NonNull
    @Override
    protected BasePurchaseAddEditFragment getPurchaseAddEditFragment() {
        return isDraft() ? new PurchaseEditDraftFragment() : new PurchaseEditFragment();
    }

    @NonNull
    @Override
    protected BasePurchaseAddEditReceiptFragment getReceiptFragment() {
        return isDraft() ? new PurchaseEditDraftReceiptFragment() : new PurchaseEditReceiptFragment();
    }

    @Override
    public void onDiscardChangesSelected() {
        presenter.onDiscardChangesSelected();
    }
}
