package ch.giantific.qwittig.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.apache.commons.math3.fraction.BigFraction;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.ItemUserPicker;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.OnlineQuery;
import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.adapter.TabsAdapter;
import ch.giantific.qwittig.ui.dialogs.AccountCreateDialogFragment;
import ch.giantific.qwittig.ui.dialogs.CompensationAddManualDialogFragment;
import ch.giantific.qwittig.ui.dialogs.CompensationChangeAmountDialogFragment;
import ch.giantific.qwittig.ui.dialogs.GroupCreateDialogFragment;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.ParseUtils;

public class CompensationsActivity extends BaseNavDrawerActivity implements
        CompensationsUnpaidFragment.FragmentInteractionListener,
        OnlineQuery.CompensationPinListener,
        LocalQuery.UserLocalQueryListener,
        CompensationAddManualDialogFragment.DialogInteractionListener,
        GroupCreateDialogFragment.DialogInteractionListener,
        CompensationChangeAmountDialogFragment.FragmentInteractionListener {

    public static final String INTENT_AUTO_START_NEW = "intent_auto_start_new";
    private static final String ACCOUNT_BALANCE_NEW_FRAGMENT = "account_balance_new_fragment";
    private static final String ACCOUNT_BALANCE_HISTORY_FRAGMENT = "account_balance_history_fragment";
    @IntDef({FRAGMENT_ADAPTER_BOTH, FRAGMENT_ADAPTER_UNPAID, FRAGMENT_ADAPTER_PAID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AdapterType {}
    private static final int FRAGMENT_ADAPTER_BOTH = 0;
    private static final int FRAGMENT_ADAPTER_UNPAID = 1;
    private static final int FRAGMENT_ADAPTER_PAID = 2;

    private static final String LOG_TAG = CompensationsActivity.class.getSimpleName();

    private CompensationsUnpaidFragment mSettlementUnpaidFragment;
    private CompensationsPaidFragment mSettlementPaidFragment;
    private String mCurrentGroupCurrency;
    private List<ParseUser> mSinglePaymentUsers;
    private ArrayList<ItemUserPicker> mSinglePaymentRecipients = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compensations);

        // check item in NavDrawer
        checkNavDrawerItem(R.id.nav_settlement);

        if (mUserIsLoggedIn) {
            if (savedInstanceState == null) {
                addViewPagerFragments();
            } else {
                mSettlementUnpaidFragment = (CompensationsUnpaidFragment) getFragmentManager()
                        .getFragment(savedInstanceState, ACCOUNT_BALANCE_NEW_FRAGMENT);
                mSettlementPaidFragment = (CompensationsPaidFragment) getFragmentManager()
                        .getFragment(savedInstanceState, ACCOUNT_BALANCE_HISTORY_FRAGMENT);
                setupTabs();
            }
        }
    }

    private void addViewPagerFragments() {
        Intent intent = getIntent();
        boolean autoStartNew = intent.getBooleanExtra(INTENT_AUTO_START_NEW, false);

        mSettlementUnpaidFragment = CompensationsUnpaidFragment.newInstance(autoStartNew);
        mSettlementPaidFragment = new CompensationsPaidFragment();

        setupTabs();
    }

    private void setupTabs() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabsAdapter tabsAdapter = new TabsAdapter(getFragmentManager());
        tabsAdapter.addFragment(mSettlementUnpaidFragment, getString(R.string.tab_compensations_new));
        tabsAdapter.addFragment(mSettlementPaidFragment, getString(R.string.tab_compensations_history));
        viewPager.setAdapter(tabsAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onGroupsFetched() {
        super.onGroupsFetched();

        mCurrentGroupCurrency = ParseUtils.getGroupCurrency();
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
        mSinglePaymentRecipients.clear();

        if (!users.isEmpty()) {
            for (ParseUser parseUser : users) {
                User user = (User) parseUser;
                if (!user.getObjectId().equals(mCurrentUser.getObjectId())) {
                    mSinglePaymentRecipients.add(new ItemUserPicker(user.getObjectId(),
                            user.getNickname(), user.getAvatar()));
                }
            }

            Collections.sort(mSinglePaymentRecipients);
        }

        showAddManualDialog(mSinglePaymentRecipients);
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
                updateFragmentAdapter(FRAGMENT_ADAPTER_UNPAID);
                compensation.saveEventually();
                MessageUtils.showBasicSnackbar(mToolbar,
                        getString(R.string.toast_payment_saved, recipientNickname));
            }
        });
    }

    @Override
    public void onlineQuery() {
        OnlineQuery.queryCompensations(this, this);
    }

    @Override
    public void onCompensationsPinFailed(String errorMessage) {
        setLoading(false);

        Snackbar snackbar = MessageUtils.getBasicSnackbar(mToolbar, errorMessage);
        snackbar.setAction(R.string.action_retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoading(true);
                onlineQuery();
            }
        });
        snackbar.show();
    }

    @Override
    public void onCompensationsPinned(boolean isPaid) {
        super.onCompensationsPinned(isPaid);
        if (isPaid) {
            updateFragmentAdapter(FRAGMENT_ADAPTER_PAID);
        } else {
            updateFragmentAdapter(FRAGMENT_ADAPTER_UNPAID);
        }
    }

    private void updateFragmentAdapter(@AdapterType int adapter) {
        switch (adapter) {
            case FRAGMENT_ADAPTER_BOTH:
                mSettlementPaidFragment.updateAdapter();
                mSettlementUnpaidFragment.updateAdapter();
                break;
            case FRAGMENT_ADAPTER_PAID:
                mSettlementPaidFragment.updateAdapter();
                break;
            case FRAGMENT_ADAPTER_UNPAID:
                mSettlementUnpaidFragment.updateAdapter();
                break;
        }
    }

    @Override
    public void setLoading(boolean isLoading) {
        super.setLoading(isLoading);

        mSettlementUnpaidFragment.setLoading(isLoading);
        mSettlementPaidFragment.setLoading(isLoading);
    }

    @Override
    public void showAccountCreateDialog() {
        AccountCreateDialogFragment accountCreateDialogFragment =
                new AccountCreateDialogFragment();
        accountCreateDialogFragment.show(getFragmentManager(), "account_create");
    }

    @Override
    public void showChangeAmountDialog(BigFraction amount, String currency) {
        CompensationChangeAmountDialogFragment storeSelectionDialogFragment =
                CompensationChangeAmountDialogFragment.newInstance(amount, currency);
        storeSelectionDialogFragment.show(getFragmentManager(), "change_amount");
    }

    @Override
    public void changeAmount(BigFraction amount) {
        mSettlementUnpaidFragment.changeAmount(amount);
    }

    @Override
    protected void onNewGroupSet() {
        updateFragmentAdapter(FRAGMENT_ADAPTER_BOTH);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save fragments in saveInstanceBundle if they are added
        if (mSettlementUnpaidFragment != null && mSettlementUnpaidFragment.isAdded() &&
                mSettlementPaidFragment != null && mSettlementPaidFragment.isAdded()) {
            getFragmentManager().putFragment(outState, ACCOUNT_BALANCE_NEW_FRAGMENT,
                    mSettlementUnpaidFragment);
            getFragmentManager().putFragment(outState, ACCOUNT_BALANCE_HISTORY_FRAGMENT,
                    mSettlementPaidFragment);
        }
    }

    @Override
    int getSelfNavDrawerItem() {
        return R.id.nav_settlement;
    }
}
