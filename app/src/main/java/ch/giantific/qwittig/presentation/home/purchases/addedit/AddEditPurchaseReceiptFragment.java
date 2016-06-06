/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.File;
import java.io.IOException;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.home.purchases.common.PurchaseReceiptBaseFragment;
import ch.giantific.qwittig.presentation.home.purchases.common.PurchaseReceiptViewModel;
import ch.giantific.qwittig.presentation.home.purchases.common.di.DaggerPurchaseReceiptComponent;
import ch.giantific.qwittig.presentation.home.purchases.common.di.PurchaseReceiptViewModelModule;
import ch.giantific.qwittig.utils.CameraUtils;

/**
 * Shows the receipt image taken by the user when adding a new purchase.
 * <p/>
 * Subclass of {@link PurchaseReceiptBaseFragment}.
 */
public class AddEditPurchaseReceiptFragment extends PurchaseReceiptBaseFragment<PurchaseReceiptViewModel, AddEditPurchaseReceiptFragment.ActivityListener> {

    private static final String KEY_RECEIPT_IMAGE_URI = "RECEIPT_IMAGE_URI";
    private static final int INTENT_REQUEST_IMAGE_CAPTURE = 1;
    private String mReceiptImagePath;

    public AddEditPurchaseReceiptFragment() {
        // required empty constructor
    }

    /**
     * Returns a new instance of {@link AddEditPurchaseReceiptFragment}.
     *
     * @param receiptImageUri the path to the receipt image taken
     * @return a new instance of {@link AddEditPurchaseReceiptFragment}
     */
    @NonNull
    public static AddEditPurchaseReceiptFragment newInstance(@NonNull String receiptImageUri) {
        final AddEditPurchaseReceiptFragment purchaseReceiptAddFragment = new AddEditPurchaseReceiptFragment();
        final Bundle args = new Bundle();
        args.putString(KEY_RECEIPT_IMAGE_URI, receiptImageUri);
        purchaseReceiptAddFragment.setArguments(args);
        return purchaseReceiptAddFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        final Bundle args = getArguments();
        final String receiptImagePath = args.getString(KEY_RECEIPT_IMAGE_URI, "");

        DaggerPurchaseReceiptComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(getActivity()))
                .purchaseReceiptViewModelModule(new PurchaseReceiptViewModelModule(savedInstanceState, this, receiptImagePath))
                .build()
                .inject(this);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_purchase_edit_receipt_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_purchase_edit_receipt_edit:
                captureImage();
                return true;
            case R.id.action_purchase_edit_receipt_delete:
                mActivity.deleteReceipt();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Checks whether the permissions to take an image are granted and if yes initiates the creation
     * of the image file.
     */
    private void captureImage() {
        if (!CameraUtils.hasCameraHardware(getActivity())) {
            showMessage(R.string.toast_no_camera);
            return;
        }

        final Context context = getActivity();
        final File imageFile;
        try {
            imageFile = CameraUtils.createImageFile(context);
        } catch (IOException e) {
            return;
        }

        mReceiptImagePath = imageFile.getAbsolutePath();
        mViewModel.onReceiptImagePathSet(mReceiptImagePath);
        final Intent cameraIntent = CameraUtils.getCameraIntent(context, imageFile);
        if (cameraIntent != null) {
            startActivityForResult(cameraIntent, INTENT_REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENT_REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                mViewModel.onReceiptImageCaptured();
                mActivity.setReceiptImagePath(mReceiptImagePath);
            }
        }
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     */
    public interface ActivityListener extends PurchaseReceiptBaseFragment.ActivityListener {
        /**
         * Handles the request to delete the receipt image file from online Parse.com database.
         */
        void deleteReceipt();

        /**
         * Communicates the path to a newly taken receipt.
         */
        void setReceiptImagePath(@NonNull String path);
    }
}
