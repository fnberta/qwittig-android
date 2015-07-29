package ch.giantific.qwittig.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.transition.Transition;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.parse.ParseFile;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.helper.RatesHelper;
import ch.giantific.qwittig.helper.ReceiptHelper;
import ch.giantific.qwittig.data.models.ImageReceipt;
import ch.giantific.qwittig.ui.dialogs.AccountCreateDialogFragment;
import ch.giantific.qwittig.ui.dialogs.DatePickerDialogFragment;
import ch.giantific.qwittig.ui.dialogs.PurchaseUserSelectionDialogFragment;
import ch.giantific.qwittig.ui.dialogs.StoreSelectionDialogFragment;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.ui.widgets.TransitionListenerAdapter;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Created by fabio on 13.12.14.
 */
public abstract class PurchaseBaseActivity extends BaseActivity implements
        PurchaseBaseFragment.FragmentInteractionListener,
        DatePickerDialog.OnDateSetListener,
        PurchaseUserSelectionDialogFragment.FragmentInteractionListener,
        ReceiptHelper.HelperInteractionListener,
        StoreSelectionDialogFragment.DialogInteractionListener,
        PurchaseReceiptAddEditFragment.FragmentInteractionListener,
        FABProgressListener,
        RatesHelper.HelperInteractionListener {

    @IntDef({PURCHASE_SAVED, PURCHASE_SAVED_AUTO, PURCHASE_DISCARDED, PURCHASE_SAVED_AS_DRAFT, PURCHASE_DRAFT_DELETED,
            PURCHASE_ERROR,
            PURCHASE_NO_CHANGES})
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

    private static final String RECEIPT_HELPER = "async_receipt_fragment";
    private static final String LOG_TAG = PurchaseBaseActivity.class.getSimpleName();

    PurchaseBaseFragment mPurchaseFragment;
    File mPhotoFile;
    FloatingActionButton mFabPurchaseSave;
    ParseFile mReceiptParseFile;
    String mCurrentPhotoPath;
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
        setContentView(R.layout.activity_purchase_add_edit);

        if (Utils.isRunningLollipopAndHigher()) {
            setActivityTransition();
        }

        mFabPurchaseSave = (FloatingActionButton) findViewById(R.id.fab_purchase_save);
        mFabPurchaseSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPurchaseFragment.savePurchase(false);
            }
        });
        mFabProgressCircle = (FABProgressCircle) findViewById(R.id.fab_purchase_save_circle);
        mFabProgressCircle.attachListener(this);

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
                circularRevealFab();
            }
        });
    }

    @Override
    public void showFab() {
        if (Utils.isRunningLollipopAndHigher()) {
            if (ViewCompat.isLaidOut(mFabPurchaseSave)) {
                circularRevealFab();
            } else {
                mFabPurchaseSave.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        v.removeOnLayoutChangeListener(this);
                        circularRevealFab();
                    }
                });
            }
        } else {
            mFabPurchaseSave.setVisibility(View.VISIBLE);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void circularRevealFab() {
        Animator reveal = Utils.getCircularRevealAnimator(mFabPurchaseSave);
        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mFabPurchaseSave.setVisibility(View.VISIBLE);
            }
        });
        reveal.start();
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
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                mPhotoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                setResultForSnackbar(PURCHASE_ERROR);
                finishPurchase();
            }
            if (mPhotoFile != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
                startActivityForResult(cameraIntent, INTENT_REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENT_REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                getReceiptFileAsync();
            }
        }
    }

    private void getReceiptFileAsync() {
        FragmentManager fragmentManager = getFragmentManager();
        ReceiptHelper receiptHelper = findReceiptHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (receiptHelper == null) {
            receiptHelper = ReceiptHelper.newInstance(mCurrentPhotoPath);

            fragmentManager.beginTransaction()
                    .add(receiptHelper, RECEIPT_HELPER)
                    .commit();
        }
    }

    private ReceiptHelper findReceiptHelper(FragmentManager fragmentManager) {
        return (ReceiptHelper) fragmentManager.findFragmentByTag(RECEIPT_HELPER);
    }

    @Override
    public void onPostExecute(ImageReceipt receipt) {
        FragmentManager fragmentManager = getFragmentManager();

        if (receipt != null) {
            mReceiptParseFile = receipt.getParseFile();
            invalidateOptionsMenu();

            PurchaseReceiptAddEditFragment purchaseReceiptAddEditFragment =
                    (PurchaseReceiptAddEditFragment) fragmentManager.findFragmentByTag(
                            PURCHASE_RECEIPT_FRAGMENT);
            if (purchaseReceiptAddEditFragment != null) {
                purchaseReceiptAddEditFragment.setReceiptImage(mReceiptParseFile);
            }
        }

        // remove async fragment because we need a new one when the user wants to edit the receipt
        ReceiptHelper receiptHelper = findReceiptHelper(fragmentManager);
        if (receiptHelper != null) {
            getFragmentManager().beginTransaction().remove(receiptHelper).commit();
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
    public void onFABProgressAnimationEnd() {
        finishPurchase();
    }

    @Override
    public void finishPurchase() {
        ActivityCompat.finishAfterTransition(this);
        if (mPhotoFile != null) {
            boolean fileWasDeleted = mPhotoFile.delete();
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
