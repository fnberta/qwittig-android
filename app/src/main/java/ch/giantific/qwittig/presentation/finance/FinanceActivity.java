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
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.LocalBroadcast;
import ch.giantific.qwittig.data.bus.LocalBroadcast.DataType;
import ch.giantific.qwittig.data.push.PushBroadcastReceiver;
import ch.giantific.qwittig.databinding.ActivityFinanceBinding;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.presentation.common.adapters.TabsAdapter;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.finance.di.FinanceCompsPaidViewModelModule;
import ch.giantific.qwittig.presentation.finance.di.FinanceCompsUnpaidViewModelModule;
import ch.giantific.qwittig.presentation.finance.di.FinanceHeaderViewModelModule;
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
public class FinanceActivity extends BaseNavDrawerActivity<FinanceSubcomponent> implements
        BalanceHeaderViewModel.ViewListener,
        CompConfirmAmountDialogFragment.DialogInteractionListener,
        CompsQueryMoreWorkerListener,
        CompRemindWorkerListener {

    @Inject
    BalanceHeaderViewModel mHeaderViewModel;
    @Inject
    CompsPaidViewModel mCompsPaidViewModel;
    @Inject
    CompsUnpaidViewModel mCompsUnpaidViewModel;
    private ActivityFinanceBinding mBinding;

    @Override
    protected void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);

        switch (dataType) {
            case DataType.IDENTITIES_UPDATED: {
                final boolean successful = intent.getBooleanExtra(LocalBroadcast.INTENT_EXTRA_SUCCESSFUL, false);
                mHeaderViewModel.onDataUpdated(successful);
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
        mBinding.setViewModel(mHeaderViewModel);

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
        mComponent = navComp.plus(new FinanceHeaderViewModelModule(savedInstanceState),
                new FinanceCompsUnpaidViewModelModule(savedInstanceState),
                new FinanceCompsPaidViewModelModule(savedInstanceState));
        mComponent.inject(this);
        mHeaderViewModel.attachView(this);
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{mHeaderViewModel, mCompsUnpaidViewModel,
                mCompsPaidViewModel});
    }

    private void setupTabs() {
        @FragmentTabs
        final int tabToSelect = getIntent().getIntExtra(
                PushBroadcastReceiver.INTENT_EXTRA_FINANCE_FRAGMENT, FragmentTabs.NONE);

        final TabsAdapter tabsAdapter = new TabsAdapter(getSupportFragmentManager());
        tabsAdapter.addInitialFragment(new CompsUnpaidFragment(), getString(R.string.tab_compensations_new));
        tabsAdapter.addInitialFragment(new CompsPaidFragment(), getString(R.string.tab_compensations_archive));
        mBinding.viewpager.setAdapter(tabsAdapter);
        if (tabToSelect != FragmentTabs.NONE) {
            mBinding.viewpager.setCurrentItem(tabToSelect);
        }

        mBinding.tabs.setupWithViewPager(mBinding.viewpager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mHeaderViewModel.onViewVisible();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mHeaderViewModel.onViewGone();
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_finance;
    }

    @Override
    public void setupScreenAfterLogin() {
        super.setupScreenAfterLogin();

        setupTabs();
    }

    @Override
    public void setColorTheme(@NonNull BigFraction balance) {
        int color;
        int colorDark;
        int style;
        if (Utils.isPositive(balance)) {
            color = ContextCompat.getColor(this, R.color.green);
            colorDark = ContextCompat.getColor(this, R.color.green_dark);
            style = R.style.AppTheme_DrawStatusBar_Green;
        } else {
            color = ContextCompat.getColor(this, R.color.red);
            colorDark = ContextCompat.getColor(this, R.color.red_dark);
            style = R.style.AppTheme_DrawStatusBar_Red;
        }
        setTheme(style);
        mToolbar.setBackgroundColor(color);
        mBinding.tabs.setBackgroundColor(color);
        setStatusBarBackgroundColor(colorDark);
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

    @Override
    public void onWorkerError(@NonNull String workerTag) {
        mCompsPaidViewModel.onWorkerError(workerTag);
        mCompsUnpaidViewModel.onWorkerError(workerTag);
    }

    @IntDef({FragmentTabs.NONE, FragmentTabs.COMPS_UNPAID, FragmentTabs.COMPS_PAID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FragmentTabs {
        int NONE = -1;
        int COMPS_UNPAID = 0;
        int COMPS_PAID = 1;
    }
}
