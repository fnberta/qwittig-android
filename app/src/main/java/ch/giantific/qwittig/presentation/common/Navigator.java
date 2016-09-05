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
import ch.giantific.qwittig.presentation.assignments.addedit.AssignmentAddActivity;
import ch.giantific.qwittig.presentation.assignments.addedit.AssignmentEditActivity;
import ch.giantific.qwittig.presentation.assignments.list.AssignmentsActivity;
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
import ch.giantific.qwittig.presentation.assignments.details.AssignmentDetailsActivity;

/**
 * Created by fabio on 17.06.16.
 */
@SuppressWarnings("unchecked")
public class Navigator {

    public static final String INTENT_PURCHASE_ID = BuildConfig.APPLICATION_ID + ".intents.INTENT_PURCHASE_ID";
    public static final String INTENT_PURCHASE_EDIT_DRAFT = BuildConfig.APPLICATION_ID + ".intents.INTENT_PURCHASE_EDIT_DRAFT";
    public static final String INTENT_ASSIGNMENT_ID = BuildConfig.APPLICATION_ID + ".intents.INTENT_ASSIGNMENT_ID";
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
    public static final int INTENT_REQUEST_ASSIGNMENT_NEW = 8;
    public static final int INTENT_REQUEST_ASSIGNMENT_MODIFY = 9;
    public static final int INTENT_REQUEST_ASSIGNMENT_DETAILS = 10;
    public static final int INTENT_REQUEST_IMAGE_PICK = 11;

    private final FragmentActivity activity;

    public Navigator(@NonNull FragmentActivity activity) {
        this.activity = activity;
    }

    public void finish(int result, @NonNull String objectId) {
        final Intent data = new Intent();
        data.putExtra(INTENT_OBJECT_ID, objectId);
        activity.setResult(result, data);
        ActivityCompat.finishAfterTransition(activity);
    }

    public void finish(int result) {
        activity.setResult(result);
        ActivityCompat.finishAfterTransition(activity);
    }

    public void finish() {
        ActivityCompat.finishAfterTransition(activity);
    }

    public void startProfileSettings() {
        final Intent intent = new Intent(activity, SettingsProfileActivity.class);
        final ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        activity.startActivityForResult(intent, INTENT_REQUEST_SETTINGS_PROFILE,
                options.toBundle());
    }

    public void startHome() {
        final Intent intent = new Intent(activity, HomeActivity.class);
        activity.startActivity(intent);
    }

    public void startLogin() {
        final Intent intentLogin = new Intent(activity, LoginActivity.class);
        intentLogin.setData(activity.getIntent().getData());
//        Starting an activity with forResult and transitions during a lifecycle method results on
//        onActivityResult not being called
//        ActivityOptionsCompat activityOptionsCompat =
//                ActivityOptionsCompat.makeSceneTransitionAnimation(this);
        activity.startActivityForResult(intentLogin, INTENT_REQUEST_LOGIN);
    }

    public void startPurchaseAdd(@Nullable String ocrPurchaseId) {
        final Intent intent = new Intent(activity, PurchaseAddActivity.class);
        intent.putExtra(INTENT_OCR_PURCHASE_ID, ocrPurchaseId);
        final ActivityOptionsCompat activityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        activity.startActivityForResult(intent, INTENT_REQUEST_PURCHASE_MODIFY,
                activityOptionsCompat.toBundle());
    }

    public void startPurchaseEdit(@NonNull String purchaseId, boolean isDraft) {
        final Intent intent = new Intent(activity, PurchaseEditActivity.class);
        intent.putExtra(INTENT_PURCHASE_ID, purchaseId);
        intent.putExtra(INTENT_PURCHASE_EDIT_DRAFT, isDraft);
        final ActivityOptionsCompat activityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        activity.startActivityForResult(intent, INTENT_REQUEST_PURCHASE_MODIFY,
                activityOptionsCompat.toBundle());
    }

    public void startPurchaseDetails(@NonNull String purchaseId) {
        final Intent intent = new Intent(activity, PurchaseDetailsActivity.class);
        intent.putExtra(INTENT_PURCHASE_ID, purchaseId);
        final ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        activity.startActivityForResult(intent, INTENT_REQUEST_PURCHASE_DETAILS,
                options.toBundle());
    }

    public void startOcrRating(@NonNull String ocrDataId) {
        final Intent intent = new Intent(activity, OcrRatingActivity.class);
        intent.putExtra(INTENT_OCR_DATA_ID, ocrDataId);
        activity.startActivity(intent);
    }

    public void startFinance() {
        final Intent intent = new Intent(activity, FinanceActivity.class);
        activity.startActivity(intent);
    }

    public void startAssignments() {
        final Intent intent = new Intent(activity, AssignmentsActivity.class);
        activity.startActivity(intent);
    }

    public void startAssignmentEdit(@NonNull String assignmentId) {
        final Intent intent = new Intent(activity, AssignmentEditActivity.class);
        intent.putExtra(Navigator.INTENT_ASSIGNMENT_ID, assignmentId);
        final ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        activity.startActivityForResult(intent, INTENT_REQUEST_ASSIGNMENT_MODIFY,
                options.toBundle());
    }

    public void startAssignmentDetails(@NonNull String assignmentId) {
        final Intent intent = new Intent(activity, AssignmentDetailsActivity.class);
        intent.putExtra(INTENT_ASSIGNMENT_ID, assignmentId);
        final ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        activity.startActivityForResult(intent, INTENT_REQUEST_ASSIGNMENT_DETAILS, options.toBundle());
    }

    public void startAssignmentAdd() {
        final Intent intent = new Intent(activity, AssignmentAddActivity.class);
        final ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        activity.startActivityForResult(intent, INTENT_REQUEST_ASSIGNMENT_NEW, options.toBundle());
    }

    public void startStats() {
        final Intent intent = new Intent(activity, StatsActivity.class);
        activity.startActivity(intent);
    }

    public void startSettings() {
        final Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivityForResult(intent, INTENT_REQUEST_SETTINGS);
    }

    public void startHelpFeedback() {
        final Intent intent = new Intent(activity, HelpFeedbackActivity.class);
        activity.startActivity(intent);
    }

    public void startFirstRun() {
        final Intent intent = new Intent(activity, AppIntroActivity.class);
        activity.startActivity(intent);
    }

    public void startSystemSettings() {
        final Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + activity.getPackageName()));
        activity.startActivity(intent);
    }

    public void startImagePicker() {
        final Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activity.startActivityForResult(intent, INTENT_REQUEST_IMAGE_PICK);
    }

    public void startCamera() {
        final Intent intent = new Intent(activity, CameraActivity.class);
        activity.startActivityForResult(intent, INTENT_REQUEST_IMAGE_CAPTURE);
    }

    public void openWebsite(@NonNull String url) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(intent);
    }

    public void startAbout() {
        final Intent intent = new Intent(activity, AboutActivity.class);
        activity.startActivity(intent);
    }
}
