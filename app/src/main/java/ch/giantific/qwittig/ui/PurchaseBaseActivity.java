package ch.giantific.qwittig.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.transition.Transition;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.Receipt;
import ch.giantific.qwittig.helper.PurchaseSaveHelper;
import ch.giantific.qwittig.helper.RatesHelper;
import ch.giantific.qwittig.ui.dialogs.AccountCreateDialogFragment;
import ch.giantific.qwittig.ui.dialogs.DatePickerDialogFragment;
import ch.giantific.qwittig.ui.dialogs.ManualExchangeRateDialogFragment;
import ch.giantific.qwittig.ui.dialogs.PermissionDeniedDialogFragment;
import ch.giantific.qwittig.ui.dialogs.PurchaseUserSelectionDialogFragment;
import ch.giantific.qwittig.ui.dialogs.StoreSelectionDialogFragment;
import ch.giantific.qwittig.ui.widgets.TransitionListenerAdapter;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Created by fabio on 13.12.14.
 */
public abstract class PurchaseBaseActivity extends BaseActivity implements
        PurchaseBaseFragment.FragmentInteractionListener,
        DatePickerDialog.OnDateSetListener,
        PurchaseUserSelectionDialogFragment.FragmentInteractionListener,
        StoreSelectionDialogFragment.DialogInteractionListener,
        PurchaseReceiptAddEditFragment.FragmentInteractionListener,
        RatesHelper.HelperInteractionListener,
        PurchaseSaveHelper.HelperInteractionListener,
        ManualExchangeRateDialogFragment.DialogInteractionListener,
        PermissionDeniedDialogFragment.DialogInteractionListener {

    @IntDef({PURCHASE_SAVED, PURCHASE_SAVED_AUTO, PURCHASE_DISCARDED, PURCHASE_SAVED_AS_DRAFT,
            PURCHASE_DRAFT_DELETED, PURCHASE_ERROR, PURCHASE_NO_CHANGES})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PurchaseAction {}
    public static final int PURCHASE_SAVED = 0;
    public static final int PURCHASE_SAVED_AUTO = 1;
    public static final int PURCHASE_DISCARDED = 2;
    public static final int PURCHASE_SAVED_AS_DRAFT = 3;
    public static final int PURCHASE_DRAFT_DELETED = 4;
    public static final int PURCHASE_ERROR = 5;
    public static final int PURCHASE_NO_CHANGES = 6;
    public static final int RESULT_PURCHASE_SAVED = 2;
    public static final int RESULT_PURCHASE_SAVED_AUTO = 3;
    public static final int RESULT_PURCHASE_DRAFT = 4;
    public static final int RESULT_PURCHASE_ERROR = 5;
    public static final int RESULT_PURCHASE_DISCARDED = 6;
    public static final int RESULT_PURCHASE_DRAFT_DELETED = 7;
    static final int INTENT_REQUEST_IMAGE_CAPTURE = 1;
    static final String PURCHASE_RECEIPT_FRAGMENT = "purchase_receipt_fragment";

    private static final int PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 1;
    private static final String LOG_TAG = PurchaseBaseActivity.class.getSimpleName();

    PurchaseBaseFragment mPurchaseFragment;
    File mReceiptPhotoFile;
    FloatingActionButton mFabPurchaseSave;
    ParseFile mReceiptParseFile;
    private FABProgressCircle mFabProgressCircle;

    @Override
    public ParseFile getReceiptParseFile() {
        return mReceiptParseFile;
    }

    @Override
    public void setReceiptParseFile(ParseFile receiptParseFile) {
        mReceiptParseFile = receiptParseFile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Utils.isRunningLollipopAndHigher()) {
            setActivityTransition();
        }
        setContentView(R.layout.activity_purchase_add_edit);

        mFabPurchaseSave = (FloatingActionButton) findViewById(R.id.fab_purchase_save);
        mFabPurchaseSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPurchaseFragment.savePurchase(false);
            }
        });
        mFabProgressCircle = (FABProgressCircle) findViewById(R.id.fab_purchase_save_circle);
        mFabProgressCircle.attachListener(new FABProgressListener() {
            @Override
            public void onFABProgressAnimationEnd() {
                finishPurchase();
            }
        });

        if (savedInstanceState == null) {
            if (Utils.isRunningLollipopAndHigher()) {
                addActivityTransitionListener();
            } else {
                showFab();
            }
        }
    }

    /**
     * Slide from Bottom in Add, and slide from right side in Edit
     */
    protected abstract void setActivityTransition();

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void addActivityTransitionListener() {
        Transition enter = getWindow().getEnterTransition();
        enter.addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                super.onTransitionEnd(transition);
                transition.removeListener(this);

                showFab();
            }
        });
    }

    public void showFab() {
        showFab(false);
    }

    @Override
    public void showFab(final boolean isSaving) {
        mFabPurchaseSave.show();
        if (isSaving) {
            mFabProgressCircle.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        findPurchaseFragment();
    }

    protected abstract void findPurchaseFragment();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_purchase_add_edit, menu);

        if (mReceiptParseFile != null) {
            menu.findItem(R.id.action_purchase_add_edit_receipt_show).setVisible(true);
            menu.findItem(R.id.action_purchase_add_edit_receipt_add).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_purchase_add_edit_receipt_show:
                showReceiptFragment();
                return true;
            case R.id.action_purchase_add_edit_receipt_add:
                captureImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void captureImage() {
        if (permissionsAreGranted()) {
            getImage();
        }
    }

    private void getImage() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                mReceiptPhotoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                setResultForSnackbar(PURCHASE_ERROR);
                finishPurchase();
            }
            if (mReceiptPhotoFile != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mReceiptPhotoFile));
                startActivityForResult(cameraIntent, INTENT_REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private boolean permissionsAreGranted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            String[] permissionsToGrant = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, permissionsToGrant,
                    PERMISSIONS_REQUEST_EXTERNAL_STORAGE);

            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImage();
                } else {
                    PermissionDeniedDialogFragment permissionDeniedDialogFragment =
                            PermissionDeniedDialogFragment.newInstance(
                            getString(R.string.dialog_message_permission_storage_denied));
                    permissionDeniedDialogFragment.show(getFragmentManager(), "permission_denied");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Callback from permission denied dialog when user decides to launch system settings.
     */
    @Override
    public void startSystemSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENT_REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                getReceiptFile();
                updateReceiptFragment();
            }
        }
    }

    private void getReceiptFile() {
        Glide.with(this).load(mReceiptPhotoFile)
                .asBitmap()
                .toBytes(Bitmap.CompressFormat.JPEG, Receipt.JPEG_COMPRESSION_RATE)
                .centerCrop()
                .into(new SimpleTarget<byte[]>(Receipt.WIDTH, Receipt.HEIGHT) {
                    @Override
                    public void onResourceReady(byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                        mReceiptParseFile = new ParseFile(Receipt.PARSE_FILE_NAME, resource);
                    }
                });
    }

    private void updateReceiptFragment() {
        PurchaseReceiptAddEditFragment purchaseReceiptAddEditFragment =
                (PurchaseReceiptAddEditFragment) getFragmentManager().findFragmentByTag(PURCHASE_RECEIPT_FRAGMENT);

        if (purchaseReceiptAddEditFragment != null) {
            purchaseReceiptAddEditFragment.updateReceiptImage(mReceiptPhotoFile);
        }
    }

    @Override
    public void showDatePickerDialog() {
        DatePickerDialogFragment datePickerDialogFragment = new DatePickerDialogFragment();
        datePickerDialogFragment.show(getFragmentManager(), "date_picker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Date date = DateUtils.parseDateFromPicker(year, month, day);
        mPurchaseFragment.setDate(date);
    }

    @Override
    public void showStorePickerDialog(String defaultStore) {
        StoreSelectionDialogFragment storeSelectionDialogFragment = StoreSelectionDialogFragment
                .newInstance(defaultStore);
        storeSelectionDialogFragment.show(getFragmentManager(), "store_selector");
    }

    @Override
    public void setStore(String store, boolean manuallyEntered) {
        mPurchaseFragment.setStore(store, manuallyEntered);
    }

    @Override
    public void showUserPickerDialog(CharSequence[] usersAvailable, boolean[] usersChecked) {
        PurchaseUserSelectionDialogFragment purchaseUserSelectionDialogFragment =
                PurchaseUserSelectionDialogFragment.newInstance(usersAvailable, usersChecked);
        purchaseUserSelectionDialogFragment.show(getFragmentManager(), "user_picker");
    }

    @Override
    public void onItemUsersInvolvedSet(List<Integer> usersInvolved) {
        mPurchaseFragment.setItemUsersInvolved(usersInvolved);
    }

    @Override
    public void showAccountCreateDialog() {
        AccountCreateDialogFragment accountCreateDialogFragment =
                new AccountCreateDialogFragment();
        accountCreateDialogFragment.show(getFragmentManager(), "account_create");
    }

    @Override
    public void showReceiptFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        PurchaseReceiptAddEditFragment purchaseReceiptAddEditFragment =
                new PurchaseReceiptAddEditFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.container, purchaseReceiptAddEditFragment, PURCHASE_RECEIPT_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void deleteReceipt() {
        mReceiptParseFile = null;
        getFragmentManager().popBackStack();
        invalidateOptionsMenu();
        MessageUtils.showBasicSnackbar(mFabPurchaseSave, getString(R.string.toast_receipt_deleted));
    }

    @Override
    public void showManualExchangeRateSelectorDialog(String exchangeRate) {
        ManualExchangeRateDialogFragment manualExchangeRateDialogFragment =
                ManualExchangeRateDialogFragment.newInstance(exchangeRate);
        manualExchangeRateDialogFragment.show(getFragmentManager(), "manual_exchange_rate");
    }

    @Override
    public void setExchangeRate(float exchangeRate) {
        mPurchaseFragment.setExchangeRateManual(exchangeRate);
    }

    @Override
    public void onRatesFetchSuccessful(Map<String, Float> exchangeRates) {
        mPurchaseFragment.onRatesFetchSuccessful(exchangeRates);
    }

    @Override
    public void onRatesFetchFailed(String errorMessage) {
        mPurchaseFragment.onRatesFetchFailed(errorMessage);
    }

    @Override
    public void onPurchaseSaveAndPinSucceeded() {
        mPurchaseFragment.onPurchaseSaveAndPinSucceeded();
    }

    @Override
    public void onPurchaseSaveFailed(ParseException e) {
        mPurchaseFragment.onPurchaseSaveFailed(e);
    }

    @Override
    public void progressCircleShow() {
        mFabProgressCircle.show();
    }

    @Override
    public void progressCircleStartFinal() {
        mFabProgressCircle.beginFinalAnimation();
    }

    @Override
    public void progressCircleHide() {
        mFabProgressCircle.hide();
    }

    @Override
    public void finishPurchase() {
        ActivityCompat.finishAfterTransition(this);
        if (mReceiptPhotoFile != null) {
            boolean fileWasDeleted = mReceiptPhotoFile.delete();
            if (!fileWasDeleted) {
                Log.e(LOG_TAG, "could not delete file");
            }
        }
    }

    @Override
    public final void setResultForSnackbar(@PurchaseAction int purchaseAction) {
        switch (purchaseAction) {
            case PURCHASE_SAVED:
                setResult(RESULT_PURCHASE_SAVED);
                break;
            case PURCHASE_SAVED_AUTO:
                setResult(RESULT_PURCHASE_SAVED_AUTO);
                break;
            case PURCHASE_DISCARDED:
                setResult(RESULT_PURCHASE_DISCARDED);
                break;
            case PURCHASE_SAVED_AS_DRAFT:
                setResult(RESULT_PURCHASE_DRAFT);
                break;
            case PURCHASE_DRAFT_DELETED:
                setResult(RESULT_PURCHASE_DRAFT_DELETED);
                break;
            case PURCHASE_ERROR:
                setResult(RESULT_PURCHASE_ERROR);
                break;
            case PURCHASE_NO_CHANGES:
                setResult(RESULT_CANCELED);
                break;
        }
    }
}
