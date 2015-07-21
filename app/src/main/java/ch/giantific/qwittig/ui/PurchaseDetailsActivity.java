package ch.giantific.qwittig.ui;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.NavUtils;
import android.transition.Explode;
import android.transition.Transition;
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
import ch.giantific.qwittig.utils.Utils;


public class PurchaseDetailsActivity extends BaseNavDrawerActivity implements
        PurchaseDetailsFragment.FragmentInteractionListener,
        PurchaseReceiptDetailsFragment.FragmentInteractionListener {

    public static final int RESULT_PURCHASE_DELETED = 2;
    public static final int RESULT_GROUP_CHANGED = 3;
    private static final String PURCHASE_DETAILS_FRAGMENT = "purchase_details_fragment";
    private static final String PURCHASE_RECEIPT_FRAGMENT = "purchase_receipt_fragment";
    private String mPurchaseId;
    private PurchaseDetailsActivity mThis = this;
    private boolean mShowEditOptions = false;
    private boolean mHasReceiptFile = false;
    private TextView mTextViewStore;
    private TextView mTextViewDate;
    private PurchaseDetailsFragment mPurchaseDetailsFragment;

    private static final String LOG_TAG = PurchaseDetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // green or red toolbar
        applyCorrectTheme();
        // disable default actionBar title
        getSupportActionBar().setTitle(null);

        replaceDrawerIndicatorWithUp();
        uncheckNavDrawerItems();

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(mThis);
            }
        });

        mTextViewStore = (TextView) findViewById(R.id.tv_store);
        mTextViewDate = (TextView) findViewById(R.id.tv_date);

        if (Utils.isRunningLollipopAndHigher()) {
            setActivityTransition();
        }
        supportPostponeEnterTransition();

        getPurchaseId();

        if (savedInstanceState == null && mUserIsLoggedIn) {
            addDetailsFragment();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setActivityTransition() {
        Transition transitionEnter = new Explode();
        transitionEnter.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setEnterTransition(transitionEnter);
    }

    /**
     * Applies the correct theme, depending on whether the currentUser is the buyer of the purchase
     * or not.
     */
    private void applyCorrectTheme() {
        final Intent intent = getIntent();
        boolean isGreen = intent.getBooleanExtra(HomePurchasesFragment.INTENT_THEME, true);
        int theme;
        if (isGreen) {
            theme = R.style.AppTheme_WithNavDrawer_Green;
        } else {
            theme = R.style.AppTheme_WithNavDrawer_Red;
        }
        setTheme(theme);
        setContentView(R.layout.activity_purchase_details);
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
                e.printStackTrace();
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
        if (mHasReceiptFile) {
            menu.findItem(R.id.action_purchase_show_receipt).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_purchase_show_receipt:
                replaceWithReceiptFragment();
                return true;
            case R.id.action_purchase_edit:
                editPurchase();
                return true;
            case R.id.action_purchase_delete:
                mPurchaseDetailsFragment.deletePurchase();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void replaceWithReceiptFragment() {
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
    public void updateActionBarMenu(boolean showEditOptions, boolean hasReceiptFile) {
        mShowEditOptions = showEditOptions;
        mHasReceiptFile = hasReceiptFile;
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
    public void finishAfterDelete() {
        setResult(RESULT_PURCHASE_DELETED);
        finish();
    }

    @Override
    protected void onNewGroupSet() {
        // NavDrawer group setting needs to be updated
        setResult(RESULT_GROUP_CHANGED);
        finish();
    }
}
