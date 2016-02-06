/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.parse.ParseFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ch.berta.fabio.fabprogress.FabProgress;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentPurchaseAddEditBinding;
import ch.giantific.qwittig.domain.models.MessageAction;
import ch.giantific.qwittig.domain.models.Receipt;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;
import ch.giantific.qwittig.presentation.common.fragments.DatePickerDialogFragment;
import ch.giantific.qwittig.presentation.common.fragments.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.home.purchases.PurchaseReceiptBaseFragment;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseAddEditItem.Type;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseAddEditViewModel.PurchaseResult;
import ch.giantific.qwittig.utils.CameraUtils;
import ch.giantific.qwittig.utils.Utils;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Displays the interface where the user can add a new purchase by setting store, date, users
 * involved and the different items.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
 */
public abstract class PurchaseAddEditBaseFragment<T extends PurchaseAddEditViewModel, S extends PurchaseAddEditBaseFragment.ActivityListener>
        extends BaseRecyclerViewFragment<T, S>
        implements PurchaseAddEditViewModel.ViewListener {

    public static final String PURCHASE_NOTE_FRAGMENT = "PURCHASE_NOTE_FRAGMENT";
    static final String KEY_EDIT_PURCHASE_ID = "EDIT_PURCHASE_ID";
    private static final int PERMISSIONS_REQUEST_CAPTURE_IMAGES = 1;
    private static final int INTENT_REQUEST_IMAGE_CAPTURE = 1;
    private static final int INTENT_REQUEST_IMAGE_CAPTURE_CUSTOM = 2;
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
                mViewModel.onItemDismissed(viewHolder.getAdapterPosition());
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            if (!mViewModel.isLoading()) {
                mActivity.showFab();
            }
        }
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
                    case Activity.RESULT_CANCELED:
                        mViewModel.onReceiptImageFailed();
                        break;
                }
                break;
            case INTENT_REQUEST_IMAGE_CAPTURE_CUSTOM:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        final List<String> paths = data.getStringArrayListExtra(CameraActivity.INTENT_EXTRA_PATHS);
                        mViewModel.onReceiptImagesTaken(paths);
                        break;
                    case Activity.RESULT_CANCELED:
                        mViewModel.onReceiptImageFailed();
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
        return new PurchaseAddEditRecyclerAdapter(mViewModel);
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setAddEditViewModel(mViewModel);
    }

    @Override
    public void showOptionsMenu() {
        setMenuVisibility(true);
    }

    @Override
    public Single<byte[]> getReceiptImage(@NonNull final String imagePath) {
        final PurchaseAddEditBaseFragment fragment = this;
        return Single.create(new Single.OnSubscribe<byte[]>() {
            @Override
            public void call(final SingleSubscriber<? super byte[]> singleSubscriber) {
                Glide.with(fragment).load(imagePath)
                        .asBitmap()
                        .toBytes(Bitmap.CompressFormat.JPEG, Receipt.JPEG_COMPRESSION_RATE)
                        .centerCrop()
                        .into(new SimpleTarget<byte[]>(Receipt.WIDTH, Receipt.HEIGHT) {
                            @Override
                            public void onResourceReady(@NonNull byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                                if (!singleSubscriber.isUnsubscribed()) {
                                    singleSubscriber.onSuccess(resource);
                                }
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                super.onLoadFailed(e, errorDrawable);

                                if (!singleSubscriber.isUnsubscribed()) {
                                    singleSubscriber.onError(e);
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void loadFetchExchangeRatesWorker(@NonNull String baseCurrency, @NonNull String currency) {
        RatesWorker.attach(getFragmentManager(), baseCurrency, currency);
    }

    @Override
    public void loadOcrWorker(@NonNull String receiptImagePath) {
        OcrWorker.attach(getFragmentManager(), receiptImagePath);
    }

    @Override
    public void loadSavePurchaseWorker(@NonNull Purchase purchase, @Nullable byte[] receiptImage) {
        PurchaseSaveWorker.attach(getFragmentManager(), purchase, receiptImage);
    }

    @Override
    public void loadSavePurchaseWorker(@NonNull Purchase purchase, @Nullable byte[] receiptImage,
                                       @Nullable ParseFile receiptFileOld,
                                       boolean deleteOldReceipt, boolean draft) {
        PurchaseEditSaveWorker.attach(getFragmentManager(), purchase, receiptImage, receiptFileOld,
                deleteOldReceipt, draft);
    }

    @Override
    public void showDatePickerDialog() {
        DatePickerDialogFragment.display(getFragmentManager());
    }

    @Override
    public void showManualExchangeRateSelectorDialog(@NonNull String exchangeRate) {
        PurchaseExchangeRateDialogFragment.display(getFragmentManager(), exchangeRate);
    }

    @Override
    public void showPurchaseDiscardDialog() {
        PurchaseDiscardDialogFragment.display(getFragmentManager());
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
        final FragmentManager fragmentManager = getFragmentManager();
        final PurchaseReceiptBaseFragment fragment =
                PurchaseReceiptAddEditFragment.newAddInstance(receiptImagePath);

        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, PURCHASE_RECEIPT_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showReceiptImage(@NonNull String objectId,
                                 @NonNull String receiptImagePath, boolean isDraft) {
        final FragmentManager fragmentManager = getFragmentManager();
        final PurchaseReceiptBaseFragment fragment = !TextUtils.isEmpty(receiptImagePath)
                ? PurchaseReceiptAddEditFragment.newAddInstance(receiptImagePath)
                : PurchaseReceiptAddEditFragment.newEditInstance(objectId, isDraft);

        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, PURCHASE_RECEIPT_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showNote(@NonNull String note) {
        final FragmentManager fragmentManager = getFragmentManager();
        final PurchaseNoteFragment noteFragment = PurchaseNoteFragment.newInstance(note);

        fragmentManager.beginTransaction()
                .replace(R.id.container, noteFragment, PURCHASE_NOTE_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showAddEditNoteDialog(@NonNull String note) {
        PurchaseNoteDialogFragment.display(getFragmentManager(), note);
    }

    @Override
    public void captureImage(boolean useCustomCamera) {
        if (!CameraUtils.hasCameraHardware(getActivity())) {
            showMessage(R.string.toast_no_camera);
            return;
        }

        if (permissionsAreGranted(useCustomCamera)) {
            getImage(useCustomCamera);
        }
    }

    private boolean permissionsAreGranted(boolean useCustomCamera) {
        if (useCustomCamera) {
            int hasCameraPerm = ContextCompat.checkSelfPermission(
                    getActivity(), Manifest.permission.CAMERA);
            if (hasCameraPerm != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        PERMISSIONS_REQUEST_CAPTURE_IMAGES);
                return false;
            }

            return true;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAPTURE_IMAGES:
                if (Utils.verifyPermissions(grantResults)) {
                    getImage(true);
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
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startSystemSettings() {
        final Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
        startActivity(intent);
    }

    private void getImage(boolean useCustomCamera) {
        if (useCustomCamera) {
            final Intent intent = new Intent(getActivity(), CameraActivity.class);
            startActivityForResult(intent, INTENT_REQUEST_IMAGE_CAPTURE);
        } else {
            final Context context = getActivity();
            File imageFile;
            try {
                imageFile = CameraUtils.createImageFile(context);
            } catch (IOException e) {
                finishScreen(PurchaseResult.PURCHASE_ERROR);
                return;
            }

            mViewModel.onReceiptImagePathSet(imageFile.getAbsolutePath());
            final Intent cameraIntent = CameraUtils.getCameraIntent(context, imageFile);
            if (cameraIntent != null) {
                startActivityForResult(cameraIntent, INTENT_REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void startSaveAnim() {
        mActivity.startProgressAnim();
    }

    @Override
    public void stopSaveAnim() {
        mActivity.stopProgressAnim();
    }

    @Override
    public void showSaveFinishedAnim() {
        mActivity.startFinalProgressAnim();
    }

    @Override
    public void finishScreen(int purchaseResult) {
        final Activity activity = getActivity();
        activity.setResult(purchaseResult);
        ActivityCompat.finishAfterTransition(activity);
    }

    public interface ActivityListener extends BaseRecyclerViewFragment.ActivityListener {
        /**
         * Sets the view model to the activity.
         *
         * @param viewModel the view model to set
         */
        void setAddEditViewModel(@NonNull PurchaseAddEditViewModel viewModel);

        /**
         * Indicates that the {@link FloatingActionButton} should be revealed.
         */
        void showFab();

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

        /**
         * Indicates to start the loading animation of the {@link FabProgress}.
         */
        void startProgressAnim();

        /**
         * Indicates to start the final loading animation of the {@link FabProgress}.
         */
        void startFinalProgressAnim();

        /**
         * Indicates to hide the loading animation of the {@link FabProgress}.
         */
        void stopProgressAnim();
    }
}
