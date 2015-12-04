/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.activities;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.berta.fabio.fabprogress.FabProgress;
import ch.berta.fabio.fabprogress.ProgressFinalAnimationListener;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.helpers.RatesHelper;
import ch.giantific.qwittig.data.helpers.save.PurchaseSaveHelper;
import ch.giantific.qwittig.ui.fragments.PurchaseBaseFragment;
import ch.giantific.qwittig.ui.fragments.PurchaseNoteFragment;
import ch.giantific.qwittig.ui.fragments.PurchaseReceiptAddFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.ManualExchangeRateDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.PurchaseNoteEditDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.PurchaseUserSelectionDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.StoreSelectionDialogFragment;
import ch.giantific.qwittig.ui.listeners.TransitionListenerAdapter;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Provides an abstract base class for the creation and editing of purchases. Mostly deals with
 * transition animations and the communication between the different hosted fragments.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public abstract class PurchaseBaseActivity extends BaseActivity implements
        PurchaseBaseFragment.FragmentInteractionListener,
        DatePickerDialog.OnDateSetListener,
        PurchaseUserSelectionDialogFragment.DialogInteractionListener,
        StoreSelectionDialogFragment.DialogInteractionListener,
        PurchaseReceiptAddFragment.FragmentInteractionListener,
        RatesHelper.HelperInteractionListener,
        PurchaseSaveHelper.HelperInteractionListener,
        ManualExchangeRateDialogFragment.DialogInteractionListener,
        PurchaseNoteFragment.FragmentInteractionListener,
        PurchaseNoteEditDialogFragment.DialogInteractionListener {

    static final String STATE_PURCHASE_FRAGMENT = "STATE_PURCHASE_FRAGMENT";
    private static final String STATE_HAS_RECEIPT_FILE = "STATE_HAS_RECEIPT_FILE";
    private static final String LOG_TAG = PurchaseBaseActivity.class.getSimpleName();

    PurchaseBaseFragment mPurchaseFragment;
    private boolean mHasReceiptFile;
    private boolean mHasNote;
    private FabProgress mFabProgress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_add_edit);

        mFabProgress = (FabProgress) findViewById(R.id.fab_purchase_save);
        mFabProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPurchaseFragment.savePurchase(false);
            }
        });
        mFabProgress.setProgressFinalAnimationListener(new ProgressFinalAnimationListener() {
            @Override
            public void onProgressFinalAnimationComplete() {
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_HAS_RECEIPT_FILE, mHasReceiptFile);
        getFragmentManager().putFragment(outState, STATE_PURCHASE_FRAGMENT, mPurchaseFragment);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void addActivityTransitionListener() {
        Transition enter = getWindow().getEnterTransition();
        enter.addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                super.onTransitionEnd(transition);
                transition.removeListener(this);

                showFab();
            }
        });
    }

    @Override
    public void showFab() {
        if (ViewCompat.isLaidOut(mFabProgress)) {
            mFabProgress.show();
        } else {
            mFabProgress.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(@NonNull View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    mFabProgress.show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_purchase_add_edit, menu);

        if (mHasReceiptFile) {
            menu.findItem(R.id.action_purchase_add_edit_receipt_show).setVisible(true);
            menu.findItem(R.id.action_purchase_add_edit_receipt_add).setVisible(false);
        }

        if (mHasNote) {
            menu.findItem(R.id.action_purchase_add_edit_note_show).setVisible(true);
            menu.findItem(R.id.action_purchase_add_edit_note_add).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return checkIfSaving() || super.onOptionsItemSelected(item);
            case R.id.action_purchase_add_edit_receipt_show:
                mPurchaseFragment.showReceiptFragment();
                return true;
            case R.id.action_purchase_add_edit_receipt_add:
                mPurchaseFragment.captureImage();
                return true;
            case R.id.action_purchase_add_edit_note_show:
                mPurchaseFragment.showNoteFragment();
                return true;
            case R.id.action_purchase_add_edit_note_add:
                mPurchaseFragment.editNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean checkIfSaving() {
        if (mPurchaseFragment.isSaving()) {
            Snackbar.make(mToolbar, R.string.toast_saving_purchase, Snackbar.LENGTH_LONG).show();
            return true;
        }

        return false;
    }

    @Override
    public void setHasReceiptFile(boolean hasReceiptFile) {
        mHasReceiptFile = hasReceiptFile;
        invalidateOptionsMenu();
    }

    @Override
    public void deleteReceipt() {
        mPurchaseFragment.deleteReceipt();
        setHasReceiptFile(false);
        getFragmentManager().popBackStack();
        Snackbar.make(mFabProgress, R.string.toast_receipt_deleted, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void setReceiptImagePath(@NonNull String path) {
        mPurchaseFragment.setReceiptImagePath(path);
    }

    @Override
    public void setHasNote(boolean hasNote) {
        mHasNote = hasNote;
        invalidateOptionsMenu();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Date date = DateUtils.parseDateFromPicker(year, month, day);
        mPurchaseFragment.setDate(date);
    }

    @Override
    public void onStoreSet(@NonNull String store, boolean manuallyEntered) {
        mPurchaseFragment.setStore(store, manuallyEntered);
    }

    @Override
    public void onItemUsersInvolvedSet(@NonNull List<Integer> usersInvolved) {
        mPurchaseFragment.onItemUsersInvolvedSet(usersInvolved);
    }

    @Override
    public void editNote() {
        mPurchaseFragment.editNote();
    }

    @Override
    public void onNoteSet(@NonNull String note) {
        mPurchaseFragment.onNoteSet(note);
        setHasNote(true);

        PurchaseNoteFragment fragment = findPurchaseNoteFragment();
        if (fragment != null) {
            fragment.updateNote(note);
        } else {
            Snackbar.make(mFabProgress, R.string.toast_note_added, Snackbar.LENGTH_LONG).show();
        }
    }

    private PurchaseNoteFragment findPurchaseNoteFragment() {
        return (PurchaseNoteFragment) getFragmentManager()
                .findFragmentByTag(PurchaseBaseFragment.PURCHASE_NOTE_FRAGMENT);
    }

    @Override
    public void deleteNote() {
        mPurchaseFragment.deleteNote();
        setHasNote(false);
        getFragmentManager().popBackStack();
        Snackbar.make(mFabProgress, R.string.toast_note_deleted, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onExchangeRateSet(float exchangeRate) {
        mPurchaseFragment.onExchangeRateSet(exchangeRate);
    }

    @Override
    public void onRatesFetched(@NonNull Map<String, Float> exchangeRates) {
        mPurchaseFragment.onRatesFetched(exchangeRates);
    }

    @Override
    public void onRatesFetchFailed(@NonNull String errorMessage) {
        mPurchaseFragment.onRatesFetchFailed(errorMessage);
    }

    @Override
    public void onPurchaseSavedAndPinned() {
        mPurchaseFragment.onPurchaseSavedAndPinned();
    }

    @Override
    public void onPurchaseSaveFailed(int errorCode) {
        mPurchaseFragment.onPurchaseSaveFailed(errorCode);
    }

    @Override
    public void startProgressAnim() {
        mFabProgress.startProgress();
    }

    @Override
    public void startFinalProgressAnim() {
        mFabProgress.beginProgressFinalAnimation();
    }

    @Override
    public void stopProgressAnim() {
        mFabProgress.stopProgress();
    }

    @Override
    public void onBackPressed() {
        if (!checkIfSaving()) {
            super.onBackPressed();
        }
    }
}
