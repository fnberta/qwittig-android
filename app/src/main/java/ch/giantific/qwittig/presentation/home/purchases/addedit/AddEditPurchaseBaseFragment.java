/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.IOException;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentPurchaseAddEditBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;
import ch.giantific.qwittig.presentation.common.fragments.DatePickerDialogFragment;
import ch.giantific.qwittig.presentation.common.fragments.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.AddEditPurchaseBaseItem.Type;
import ch.giantific.qwittig.presentation.home.purchases.common.PurchaseReceiptBaseFragment;
import ch.giantific.qwittig.utils.CameraUtils;

/**
 * Displays the interface where the user can add a new purchase by setting store, date, users
 * involved and the different items.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
 */
public abstract class AddEditPurchaseBaseFragment<T extends AddEditPurchaseViewModel, S extends AddEditPurchaseBaseFragment.ActivityListener>
        extends BaseRecyclerViewFragment<T, S>
        implements AddEditPurchaseViewModel.ViewListener {

    private static final int INTENT_REQUEST_IMAGE_CAPTURE = 1;
    private static final String PURCHASE_NOTE_FRAGMENT = "PURCHASE_NOTE_FRAGMENT";
    private static final String PURCHASE_RECEIPT_FRAGMENT = "PURCHASE_RECEIPT_FRAGMENT";
    private FragmentPurchaseAddEditBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentPurchaseAddEditBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                mViewModel.onItemDismiss(viewHolder.getAdapterPosition());
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                final int position = viewHolder.getAdapterPosition();
                if (mViewModel.getItemViewType(position) != Type.ITEM) {
                    return 0;
                }

                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        });
        touchHelper.attachToRecyclerView(mBinding.rvPurchaseAddEdit);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_IMAGE_CAPTURE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mViewModel.onReceiptImageTaken();
                        break;
                }
                break;
        }
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.rvPurchaseAddEdit;
    }

    @Override
    protected RecyclerView.Adapter getRecyclerAdapter() {
        return new AddEditPurchaseRecyclerAdapter(mViewModel);
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setAddEditViewModel(mViewModel);
    }

    @Override
    public void loadFetchExchangeRatesWorker(@NonNull String baseCurrency, @NonNull String currency) {
        RatesWorker.attach(getFragmentManager(), baseCurrency, currency);
    }

    @Override
    public void showDatePickerDialog() {
        DatePickerDialogFragment.display(getFragmentManager());
    }

    @Override
    public void showManualExchangeRateSelectorDialog(@NonNull String exchangeRate) {
        ExchangeRateDialogFragment.display(getFragmentManager(), exchangeRate);
    }

    @Override
    public void showPurchaseDiscardDialog() {
        DiscardPurchaseDialogFragment.display(getFragmentManager());
    }

    @Override
    public void showDiscardEditChangesDialog() {
        DiscardChangesDialogFragment.display(getFragmentManager());
    }

    @Override
    public void toggleReceiptMenuOption(boolean show) {
        mActivity.toggleReceiptMenuOption(show);
    }

    @Override
    public void toggleNoteMenuOption(boolean show) {
        mActivity.toggleNoteMenuOption(show);
    }

    @Override
    public void showReceiptImage(@NonNull String receiptImagePath) {
        final PurchaseReceiptBaseFragment fragment =
                AddEditPurchaseReceiptFragment.newInstance(receiptImagePath);

        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, PURCHASE_RECEIPT_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showNote(@NonNull String note) {
        final AddEditPurchaseNoteFragment noteFragment = AddEditPurchaseNoteFragment.newInstance(note);

        getFragmentManager().beginTransaction()
                .replace(R.id.container, noteFragment, PURCHASE_NOTE_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showAddEditNoteDialog(@NonNull String note) {
        NoteDialogFragment.display(getFragmentManager(), note);
    }

    @Override
    public void captureImage() {
        if (!CameraUtils.hasCameraHardware(getActivity())) {
            showMessage(R.string.toast_no_camera);
            return;
        }

        final Context context = getActivity();
        final File imageFile;
        try {
            imageFile = CameraUtils.createImageFile(context);
        } catch (IOException e) {
            mViewModel.onReceiptImageTakeFailed();
            return;
        }

        mViewModel.onReceiptImagePathSet(imageFile.getAbsolutePath());
        final Intent cameraIntent = CameraUtils.getCameraIntent(context, imageFile);
        if (cameraIntent != null) {
            startActivityForResult(cameraIntent, INTENT_REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void finishScreen(int purchaseResult) {
        final Activity activity = getActivity();
        activity.setResult(purchaseResult);
        ActivityCompat.finishAfterTransition(activity);
    }

    /**
     * Defines the interaction with the hosting activity.
     */
    public interface ActivityListener extends BaseRecyclerViewFragment.ActivityListener {
        /**
         * Sets the view model to the activity.
         *
         * @param viewModel the view model to set
         */
        void setAddEditViewModel(@NonNull AddEditPurchaseViewModel viewModel);

        /**
         * Handles the update of action bar menu of the activity regarding the receipt file menu
         * option.
         *
         * @param show whether to show the option to show the receipt file or not
         */
        void toggleReceiptMenuOption(boolean show);

        /**
         * Handles the update of action bar menu of the activity regarding the note  menu option.
         *
         * @param show whether to show the option to show the note or not
         */
        void toggleNoteMenuOption(boolean show);
    }
}
