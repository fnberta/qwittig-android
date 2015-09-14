package ch.giantific.qwittig.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import ch.giantific.qwittig.PushBroadcastReceiver;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.ui.dialogs.AccountCreateDialogFragment;
import ch.giantific.qwittig.utils.MessageUtils;


public class PurchaseDetailsActivity extends BaseNavDrawerActivity implements
        PurchaseDetailsFragment.FragmentInteractionListener {

    public static final int RESULT_PURCHASE_DELETED = 2;
    public static final int RESULT_GROUP_CHANGED = 3;
    private static final String PURCHASE_DETAILS_FRAGMENT = "purchase_details_fragment";
    private static final String PURCHASE_RECEIPT_FRAGMENT = "purchase_receipt_fragment";
    private String mPurchaseId;
    private boolean mShowEditOptions;
    private boolean mHasForeignCurrency;
    private TextView mTextViewStore;
    private TextView mTextViewDate;
    private PurchaseDetailsFragment mPurchaseDetailsFragment;

    private static final String LOG_TAG = PurchaseDetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_details);

        // disable default actionBar title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        replaceDrawerIndicatorWithUp();
        uncheckNavDrawerItems();

        final PurchaseDetailsActivity activity = this;
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(activity);
            }
        });

        mTextViewStore = (TextView) findViewById(R.id.tv_details_title);
        mTextViewDate = (TextView) findViewById(R.id.tv_details_subtitle);

        supportPostponeEnterTransition();
        getPurchaseId();

        if (savedInstanceState == null && mUserIsLoggedIn) {
            addDetailsFragment();
        }
    }

    private void addDetailsFragment() {
        getFragmentManager().beginTransaction()
                .add(R.id.container, PurchaseDetailsFragment.newInstance(mPurchaseId),
                        PURCHASE_DETAILS_FRAGMENT)
                .commit();
    }

    /**
     * Gets data passed on in intent from HomeActivity or Push Notification
     */
    private void getPurchaseId() {
        final Intent intent = getIntent();
        mPurchaseId = intent.getStringExtra(HomePurchasesFragment.INTENT_PURCHASE_ID); // started from HomeActivity

        if (mPurchaseId == null) { // started via Push Notification
            try {
                JSONObject jsonExtras = PushBroadcastReceiver.getData(intent);
                mPurchaseId = jsonExtras.optString(PushBroadcastReceiver.PUSH_PARAM_PURCHASE);
            } catch (JSONException e) {
                MessageUtils.showBasicSnackbar(mToolbar, getString(R.string.toast_error_purchase_details_load));
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mUserIsLoggedIn) {
            findDetailsFragment();
        }
    }

    private void findDetailsFragment() {
        mPurchaseDetailsFragment = (PurchaseDetailsFragment) getFragmentManager()
                .findFragmentByTag(PURCHASE_DETAILS_FRAGMENT);
    }

    @Override
    void afterLoginSetup() {
        super.afterLoginSetup();

        addDetailsFragment();
        getFragmentManager().executePendingTransactions();
        findDetailsFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_purchase_details, menu);
        if (mShowEditOptions) {
            menu.findItem(R.id.action_purchase_edit).setVisible(true);
            menu.findItem(R.id.action_purchase_delete).setVisible(true);
        }
        if (mHasForeignCurrency) {
            menu.findItem(R.id.action_purchase_show_exchange_rate).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_purchase_edit:
                editPurchase();
                return true;
            case R.id.action_purchase_delete:
                mPurchaseDetailsFragment.deletePurchase();
                return true;
            case R.id.action_purchase_show_exchange_rate:
                mPurchaseDetailsFragment.showExchangeRate();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void replaceWithReceiptFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        PurchaseReceiptDetailsFragment purchaseReceiptDetailsFragment =
                PurchaseReceiptDetailsFragment.newInstance(mPurchaseId);
        fragmentManager.beginTransaction()
                .replace(R.id.container, purchaseReceiptDetailsFragment, PURCHASE_RECEIPT_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    private void editPurchase() {
        Intent intent = new Intent(this, PurchaseEditActivity.class);
        intent.putExtra(HomePurchasesFragment.INTENT_PURCHASE_ID, mPurchaseId);
        ActivityOptionsCompat activityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this);
        startActivityForResult(intent, HomeActivity.INTENT_REQUEST_PURCHASE_MODIFY,
                activityOptionsCompat.toBundle());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == HomeActivity.INTENT_REQUEST_PURCHASE_MODIFY) {
            switch (resultCode) {
                case PurchaseEditActivity.RESULT_PURCHASE_SAVED:
                    MessageUtils.showBasicSnackbar(mToolbar, getString(R.string.toast_changes_saved));
                    break;
                case PurchaseEditActivity.RESULT_PURCHASE_DISCARDED:
                    MessageUtils.showBasicSnackbar(mToolbar, getString(R.string.toast_changes_discarded));
                    break;
            }
        }
    }

    @Override
    public void setToolbarStoreDate(String store, String date) {
        mTextViewStore.setText(store);
        mTextViewDate.setText(date);
    }

    @Override
    public void toggleActionBarOptions(boolean showEditOptions, boolean hasForeignCurrency) {
        mShowEditOptions = showEditOptions;
        mHasForeignCurrency = hasForeignCurrency;
        invalidateOptionsMenu();
    }

    @Override
    public void showAccountCreateDialog() {
        AccountCreateDialogFragment accountCreateDialogFragment =
                new AccountCreateDialogFragment();
        accountCreateDialogFragment.show(getFragmentManager(), "account_create");
    }

    @Override
    public void onPurchasesPinned() {
        super.onPurchasesPinned();

        if (mPurchaseDetailsFragment.isAdded()) {
            mPurchaseDetailsFragment.queryData();
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewGroupSet() {
        // NavDrawer group setting needs to be updated
        setResult(RESULT_GROUP_CHANGED);
        finish();
    }
}
