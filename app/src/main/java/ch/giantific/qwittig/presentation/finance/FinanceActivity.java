/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;

import org.apache.commons.math3.fraction.BigFraction;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.LocalBroadcast;
import ch.giantific.qwittig.data.bus.LocalBroadcast.DataType;
import ch.giantific.qwittig.data.push.PushBroadcastReceiver;
import ch.giantific.qwittig.databinding.ActivityFinanceBinding;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.presentation.common.adapters.TabsAdapter;
import ch.giantific.qwittig.presentation.finance.di.BalanceHeaderViewModelModule;
import ch.giantific.qwittig.presentation.finance.di.FinanceSubcomponent;
import ch.giantific.qwittig.presentation.finance.paid.CompsPaidFragment;
import ch.giantific.qwittig.presentation.finance.paid.CompsPaidViewModel;
import ch.giantific.qwittig.presentation.finance.paid.CompsQueryMoreWorkerListener;
import ch.giantific.qwittig.presentation.finance.unpaid.CompConfirmAmountDialogFragment;
import ch.giantific.qwittig.presentation.finance.unpaid.CompRemindWorkerListener;
import ch.giantific.qwittig.presentation.finance.unpaid.CompsUnpaidFragment;
import ch.giantific.qwittig.presentation.finance.unpaid.CompsUnpaidViewModel;
import ch.giantific.qwittig.presentation.navdrawer.BaseNavDrawerActivity;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerComponent;
import ch.giantific.qwittig.utils.Utils;
import rx.Observable;
import rx.Single;

/**
 * Handles tasks related to financial state of the users.
 * <p/>
 * Hosts a view pager with different fragments dealing with balances, unpaid and paid compensations.
 * Only loads the fragments if the user is logged in. Also allows the user to make single payments.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class FinanceActivity extends BaseNavDrawerActivity<BalanceHeaderViewModel> implements
        BalanceHeaderViewModel.ViewListener,
        CompsUnpaidFragment.ActivityListener,
        CompsPaidFragment.ActivityListener,
        CompConfirmAmountDialogFragment.DialogInteractionListener,
        CompsQueryMoreWorkerListener,
        CompRemindWorkerListener {

    private ActivityFinanceBinding mBinding;
    private CompsPaidViewModel mCompsPaidViewModel;
    private CompsUnpaidViewModel mCompsUnpaidViewModel;

    @Override
    protected void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);

        switch (dataType) {
            case DataType.IDENTITIES_UPDATED: {
                final boolean successful = intent.getBooleanExtra(LocalBroadcast.INTENT_EXTRA_SUCCESSFUL, false);
                mViewModel.onDataUpdated(successful);
                break;
            }
            case DataType.COMPENSATIONS_UPDATED: {
                final boolean successful = intent.getBooleanExtra(LocalBroadcast.INTENT_EXTRA_SUCCESSFUL, true);
                final boolean paid = intent.getBooleanExtra(LocalBroadcast.INTENT_EXTRA_COMPENSATION_PAID, false);
                if (paid) {
                    mCompsPaidViewModel.onDataUpdated(successful);
                } else {
                    mCompsUnpaidViewModel.onDataUpdated(successful);
                }
                break;
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_finance);
        mBinding.setViewModel(mViewModel);

        // check item in NavDrawer
        checkNavDrawerItem(R.id.nav_finance);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        if (mUserLoggedIn) {
            setupTabs();
        }
    }

    @Override
    protected void injectDependencies(@NonNull NavDrawerComponent navComp,
                                      @Nullable Bundle savedInstanceState) {
        final FinanceSubcomponent component =
                navComp.plus(new BalanceHeaderViewModelModule(savedInstanceState, this));
        component.inject(this);
        mViewModel = component.getBalanceHeaderViewModel();
    }

    private void setupTabs() {
        @FragmentTabs
        final int tabToSelect = getIntent().getIntExtra(
                PushBroadcastReceiver.INTENT_EXTRA_FINANCE_FRAGMENT, FragmentTabs.NONE);

        final TabsAdapter tabsAdapter = new TabsAdapter(getSupportFragmentManager());
        tabsAdapter.addFragment(new CompsUnpaidFragment(), getString(R.string.tab_compensations_new));
        tabsAdapter.addFragment(new CompsPaidFragment(), getString(R.string.tab_compensations_archive));
        mBinding.viewpager.setAdapter(tabsAdapter);
        if (tabToSelect != FragmentTabs.NONE) {
            mBinding.viewpager.setCurrentItem(tabToSelect);
        }

        mBinding.tabs.setupWithViewPager(mBinding.viewpager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mViewModel.onViewVisible();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mViewModel.onViewGone();
    }

    @Override
    public void setCompsUnpaidViewModel(@NonNull CompsUnpaidViewModel viewModel) {
        mCompsUnpaidViewModel = viewModel;
    }

    @Override
    public void setCompsPaidViewModel(@NonNull CompsPaidViewModel viewModel) {
        mCompsPaidViewModel = viewModel;
    }

    @Override
    protected void onLoginSuccessful() {
        super.onLoginSuccessful();

        setupTabs();
    }

    @Override
    public void onIdentitySelected() {
        super.onIdentitySelected();

        mCompsPaidViewModel.onIdentitySelected();
        mCompsUnpaidViewModel.onIdentitySelected();
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_finance;
    }

    @Override
    public void setColorTheme(@NonNull BigFraction balance) {
        int color;
        int colorDark;
        int style;
        if (Utils.isPositive(balance)) {
            color = ContextCompat.getColor(this, R.color.green_500);
            colorDark = ContextCompat.getColor(this, R.color.green_700);
            style = R.style.AppTheme_DrawStatusBar_Green;
        } else {
            color = ContextCompat.getColor(this, R.color.red_500);
            colorDark = ContextCompat.getColor(this, R.color.red_700);
            style = R.style.AppTheme_DrawStatusBar_Red;
        }
        setTheme(style);
        mToolbar.setBackgroundColor(color);
        mBinding.tabs.setBackgroundColor(color);
        setStatusBarBackgroundColor(colorDark);
    }

    @Override
    public void reloadCompsPaid() {
        mCompsPaidViewModel.loadData();
    }

    @Override
    public void onAmountConfirmed(double amount) {
        mCompsUnpaidViewModel.onAmountConfirmed(amount);
    }

    @Override
    public void setCompensationsQueryMoreStream(@NonNull Observable<Compensation> observable,
                                                @NonNull String workerTag) {
        mCompsPaidViewModel.setCompensationsQueryMoreStream(observable, workerTag);
    }

    @Override
    public void setCompensationRemindStream(@NonNull Single<String> single,
                                            @NonNull String compensationId,
                                            @NonNull String workerTag) {
        mCompsUnpaidViewModel.setCompensationRemindStream(single, compensationId, workerTag);
    }

    @IntDef({FragmentTabs.NONE, FragmentTabs.COMPS_UNPAID, FragmentTabs.COMPS_PAID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FragmentTabs {
        int NONE = -1;
        int COMPS_UNPAID = 0;
        int COMPS_PAID = 1;
    }
}
