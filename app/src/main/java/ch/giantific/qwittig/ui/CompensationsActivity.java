package ch.giantific.qwittig.ui;

import android.app.FragmentManager;
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
import com.parse.ParseObject;
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
import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.helper.CompensationQueryHelper;
import ch.giantific.qwittig.helper.CompensationRemindHelper;
import ch.giantific.qwittig.helper.MoreQueryHelper;
import ch.giantific.qwittig.helper.SettlementHelper;
import ch.giantific.qwittig.ui.adapter.TabsAdapter;
import ch.giantific.qwittig.ui.dialogs.AccountCreateDialogFragment;
import ch.giantific.qwittig.ui.dialogs.CompensationAddManualDialogFragment;
import ch.giantific.qwittig.ui.dialogs.CompensationChangeAmountDialogFragment;
import ch.giantific.qwittig.ui.dialogs.GroupCreateDialogFragment;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

public class CompensationsActivity extends BaseNavDrawerActivity implements
        CompensationsUnpaidFragment.FragmentInteractionListener,
        LocalQuery.UserLocalQueryListener,
        CompensationAddManualDialogFragment.DialogInteractionListener,
        GroupCreateDialogFragment.DialogInteractionListener,
        CompensationChangeAmountDialogFragment.FragmentInteractionListener,
        CompensationQueryHelper.HelperInteractionListener,
        MoreQueryHelper.HelperInteractionListener,
        SettlementHelper.HelperInteractionListener,
        CompensationRemindHelper.HelperInteractionListener {

    public static final String INTENT_AUTO_START_NEW = "intent_auto_start_new";
    private static final String COMPENSATIONS_UNPAID_FRAGMENT = "compensations_unpaid_fragment";
    private static final String COMPENSATIONS_PAID_FRAGMENT = "compensations_paid_fragment";
    @IntDef({FRAGMENT_ADAPTER_BOTH, FRAGMENT_ADAPTER_UNPAID, FRAGMENT_ADAPTER_PAID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AdapterType {}
    private static final int FRAGMENT_ADAPTER_BOTH = 0;
    private static final int FRAGMENT_ADAPTER_UNPAID = 1;
    private static final int FRAGMENT_ADAPTER_PAID = 2;

    private static final String LOG_TAG = CompensationsActivity.class.getSimpleName();

    private CompensationsUnpaidFragment mCompensationsUnpaidFragment;
    private CompensationsPaidFragment mCompensationsPaidFragment;
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
                mCompensationsUnpaidFragment = (CompensationsUnpaidFragment) getFragmentManager()
                        .getFragment(savedInstanceState, COMPENSATIONS_UNPAID_FRAGMENT);
                mCompensationsPaidFragment = (CompensationsPaidFragment) getFragmentManager()
                        .getFragment(savedInstanceState, COMPENSATIONS_PAID_FRAGMENT);
                setupTabs();
            }
        }
    }

    private void addViewPagerFragments() {
        Intent intent = getIntent();
        boolean autoStartNew = intent.getBooleanExtra(INTENT_AUTO_START_NEW, false);

        mCompensationsUnpaidFragment = CompensationsUnpaidFragment.newInstance(autoStartNew);
        mCompensationsPaidFragment = new CompensationsPaidFragment();

        setupTabs();
    }

    private void setupTabs() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabsAdapter tabsAdapter = new TabsAdapter(getFragmentManager());
        tabsAdapter.addFragment(mCompensationsUnpaidFragment, getString(R.string.tab_compensations_new));
        tabsAdapter.addFragment(mCompensationsPaidFragment, getString(R.string.tab_compensations_history));
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
        if (!Utils.isConnected(this)) {
            setLoading(false);
            showErrorSnackbar(getString(R.string.toast_no_connection));
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        CompensationQueryHelper compensationQueryHelper = findQueryHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (compensationQueryHelper == null) {
            compensationQueryHelper = new CompensationQueryHelper();

            fragmentManager.beginTransaction()
                    .add(compensationQueryHelper, CompensationQueryHelper.COMPENSATION_QUERY_HELPER)
                    .commit();
        }
    }

    private CompensationQueryHelper findQueryHelper(FragmentManager fragmentManager) {
        return (CompensationQueryHelper) fragmentManager.findFragmentByTag(CompensationQueryHelper.COMPENSATION_QUERY_HELPER);
    }

    @Override
    public void onCompensationsPinFailed(ParseException e) {
        ParseErrorHandler.handleParseError(this, e);
        showErrorSnackbar(ParseErrorHandler.getErrorMessage(this, e));
        removeQueryHelper();

        setLoading(false);
    }

    private void showErrorSnackbar(String errorMessage) {
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

        removeQueryHelper();
        setLoading(false);

        if (isPaid) {
            updateFragmentAdapter(FRAGMENT_ADAPTER_PAID);
        } else {
            updateFragmentAdapter(FRAGMENT_ADAPTER_UNPAID);
        }
    }

    private void removeQueryHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        CompensationQueryHelper compensationQueryHelper = findQueryHelper(fragmentManager);

        if (compensationQueryHelper != null) {
            fragmentManager.beginTransaction().remove(compensationQueryHelper).commitAllowingStateLoss();
        }
    }

    private void updateFragmentAdapter(@AdapterType int adapter) {
        switch (adapter) {
            case FRAGMENT_ADAPTER_BOTH:
                mCompensationsPaidFragment.updateAdapter();
                mCompensationsUnpaidFragment.updateAdapter();
                break;
            case FRAGMENT_ADAPTER_PAID:
                mCompensationsPaidFragment.updateAdapter();
                break;
            case FRAGMENT_ADAPTER_UNPAID:
                mCompensationsUnpaidFragment.updateAdapter();
                break;
        }
    }

    private void setLoading(boolean isLoading) {
        mCompensationsUnpaidFragment.setLoading(isLoading);
        mCompensationsPaidFragment.setLoading(isLoading);
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
        mCompensationsUnpaidFragment.changeAmount(amount);
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
        updateFragmentAdapter(FRAGMENT_ADAPTER_BOTH);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save fragments in saveInstanceBundle if they are added
        if (mCompensationsUnpaidFragment != null && mCompensationsUnpaidFragment.isAdded() &&
                mCompensationsPaidFragment != null && mCompensationsPaidFragment.isAdded()) {
            getFragmentManager().putFragment(outState, COMPENSATIONS_UNPAID_FRAGMENT,
                    mCompensationsUnpaidFragment);
            getFragmentManager().putFragment(outState, COMPENSATIONS_PAID_FRAGMENT,
                    mCompensationsPaidFragment);
        }
    }

    @Override
    int getSelfNavDrawerItem() {
        return R.id.nav_settlement;
    }
}
