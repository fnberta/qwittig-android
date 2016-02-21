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
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;

import org.apache.commons.math3.fraction.BigFraction;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.LocalBroadcast.DataType;
import ch.giantific.qwittig.LocalBroadcastImpl;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.receivers.PushBroadcastReceiver;
import ch.giantific.qwittig.databinding.ActivityFinanceBinding;
import ch.giantific.qwittig.di.components.NavDrawerComponent;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.adapters.TabsAdapter;
import ch.giantific.qwittig.presentation.finance.identities.IdentitiesFragment;
import ch.giantific.qwittig.presentation.finance.identities.IdentitiesUpdateWorkerListener;
import ch.giantific.qwittig.presentation.finance.identities.IdentitiesViewModel;
import ch.giantific.qwittig.presentation.navdrawer.BaseNavDrawerActivity;
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
public class FinanceActivity extends BaseNavDrawerActivity<CompsUnpaidViewModel> implements
        IdentitiesFragment.ActivityListener,
        CompsUnpaidFragment.ActivityListener,
        CompsPaidFragment.ActivityListener,
        CompConfirmAmountDialogFragment.DialogInteractionListener,
        CompsUpdateWorkerListener,
        CompsQueryMoreWorkerListener,
        CompRemindWorkerListener,
        IdentitiesUpdateWorkerListener {

    private ActivityFinanceBinding mBinding;
    private IdentitiesViewModel mIdentitiesViewModel;
    private CompsPaidViewModel mCompsPaidViewModel;

    @Override
    protected void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);
        switch (dataType) {
            case DataType.IDENTITIES_UPDATED:
                mIdentitiesViewModel.loadData();
                break;
            case DataType.COMPENSATIONS_UPDATED:
                final boolean paid = intent.getBooleanExtra(LocalBroadcastImpl.INTENT_EXTRA_COMPENSATION_PAID, false);
                if (paid) {
                    mCompsPaidViewModel.loadData();
                } else {
                    mViewModel.loadData();
                }
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_finance);

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
    protected void injectNavDrawerDependencies(@NonNull NavDrawerComponent navComp) {
        navComp.inject(this);
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
    public void setCompsUnpaidViewModel(@NonNull CompsUnpaidViewModel viewModel) {
        mViewModel = viewModel;
        mBinding.setViewModel(viewModel);
    }

    @Override
    public void setCompsPaidViewModel(@NonNull CompsPaidViewModel viewModel) {
        mCompsPaidViewModel = viewModel;
    }

    @Override
    public void setIdentitiesViewModel(@NonNull IdentitiesViewModel viewModel) {
        mIdentitiesViewModel = viewModel;
    }

    @Override
    protected void onLoginSuccessful() {
        super.onLoginSuccessful();

        // TODO: fix setLoading(true) because online query is still happening
        setupTabs();
    }

    @Override
    public void onIdentitySelected() {
        super.onIdentitySelected();

        if (mIdentitiesViewModel != null) {
            mIdentitiesViewModel.onIdentitySelected();
        }
        mCompsPaidViewModel.onIdentitySelected();
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
        mViewModel.onAmountConfirmed(amount);
    }

    @Override
    public void setIdentitiesUpdateStream(@NonNull Observable<Identity> observable,
                                          @NonNull String workerTag) {
        mIdentitiesViewModel.setIdentitiesUpdateStream(observable, workerTag);
    }

    @Override
    public void setCompensationsUpdateStream(@NonNull Observable<Compensation> observable,
                                             boolean paid, @NonNull String workerTag) {
        if (paid) {
            mCompsPaidViewModel.setCompensationsUpdateStream(observable, true, workerTag);
        } else {
            mViewModel.setCompensationsUpdateStream(observable, false, workerTag);
        }
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
        mViewModel.setCompensationRemindStream(single, compensationId, workerTag);
    }

    @IntDef({FragmentTabs.NONE, FragmentTabs.COMPS_UNPAID, FragmentTabs.COMPS_PAID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FragmentTabs {
        int NONE = -1;
        int COMPS_UNPAID = 0;
        int COMPS_PAID = 1;
    }
}
