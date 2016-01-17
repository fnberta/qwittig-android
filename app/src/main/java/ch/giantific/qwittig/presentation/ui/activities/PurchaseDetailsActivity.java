/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.parse.ParseObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import ch.giantific.qwittig.LocalBroadcast;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.receivers.PushBroadcastReceiver;
import ch.giantific.qwittig.presentation.ui.fragments.HomePurchasesFragment;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseDetailsFragment;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseEditFragment;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseReceiptDetailFragment;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.parse.ParseUtils;

/**
 * Hosts {@link PurchaseDetailsFragment} that displays the details of a purchase and
 * {@link PurchaseReceiptDetailFragment} that displays the image of a receipt.
 * <p/>
 * Displays the store and date of the purchase in the {@link Toolbar}.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class PurchaseDetailsActivity extends BaseNavDrawerActivity implements
        PurchaseDetailsFragment.FragmentInteractionListener {

    public static final int RESULT_PURCHASE_DELETED = 2;
    public static final int RESULT_GROUP_CHANGED = 3;
    private static final String STATE_TOOLBAR_TITLE = "STATE_TOOLBAR_TITLE";
    private static final String STATE_TOOLBAR_SUBTITLE = "STATE_TOOLBAR_SUBTITLE";
    private static final String STATE_PURCHASE_DETAILS_FRAGMENT = "STATE_PURCHASE_DETAILS_FRAGMENT";
    private static final String LOG_TAG = PurchaseDetailsActivity.class.getSimpleName();
    private String mPurchaseId;
    private boolean mShowEditOptions;
    private boolean mHasForeignCurrency;
    private TextView mTextViewStore;
    private TextView mTextViewDate;
    @Nullable
    private String mStore;
    private long mDate;
    private PurchaseDetailsFragment mPurchaseDetailsFragment;

    @Override
    void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);

        if (dataType == LocalBroadcast.DATA_TYPE_PURCHASES_UPDATED) {
            updateFragment();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_details);

        // disable default actionBar title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        replaceDrawerIndicatorWithUp();
        unCheckNavDrawerItems();

        final PurchaseDetailsActivity activity = this;
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(activity);
            }
        });

        mTextViewStore = (TextView) findViewById(R.id.tv_purchase_details_title);
        mTextViewDate = (TextView) findViewById(R.id.tv_purchase_details_subtitle);

        supportPostponeEnterTransition();
        getPurchaseId();

        FragmentManager fragmentManager = getFragmentManager();
        if (savedInstanceState == null) {
            mPurchaseDetailsFragment = PurchaseDetailsFragment.newInstance(mPurchaseId);
            fragmentManager.beginTransaction()
                    .add(R.id.container, mPurchaseDetailsFragment)
                    .commit();
        } else {
            mPurchaseDetailsFragment = (PurchaseDetailsFragment) fragmentManager
                    .getFragment(savedInstanceState, STATE_PURCHASE_DETAILS_FRAGMENT);

            mStore = savedInstanceState.getString(STATE_TOOLBAR_TITLE);
            mDate = savedInstanceState.getLong(STATE_TOOLBAR_SUBTITLE);
            setToolbarTitleValues();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        getFragmentManager().putFragment(outState, STATE_PURCHASE_DETAILS_FRAGMENT,
                mPurchaseDetailsFragment);
        outState.putString(STATE_TOOLBAR_TITLE, mStore);
        outState.putLong(STATE_TOOLBAR_SUBTITLE, mDate);
    }

    private void getPurchaseId() {
        final Intent intent = getIntent();
        mPurchaseId = intent.getStringExtra(HomePurchasesFragment.INTENT_PURCHASE_ID); // started from HomeActivity

        if (mPurchaseId == null) { // started via push notification
            try {
                JSONObject jsonExtras = PushBroadcastReceiver.getData(intent);
                mPurchaseId = jsonExtras.optString(PushBroadcastReceiver.PUSH_PARAM_PURCHASE_ID);
            } catch (JSONException e) {
                Snackbar.make(mToolbar, R.string.toast_error_purchase_details_load,
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_purchase_edit:
                editPurchase();
                return true;
            case R.id.action_purchase_delete:
                deletePurchase();
                return true;
            case R.id.action_purchase_show_exchange_rate:
                showExchangeRate();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Starts {@link PurchaseEditActivity} to edit the purchase.
     */
    private void editPurchase() {
        Intent intent = new Intent(this, PurchaseEditActivity.class);
        intent.putExtra(HomePurchasesFragment.INTENT_PURCHASE_ID, mPurchaseId);
        ActivityOptionsCompat activityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this);
        startActivityForResult(intent, INTENT_REQUEST_PURCHASE_MODIFY,
                activityOptionsCompat.toBundle());
    }

    /**
     * Deletes the purchase and all of its items and finishes if the user is not a test user.
     */
    private void deletePurchase() {
        if (!ParseUtils.isTestUser(mCurrentUser)) {
            Purchase purchase = (Purchase) ParseObject.createWithoutData(Purchase.CLASS, mPurchaseId);
            purchase.deleteEventually();

            setResult(RESULT_PURCHASE_DELETED);
            finish();
        } else {
            showAccountCreateDialog();
        }
    }

    /**
     * Shows the user a {@link Snackbar} with the currency exchange rate used in the purchase.
     */
    private void showExchangeRate() {
        float exchangeRate = mPurchaseDetailsFragment.getExchangeRate();
        String message = getString(R.string.toast_exchange_rate_value,
                MoneyUtils.formatMoneyNoSymbol(exchangeRate,
                        MoneyUtils.EXCHANGE_RATE_FRACTION_DIGITS));
        Snackbar.make(mToolbar, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENT_REQUEST_PURCHASE_MODIFY) {
            switch (resultCode) {
                case PurchaseEditFragment.RESULT_PURCHASE_SAVED:
                    Snackbar.make(mToolbar, R.string.toast_changes_saved, Snackbar.LENGTH_LONG).show();
                    break;
                case PurchaseEditFragment.RESULT_PURCHASE_DISCARDED:
                    Snackbar.make(mToolbar, R.string.toast_changes_discarded, Snackbar.LENGTH_LONG).show();
                    break;
            }
        }
    }

    @Override
    public void setToolbarStoreAndDate(@NonNull String store, @NonNull Date date) {
        mStore = store;
        mDate = DateUtils.parseDateToLong(date);

        setToolbarTitleValues();
    }

    private void setToolbarTitleValues() {
        mTextViewStore.setText(mStore);
        mTextViewDate.setText(DateUtils.formatDateLong(mDate));
    }

    @Override
    public void toggleActionBarOptions(boolean showEditOptions, boolean hasForeignCurrency) {
        mShowEditOptions = showEditOptions;
        mHasForeignCurrency = hasForeignCurrency;
        invalidateOptionsMenu();
    }

    private void updateFragment() {
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
    protected void onGroupChanged() {
        // NavDrawer group setting needs to be updated
        setResult(RESULT_GROUP_CHANGED);
        finish();
    }
}
