package ch.giantific.qwittig.data.repositories;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import ch.giantific.qwittig.Constants;
import ch.giantific.qwittig.data.helper.SharedPrefsHelper;
import ch.giantific.qwittig.data.jobs.UploadReceiptJob;
import ch.giantific.qwittig.data.queues.OcrQueue;
import ch.giantific.qwittig.data.rest.ExchangeRates;
import ch.giantific.qwittig.data.rest.ExchangeRatesResult;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxFirebaseDatabase;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxFirebaseStorage;
import ch.giantific.qwittig.domain.models.OcrData;
import ch.giantific.qwittig.domain.models.OcrRating;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.utils.Utils;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by fabio on 02.07.16.
 */
public class PurchaseRepository {

    public static final int JPEG_COMPRESSION_RATE = 100;
    public static final int HEIGHT = 2048;
    public static final int WIDTH = 1024;
    private static final String PATH_OCR_QUEUE = "queue/ocr/tasks";

    private final DatabaseReference databaseRef;
    private final StorageReference storageRef;
    private final FirebaseJobDispatcher jobDispatcher;
    private final ExchangeRates exchangeRates;
    private final SharedPrefsHelper sharedPrefsHelper;

    @Inject
    public PurchaseRepository(@NonNull FirebaseDatabase database,
                              @NonNull FirebaseStorage firebaseStorage,
                              @NonNull FirebaseJobDispatcher jobDispatcher,
                              @NonNull ExchangeRates exchangeRates,
                              @NonNull SharedPrefsHelper sharedPrefsHelper) {
        this.jobDispatcher = jobDispatcher;
        databaseRef = database.getReference();
        storageRef = firebaseStorage.getReferenceFromUrl(Constants.STORAGE_URL).child("receipts");
        this.exchangeRates = exchangeRates;
        this.sharedPrefsHelper = sharedPrefsHelper;
    }

    public Observable<RxChildEvent<Purchase>> observePurchaseChildren(@NonNull final String groupId,
                                                                      @NonNull final String currentIdentityId,
                                                                      final boolean getDrafts) {
        final Query query = getDrafts
                ? databaseRef.child(Purchase.BASE_PATH_DRAFTS).child(currentIdentityId).orderByChild(Purchase.PATH_GROUP).equalTo(groupId)
                : databaseRef.child(Purchase.BASE_PATH_PURCHASES).orderByChild(Purchase.PATH_GROUP).equalTo(groupId);
        return RxFirebaseDatabase.observeChildren(query, Purchase.class)
                .filter(event -> {
                    if (getDrafts) {
                        return true;
                    }

                    final Purchase purchase = event.getValue();
                    return Objects.equals(purchase.getBuyer(), currentIdentityId)
                            || purchase.getIdentitiesIds().contains(currentIdentityId);
                });
    }

    public Observable<Purchase> getPurchases(@NonNull final String groupId,
                                             @NonNull final String currentIdentityId,
                                             final boolean getDrafts) {
        final Query query = getDrafts
                ? databaseRef.child(Purchase.BASE_PATH_DRAFTS).child(currentIdentityId).orderByChild(Purchase.PATH_GROUP).equalTo(groupId)
                : databaseRef.child(Purchase.BASE_PATH_PURCHASES).orderByChild(Purchase.PATH_GROUP).equalTo(groupId);
        return RxFirebaseDatabase.observeValueListOnce(query, Purchase.class)
                .filter(purchase -> getDrafts
                        || Objects.equals(purchase.getBuyer(), currentIdentityId)
                        || purchase.getIdentitiesIds().contains(currentIdentityId));
    }

    public Observable<Purchase> observePurchase(@NonNull String purchaseId) {
        final Query query = databaseRef.child(Purchase.BASE_PATH_PURCHASES).child(purchaseId);
        return RxFirebaseDatabase.observeValue(query, Purchase.class);
    }

    public Single<Purchase> getPurchase(@NonNull String purchaseId) {
        final Query query = databaseRef.child(Purchase.BASE_PATH_PURCHASES).child(purchaseId);
        return RxFirebaseDatabase.observeValueOnce(query, Purchase.class);
    }

    public Single<Purchase> getDraft(@NonNull String draftId, @NonNull String buyerId) {
        final Query query = databaseRef.child(Purchase.BASE_PATH_DRAFTS).child(buyerId).child(draftId);
        return RxFirebaseDatabase.observeValueOnce(query, Purchase.class);
    }

    public Observable<Boolean> isDraftsAvailable(@NonNull final String groupId,
                                                 @NonNull final String currentIdentityId) {
        final Query query = databaseRef.child(Purchase.BASE_PATH_DRAFTS).child(currentIdentityId)
                .orderByChild(Purchase.PATH_GROUP).equalTo(groupId);
        return RxFirebaseDatabase.observeValueList(query, Purchase.class)
                .map(drafts -> !drafts.isEmpty());
    }

    public void uploadReceiptForOcr(@NonNull String receiptBase64, @NonNull String userId) {
        final OcrQueue ocrQueue = new OcrQueue(receiptBase64, userId);
        databaseRef.child(PATH_OCR_QUEUE).push().setValue(ocrQueue);
    }

    public void saveDraft(@NonNull Purchase purchase, @Nullable String purchaseId) {
        final String buyerId = purchase.getBuyer();
        final String key = TextUtils.isEmpty(purchaseId)
                ? databaseRef.child(Purchase.BASE_PATH_DRAFTS).child(buyerId).push().getKey()
                : purchaseId;
        databaseRef.child(Purchase.BASE_PATH_DRAFTS).child(buyerId).child(key).setValue(purchase);

        final String receipt = purchase.getReceipt();
        if (!TextUtils.isEmpty(receipt) && !Utils.isHttpsUrl(receipt)) {
            UploadReceiptJob.schedule(jobDispatcher, purchase.getId(), buyerId, receipt);
        }
    }

    public void savePurchase(@NonNull Purchase purchase, @Nullable String purchaseId,
                             @NonNull String userId, boolean wasDraft) {
        final String key = TextUtils.isEmpty(purchaseId)
                ? databaseRef.child(Purchase.BASE_PATH_PURCHASES).push().getKey()
                : purchaseId;
        final String buyerId = purchase.getBuyer();

        final Map<String, Object> childUpdates = new HashMap<>();
        final String ocrDataId = purchase.getOcrData();
        if (!TextUtils.isEmpty(ocrDataId)) {
            childUpdates.put(OcrData.BASE_PATH + "/" + userId + "/" + ocrDataId + "/" + OcrData.PATH_PROCESSED, true);
        }
        final Map<String, Object> purchaseMap = purchase.toMap();
        if (wasDraft) {
            childUpdates.put(Purchase.BASE_PATH_DRAFTS + "/" + buyerId + "/" + key, null);
            purchaseMap.put(Purchase.PATH_DRAFT, false);
        }
        childUpdates.put(Purchase.BASE_PATH_PURCHASES + "/" + key, purchaseMap);
        databaseRef.updateChildren(childUpdates);

        final String receipt = purchase.getReceipt();
        if (!TextUtils.isEmpty(receipt) && !Utils.isHttpsUrl(receipt)) {
            UploadReceiptJob.schedule(jobDispatcher, key, buyerId, receipt);
        }
    }

    public Single<UploadTask.TaskSnapshot> uploadReceipt(@NonNull final String purchaseId,
                                                         @Nullable final String buyerId,
                                                         @NonNull String receipt) {
        final File file = new File(receipt);
        final StorageReference receiptRef = storageRef.child(purchaseId);
        return RxFirebaseStorage.putFile(receiptRef, Uri.fromFile(file))
                .doOnSuccess(taskSnapshot -> {
                    final Uri url = taskSnapshot.getDownloadUrl();
                    if (url != null) {
                        if (TextUtils.isEmpty(buyerId)) {
                            databaseRef.child(Purchase.BASE_PATH_PURCHASES)
                                    .child(purchaseId)
                                    .child(Purchase.PATH_RECEIPT)
                                    .setValue(url.toString());
                        } else {
                            databaseRef.child(Purchase.BASE_PATH_DRAFTS)
                                    .child(buyerId)
                                    .child(purchaseId)
                                    .child(Purchase.PATH_RECEIPT)
                                    .setValue(url.toString());
                        }
                    }
                });
    }

    public void deletePurchase(@NonNull String purchaseId) {
        databaseRef.child(Purchase.BASE_PATH_PURCHASES).child(purchaseId).removeValue();
    }

    public void deleteDraft(@NonNull String draftId, @NonNull String buyerId) {
        databaseRef.child(Purchase.BASE_PATH_DRAFTS).child(buyerId).child(draftId).removeValue();
    }

    public void updateReadBy(@NonNull String purchaseId, @NonNull String currentIdentityId) {
        databaseRef.child(Purchase.BASE_PATH_PURCHASES).child(purchaseId)
                .child(Purchase.PATH_READ_BY).child(currentIdentityId).setValue(true);
    }

    public Observable<List<OcrData>> observeOcrData(@NonNull String currentUserId, boolean processed) {
        final Query query = databaseRef.child(OcrData.BASE_PATH).child(currentUserId).orderByChild(OcrData.PATH_PROCESSED).equalTo(processed);
        return RxFirebaseDatabase.observeValueList(query, OcrData.class);
    }

    public Single<OcrData> getOcrData(@NonNull String ocrDataId, @NonNull String userId) {
        final Query query = databaseRef.child(OcrData.BASE_PATH).child(userId).child(ocrDataId);
        return RxFirebaseDatabase.observeValueOnce(query, OcrData.class);
    }

    public void discardOcrData(@NonNull String currentUserId, @NonNull String ocrDataId) {
        final Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(OcrData.BASE_PATH + "/" + currentUserId + "/" + ocrDataId + "/" + OcrData.PATH_PROCESSED, true);
        childUpdates.put(OcrData.BASE_PATH + "/" + currentUserId + "/" + ocrDataId + "/" + OcrData.PATH_PURCHASE, null);
        databaseRef.updateChildren(childUpdates);
    }

    /**
     * TODO: only needed as long as firebase node.js sdk lacks storage support
     */
    public Single<Uri> setOcrDataReceiptUrl(@NonNull final String currentUserId,
                                            @NonNull final String ocrDataId,
                                            @NonNull String purchaseId) {
        return RxFirebaseStorage.getDownloadUrl(storageRef.child(purchaseId + ".jpg"))
                .doOnSuccess(uri -> databaseRef.child(OcrData.BASE_PATH)
                        .child(currentUserId)
                        .child(ocrDataId)
                        .child(OcrData.PATH_RECEIPT)
                        .setValue(uri.toString()));
    }

    public void saveOcrRating(int satisfaction, int ratingNames, int ratingPrices,
                              int ratingMissing, int ratingSpeed, @NonNull String ocrDataId) {
        final OcrRating ocrRating = new OcrRating(satisfaction, ratingNames, ratingPrices,
                ratingMissing, ratingSpeed, ocrDataId);
        databaseRef.child(OcrRating.BASE_PATH).push().setValue(ocrRating);
    }

    public Single<Float> getExchangeRate(@NonNull String baseCurrency, @NonNull String currency) {
        if (Objects.equals(currency, baseCurrency)) {
            return Single.just(1f);
        }

        final float rate = sharedPrefsHelper.getExchangeRate(currency);
        if (rate == 1) {
            return loadExchangeRates(baseCurrency, currency);
        } else {
            if (sharedPrefsHelper.isExchangeRatesFetchNeeded()) {
                return loadExchangeRates(baseCurrency, currency);
            } else {
                return Single.just(rate);
            }
        }
    }

    @NonNull
    private Single<Float> loadExchangeRates(@NonNull String baseCurrency,
                                            @NonNull final String currency) {
        return exchangeRates.getRates(baseCurrency)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(ExchangeRatesResult::getRates)
                .doOnSuccess(sharedPrefsHelper::saveExchangeRates)
                .map(exchangeRates12 -> exchangeRates12.get(currency));
    }
}
