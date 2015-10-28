/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.io.IOException;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.utils.CameraUtils;
import ch.giantific.qwittig.utils.MessageUtils;

/**
 * Shows the receipt image taken by the user when adding a new purchase.
 * <p/>
 * Subclass of {@link PurchaseReceiptBaseFragment}.
 */
public class PurchaseReceiptAddFragment extends PurchaseReceiptBaseFragment {

    private static final String BUNDLE_IMAGE_PATH = "BUNDLE_IMAGE_PATH";
    private static final int INTENT_REQUEST_IMAGE_CAPTURE = 1;
    private FragmentInteractionListener mListener;
    private String mReceiptPath;

    public PurchaseReceiptAddFragment() {
    }

    /**
     * Returns a new instance of {@link PurchaseReceiptAddFragment}.
     *
     * @param imagePath the path to the receipt image taken
     * @return a new instance of {@link PurchaseReceiptAddFragment}
     */
    @NonNull
    public static PurchaseReceiptAddFragment newInstance(@NonNull String imagePath) {
        PurchaseReceiptAddFragment purchaseReceiptAddFragment = new PurchaseReceiptAddFragment();
        Bundle args = new Bundle();
        args.putString(BUNDLE_IMAGE_PATH, imagePath);
        purchaseReceiptAddFragment.setArguments(args);
        return purchaseReceiptAddFragment;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        getDataFromBundle();
    }

    void getDataFromBundle() {
        Bundle args = getArguments();
        if (args != null) {
            mReceiptPath = args.getString(BUNDLE_IMAGE_PATH, "");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setData();
    }

    void setData() {
        setReceiptImage(mReceiptPath);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_purchase_edit_receipt, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_purchase_edit_receipt_edit:
                captureImage();
                return true;
            case R.id.action_purchase_edit_receipt_delete:
                mListener.deleteReceipt();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Checks whether the permissions to take an image are granted and if yes initiates the creation
     * of the image file.
     *
     * TODO: check permissions if we decide to use custom camera!
     */
    public void captureImage() {
        if (!CameraUtils.hasCameraHardware(getActivity())) {
            MessageUtils.showBasicSnackbar(mImageViewReceipt, getString(R.string.toast_no_camera));
            return;
        }

        final Context context = getActivity();
        File imageFile;
        try {
            imageFile = CameraUtils.createImageFile(context);
        } catch (IOException e) {
            return;
        }

        mReceiptPath = imageFile.getAbsolutePath();
        Intent cameraIntent = CameraUtils.getCameraIntent(context, imageFile);
        if (cameraIntent != null) {
            startActivityForResult(cameraIntent, INTENT_REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENT_REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                setImage(mReceiptPath);
                mListener.setReceiptImagePath(mReceiptPath);
                MessageUtils.showBasicSnackbar(mImageViewReceipt, getString(R.string.toast_receipt_changed));
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     */
    public interface FragmentInteractionListener {
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
