/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.LocalBroadcast;
import ch.giantific.qwittig.databinding.ActivityPurchaseAddEditBinding;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.TransitionListenerAdapter;
import ch.giantific.qwittig.presentation.common.fragments.DatePickerDialogFragment;
import ch.giantific.qwittig.presentation.common.fragments.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddFragment;
import ch.giantific.qwittig.presentation.purchases.list.CameraActivity;
import ch.giantific.qwittig.utils.CameraUtils;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MessageAction;
import ch.giantific.qwittig.utils.Utils;
import rx.Single;

/**
 * Hosts {@link PurchaseAddFragment} that handles the creation of a new purchase.
 * <p/>
 * Asks the user if he wants to discard the new purchase when dismissing the activity.
 */
public abstract class BasePurchaseAddEditActivity<T> extends BaseActivity<T> implements
        BasePurchaseAddEditFragment.ActivityListener<T>,
        PurchaseAddEditViewModel.ViewListener,
        RatesWorkerListener, DatePickerDialog.OnDateSetListener,
        NoteDialogFragment.DialogInteractionListener,
        ExchangeRateDialogFragment.DialogInteractionListener,
        DiscardPurchaseDialogFragment.DialogInteractionListener {

    public static final String INTENT_OCR_PURCHASE_ID = "INTENT_OCR_PURCHASE_ID";
    public static final String PURCHASE_NOTE_FRAGMENT = "PURCHASE_NOTE_FRAGMENT";
    public static final String PURCHASE_RECEIPT_FRAGMENT = "PURCHASE_RECEIPT_FRAGMENT";
    private static final String STATE_HAS_RECEIPT_FILE = "STATE_HAS_RECEIPT_FILE";
    private static final String STATE_HAS_NOTE = "STATE_HAS_NOTE";
    private static final int PERMISSIONS_REQUEST_CAPTURE_IMAGES = 12;
    protected PurchaseAddEditViewModel mAddEditPurchaseViewModel;
    @Inject
    protected Navigator mNavigator;
    private ActivityPurchaseAddEditBinding mBinding;
    private boolean mHasReceiptFile;
    private boolean mHasNote;

    @Override
    protected void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);

        switch (dataType) {
            case LocalBroadcast.DataType.OCR_PURCHASE_UPDATED: {
                final boolean successful = intent.getBooleanExtra(LocalBroadcast.INTENT_EXTRA_SUCCESSFUL, false);
                if (successful) {
                    mAddEditPurchaseViewModel.loadData();
                }
                break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_purchase_add_edit);
        mBinding.setViewModel(mAddEditPurchaseViewModel);

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
            showFab();

            mHasReceiptFile = savedInstanceState.getBoolean(STATE_HAS_RECEIPT_FILE);
            mHasNote = savedInstanceState.getBoolean(STATE_HAS_NOTE);
        }
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{mAddEditPurchaseViewModel});
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

    protected final void showFab() {
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
    protected abstract BasePurchaseAddEditFragment getPurchaseAddEditFragment();

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
                mAddEditPurchaseViewModel.onExitClick();
                return true;
            case R.id.action_purchase_add_edit_receipt_show:
                mAddEditPurchaseViewModel.onShowReceiptImageMenuClick();
                return true;
            case R.id.action_purchase_add_edit_receipt_add:
                mAddEditPurchaseViewModel.onAddReceiptImageMenuClick();
                return true;
            case R.id.action_purchase_add_edit_note_show:
                mAddEditPurchaseViewModel.onShowNoteMenuClick();
                return true;
            case R.id.action_purchase_add_edit_note_add:
                mAddEditPurchaseViewModel.onAddNoteMenuClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Navigator.INTENT_REQUEST_IMAGE_CAPTURE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        final String imagePath = data.getStringExtra(CameraActivity.INTENT_EXTRA_IMAGE_PATH);
                        mAddEditPurchaseViewModel.onReceiptImageTaken(imagePath);
                        break;
                    case CameraActivity.RESULT_ERROR:
                        mAddEditPurchaseViewModel.onReceiptImageTakeFailed();
                        break;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAPTURE_IMAGES:
                if (Utils.verifyPermissions(grantResults)) {
                    mNavigator.startCamera();
                } else {
                    showMessageWithAction(R.string.snackbar_permission_storage_denied,
                            new MessageAction(R.string.snackbar_action_open_settings) {
                                @Override
                                public void onClick(View v) {
                                    mNavigator.startSystemSettings();
                                }
                            });
                }

                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (!getSupportFragmentManager().popBackStackImmediate()) {
            mAddEditPurchaseViewModel.onExitClick();
        }
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
    public void onDiscardPurchaseSelected() {
        mAddEditPurchaseViewModel.onDiscardChangesSelected();
    }

    @Override
    public void onSaveAsDraftSelected() {
        mAddEditPurchaseViewModel.onSaveAsDraftMenuClick();
    }

    @Override
    public void loadFetchExchangeRatesWorker(@NonNull String baseCurrency, @NonNull String currency) {
        RatesWorker.attach(getSupportFragmentManager(), baseCurrency, currency);
    }

    @Override
    public void showDatePickerDialog() {
        DatePickerDialogFragment.display(getSupportFragmentManager());
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        final Date date = DateUtils.parseDateFromPicker(year, monthOfYear, dayOfMonth);
        mAddEditPurchaseViewModel.onDateSet(date);
    }

    @Override
    public void showManualExchangeRateSelectorDialog(@NonNull String exchangeRate) {
        ExchangeRateDialogFragment.display(getSupportFragmentManager(), exchangeRate);
    }

    @Override
    public void onExchangeRateManuallySet(double exchangeRate) {
        mAddEditPurchaseViewModel.onExchangeRateManuallySet(exchangeRate);
    }

    @Override
    public void showPurchaseDiscardDialog() {
        DiscardPurchaseDialogFragment.display(getSupportFragmentManager());
    }

    @Override
    public void showDiscardEditChangesDialog() {
        DiscardChangesDialogFragment.display(getSupportFragmentManager());
    }

    @Override
    public void showReceiptImage(@NonNull String receiptImageUri) {
        final Fragment fragment = getReceiptFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, PURCHASE_RECEIPT_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    protected abstract BasePurchaseAddEditReceiptFragment getReceiptFragment();

    @Override
    public void showNote(@NonNull String note) {
        final Fragment noteFragment = getNoteFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, noteFragment, PURCHASE_NOTE_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    protected abstract BasePurchaseAddEditNoteFragment getNoteFragment();

    @Override
    public void showAddEditNoteDialog(@NonNull String note) {
        NoteDialogFragment.display(getSupportFragmentManager(), note);
    }

    @Override
    public void onNoteSet(@NonNull String note) {
        mAddEditPurchaseViewModel.onNoteSet(note);
    }

    @Override
    public void captureImage() {
        if (!CameraUtils.hasCameraHardware(this)) {
            showMessage(R.string.toast_no_camera);
            return;
        }

        if (permissionsAreGranted()) {
            mNavigator.startCamera();
        }
    }

    private boolean permissionsAreGranted() {
        int hasCameraPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (hasCameraPerm != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAPTURE_IMAGES);
            return false;
        }

        return true;
    }

    @Override
    public void showPurchaseScreen() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void setRateFetchStream(@NonNull Single<Float> single,
                                   @NonNull String workerTag) {
        mAddEditPurchaseViewModel.setRateFetchStream(single, workerTag);
    }
}
