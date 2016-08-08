package ch.giantific.qwittig.presentation.common;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;

import ch.giantific.qwittig.BuildConfig;
import ch.giantific.qwittig.presentation.about.AboutActivity;
import ch.giantific.qwittig.presentation.camera.CameraActivity;
import ch.giantific.qwittig.presentation.finance.FinanceActivity;
import ch.giantific.qwittig.presentation.helpfeedback.HelpFeedbackActivity;
import ch.giantific.qwittig.presentation.intro.AppIntroActivity;
import ch.giantific.qwittig.presentation.login.LoginActivity;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddActivity;
import ch.giantific.qwittig.presentation.purchases.addedit.edit.PurchaseEditActivity;
import ch.giantific.qwittig.presentation.purchases.details.PurchaseDetailsActivity;
import ch.giantific.qwittig.presentation.purchases.list.HomeActivity;
import ch.giantific.qwittig.presentation.purchases.ocrrating.OcrRatingActivity;
import ch.giantific.qwittig.presentation.settings.general.SettingsActivity;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileActivity;
import ch.giantific.qwittig.presentation.stats.StatsActivity;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddActivity;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskEditActivity;
import ch.giantific.qwittig.presentation.tasks.details.TaskDetailsActivity;
import ch.giantific.qwittig.presentation.tasks.list.TasksActivity;

/**
 * Created by fabio on 17.06.16.
 */
@SuppressWarnings("unchecked")
public class Navigator {

    public static final String INTENT_PURCHASE_ID = BuildConfig.APPLICATION_ID + ".intents.INTENT_PURCHASE_ID";
    public static final String INTENT_PURCHASE_EDIT_DRAFT = BuildConfig.APPLICATION_ID + ".intents.INTENT_PURCHASE_EDIT_DRAFT";
    public static final String INTENT_TASK_ID = BuildConfig.APPLICATION_ID + ".intents.INTENT_TASK_ID";
    public static final String INTENT_OCR_DATA_ID = BuildConfig.APPLICATION_ID + ".intents.INTENT_OCR_DATA_ID";
    public static final String INTENT_OCR_PURCHASE_ID = BuildConfig.APPLICATION_ID + ".intents.INTENT_OCR_PURCHASE_ID";
    public static final String INTENT_OBJECT_ID = BuildConfig.APPLICATION_ID + ".intents.INTENT_OBJECT_ID";
    public static final int INTENT_REQUEST_LOGIN = 1;
    public static final int INTENT_REQUEST_SETTINGS = 2;
    public static final int INTENT_REQUEST_PURCHASE_MODIFY = 3;
    public static final int INTENT_REQUEST_PURCHASE_DETAILS = 4;
    public static final int INTENT_REQUEST_IMAGE_CAPTURE = 5;
    public static final int INTENT_REQUEST_SETTINGS_PROFILE = 6;
    public static final int INTENT_REQUEST_SETTINGS_ADD_GROUP = 7;
    public static final int INTENT_REQUEST_TASK_NEW = 8;
    public static final int INTENT_REQUEST_TASK_MODIFY = 9;
    public static final int INTENT_REQUEST_TASK_DETAILS = 10;
    public static final int INTENT_REQUEST_IMAGE_PICK = 11;
    private final FragmentActivity mActivity;

    public Navigator(@NonNull FragmentActivity activity) {
        mActivity = activity;
    }

    public void finish(int result, @NonNull String objectId) {
        final Intent data = new Intent();
        data.putExtra(INTENT_OBJECT_ID, objectId);
        mActivity.setResult(result, data);
        ActivityCompat.finishAfterTransition(mActivity);
    }

    public void finish(int result) {
        mActivity.setResult(result);
        ActivityCompat.finishAfterTransition(mActivity);
    }

    public void finish() {
        ActivityCompat.finishAfterTransition(mActivity);
    }

    public void startProfileSettings() {
        final Intent intent = new Intent(mActivity, SettingsProfileActivity.class);
        final ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(mActivity);
        mActivity.startActivityForResult(intent, INTENT_REQUEST_SETTINGS_PROFILE,
                options.toBundle());
    }

    public void startHome() {
        final Intent intent = new Intent(mActivity, HomeActivity.class);
        mActivity.startActivity(intent);
    }

    public void startLogin() {
        final Intent intentLogin = new Intent(mActivity, LoginActivity.class);
        intentLogin.setData(mActivity.getIntent().getData());
//        Starting an activity with forResult and transitions during a lifecycle method results on
//        onActivityResult not being called
//        ActivityOptionsCompat activityOptionsCompat =
//                ActivityOptionsCompat.makeSceneTransitionAnimation(this);
        mActivity.startActivityForResult(intentLogin, INTENT_REQUEST_LOGIN);
    }

    public void startPurchaseAdd(@Nullable String ocrPurchaseId) {
        final Intent intent = new Intent(mActivity, PurchaseAddActivity.class);
        intent.putExtra(INTENT_OCR_PURCHASE_ID, ocrPurchaseId);
        final ActivityOptionsCompat activityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(mActivity);
        mActivity.startActivityForResult(intent, INTENT_REQUEST_PURCHASE_MODIFY,
                activityOptionsCompat.toBundle());
    }

    public void startPurchaseEdit(@NonNull String purchaseId, boolean isDraft) {
        final Intent intent = new Intent(mActivity, PurchaseEditActivity.class);
        intent.putExtra(INTENT_PURCHASE_ID, purchaseId);
        intent.putExtra(INTENT_PURCHASE_EDIT_DRAFT, isDraft);
        final ActivityOptionsCompat activityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(mActivity);
        mActivity.startActivityForResult(intent, INTENT_REQUEST_PURCHASE_MODIFY,
                activityOptionsCompat.toBundle());
    }

    public void startPurchaseDetails(@NonNull String purchaseId) {
        final Intent intent = new Intent(mActivity, PurchaseDetailsActivity.class);
        intent.putExtra(INTENT_PURCHASE_ID, purchaseId);
        final ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(mActivity);
        mActivity.startActivityForResult(intent, INTENT_REQUEST_PURCHASE_DETAILS,
                options.toBundle());
    }

    public void startOcrRating(@NonNull String ocrDataId) {
        final Intent intent = new Intent(mActivity, OcrRatingActivity.class);
        intent.putExtra(INTENT_OCR_DATA_ID, ocrDataId);
        mActivity.startActivity(intent);
    }

    public void startFinance() {
        final Intent intent = new Intent(mActivity, FinanceActivity.class);
        mActivity.startActivity(intent);
    }

    public void startTasks() {
        final Intent intent = new Intent(mActivity, TasksActivity.class);
        mActivity.startActivity(intent);
    }

    public void startTaskEdit(@NonNull String taskId) {
        final Intent intent = new Intent(mActivity, TaskEditActivity.class);
        intent.putExtra(Navigator.INTENT_TASK_ID, taskId);
        final ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(mActivity);
        mActivity.startActivityForResult(intent, INTENT_REQUEST_TASK_MODIFY,
                options.toBundle());
    }

    public void startTaskDetails(@NonNull String taskId) {
        final Intent intent = new Intent(mActivity, TaskDetailsActivity.class);
        intent.putExtra(INTENT_TASK_ID, taskId);
        final ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(mActivity);
        mActivity.startActivityForResult(intent, INTENT_REQUEST_TASK_DETAILS, options.toBundle());
    }

    public void startTaskAdd() {
        final Intent intent = new Intent(mActivity, TaskAddActivity.class);
        final ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(mActivity);
        mActivity.startActivityForResult(intent, INTENT_REQUEST_TASK_NEW, options.toBundle());
    }

    public void startStats() {
        final Intent intent = new Intent(mActivity, StatsActivity.class);
        mActivity.startActivity(intent);
    }

    public void startSettings() {
        final Intent intent = new Intent(mActivity, SettingsActivity.class);
        mActivity.startActivityForResult(intent, INTENT_REQUEST_SETTINGS);
    }

    public void startHelpFeedback() {
        final Intent intent = new Intent(mActivity, HelpFeedbackActivity.class);
        mActivity.startActivity(intent);
    }

    public void startFirstRun() {
        final Intent intent = new Intent(mActivity, AppIntroActivity.class);
        mActivity.startActivity(intent);
    }

    public void startSystemSettings() {
        final Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + mActivity.getPackageName()));
        mActivity.startActivity(intent);
    }

    public void startImagePicker() {
        final Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        mActivity.startActivityForResult(intent, INTENT_REQUEST_IMAGE_PICK);
    }

    public void startCamera() {
        final Intent intent = new Intent(mActivity, CameraActivity.class);
        mActivity.startActivityForResult(intent, INTENT_REQUEST_IMAGE_CAPTURE);
    }

    public void openWebsite(@NonNull String url) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        mActivity.startActivity(intent);
    }

    public void startAbout() {
        final Intent intent = new Intent(mActivity, AboutActivity.class);
        mActivity.startActivity(intent);
    }
}
