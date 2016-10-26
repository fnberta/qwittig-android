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

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.push.FcmMessagingService;
import ch.giantific.qwittig.databinding.ActivityPurchaseDetailsBinding;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.PersistentViewModelsModule;
import ch.giantific.qwittig.presentation.common.listadapters.TabsAdapter;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.navdrawer.BaseNavDrawerActivity;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerComponent;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditContract.PurchaseResult;
import ch.giantific.qwittig.presentation.purchases.details.di.PurchaseDetailsPresenterModule;
import ch.giantific.qwittig.presentation.purchases.details.di.PurchaseDetailsSubcomponent;
import ch.giantific.qwittig.presentation.purchases.details.viewmodels.PurchaseDetailsViewModel;
import ch.giantific.qwittig.presentation.purchases.details.viewmodels.items.PurchaseDetailsArticleItemViewModel;
import ch.giantific.qwittig.presentation.purchases.details.viewmodels.items.PurchaseDetailsIdentityItemViewModel;

/**
 * Hosts {@link PurchaseDetailsFragment} that displays the details of a purchase and
 * {@link PurchaseDetailsReceiptFragment} that displays the image of a receipt.
 * <p/>
 * Displays the store and date of the purchase in the {@link Toolbar}.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class PurchaseDetailsActivity extends BaseNavDrawerActivity<PurchaseDetailsSubcomponent>
        implements PurchaseDetailsContract.ViewListener {

    private static final String STATE_DETAILS_FRAGMENT = "STATE_DETAILS_FRAGMENT";
    @Inject
    PurchaseDetailsContract.Presenter presenter;
    @Inject
    PurchaseDetailsViewModel viewModel;
    private PurchaseDetailsFragment detailsFragment;
    private boolean showEditOptions;
    private boolean showExchangeRate;
    private ActivityPurchaseDetailsBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_purchase_details);
        binding.setViewModel(viewModel);

        // disable default actionBar title
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        replaceDrawerIndicatorWithUp();
        unCheckNavDrawerItems();
        supportPostponeEnterTransition();

        toolbar.setNavigationOnClickListener(v -> NavUtils.navigateUpFromSameTask(this));

        if (userLoggedIn) {
            setupTabs(savedInstanceState);
        }
    }

    @Override
    protected void injectDependencies(@NonNull NavDrawerComponent navComp,
                                      @Nullable Bundle savedInstanceState) {
        component = navComp.plus(new PurchaseDetailsPresenterModule(getPurchaseId(),
                        getIntent().getStringExtra(FcmMessagingService.PUSH_GROUP_ID)),
                new PersistentViewModelsModule(savedInstanceState));
        component.inject(this);
        presenter.attachView(this);
    }

    private String getPurchaseId() {
        final Intent intent = getIntent();
        // started from HomeActivity
        String purchaseId = intent.getStringExtra(Navigator.EXTRA_PURCHASE_ID);
        if (TextUtils.isEmpty(purchaseId)) {
            // started via push notification
            purchaseId = intent.getStringExtra(FcmMessagingService.PUSH_PURCHASE_ID);
        }

        return purchaseId;
    }

    @Override
    protected List<BasePresenter> getPresenters() {
        return Arrays.asList(new BasePresenter[]{navPresenter, presenter});
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(PurchaseDetailsViewModel.TAG, viewModel);
        getSupportFragmentManager().putFragment(outState, STATE_DETAILS_FRAGMENT, detailsFragment);
    }

    private void setupTabs(@Nullable Bundle savedInstanceState) {
        final TabsAdapter tabsAdapter = new TabsAdapter(getSupportFragmentManager());
        detailsFragment = savedInstanceState == null
                          ? new PurchaseDetailsFragment()
                          : (PurchaseDetailsFragment) getSupportFragmentManager().getFragment(savedInstanceState, STATE_DETAILS_FRAGMENT);
        tabsAdapter.addInitialFragment(detailsFragment, getString(R.string.tab_details_purchase));
        tabsAdapter.addInitialFragment(new PurchaseDetailsReceiptFragment(), getString(R.string.tab_details_receipt));
        binding.viewpager.setAdapter(tabsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_purchase_details, menu);

        if (showEditOptions) {
            menu.findItem(R.id.action_purchase_edit).setVisible(true);
            menu.findItem(R.id.action_purchase_delete).setVisible(true);
        }
        if (showExchangeRate) {
            menu.findItem(R.id.action_purchase_show_exchange_rate).setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_purchase_edit:
                presenter.onEditPurchaseClick();
                return true;
            case R.id.action_purchase_delete:
                presenter.onDeletePurchaseClick();
                return true;
            case R.id.action_purchase_show_exchange_rate:
                presenter.onShowExchangeRateClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Navigator.RC_PURCHASE_MODIFY) {
            switch (resultCode) {
                case PurchaseResult.PURCHASE_SAVED:
                    showMessage(R.string.toast_changes_saved);
                    break;
                case PurchaseResult.PURCHASE_DISCARDED:
                    showMessage(R.string.toast_changes_discarded);
                    break;
            }
        }
    }

    @Override
    public void setupScreenAfterLogin() {
        super.setupScreenAfterLogin();

        setupTabs(null);
    }

    @Override
    public void startEnterTransition() {
        ActivityCompat.startPostponedEnterTransition(this);
    }

    @Override
    public void toggleMenuOptions(boolean showEditOptions, boolean showExchangeRateOption) {
        this.showEditOptions = showEditOptions;
        showExchangeRate = showExchangeRateOption;
        invalidateOptionsMenu();
    }

    @Override
    public void addArticle(@NonNull PurchaseDetailsArticleItemViewModel item) {
        detailsFragment.getArticlesRecyclerAdapter().addItem(item);
    }

    @Override
    public void clearArticles() {
        detailsFragment.getArticlesRecyclerAdapter().clearItems();
    }

    @Override
    public boolean isArticlesEmpty() {
        return detailsFragment.getArticlesRecyclerAdapter().getItemCount() == 0;
    }

    @Override
    public void notifyArticlesChanged() {
        detailsFragment.getArticlesRecyclerAdapter().notifyDataSetChanged();
    }

    @Override
    public void addIdentities(@NonNull List<PurchaseDetailsIdentityItemViewModel> items) {
        detailsFragment.getIdentitiesRecyclerAdapter().addItems(items);
    }

    @Override
    public void clearIdentities() {
        detailsFragment.getIdentitiesRecyclerAdapter().clearItems();
    }

    @Override
    public void notifyIdentitiesChanged() {
        detailsFragment.getIdentitiesRecyclerAdapter().notifyDataSetChanged();
    }
}
