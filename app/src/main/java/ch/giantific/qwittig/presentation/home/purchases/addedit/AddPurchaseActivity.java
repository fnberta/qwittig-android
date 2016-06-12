/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.push.PushBroadcastReceiver;
import ch.giantific.qwittig.databinding.ActivityPurchaseAddEditBinding;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.TransitionListenerAdapter;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.Utils;
import rx.Single;

/**
 * Hosts {@link AddPurchaseFragment} that handles the creation of a new purchase.
 * <p/>
 * Asks the user if he wants to discard the new purchase when dismissing the activity.
 */
public class AddPurchaseActivity extends BaseActivity<AddEditPurchaseViewModel> implements
        AddEditPurchaseBaseFragment.ActivityListener,
        AddEditPurchaseNoteFragment.ActivityListener,
        AddEditPurchaseReceiptFragment.ActivityListener,
        RatesWorkerListener, DatePickerDialog.OnDateSetListener,
        NoteDialogFragment.DialogInteractionListener,
        ExchangeRateDialogFragment.DialogInteractionListener,
        DiscardPurchaseDialogFragment.DialogInteractionListener {

    public static final String INTENT_OCR_PURCHASE_ID = "INTENT_OCR_PURCHASE_ID";
    private static final String STATE_HAS_RECEIPT_FILE = "STATE_HAS_RECEIPT_FILE";
    private static final String STATE_HAS_NOTE = "STATE_HAS_NOTE";
    private ActivityPurchaseAddEditBinding mBinding;
    private AddEditPurchaseNoteViewModel mNoteViewModel;
    private boolean mHasReceiptFile;
    private boolean mHasNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_purchase_add_edit);

        if (savedInstanceState == null) {
            if (Utils.isRunningLollipopAndHigher()) {
                addActivityTransitionListener();
            } else {
                showFab();
            }

            getSupportFragmentManager().beginTransaction()
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

    private void showFab() {
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
    AddEditPurchaseBaseFragment getPurchaseAddEditFragment() {
        final String ocrPurchaseId = getOcrPurchaseId();
        if (!TextUtils.isEmpty(ocrPurchaseId)) {
            return AddPurchaseFragment.newAddOcrInstance(ocrPurchaseId);
        }

        return AddPurchaseFragment.newAddInstance();
    }

    private String getOcrPurchaseId() {
        final Intent intent = getIntent();
        String ocrPurchaseId = intent.getStringExtra(INTENT_OCR_PURCHASE_ID);

        if (TextUtils.isEmpty(ocrPurchaseId)) {
            try {
                final JSONObject jsonExtras = PushBroadcastReceiver.getData(intent);
                ocrPurchaseId = jsonExtras.optString(PushBroadcastReceiver.PUSH_PARAM_OCR_DATA_ID);
            } catch (JSONException ignored) {
            }
        }

        return ocrPurchaseId;
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
                mViewModel.onExitClick();
                return true;
            case R.id.action_purchase_add_edit_receipt_show:
                mViewModel.onShowReceiptImageMenuClick();
                return true;
            case R.id.action_purchase_add_edit_receipt_add:
                mViewModel.onAddReceiptImageMenuClick();
                return true;
            case R.id.action_purchase_add_edit_note_show:
                mViewModel.onShowNoteMenuClick();
                return true;
            case R.id.action_purchase_add_edit_note_add:
                mViewModel.onAddNoteMenuClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (!getSupportFragmentManager().popBackStackImmediate()) {
            mViewModel.onExitClick();
        }
    }

    @Override
    public void setAddEditViewModel(@NonNull AddEditPurchaseViewModel viewModel) {
        mViewModel = viewModel;
        mBinding.setViewModel(mViewModel);
    }

    @Override
    public void setNoteViewModel(@NonNull AddEditPurchaseNoteViewModel viewModel) {
        mNoteViewModel = viewModel;
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
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        final Date date = DateUtils.parseDateFromPicker(year, monthOfYear, dayOfMonth);
        mViewModel.onDateSet(date);
    }

    @Override
    public void onExchangeRateManuallySet(double exchangeRate) {
        mViewModel.onExchangeRateManuallySet(exchangeRate);
    }

    @Override
    public void setReceiptImagePath(@NonNull String path) {
        mViewModel.onReceiptImagePathSet(path);
    }

    @Override
    public void deleteReceipt() {
        getSupportFragmentManager().popBackStackImmediate();
        mViewModel.onDeleteReceiptImageMenuClick();
    }

    @Override
    public void onNoteSet(@NonNull String note) {
        if (mNoteViewModel != null) {
            mNoteViewModel.onNoteSet(note);
        } else {
            Snackbar.make(mToolbar, R.string.toast_note_added, Snackbar.LENGTH_LONG).show();
        }

        mViewModel.onNoteSet(note);
    }

    @Override
    public void deleteNote() {
        getSupportFragmentManager().popBackStackImmediate();
        mViewModel.onNoteSet("");
    }

    @Override
    public void setRateFetchStream(@NonNull Single<Float> single,
                                   @NonNull String workerTag) {
        mViewModel.setRateFetchStream(single, workerTag);
    }

    @Override
    public void onDiscardPurchaseSelected() {
        mViewModel.onDiscardChangesSelected();
    }

    @Override
    public void onSaveAsDraftSelected() {
        mViewModel.onSaveAsDraftMenuClick();
    }
}
