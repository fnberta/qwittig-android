package ch.giantific.qwittig.data.push;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.push.di.DaggerFcmServiceComponent;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.presentation.finance.FinanceActivity;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddActivity;
import ch.giantific.qwittig.presentation.purchases.list.HomeActivity;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.SingleSubscriber;
import timber.log.Timber;

/**
 * Created by fabio on 28.07.16.
 */
public class FcmMessagingService extends FirebaseMessagingService {

    public static final String PUSH_TYPE = "type";
    public static final String PUSH_TYPE_OCR_PROCESSED = "OCR_PROCESSED";
    public static final String PUSH_TYPE_REMIND_COMPENSATION = "REMIND_COMPENSATION";
    public static final String PUSH_TYPE_COMPENSATION_PAID = "COMPENSATION_PAID";
    public static final String PUSH_TYPE_GROUP_LEFT = "GROUP_LEFT";
    public static final String PUSH_TYPE_GROUP_JOINED = "GROUP_JOINED";
    public static final String PUSH_TYPE_USER_DELETED = "USER_DELETED";
    public static final String PUSH_FINANCE_TAB = "tab";
    public static final String PUSH_OCR_DATA_ID = "ocrDataId";
    public static final String PUSH_PURCHASE_ID = "purchaseId";
    public static final String PUSH_GROUP_ID = "groupId";
    public static final String PUSH_AMOUNT = "amount";
    public static final String PUSH_NICKNAME = "nickname";
    public static final String PUSH_CURRENCY = "currency";
    public static final String PUSH_GROUP_NAME = "groupName";
    public static final String PUSH_ASSIGNMENT_ID = "assignmentId";
    @Inject
    PurchaseRepository purchaseRepo;
    @Inject
    UserRepository userRepo;
    @Inject
    NotificationManagerCompat notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        injectDependencies();
    }

    private void injectDependencies() {
        DaggerFcmServiceComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .build()
                .inject(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        final FirebaseUser firebaseUser = userRepo.getCurrentUser();
        if (firebaseUser == null) {
            return;
        }

        final Map<String, String> data = remoteMessage.getData();
        handleMessage(data, firebaseUser.getUid());
    }

    private void handleMessage(@NonNull Map<String, String> data,
                               @NonNull String currentUserId) {
        final String type = data.get(PUSH_TYPE);
        switch (type) {
            case PUSH_TYPE_OCR_PROCESSED: {
                final String ocrDataId = data.get(PUSH_OCR_DATA_ID);
                final String purchaseId = data.get(PUSH_PURCHASE_ID);
                setReceiptUrl(currentUserId, ocrDataId, purchaseId);

                final Intent intent = new Intent(this, PurchaseAddActivity.class);
                intent.putExtra(PUSH_OCR_DATA_ID, ocrDataId);
                final Notification notification =
                        buildNotification(getString(R.string.push_purchase_ocr_title),
                                getString(R.string.push_purchase_ocr_alert), intent);
                notificationManager.notify(ocrDataId.hashCode(), notification);
                break;
            }
            case PUSH_TYPE_REMIND_COMPENSATION: {
                final int id = (int) System.currentTimeMillis();
                final Intent intent = new Intent(this, FinanceActivity.class);
                final NumberFormat moneyFormatter =
                        MoneyUtils.getMoneyFormatter(data.get(PUSH_CURRENCY), true, true);
                final String amount = moneyFormatter.format(data.get(PUSH_AMOUNT));
                final String nickname = data.get(PUSH_NICKNAME);
                final Notification notification =
                        buildNotification(getString(R.string.push_compensation_remind_title),
                                getString(R.string.push_compensation_remind_alert, nickname, amount), intent);
                notificationManager.notify(id, notification);
                break;
            }
            case PUSH_TYPE_COMPENSATION_PAID: {
                final int id = (int) System.currentTimeMillis();
                final Intent intent = new Intent(this, FinanceActivity.class);
                final NumberFormat moneyFormatter =
                        MoneyUtils.getMoneyFormatter(data.get(PUSH_CURRENCY), true, true);
                final String amount = moneyFormatter.format(data.get(PUSH_AMOUNT));
                final String nickname = data.get(PUSH_NICKNAME);
                final Notification notification =
                        buildNotification(getString(R.string.push_compensation_payment_done_title),
                                getString(R.string.push_compensation_payment_done_alert, nickname, amount), intent);
                notificationManager.notify(id, notification);
                break;
            }
            case PUSH_TYPE_GROUP_LEFT: {
                final int id = (int) System.currentTimeMillis();
                final Intent intent = new Intent(this, HomeActivity.class);
                final String nickname = data.get(PUSH_NICKNAME);
                final String groupName = data.get(PUSH_GROUP_NAME);
                final Notification notification =
                        buildNotification(getString(R.string.push_user_left_group_title, nickname),
                                getString(R.string.push_user_left_group_alert, nickname, groupName), intent);
                notificationManager.notify(id, notification);
                break;
            }
            case PUSH_TYPE_GROUP_JOINED: {
                final int id = (int) System.currentTimeMillis();
                final Intent intent = new Intent(this, HomeActivity.class);
                final String nickname = data.get(PUSH_NICKNAME);
                final String groupName = data.get(PUSH_GROUP_NAME);
                final Notification notification =
                        buildNotification(getString(R.string.push_user_joined_title, groupName),
                                getString(R.string.push_user_joined_alert, nickname), intent);
                notificationManager.notify(id, notification);
                break;
            }
            case PUSH_TYPE_USER_DELETED: {
                final int id = (int) System.currentTimeMillis();
                final Intent intent = new Intent(this, HomeActivity.class);
                final String nickname = data.get(PUSH_NICKNAME);
                final Notification notification =
                        buildNotification(getString(R.string.push_user_deleted_title, nickname),
                                getString(R.string.push_user_deleted_alert), intent);
                notificationManager.notify(id, notification);
                break;
            }
        }
    }

    private Notification buildNotification(@NonNull String title,
                                           @NonNull String alert,
                                           @NonNull Intent intent) {
        final PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_shopping_cart_white_24dp)
                .setContentTitle(title)
                .setContentText(alert)
                .setContentIntent(pendingIntent)
                .setTicker(String.format(Locale.getDefault(), "%s: %s", title, alert))
                .build();
    }

    private void setReceiptUrl(@NonNull String currentUserId,
                               @NonNull String ocrDataId,
                               @NonNull String purchaseId) {
        purchaseRepo.setOcrDataReceiptUrl(currentUserId, ocrDataId, purchaseId)
                .subscribe(new SingleSubscriber<Uri>() {
                    @Override
                    public void onSuccess(Uri value) {
                        // do nothing
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to set receipt url with error:");
                    }
                });
    }
}
