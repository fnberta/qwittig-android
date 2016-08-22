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
import ch.giantific.qwittig.databinding.ActivityPurchaseAddEditBinding;
import ch.giantific.qwittig.presentation.camera.CameraActivity;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.MessageAction;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.TransitionListenerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.TabsAdapter;
import ch.giantific.qwittig.presentation.common.fragments.dialogs.DatePickerDialogFragment;
import ch.giantific.qwittig.presentation.common.fragments.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddFragment;
import ch.giantific.qwittig.utils.CameraUtils;
import ch.giantific.qwittig.utils.DateUtils;
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

    private static final int PERMISSIONS_REQUEST_CAPTURE_IMAGES = 12;

    protected PurchaseAddEditViewModel addEditViewModel;
    @Inject
    protected Navigator navigator;
    private ActivityPurchaseAddEditBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_purchase_add_edit);
        binding.setViewModel(addEditViewModel);

        setupTabs();

        if (savedInstanceState == null) {
            if (Utils.isRunningLollipopAndHigher()) {
                addActivityTransitionListener();
            } else {
                showFab();
            }
        } else {
            showFab();
        }

    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{addEditViewModel});
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
        if (ViewCompat.isLaidOut(binding.fabPurchaseSave)) {
            binding.fabPurchaseSave.show();
        } else {
            binding.fabPurchaseSave.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(@NonNull View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    binding.fabPurchaseSave.show();
                }
            });
        }
    }

    private void setupTabs() {
        final TabsAdapter tabsAdapter = new TabsAdapter(getSupportFragmentManager());
        tabsAdapter.addInitialFragment(getPurchaseAddEditFragment(), getString(R.string.tab_details_purchase));
        tabsAdapter.addInitialFragment(getReceiptFragment(), getString(R.string.tab_details_receipt));
        binding.viewpager.setAdapter(tabsAdapter);
    }

    @NonNull
    protected abstract BasePurchaseAddEditFragment getPurchaseAddEditFragment();

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_purchase_add_edit, menu);

        if (addEditViewModel.isNoteAvailable()) {
            menu.findItem(R.id.action_purchase_add_edit_note_edit).setVisible(true);
            menu.findItem(R.id.action_purchase_add_edit_note_add).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                addEditViewModel.onExitClick();
                return true;
            case R.id.action_purchase_add_edit_note_edit:
                // fall through
            case R.id.action_purchase_add_edit_note_add:
                addEditViewModel.onAddEditNoteMenuClick();
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
                        addEditViewModel.onReceiptImageTaken(imagePath);
                        break;
                    case CameraActivity.RESULT_ERROR:
                        addEditViewModel.onReceiptImageTakeFailed();
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
                    navigator.startCamera();
                } else {
                    showMessageWithAction(R.string.snackbar_permission_storage_denied,
                            new MessageAction(R.string.snackbar_action_open_settings) {
                                @Override
                                public void onClick(View v) {
                                    navigator.startSystemSettings();
                                }
                            });
                }

                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (!getSupportFragmentManager().popBackStackImmediate()) {
            addEditViewModel.onExitClick();
        }
    }

    @Override
    public void onDiscardPurchaseSelected() {
        addEditViewModel.onDiscardChangesSelected();
    }

    @Override
    public void onSaveAsDraftSelected() {
        addEditViewModel.onSaveAsDraftMenuClick();
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
        addEditViewModel.onDateSet(date);
    }

    @Override
    public void showManualExchangeRateSelectorDialog(@NonNull String exchangeRate) {
        ExchangeRateDialogFragment.display(getSupportFragmentManager(), exchangeRate);
    }

    @Override
    public void onExchangeRateManuallySet(double exchangeRate) {
        addEditViewModel.onExchangeRateManuallySet(exchangeRate);
    }

    @Override
    public void showPurchaseDiscardDialog() {
        DiscardPurchaseDialogFragment.display(getSupportFragmentManager());
    }

    @Override
    public void showDiscardEditChangesDialog() {
        DiscardChangesDialogFragment.display(getSupportFragmentManager());
    }

    protected abstract BasePurchaseAddEditReceiptFragment getReceiptFragment();

    @Override
    public void showAddEditNoteDialog(@NonNull String note) {
        NoteDialogFragment.display(getSupportFragmentManager(), note);
    }

    @Override
    public void onNoteSet(@NonNull String note) {
        addEditViewModel.onNoteSet(note);
    }

    @Override
    public void onDeleteNote() {
        addEditViewModel.onDeleteNote();
    }

    @Override
    public void captureImage() {
        if (!CameraUtils.hasCameraHardware(this)) {
            showMessage(R.string.toast_no_camera);
            return;
        }

        if (permissionsAreGranted()) {
            navigator.startCamera();
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
    public void showPurchaseItems() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void reloadOptionsMenu() {
        invalidateOptionsMenu();
    }

    @Override
    public void setRateFetchStream(@NonNull Single<Float> single,
                                   @NonNull String workerTag) {
        addEditViewModel.setRateFetchStream(single, workerTag);
    }
}
