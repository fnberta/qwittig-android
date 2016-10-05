/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;

import org.apache.commons.math3.fraction.BigFraction;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.push.FcmMessagingService;
import ch.giantific.qwittig.databinding.ActivityFinanceBinding;
import ch.giantific.qwittig.presentation.common.listadapters.TabsAdapter;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.finance.di.FinanceCompsPaidPresenterModule;
import ch.giantific.qwittig.presentation.finance.di.FinanceCompsUnpaidPresenterModule;
import ch.giantific.qwittig.presentation.finance.di.FinanceHeaderPresenterModule;
import ch.giantific.qwittig.presentation.finance.di.FinanceSubcomponent;
import ch.giantific.qwittig.presentation.finance.paid.CompsPaidContract;
import ch.giantific.qwittig.presentation.finance.paid.CompsPaidFragment;
import ch.giantific.qwittig.presentation.finance.unpaid.CompConfirmAmountDialogFragment;
import ch.giantific.qwittig.presentation.finance.unpaid.CompsUnpaidContract;
import ch.giantific.qwittig.presentation.finance.unpaid.CompsUnpaidFragment;
import ch.giantific.qwittig.presentation.navdrawer.BaseNavDrawerActivity;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerComponent;
import ch.giantific.qwittig.utils.Utils;

/**
 * Handles tasks related to financial state of the users.
 * <p/>
 * Hosts a view pager with different fragments dealing with balances, unpaid and paid compensations.
 * Only loads the fragments if the user is logged in. Also allows the user to make single payments.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class FinanceActivity extends BaseNavDrawerActivity<FinanceSubcomponent> implements
        FinanceHeaderContract.ViewListener,
        CompConfirmAmountDialogFragment.DialogInteractionListener {

    @Inject
    FinanceHeaderContract.Presenter headerPresenter;
    @Inject
    CompsPaidContract.Presenter paidPresenter;
    @Inject
    CompsUnpaidContract.Presenter unpaidPresenter;
    private ActivityFinanceBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_finance);
        binding.setViewModel(headerPresenter.getViewModel());

        postponeEnterTransition();
        checkNavDrawerItem(R.id.nav_finance);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        if (userLoggedIn) {
            setupTabs();
        }
    }

    @Override
    protected void injectDependencies(@NonNull NavDrawerComponent navComp,
                                      @Nullable Bundle savedInstanceState) {
        final String groupId = getIntent().getStringExtra(FcmMessagingService.PUSH_GROUP_ID);
        component = navComp.plus(new FinanceHeaderPresenterModule(savedInstanceState),
                new FinanceCompsUnpaidPresenterModule(savedInstanceState),
                new FinanceCompsPaidPresenterModule(savedInstanceState, groupId));
        component.inject(this);
        headerPresenter.attachView(this);
    }

    @Override
    protected List<BasePresenter> getPresenters() {
        return Arrays.asList(new BasePresenter[]{navPresenter, headerPresenter, unpaidPresenter,
                paidPresenter});
    }

    private void setupTabs() {
        final TabsAdapter tabsAdapter = new TabsAdapter(getSupportFragmentManager());
        tabsAdapter.addInitialFragment(new CompsUnpaidFragment(), getString(R.string.tab_compensations_new));
        tabsAdapter.addInitialFragment(new CompsPaidFragment(), getString(R.string.tab_compensations_archive));
        binding.viewpager.setAdapter(tabsAdapter);

        @FragmentTabs
        final String tabToSelect = getIntent().getStringExtra(FcmMessagingService.PUSH_FINANCE_TAB);
        if (!TextUtils.isEmpty(tabToSelect)) {
            final int tab = Objects.equals(tabToSelect, FragmentTabs.COMPS_UNPAID) ? 0 : 1;
            binding.viewpager.setCurrentItem(tab);
        }

        binding.tabs.setupWithViewPager(binding.viewpager);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_finance;
    }

    @Override
    protected void setupScreenAfterLogin() {
        super.setupScreenAfterLogin();

        setupTabs();
    }

    @Override
    public void setColorTheme(@NonNull BigFraction balance) {
        final int color;
        final int colorDark;
        final int style;
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
        toolbar.setBackgroundColor(color);
        binding.tabs.setBackgroundColor(color);
        setStatusBarBackgroundColor(colorDark);
    }

    @Override
    public void startEnterTransition() {
        ActivityCompat.startPostponedEnterTransition(this);
    }

    @Override
    public void onAmountConfirmed(double amount) {
        unpaidPresenter.onAmountConfirmed(amount);
    }

    @StringDef({FragmentTabs.COMPS_UNPAID, FragmentTabs.COMPS_PAID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FragmentTabs {
        String COMPS_UNPAID = "unpaid";
        String COMPS_PAID = "paid";
    }
}
