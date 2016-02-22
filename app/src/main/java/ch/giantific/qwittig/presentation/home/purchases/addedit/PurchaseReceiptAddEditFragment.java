/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.File;
import java.io.IOException;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.home.purchases.common.PurchaseReceiptBaseFragment;
import ch.giantific.qwittig.presentation.home.purchases.common.PurchaseReceiptViewModel;
import ch.giantific.qwittig.presentation.home.purchases.common.di.DaggerPurchaseReceiptIdComponent;
import ch.giantific.qwittig.presentation.home.purchases.common.di.DaggerPurchaseReceiptPathComponent;
import ch.giantific.qwittig.presentation.home.purchases.common.di.PurchaseReceiptIdViewModelModule;
import ch.giantific.qwittig.presentation.home.purchases.common.di.PurchaseReceiptPathViewModelModule;
import ch.giantific.qwittig.utils.CameraUtils;

/**
 * Shows the receipt image taken by the user when adding a new purchase.
 * <p/>
 * Subclass of {@link PurchaseReceiptBaseFragment}.
 */
public class PurchaseReceiptAddEditFragment extends PurchaseReceiptBaseFragment<PurchaseReceiptViewModel, PurchaseReceiptAddEditFragment.ActivityListener> {

    private static final String KEY_RECEIPT_IMAGE_PATH = "RECEIPT_IMAGE_PATH";
    private static final String KEY_PURCHASE_ID = "PURCHASE_ID";
    private static final String KEY_DRAFT = "DRAFT";
    private static final int INTENT_REQUEST_IMAGE_CAPTURE = 1;
    private String mReceiptImagePath;

    public PurchaseReceiptAddEditFragment() {
    }

    /**
     * Returns a new instance of {@link PurchaseReceiptAddEditFragment}.
     *
     * @param imagePath the path to the receipt image taken
     * @return a new instance of {@link PurchaseReceiptAddEditFragment}
     */
    @NonNull
    public static PurchaseReceiptAddEditFragment newAddInstance(@NonNull String imagePath) {
        final PurchaseReceiptAddEditFragment purchaseReceiptAddFragment = new PurchaseReceiptAddEditFragment();
        final Bundle args = new Bundle();
        args.putString(KEY_RECEIPT_IMAGE_PATH, imagePath);
        purchaseReceiptAddFragment.setArguments(args);
        return purchaseReceiptAddFragment;
    }

    /**
     * Returns a new instance of {@link PurchaseReceiptAddEditFragment}.
     *
     * @param purchaseId the object id of the purchase of which the receipt image should be shown
     * @param isDraft    whether the purchase is a draft or not
     * @return a new instance of {@link PurchaseReceiptAddEditFragment}
     */
    @NonNull
    public static PurchaseReceiptAddEditFragment newEditInstance(@NonNull String purchaseId,
                                                                 boolean isDraft) {
        final PurchaseReceiptAddEditFragment fragment = new PurchaseReceiptAddEditFragment();
        final Bundle args = new Bundle();
        args.putString(KEY_PURCHASE_ID, purchaseId);
        args.putBoolean(KEY_DRAFT, isDraft);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        final Bundle args = getArguments();
        final String receiptImagePath = args.getString(KEY_RECEIPT_IMAGE_PATH, "");
        final String purchaseId = args.getString(KEY_PURCHASE_ID, "");
        final boolean draft = args.getBoolean(KEY_DRAFT, false);

        if (!TextUtils.isEmpty(receiptImagePath)) {
            DaggerPurchaseReceiptPathComponent.builder()
                    .applicationComponent(Qwittig.getAppComponent(getActivity()))
                    .purchaseReceiptPathViewModelModule(new PurchaseReceiptPathViewModelModule(savedInstanceState, this, receiptImagePath))
                    .build()
                    .inject(this);
        } else {
            DaggerPurchaseReceiptIdComponent.builder()
                    .applicationComponent(Qwittig.getAppComponent(getActivity()))
                    .purchaseReceiptIdViewModelModule(new PurchaseReceiptIdViewModelModule(savedInstanceState, this, purchaseId, draft))
                    .build()
                    .inject(this);
        }
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
     * <p/>
     * TODO: check permissions if we decide to use custom camera!
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
