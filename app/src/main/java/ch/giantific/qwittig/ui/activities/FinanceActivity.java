/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.apache.commons.math3.fraction.BigFraction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.ItemUserPicker;
import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.data.repositories.ParseUserRepository;
import ch.giantific.qwittig.data.helpers.query.CompensationQueryHelper;
import ch.giantific.qwittig.data.helpers.reminder.CompensationRemindHelper;
import ch.giantific.qwittig.data.helpers.save.CompensationSaveHelper;
import ch.giantific.qwittig.data.helpers.query.MoreQueryHelper;
import ch.giantific.qwittig.data.helpers.group.SettlementHelper;
import ch.giantific.qwittig.data.helpers.query.UserQueryHelper;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.ui.adapters.TabsAdapter;
import ch.giantific.qwittig.ui.fragments.FinanceCompensationsBaseFragment;
import ch.giantific.qwittig.ui.fragments.FinanceCompensationsPaidFragment;
import ch.giantific.qwittig.ui.fragments.FinanceCompensationsUnpaidFragment;
import ch.giantific.qwittig.ui.fragments.FinanceUserBalancesFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.CompensationChangeAmountDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.CompensationSingleDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.GroupCreateDialogFragment;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Handles tasks related to financial state of the users.
 * <p/>
 * Hosts a view pager with different fragments dealing with balances, unpaid and paid compensations.
 * Only loads the fragments if the user is logged in. Also allows the user to make single payments.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class FinanceActivity extends BaseNavDrawerActivity implements
        FinanceCompensationsBaseFragment.FragmentInteractionListener,
        UserRepository.GetUsersLocalListener,
        CompensationSingleDialogFragment.DialogInteractionListener,
        GroupCreateDialogFragment.DialogInteractionListener,
        CompensationChangeAmountDialogFragment.DialogInteractionListener,
        UserQueryHelper.HelperInteractionListener,
        CompensationQueryHelper.HelperInteractionListener,
        MoreQueryHelper.HelperInteractionListener,
        SettlementHelper.HelperInteractionListener,
        CompensationRemindHelper.HelperInteractionListener,
        CompensationSaveHelper.HelperInteractionListener {

    public static final String INTENT_AUTO_START_NEW = "INTENT_AUTO_START_NEW";
    private static final String STATE_USER_BALANCES_FRAGMENT = "STATE_USER_BALANCES_FRAGMENT";
    private static final String STATE_COMPENSATIONS_UNPAID_FRAGMENT = "STATE_COMPENSATIONS_UNPAID_FRAGMENT";
    private static final String STATE_COMPENSATIONS_PAID_FRAGMENT = "STATE_COMPENSATIONS_PAID_FRAGMENT";
    private static final String LOG_TAG = FinanceActivity.class.getSimpleName();
    private static final String RECIPIENT_PICKER_DIALOG = "RECIPIENT_PICKER_DIALOG";
    private static final String CREATE_GROUP_DIALOG = "CREATE_GROUP_DIALOG";
    private TabLayout mTabLayout;
    private TextView mTextViewBalance;
    private FinanceUserBalancesFragment mUserBalancesFragment;
    private FinanceCompensationsUnpaidFragment mCompensationsUnpaidFragment;
    private FinanceCompensationsPaidFragment mCompensationsPaidFragment;
    private String mCurrentGroupCurrency;
    private List<ParseUser> mSinglePaymentUsers;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance);

        // check item in NavDrawer
        checkNavDrawerItem(R.id.nav_finance);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        mTextViewBalance = (TextView) findViewById(R.id.tv_balance);

        if (mUserIsLoggedIn) {
            if (savedInstanceState == null) {
                addViewPagerFragments();
            } else {
                mUserBalancesFragment = (FinanceUserBalancesFragment) getFragmentManager()
                        .getFragment(savedInstanceState, STATE_USER_BALANCES_FRAGMENT);
                mCompensationsUnpaidFragment = (FinanceCompensationsUnpaidFragment) getFragmentManager()
                        .getFragment(savedInstanceState, STATE_COMPENSATIONS_UNPAID_FRAGMENT);
                mCompensationsPaidFragment = (FinanceCompensationsPaidFragment) getFragmentManager()
                        .getFragment(savedInstanceState, STATE_COMPENSATIONS_PAID_FRAGMENT);
                setupTabs();
            }

            fetchCurrentUserGroups();
        }
    }

    private void addViewPagerFragments() {
        Intent intent = getIntent();
        boolean autoStartNew = intent.getBooleanExtra(INTENT_AUTO_START_NEW, false);

        mUserBalancesFragment = new FinanceUserBalancesFragment();
        mCompensationsUnpaidFragment = FinanceCompensationsUnpaidFragment.newInstance(autoStartNew);
        mCompensationsPaidFragment = new FinanceCompensationsPaidFragment();

        setupTabs();
    }

    private void setupTabs() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabsAdapter tabsAdapter = new TabsAdapter(getFragmentManager());
        tabsAdapter.addFragment(mUserBalancesFragment, getString(R.string.tab_users));
        tabsAdapter.addFragment(mCompensationsUnpaidFragment, getString(R.string.tab_compensations_new));
        tabsAdapter.addFragment(mCompensationsPaidFragment, getString(R.string.tab_compensations_history));
        viewPager.setAdapter(tabsAdapter);
        viewPager.setOffscreenPageLimit(2); // TODO: do we really want this?

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save fragments in saveInstanceBundle if user is logged in
        if (mUserIsLoggedIn) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.putFragment(outState, STATE_USER_BALANCES_FRAGMENT, mUserBalancesFragment);
            fragmentManager.putFragment(outState, STATE_COMPENSATIONS_UNPAID_FRAGMENT,
                    mCompensationsUnpaidFragment);
            fragmentManager.putFragment(outState, STATE_COMPENSATIONS_PAID_FRAGMENT,
                    mCompensationsPaidFragment);
        }
    }

    @Override
    public void onGroupsFetched() {
        super.onGroupsFetched();

        mCurrentGroupCurrency = ParseUtils.getGroupCurrency();
        setToolbarHeader();
    }

    private void setToolbarHeader() {
        BigFraction balance = BigFraction.ZERO;
        if (mCurrentUser != null) {
            balance = mCurrentUser.getBalance(mCurrentGroup);
        }
        mTextViewBalance.setText(MoneyUtils.formatMoney(balance, ParseUtils.getGroupCurrency()));
        setColorTheme(balance);
    }

    private void setColorTheme(BigFraction balance) {
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
        mTabLayout.setBackgroundColor(color);
        setStatusBarBackgroundColor(colorDark);
    }

    @Override
    void afterLoginSetup() {
        addViewPagerFragments();

        super.afterLoginSetup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_compensations, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_compensation_add_manual:
                addSinglePayment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addSinglePayment() {
        if (userIsInGroup()) {
            UserRepository repo = new ParseUserRepository();
            repo.getUsersLocalAsync(mCurrentGroup, this);
        } else {
            showCreateGroupDialog();
        }
    }

    private boolean userIsInGroup() {
        return mCurrentGroup != null;
    }

    private void showCreateGroupDialog() {
        GroupCreateDialogFragment groupCreateDialogFragment =
                GroupCreateDialogFragment.newInstance(R.string.dialog_group_create_finance);
        groupCreateDialogFragment.show(getFragmentManager(), CREATE_GROUP_DIALOG);
    }

    /**
     * Called from dialog that is shown when user tries to add new purchase and is not yet part of
     * any group.
     */
    @Override
    public void onCreateGroupSelected() {
        Intent intent = new Intent(this, SettingsGroupNewActivity.class);
        startActivity(intent);
    }

    @Override
    public void onUsersLocalLoaded(@NonNull List<ParseUser> users) {
        mSinglePaymentUsers = users;
        ArrayList<ItemUserPicker> recipients;

        if (users.size() > 1) { // size = 1 would mean only the current user is in the group
            recipients = new ArrayList<>(users.size() - 1); // -1 as currentUser will not be included
            for (ParseUser parseUser : users) {
                User user = (User) parseUser;
                if (!user.getObjectId().equals(mCurrentUser.getObjectId())) {
                    recipients.add(new ItemUserPicker(user.getObjectId(),
                            user.getNickname(), user.getAvatar()));
                }
            }

            Collections.sort(recipients);
            showAddSingleCompensationDialog(recipients);
        } else {
            MessageUtils.showBasicSnackbar(mToolbar, getString(R.string.toast_only_user_in_group));
        }

    }

    private void showAddSingleCompensationDialog(ArrayList<ItemUserPicker> users) {
        CompensationSingleDialogFragment dialog =
                CompensationSingleDialogFragment.newInstance(users);
        dialog.show(getFragmentManager(), RECIPIENT_PICKER_DIALOG);
    }

    @Override
    public void onSinglePaymentValuesSet(@NonNull ItemUserPicker recipient, @NonNull String amountString) {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            showAccountCreateDialog();
            return;
        }

        User recipientSelected = null;
        for (ParseUser parseUser : mSinglePaymentUsers) {
            User user = (User) parseUser;
            if (recipient.getObjectId().equals(user.getObjectId())) {
                recipientSelected = user;
            }
        }

        if (recipientSelected != null) {
            int maxFractionDigits = MoneyUtils.getMaximumFractionDigits(mCurrentGroupCurrency);
            BigDecimal amount = MoneyUtils.parsePrice(amountString).setScale(maxFractionDigits,
                    BigDecimal.ROUND_HALF_UP);
            BigFraction amountSelected = new BigFraction(amount.doubleValue());
            saveSingleCompensation(recipientSelected, amountSelected,
                    recipientSelected.getNickname());
        }
    }

    private void saveSingleCompensation(@NonNull User recipientSelected,
                                        @NonNull BigFraction amountSelected,
                                        @NonNull final String recipientNickname) {
        final Compensation compensation = new Compensation(mCurrentGroup, mCurrentUser, recipientSelected,
                amountSelected, false);
        compensation.pinInBackground(Compensation.PIN_LABEL_UNPAID, new SaveCallback() {
            @Override
            public void done(@Nullable ParseException e) {
                if (e == null) {
                    mCompensationsUnpaidFragment.onCompensationsUpdated();
                    compensation.saveEventually();
                    MessageUtils.showBasicSnackbar(mToolbar,
                            getString(R.string.toast_payment_saved, recipientNickname));
                }
            }
        });
    }

    @Override
    public void onChangedAmountSet(@NonNull BigFraction amount) {
        mCompensationsUnpaidFragment.onChangedAmountSet(amount);
    }

    @Override
    public void onUserUpdateFailed(int errorCode) {
        mUserBalancesFragment.onUserUpdateFailed(errorCode);
    }

    @Override
    public void onUsersUpdated() {
        super.onUsersUpdated();

        mUserBalancesFragment.onUsersUpdated();
        setToolbarHeader();
    }

    @Override
    public void onCompensationUpdateFailed(int errorCode, boolean isPaid) {
        if (isPaid) {
            mCompensationsPaidFragment.onCompensationUpdateFailed(errorCode);
        } else {
            mCompensationsUnpaidFragment.onCompensationUpdateFailed(errorCode);
        }
    }

    @Override
    public void onAllCompensationsPaidUpdated() {
        mCompensationsPaidFragment.onAllCompensationsUpdated();
    }

    @Override
    public void onCompensationsUpdated(boolean isPaid) {
        super.onCompensationsUpdated(isPaid);

        if (isPaid) {
            mCompensationsPaidFragment.onCompensationsUpdated();
        } else {
            mCompensationsUnpaidFragment.onCompensationsUpdated();
        }
    }

    @Override
    public void onNewSettlementCreated() {
        mCompensationsUnpaidFragment.onNewSettlementCreated();
    }

    @Override
    public void onNewSettlementCreationFailed(int errorCode) {
        mCompensationsUnpaidFragment.onNewSettlementCreationFailed(errorCode);
    }

    @Override
    public void onCompensationSaved(@NonNull ParseObject compensation) {
        mCompensationsUnpaidFragment.onCompensationSaved(compensation);
    }

    @Override
    public void onCompensationSaveFailed(@NonNull ParseObject compensation, int errorCode) {
        mCompensationsUnpaidFragment.onCompensationSaveFailed(compensation, errorCode);
    }

    @Override
    public void onUserReminded(int remindType, @NonNull String compensationId) {
        mCompensationsUnpaidFragment.onUserReminded(remindType, compensationId);
    }

    @Override
    public void onUserRemindFailed(int remindType, @NonNull String compensationId, int errorCode) {
        mCompensationsUnpaidFragment.onUserRemindFailed(compensationId, errorCode);
    }

    @Override
    public void onMoreObjectsLoaded(@NonNull List<ParseObject> objects) {
        mCompensationsPaidFragment.onMoreObjectsLoaded(objects);
    }

    @Override
    public void onMoreObjectsLoadFailed(int errorCode) {
        mCompensationsPaidFragment.onMoreObjectsLoadFailed(errorCode);
    }

    @Override
    protected void onNewGroupSet() {
        updateFragmentAdapters();
        setToolbarHeader();
    }

    private void updateFragmentAdapters() {
        mUserBalancesFragment.updateAdapter();
        mCompensationsPaidFragment.updateAdapter();
        mCompensationsUnpaidFragment.updateAdapter();
    }

    @Override
    int getSelfNavDrawerItem() {
        return R.id.nav_finance;
    }
}
