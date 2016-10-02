/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import ch.giantific.qwittig.presentation.camera.CameraContract.CameraResult;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.MessageAction;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.dialogs.DatePickerDialogFragment;
import ch.giantific.qwittig.presentation.common.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.listadapters.TabsAdapter;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.dialogs.DiscardPurchaseDialogFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.dialogs.ExchangeRateDialogFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.dialogs.NoteDialogFragment;
import ch.giantific.qwittig.utils.CameraUtils;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.Utils;
import ch.giantific.qwittig.utils.rxwrapper.android.RxAndroidViews;
import ch.giantific.qwittig.utils.rxwrapper.android.transitions.TransitionEvent;
import rx.Observable;
import rx.Single;
import rx.subjects.ReplaySubject;

/**
 * Hosts {@link PurchaseAddFragment} that handles the creation of a new purchase.
 * <p/>
 * Asks the user if he wants to discard the new purchase when dismissing the activity.
 */
public abstract class BasePurchaseAddEditActivity<T> extends BaseActivity<T> implements
        BasePurchaseAddEditFragment.ActivityListener<T>,
        PurchaseAddEditContract.ViewListener,
        RatesWorkerListener, DatePickerDialog.OnDateSetListener,
        NoteDialogFragment.DialogInteractionListener,
        ExchangeRateDialogFragment.DialogInteractionListener,
        DiscardPurchaseDialogFragment.DialogInteractionListener {

    private static final int RC_CAPTURE_IMAGES = 12;
    protected final ReplaySubject<TransitionEvent> transitionSubject = ReplaySubject.create();
    protected PurchaseAddEditContract.Presenter presenter;
    @Inject
    protected Navigator navigator;
    private ActivityPurchaseAddEditBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_purchase_add_edit);
        binding.setPresenter(presenter);

        setupTabs();
        handleEnterTransition(savedInstanceState);
    }

    @Override
    protected List<BasePresenter> getPresenters() {
        return Arrays.asList(new BasePresenter[]{presenter});
    }

    private void setupTabs() {
        final TabsAdapter tabsAdapter = new TabsAdapter(getSupportFragmentManager());
        tabsAdapter.addInitialFragment(getPurchaseAddEditFragment(), getString(R.string.tab_details_purchase));
        tabsAdapter.addInitialFragment(getReceiptFragment(), getString(R.string.tab_details_receipt));
        binding.viewpager.setAdapter(tabsAdapter);
    }

    protected abstract void handleEnterTransition(@Nullable Bundle savedInstanceState);

    @NonNull
    protected abstract BasePurchaseAddEditFragment getPurchaseAddEditFragment();

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_purchase_add_edit, menu);

        if (presenter.getViewModel().isNoteAvailable()) {
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
                presenter.onExitClick();
                return true;
            case R.id.action_purchase_add_edit_note_edit:
                // fall through
            case R.id.action_purchase_add_edit_note_add:
                presenter.onAddEditNoteMenuClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Navigator.RC_IMAGE_CAPTURE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        final String imagePath = data.getStringExtra(Navigator.EXTRA_GENERIC_STRING);
                        presenter.onReceiptImageTaken(imagePath);
                        break;
                    case CameraResult.ERROR:
                        presenter.onReceiptImageTakeFailed();
                        break;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case RC_CAPTURE_IMAGES:
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
            presenter.onExitClick();
        }
    }

    @Override
    public Observable<TransitionEvent> getEnterTransition() {
        return transitionSubject.asObservable();
    }

    protected void dispatchFakeEnterTransitionEnd() {
        transitionSubject.onNext(TransitionEvent.createEmptyEnd());
        transitionSubject.onCompleted();
    }

    @Override
    public Single<FloatingActionButton> showFab() {
        return RxAndroidViews.getFabVisibilityChange(binding.fabPurchaseSave);
    }

    @Override
    public void onDiscardPurchaseSelected() {
        presenter.onDiscardChangesSelected();
    }

    @Override
    public void onSaveAsDraftSelected() {
        presenter.onSaveAsDraftMenuClick();
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
        presenter.onDateSet(date);
    }

    @Override
    public void showManualExchangeRateSelectorDialog(@NonNull String exchangeRate) {
        ExchangeRateDialogFragment.display(getSupportFragmentManager(), exchangeRate);
    }

    @Override
    public void onExchangeRateManuallySet(double exchangeRate) {
        presenter.onExchangeRateManuallySet(exchangeRate);
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
        presenter.onNoteSet(note);
    }

    @Override
    public void onDeleteNote() {
        presenter.onDeleteNote();
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
                    RC_CAPTURE_IMAGES);
            return false;
        }

        return true;
    }

    @Override
    public void reloadOptionsMenu() {
        invalidateOptionsMenu();
    }

    @Override
    public void setRateFetchStream(@NonNull Single<Float> single,
                                   @NonNull String workerTag) {
        presenter.setRateFetchStream(single, workerTag);
    }
}
