/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.activities;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import java.util.Date;

import ch.berta.fabio.fabprogress.ProgressFinalAnimationListener;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.ActivityPurchaseAddEditBinding;
import ch.giantific.qwittig.domain.models.ocr.OcrPurchase;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseAddEditBaseFragment;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseAddFragment;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseNoteFragment;
import ch.giantific.qwittig.presentation.ui.fragments.PurchaseReceiptAddEditFragment;
import ch.giantific.qwittig.presentation.ui.fragments.dialogs.ManualExchangeRateDialogFragment;
import ch.giantific.qwittig.presentation.ui.fragments.dialogs.PurchaseDiscardDialogFragment;
import ch.giantific.qwittig.presentation.ui.fragments.dialogs.PurchaseNoteAddEditDialogFragment;
import ch.giantific.qwittig.presentation.ui.listeners.TransitionListenerAdapter;
import ch.giantific.qwittig.presentation.viewmodels.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.workerfragments.OcrWorkerListener;
import ch.giantific.qwittig.presentation.workerfragments.RatesWorkerListener;
import ch.giantific.qwittig.presentation.workerfragments.save.PurchaseSaveWorkerListener;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.Utils;
import rx.Single;

/**
 * Hosts {@link PurchaseAddFragment} that handles the creation of a new purchase.
 * <p/>
 * Asks the user if he wants to discard the new purchase when dismissing the activity.
 */
public class PurchaseAddActivity extends BaseActivity<PurchaseAddEditViewModel> implements
        PurchaseAddEditBaseFragment.ActivityListener,
        PurchaseNoteFragment.FragmentInteractionListener,
        PurchaseReceiptAddEditFragment.ActivityListener,
        PurchaseSaveWorkerListener, RatesWorkerListener, DatePickerDialog.OnDateSetListener,
        PurchaseNoteAddEditDialogFragment.DialogInteractionListener,
        ManualExchangeRateDialogFragment.DialogInteractionListener,
        PurchaseDiscardDialogFragment.DialogInteractionListener,
        OcrWorkerListener {

    public static final String INTENT_PURCHASE_NEW_AUTO = "INTENT_PURCHASE_NEW_AUTO";
    private static final String STATE_HAS_RECEIPT_FILE = "STATE_HAS_RECEIPT_FILE";
    private static final String STATE_HAS_NOTE = "STATE_HAS_NOTE";
    private ActivityPurchaseAddEditBinding mBinding;
    private boolean mHasReceiptFile;
    private boolean mHasNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_purchase_add_edit);

        // TODO: move to data binding
        mBinding.fabPurchaseSave.setProgressFinalAnimationListener(new ProgressFinalAnimationListener() {
            @Override
            public void onProgressFinalAnimationComplete() {
                mViewModel.onProgressFinalAnimationComplete();
            }
        });
        mBinding.fabPurchaseSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.onFabSavePurchaseClick(v);
            }
        });

        if (savedInstanceState == null) {
            if (Utils.isRunningLollipopAndHigher()) {
                addActivityTransitionListener();
            } else {
                showFab();
            }

            getFragmentManager().beginTransaction()
                    .add(R.id.container, getPurchaseAddEditFragment())
                    .commit();
        } else {
            mHasReceiptFile = savedInstanceState.getBoolean(STATE_HAS_RECEIPT_FILE);
            mHasNote = savedInstanceState.getBoolean(STATE_HAS_NOTE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_HAS_RECEIPT_FILE, mHasReceiptFile);
        outState.putBoolean(STATE_HAS_NOTE, mHasNote);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void addActivityTransitionListener() {
        final Transition enter = getWindow().getEnterTransition();
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
        if (ViewCompat.isLaidOut(mBinding.fabPurchaseSave)) {
            mBinding.fabPurchaseSave.show();
        } else {
            mBinding.fabPurchaseSave.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(@NonNull View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    mBinding.fabPurchaseSave.show();
                }
            });
        }
    }

    @NonNull
    PurchaseAddEditBaseFragment getPurchaseAddEditFragment() {
        final boolean autoMode = getIntent().getBooleanExtra(INTENT_PURCHASE_NEW_AUTO, false);
        return autoMode
                ? PurchaseAddFragment.newAddAutoInstance()
                : PurchaseAddFragment.newAddInstance();
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
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                mViewModel.onUpOrBackClick();
                return true;
            case R.id.action_purchase_add_edit_receipt_show:
                mViewModel.onShowReceiptImageClick();
                return true;
            case R.id.action_purchase_add_edit_receipt_add:
                mViewModel.onAddReceiptImageClick();
                return true;
            case R.id.action_purchase_add_edit_note_show:
                mViewModel.onShowNoteClick();
                return true;
            case R.id.action_purchase_add_edit_note_add:
                mViewModel.onAddNoteClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setAddEditViewModel(@NonNull PurchaseAddEditViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public void setReceiptImagePath(@NonNull String path) {
        mViewModel.onReceiptImagePathSet(path);
    }

    @Override
    public void deleteReceipt() {
        mViewModel.onDeleteReceiptImageClick();
        getFragmentManager().popBackStackImmediate();

        toggleReceiptMenuOption(false);
        Snackbar.make(mToolbar, R.string.toast_receipt_deleted, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void editNote() {
        mViewModel.onEditNoteClick();
    }

    @Override
    public void deleteNote() {
        mViewModel.onDeleteNoteClick();
        getFragmentManager().popBackStack();

        mHasNote = false;
        invalidateOptionsMenu();
        Snackbar.make(mToolbar, R.string.toast_note_deleted, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onDiscardPurchaseSelected() {
        mViewModel.onDiscardChangesSelected();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        final Date date = DateUtils.parseDateFromPicker(year, monthOfYear, dayOfMonth);
        mViewModel.onDateSet(date);
    }

    @Override
    public void onExchangeRateManuallySet(float exchangeRate) {
        mViewModel.onExchangeRateManuallySet(exchangeRate);
    }

    @Override
    public void setRateFetchStream(@NonNull Single<Float> single,
                                    @NonNull String workerTag) {
        mViewModel.setRateFetchStream(single, workerTag);
    }

    @Override
    public void onNoteSet(@NonNull String note) {
        mViewModel.onNoteSet(note);
        mHasNote = true;
        invalidateOptionsMenu();

        final PurchaseNoteFragment fragment = findPurchaseNoteFragment();
        if (fragment != null) {
            fragment.updateNote(note);
        } else {
            Snackbar.make(mToolbar, R.string.toast_note_added, Snackbar.LENGTH_LONG).show();
        }
    }

    private PurchaseNoteFragment findPurchaseNoteFragment() {
        return (PurchaseNoteFragment) getFragmentManager()
                .findFragmentByTag(PurchaseAddFragment.PURCHASE_NOTE_FRAGMENT);
    }

    @Override
    public void toggleReceiptMenuOption(boolean show) {
        mHasReceiptFile = show;
        invalidateOptionsMenu();
    }

    @Override
    public void toggleNoteMenuOption(boolean show) {
        mHasNote = show;
        invalidateOptionsMenu();
    }

    @Override
    public void startProgressAnim() {
        mBinding.fabPurchaseSave.startProgress();
    }

    @Override
    public void startFinalProgressAnim() {
        mBinding.fabPurchaseSave.startProgressFinalAnimation();
    }

    @Override
    public void stopProgressAnim() {
        mBinding.fabPurchaseSave.stopProgress();
    }

    @Override
    public void setOcrStream(@NonNull Single<OcrPurchase> single, @NonNull String workerTag) {
        mViewModel.setOcrStream(single, workerTag);
    }

    @Override
    public void onSavePurchaseAsDraftSelected() {
        mViewModel.onSavePurchaseAsDraftClick();
    }

    @Override
    public void setPurchaseSaveStream(@NonNull Single<Purchase> single, @NonNull String workerTag) {
        mViewModel.setPurchaseSaveStream(single, workerTag);
    }

    @Override
    public void onBackPressed() {
        if (!getFragmentManager().popBackStackImmediate()) {
            mViewModel.onUpOrBackClick();
        }
    }
}
