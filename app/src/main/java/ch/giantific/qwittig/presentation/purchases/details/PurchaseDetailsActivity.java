/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.LocalBroadcast;
import ch.giantific.qwittig.data.push.PushBroadcastReceiver;
import ch.giantific.qwittig.databinding.ActivityPurchaseDetailsBinding;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.navdrawer.BaseNavDrawerActivity;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerComponent;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.details.di.PurchaseDetailsSubcomponent;
import ch.giantific.qwittig.presentation.purchases.details.di.PurchaseDetailsViewModelModule;

/**
 * Hosts {@link PurchaseDetailsFragment} that displays the details of a purchase and
 * {@link PurchaseDetailsReceiptFragment} that displays the image of a receipt.
 * <p/>
 * Displays the store and date of the purchase in the {@link Toolbar}.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class PurchaseDetailsActivity extends BaseNavDrawerActivity<PurchaseDetailsSubcomponent>
        implements PurchaseDetailsFragment.ActivityListener, PurchaseDetailsViewModel.ViewListener,
        PurchaseDetailsReceiptFragment.ActivityListener {

    public static final String PURCHASE_DETAILS_FRAGMENT = "PURCHASE_DETAILS_FRAGMENT";
    public static final String PURCHASE_RECEIPT_FRAGMENT = "PURCHASE_RECEIPT_FRAGMENT";
    @Inject
    PurchaseDetailsViewModel mPurchaseDetailsViewModel;
    private boolean mShowEditOptions;
    private boolean mShowExchangeRate;
    private boolean mShowReceipt;

    @Override
    protected void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);

        if (dataType == LocalBroadcast.DataType.PURCHASES_UPDATED) {
            final boolean successful = intent.getBooleanExtra(LocalBroadcast.INTENT_EXTRA_SUCCESSFUL, false);
            if (successful) {
                mPurchaseDetailsViewModel.loadData();
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityPurchaseDetailsBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_purchase_details);
        binding.setViewModel(mPurchaseDetailsViewModel);

        // disable default actionBar title
        final ActionBar actionBar = getSupportActionBar();
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


        if (mUserLoggedIn && savedInstanceState == null) {
            addDetailsFragment();
        }
    }

    private void addDetailsFragment() {
        final PurchaseDetailsFragment fragment = new PurchaseDetailsFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment, PURCHASE_DETAILS_FRAGMENT)
                .commit();
    }

    @Override
    protected void injectDependencies(@NonNull NavDrawerComponent navComp,
                                      @Nullable Bundle savedInstanceState) {
        mComponent = navComp.plus(new PurchaseDetailsViewModelModule(savedInstanceState, getPurchaseId()));
        mComponent.inject(this);
        mPurchaseDetailsViewModel.attachView(this);
    }

    private String getPurchaseId() {
        final Intent intent = getIntent();
        String purchaseId = intent.getStringExtra(Navigator.INTENT_PURCHASE_ID); // started from HomeActivity

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
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{mPurchaseDetailsViewModel});
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Navigator.INTENT_REQUEST_PURCHASE_MODIFY) {
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
    public void setupScreenAfterLogin() {
        super.setupScreenAfterLogin();

        addDetailsFragment();
    }

    @Override
    public boolean isShowEditOptions() {
        return mShowEditOptions;
    }

    @Override
    public boolean isShowExchangeRate() {
        return mShowExchangeRate;
    }

    @Override
    public boolean isShowReceipt() {
        return mShowReceipt;
    }

    @Override
    public void startEnterTransition() {
        ActivityCompat.startPostponedEnterTransition(this);
    }

    @Override
    public void showPurchaseDetailsReceipt() {
        final Fragment fragment = new PurchaseDetailsReceiptFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, PURCHASE_RECEIPT_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void toggleMenuOptions(boolean showEditOptions, boolean hasReceiptImage,
                                  boolean hasForeignCurrency) {
        mShowEditOptions = showEditOptions;
        mShowExchangeRate = hasForeignCurrency;
        mShowReceipt = hasReceiptImage;
        invalidateOptionsMenu();
    }
}
