package ch.giantific.qwittig.ui.activities;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.parse.ParseException;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.helpers.PurchaseSaveHelper;
import ch.giantific.qwittig.helpers.RatesHelper;
import ch.giantific.qwittig.ui.fragments.PurchaseBaseFragment;
import ch.giantific.qwittig.ui.fragments.PurchaseReceiptAddFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.ManualExchangeRateDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.PurchaseUserSelectionDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.StoreSelectionDialogFragment;
import ch.giantific.qwittig.ui.listeners.TransitionListenerAdapter;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Created by fabio on 13.12.14.
 */
public abstract class PurchaseBaseActivity extends BaseActivity implements
        PurchaseBaseFragment.FragmentInteractionListener,
        DatePickerDialog.OnDateSetListener,
        PurchaseUserSelectionDialogFragment.FragmentInteractionListener,
        StoreSelectionDialogFragment.DialogInteractionListener,
        PurchaseReceiptAddFragment.FragmentInteractionListener,
        RatesHelper.HelperInteractionListener,
        PurchaseSaveHelper.HelperInteractionListener,
        ManualExchangeRateDialogFragment.DialogInteractionListener {

    static final String STATE_PURCHASE_FRAGMENT = "STATE_PURCHASE_FRAGMENT";
    private static final String STATE_HAS_RECEIPT_FILE = "STATE_HAS_RECEIPT_FILE";
    private static final String LOG_TAG = PurchaseBaseActivity.class.getSimpleName();

    PurchaseBaseFragment mPurchaseFragment;
    private boolean mHasReceiptFile;
    private FloatingActionButton mFabPurchaseSave;
    private FABProgressCircle mFabProgressCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_add_edit);

        mFabPurchaseSave = (FloatingActionButton) findViewById(R.id.fab_purchase_save);
        mFabPurchaseSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPurchaseFragment.savePurchase(false);
            }
        });
        mFabProgressCircle = (FABProgressCircle) findViewById(R.id.fab_purchase_save_circle);
        mFabProgressCircle.attachListener(new FABProgressListener() {
            @Override
            public void onFABProgressAnimationEnd() {
                mPurchaseFragment.finishPurchase();
            }
        });

        if (savedInstanceState == null) {
            if (Utils.isRunningLollipopAndHigher()) {
                addActivityTransitionListener();
            } else {
                showFab();
            }
        } else {
            mHasReceiptFile = savedInstanceState.getBoolean(STATE_HAS_RECEIPT_FILE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_HAS_RECEIPT_FILE, mHasReceiptFile);
        getFragmentManager().putFragment(outState, STATE_PURCHASE_FRAGMENT, mPurchaseFragment);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void addActivityTransitionListener() {
        Transition enter = getWindow().getEnterTransition();
        enter.addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                super.onTransitionEnd(transition);
                transition.removeListener(this);

                showFab();
            }
        });
    }

    public void showFab() {
        showFab(false);
    }

    @Override
    public void showFab(final boolean isSaving) {
        if (ViewCompat.isLaidOut(mFabPurchaseSave)) {
            revealFab(isSaving);
        } else {
            mFabPurchaseSave.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    revealFab(isSaving);
                }
            });
        }
    }

    private void revealFab(boolean isSaving) {
        mFabPurchaseSave.show();
        if (isSaving) {
            mFabProgressCircle.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_purchase_add_edit, menu);

        if (mHasReceiptFile) {
            menu.findItem(R.id.action_purchase_add_edit_receipt_show).setVisible(true);
            menu.findItem(R.id.action_purchase_add_edit_receipt_add).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_purchase_add_edit_receipt_show:
                mPurchaseFragment.showReceiptFragment();
                return true;
            case R.id.action_purchase_add_edit_receipt_add:
                mPurchaseFragment.captureImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void captureImage() {
        mPurchaseFragment.captureImage();
    }

    @Override
    public void updateActionBarMenu(boolean hasReceiptFile) {
        mHasReceiptFile = hasReceiptFile;
        invalidateOptionsMenu();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Date date = DateUtils.parseDateFromPicker(year, month, day);
        mPurchaseFragment.setDate(date);
    }

    @Override
    public void onStoreSet(String store, boolean manuallyEntered) {
        mPurchaseFragment.setStore(store, manuallyEntered);
    }

    @Override
    public void onItemUsersInvolvedSet(List<Integer> usersInvolved) {
        mPurchaseFragment.onItemUsersInvolvedSet(usersInvolved);
    }

    @Override
    public void deleteReceipt() {
        mPurchaseFragment.deleteReceipt();
        updateActionBarMenu(false);
        getFragmentManager().popBackStack();
        MessageUtils.showBasicSnackbar(mFabPurchaseSave, getString(R.string.toast_receipt_deleted));

        // TODO: really delete receipt
    }

    @Override
    public void onExchangeRateSet(float exchangeRate) {
        mPurchaseFragment.onExchangeRateSet(exchangeRate);
    }

    @Override
    public void onRatesFetched(Map<String, Float> exchangeRates) {
        mPurchaseFragment.onRatesFetched(exchangeRates);
    }

    @Override
    public void onRatesFetchFailed(String errorMessage) {
        mPurchaseFragment.onRatesFetchFailed(errorMessage);
    }

    @Override
    public void onPurchaseSavedAndPinned() {
        mPurchaseFragment.onPurchaseSavedAndPinned();
    }

    @Override
    public void onPurchaseSaveFailed(ParseException e) {
        mPurchaseFragment.onPurchaseSaveFailed(e);
    }

    @Override
    public void progressCircleShow() {
        mFabProgressCircle.show();
    }

    @Override
    public void progressCircleStartFinal() {
        mFabProgressCircle.beginFinalAnimation();
    }

    @Override
    public void progressCircleHide() {
        mFabProgressCircle.hide();
    }
}
