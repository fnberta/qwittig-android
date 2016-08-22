package ch.giantific.qwittig.data.push;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import timber.log.Timber;

/**
 * Created by fabio on 28.07.16.
 */
public class FcmMessagingService extends FirebaseMessagingService {

    public static final String PUSH_FINANCE_TAB = "TAB";
    public static final String PUSH_OCR_DATA_ID = "OCR_DATA_ID";
    public static final String PUSH_PURCHASE_ID = "PURCHASE_ID";
    public static final String PUSH_GROUP_ID = "GROUP_ID";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Timber.i("message received: %s", remoteMessage);
    }
}
