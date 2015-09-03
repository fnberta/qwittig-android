package ch.giantific.qwittig.ui;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
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
import ch.giantific.qwittig.data.models.ItemUserPicker;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.helpers.CompensationQueryHelper;
import ch.giantific.qwittig.helpers.CompensationRemindHelper;
import ch.giantific.qwittig.helpers.CompensationSaveHelper;
import ch.giantific.qwittig.helpers.MoreQueryHelper;
import ch.giantific.qwittig.helpers.SettlementHelper;
import ch.giantific.qwittig.helpers.UserQueryHelper;
import ch.giantific.qwittig.ui.adapters.TabsAdapter;
import ch.giantific.qwittig.ui.dialogs.AccountCreateDialogFragment;
import ch.giantific.qwittig.ui.dialogs.CompensationAddManualDialogFragment;
import ch.giantific.qwittig.ui.dialogs.CompensationChangeAmountDialogFragment;
import ch.giantific.qwittig.ui.dialogs.GroupCreateDialogFragment;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

public class FinanceActivity extends BaseNavDrawerActivity implements
        FinanceCompensationsUnpaidFragment.FragmentInteractionListener,
        LocalQuery.UserLocalQueryListener,
        CompensationAddManualDialogFragment.DialogInteractionListener,
        GroupCreateDialogFragment.DialogInteractionListener,
        CompensationChangeAmountDialogFragment.FragmentInteractionListener,
        UserQueryHelper.HelperInteractionListener,
        CompensationQueryHelper.HelperInteractionListener,
        MoreQueryHelper.HelperInteractionListener,
        SettlementHelper.HelperInteractionListener,
        CompensationRemindHelper.HelperInteractionListener,
        CompensationSaveHelper.HelperInteractionListener {

    public static final String INTENT_AUTO_START_NEW = "intent_auto_start_new";
    private static final String USER_BALANCES_FRAGMENT = "user_balances_fragment";
    private static final String COMPENSATIONS_UNPAID_FRAGMENT = "compensations_unpaid_fragment";
    private static final String COMPENSATIONS_PAID_FRAGMENT = "compensations_paid_fragment";
    private static final String LOG_TAG = FinanceActivity.class.getSimpleName();
    private TabLayout mTabLayout;
    private TextView mTextViewBalance;
    private FinanceUserBalancesFragment mUserBalancesFragment;
    private FinanceCompensationsUnpaidFragment mCompensationsUnpaidFragment;
    private FinanceCompensationsPaidFragment mCompensationsPaidFragment;
    private String mCurrentGroupCurrency;
    private List<ParseUser> mSinglePaymentUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                        .getFragment(savedInstanceState, USER_BALANCES_FRAGMENT);
                mCompensationsUnpaidFragment = (FinanceCompensationsUnpaidFragment) getFragmentManager()
                        .getFragment(savedInstanceState, COMPENSATIONS_UNPAID_FRAGMENT);
                mCompensationsPaidFragment = (FinanceCompensationsPaidFragment) getFragmentManager()
                        .getFragment(savedInstanceState, COMPENSATIONS_PAID_FRAGMENT);
                setupTabs();
            }
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
            fragmentManager.putFragment(outState, USER_BALANCES_FRAGMENT, mUserBalancesFragment);
            fragmentManager.putFragment(outState, COMPENSATIONS_UNPAID_FRAGMENT,
                    mCompensationsUnpaidFragment);
            fragmentManager.putFragment(outState, COMPENSATIONS_PAID_FRAGMENT,
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_compensation_add_manual:
                if (userIsInGroup()) {
                    LocalQuery.queryUsers(this);
                } else {
                    showCreateGroupDialog();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean userIsInGroup() {
        return mCurrentGroup != null;
    }

    private void showCreateGroupDialog() {
        GroupCreateDialogFragment groupCreateDialogFragment = new GroupCreateDialogFragment();
        groupCreateDialogFragment.show(getFragmentManager(), "create_group");
    }

    /**
     * Called from dialog that is shown when user tries to add new purchase and is not yet part of
     * any group.
     */
    @Override
    public void createNewGroup() {
        Intent intent = new Intent(this, SettingsGroupNewActivity.class);
        startActivity(intent);
    }

    @Override
    public void onUsersLocalQueried(List<ParseUser> users) {
        mSinglePaymentUsers = users;
        ArrayList<ItemUserPicker> recipients;

        if (!users.isEmpty()) {
            recipients = new ArrayList<>(users.size() - 1); // -1 as currentUser will not be included
            for (ParseUser parseUser : users) {
                User user = (User) parseUser;
                if (!user.getObjectId().equals(mCurrentUser.getObjectId())) {
                    recipients.add(new ItemUserPicker(user.getObjectId(),
                            user.getNickname(), user.getAvatar()));
                }
            }

            Collections.sort(recipients);
        } else {
            recipients = new ArrayList<>();
        }

        showAddManualDialog(recipients);
    }

    private void showAddManualDialog(ArrayList<ItemUserPicker> users) {
        CompensationAddManualDialogFragment userPickerDialogFragment =
                CompensationAddManualDialogFragment.newInstance(users);
        userPickerDialogFragment.show(getFragmentManager(), "recipient_picker");
    }

    @Override
    public void onManualPaymentValuesSet(ItemUserPicker recipient, String amountString) {
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

    private void saveSingleCompensation(User recipientSelected, BigFraction amountSelected,
                                        final String recipientNickname) {
        final Compensation compensation = new Compensation(mCurrentGroup, mCurrentUser, recipientSelected,
                amountSelected, false);
        compensation.pinInBackground(Compensation.PIN_LABEL_UNPAID, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                mCompensationsUnpaidFragment.onCompensationsPinned();
                compensation.saveEventually();
                MessageUtils.showBasicSnackbar(mToolbar,
                        getString(R.string.toast_payment_saved, recipientNickname));
            }
        });
    }

    @Override
    public void showAccountCreateDialog() {
        AccountCreateDialogFragment accountCreateDialogFragment =
                new AccountCreateDialogFragment();
        accountCreateDialogFragment.show(getFragmentManager(), "account_create");
    }

    /**
     * Callback from changeAmount dialog
     * @param amount
     */
    @Override
    public void changeAmount(BigFraction amount) {
        mCompensationsUnpaidFragment.changeAmount(amount);
    }

    @Override
    public void onUsersPinFailed(ParseException e) {
        mUserBalancesFragment.onUsersPinFailed(e);
    }

    @Override
    public void onUsersPinned() {
        super.onUsersPinned();

        mUserBalancesFragment.onUsersPinned();
        setToolbarHeader();
    }

    @Override
    public void onAllUserQueriesFinished() {
        mUserBalancesFragment.onAllUserQueriesFinished();
    }

    @Override
    public void onCompensationsPinFailed(ParseException e, boolean isPaid) {
        if (isPaid) {
            mCompensationsPaidFragment.onCompensationsPinFailed(e);
        } else {
            mCompensationsUnpaidFragment.onCompensationsPinFailed(e);
        }
    }

    @Override
    public void onAllQueriesFinished(boolean isPaid) {
        if (isPaid) {
            mCompensationsPaidFragment.onAllCompensationQueriesFinished();
        } else {
            mCompensationsUnpaidFragment.onAllCompensationQueriesFinished();
        }
    }

    @Override
    public void onCompensationsPinned(boolean isPaid) {
        super.onCompensationsPinned(isPaid);

        if (isPaid) {
            mCompensationsPaidFragment.onCompensationsPinned();
        } else {
            mCompensationsUnpaidFragment.onCompensationsPinned();
        }
    }

    @Override
    public void onNewSettlementCreated(Object result) {
        mCompensationsUnpaidFragment.onNewSettlementCreated(result);
    }

    @Override
    public void onNewSettlementCreationFailed(ParseException e) {
        mCompensationsUnpaidFragment.onNewSettlementCreationFailed(e);
    }

    @Override
    public void onCompensationSaved(ParseObject compensation) {
        mCompensationsUnpaidFragment.onCompensationSaved(compensation);
    }

    @Override
    public void onCompensationSaveFailed(ParseObject compensation, ParseException e) {
        mCompensationsUnpaidFragment.onCompensationSaveFailed(compensation, e);
    }

    @Override
    public void onUserReminded(int remindType, String compensationId) {
        mCompensationsUnpaidFragment.onUserReminded(remindType, compensationId);
    }

    @Override
    public void onFailedToRemindUser(int remindType, ParseException e) {
        mCompensationsUnpaidFragment.onFailedToRemindUser(remindType, e);
    }

    @Override
    public void onMoreObjectsPinned(List<ParseObject> objects) {
        mCompensationsPaidFragment.onMoreObjectsPinned(objects);
    }

    @Override
    public void onMoreObjectsPinFailed(ParseException e) {
        mCompensationsPaidFragment.onMoreObjectsPinFailed(e);
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
