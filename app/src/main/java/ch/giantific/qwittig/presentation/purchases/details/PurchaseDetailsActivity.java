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
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.push.FcmMessagingService;
import ch.giantific.qwittig.databinding.ActivityPurchaseDetailsBinding;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.adapters.TabsAdapter;
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
        implements PurchaseDetailsViewModel.ViewListener {

    @Inject
    PurchaseDetailsViewModel mPurchaseDetailsViewModel;
    private boolean mShowEditOptions;
    private boolean mShowExchangeRate;
    private ActivityPurchaseDetailsBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_purchase_details);
        mBinding.setViewModel(mPurchaseDetailsViewModel);

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

        if (mUserLoggedIn) {
            setupTabs();
        }
    }

    @Override
    protected void injectDependencies(@NonNull NavDrawerComponent navComp,
                                      @Nullable Bundle savedInstanceState) {
        mComponent = navComp.plus(new PurchaseDetailsViewModelModule(savedInstanceState,
                getPurchaseId(), getIntent().getStringExtra(FcmMessagingService.PUSH_GROUP_ID)));
        mComponent.inject(this);
        mPurchaseDetailsViewModel.attachView(this);
    }

    private String getPurchaseId() {
        final Intent intent = getIntent();
        // started from HomeActivity
        String purchaseId = intent.getStringExtra(Navigator.INTENT_PURCHASE_ID);
        if (TextUtils.isEmpty(purchaseId)) {
            // started via push notification
            purchaseId = intent.getStringExtra(FcmMessagingService.PUSH_PURCHASE_ID);
        }

        return purchaseId;
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{mPurchaseDetailsViewModel});
    }

    private void setupTabs() {
        final TabsAdapter tabsAdapter = new TabsAdapter(getSupportFragmentManager());
        tabsAdapter.addInitialFragment(new PurchaseDetailsFragment(), getString(R.string.tab_details_purchase));
        tabsAdapter.addInitialFragment(new PurchaseDetailsReceiptFragment(), getString(R.string.tab_details_receipt));
        mBinding.viewpager.setAdapter(tabsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_purchase_details, menu);

        if (mShowEditOptions) {
            menu.findItem(R.id.action_purchase_edit).setVisible(true);
            menu.findItem(R.id.action_purchase_delete).setVisible(true);
        }
        if (mShowExchangeRate) {
            menu.findItem(R.id.action_purchase_show_exchange_rate).setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_purchase_edit:
                mPurchaseDetailsViewModel.onEditPurchaseClick();
                return true;
            case R.id.action_purchase_delete:
                mPurchaseDetailsViewModel.onDeletePurchaseClick();
                return true;
            case R.id.action_purchase_show_exchange_rate:
                mPurchaseDetailsViewModel.onShowExchangeRateClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

        setupTabs();
    }

    @Override
    public void startEnterTransition() {
        ActivityCompat.startPostponedEnterTransition(this);
    }

    @Override
    public void toggleMenuOptions(boolean showEditOptions, boolean showExchangeRateOption) {
        mShowEditOptions = showEditOptions;
        mShowExchangeRate = showExchangeRateOption;
        invalidateOptionsMenu();
    }
}
