/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import ch.giantific.qwittig.data.bus.LocalBroadcast;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.ActivityPurchaseDetailsBinding;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerComponent;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.home.purchases.list.PurchasesFragment;
import ch.giantific.qwittig.presentation.navdrawer.BaseNavDrawerActivity;
import ch.giantific.qwittig.data.receivers.PushBroadcastReceiver;

/**
 * Hosts {@link PurchaseDetailsFragment} that displays the details of a purchase and
 * {@link PurchaseReceiptDetailFragment} that displays the image of a receipt.
 * <p/>
 * Displays the store and date of the purchase in the {@link Toolbar}.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class PurchaseDetailsActivity extends BaseNavDrawerActivity<PurchaseDetailsViewModel>
        implements PurchaseDetailsFragment.ActivityListener {

    private ActivityPurchaseDetailsBinding mBinding;

    @Override
    protected void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);

        if (dataType == LocalBroadcast.DataType.PURCHASES_UPDATED) {
            mViewModel.loadData();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_purchase_details);

        // disable default actionBar title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        replaceDrawerIndicatorWithUp();
        unCheckNavDrawerItems();
        supportPostponeEnterTransition();

        final PurchaseDetailsActivity activity = this;
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(activity);
            }
        });


        if (savedInstanceState == null) {
            final PurchaseDetailsFragment fragment = PurchaseDetailsFragment.newInstance(getPurchaseId());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    protected void injectNavDrawerDependencies(@NonNull NavDrawerComponent navComp) {
        navComp.inject(this);
    }

    private String getPurchaseId() {
        final Intent intent = getIntent();
        String purchaseId = intent.getStringExtra(PurchasesFragment.INTENT_PURCHASE_ID); // started from HomeActivity

        if (TextUtils.isEmpty(purchaseId)) { // started via push notification
            try {
                JSONObject jsonExtras = PushBroadcastReceiver.getData(intent);
                purchaseId = jsonExtras.optString(PushBroadcastReceiver.PUSH_PARAM_PURCHASE_ID);
            } catch (JSONException e) {
                showMessage(R.string.toast_error_purchase_details_load);
            }
        }

        return purchaseId;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENT_REQUEST_PURCHASE_MODIFY) {
            switch (resultCode) {
                case PurchaseAddEditViewModel.PurchaseResult.PURCHASE_SAVED:
                    showMessage(R.string.toast_changes_saved);
                    break;
                case PurchaseAddEditViewModel.PurchaseResult.PURCHASE_DISCARDED:
                    showMessage(R.string.toast_changes_discarded);
                    break;
            }
        }
    }

    @Override
    public void setDetailsViewModel(@NonNull PurchaseDetailsViewModel viewModel) {
        mViewModel = viewModel;
        mBinding.setViewModel(viewModel);
    }
}
