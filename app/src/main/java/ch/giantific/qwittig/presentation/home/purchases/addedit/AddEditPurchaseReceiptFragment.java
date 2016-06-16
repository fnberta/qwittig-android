/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentPurchaseShowReceiptBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.home.CameraActivity;
import ch.giantific.qwittig.presentation.home.purchases.common.PurchaseReceiptViewModel;
import ch.giantific.qwittig.presentation.home.purchases.common.di.DaggerPurchaseReceiptComponent;
import ch.giantific.qwittig.presentation.home.purchases.common.di.PurchaseReceiptViewModelModule;
import ch.giantific.qwittig.utils.CameraUtils;
import ch.giantific.qwittig.utils.MessageAction;
import ch.giantific.qwittig.utils.Utils;

/**
 * Shows the receipt image taken by the user when adding a new purchase.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class AddEditPurchaseReceiptFragment extends BaseFragment<PurchaseReceiptViewModel, AddEditPurchaseReceiptFragment.ActivityListener>
        implements PurchaseReceiptViewModel.ViewListener {

    private static final String KEY_RECEIPT_IMAGE_URI = "RECEIPT_IMAGE_URI";
    private static final int INTENT_REQUEST_IMAGE_CAPTURE = 1;
    private static final int PERMISSIONS_REQUEST_CAPTURE_IMAGES = 1;
    private FragmentPurchaseShowReceiptBinding mBinding;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentPurchaseShowReceiptBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
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
                mViewModel.onEditReceiptMenuClick();
                return true;
            case R.id.action_purchase_edit_receipt_delete:
                mViewModel.onDeleteReceiptMenuClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void setViewModelToActivity() {
        // do nothing
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.ivReceipt;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_IMAGE_CAPTURE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        final String imagePath = data.getStringExtra(CameraActivity.INTENT_EXTRA_IMAGE_PATH);
                        mViewModel.onReceiptImageTaken(imagePath);
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
                    getImage();
                } else {
                    showMessageWithAction(R.string.snackbar_permission_storage_denied,
                            new MessageAction(R.string.snackbar_action_open_settings) {
                                @Override
                                public void onClick(View v) {
                                    startSystemSettings();
                                }
                            });
                }

                break;
        }
    }

    private void startSystemSettings() {
        final Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
        startActivity(intent);
    }

    @Override
    public void captureImage() {
        if (!CameraUtils.hasCameraHardware(getActivity())) {
            showMessage(R.string.toast_no_camera);
            return;
        }

        if (permissionsAreGranted()) {
            getImage();
        }
    }

    private boolean permissionsAreGranted() {
        int hasCameraPerm = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
        if (hasCameraPerm != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAPTURE_IMAGES);
            return false;
        }

        return true;
    }

    private void getImage() {
        final Intent intent = new Intent(getActivity(), CameraActivity.class);
        startActivityForResult(intent, INTENT_REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void showPurchaseScreen() {
        mActivity.popBackStack();
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener {

        void popBackStack();
    }
}
