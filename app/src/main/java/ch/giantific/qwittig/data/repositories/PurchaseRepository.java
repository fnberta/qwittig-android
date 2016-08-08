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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import ch.giantific.qwittig.Constants;
import ch.giantific.qwittig.data.helper.SharedPrefsHelper;
import ch.giantific.qwittig.data.jobs.UploadReceiptJob;
import ch.giantific.qwittig.data.rest.CurrencyRates;
import ch.giantific.qwittig.data.rest.ExchangeRates;
import ch.giantific.qwittig.data.rest.ReceiptOcr;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxFirebaseDatabase;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxFirebaseStorage;
import ch.giantific.qwittig.domain.models.OcrData;
import ch.giantific.qwittig.domain.models.OcrRating;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.utils.Utils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by fabio on 02.07.16.
 */
public class PurchaseRepository {

    public static final int JPEG_COMPRESSION_RATE = 100;
    public static final int HEIGHT = 2048;
    public static final int WIDTH = 1024;
    private final DatabaseReference mDatabaseRef;
    private final StorageReference mStorageRef;
    private final FirebaseJobDispatcher mJobDispatcher;
    private final ReceiptOcr mReceiptOcr;
    private final ExchangeRates mExchangeRates;
    private final SharedPrefsHelper mSharedPrefsHelper;

    @Inject
    public PurchaseRepository(@NonNull FirebaseDatabase database,
                              @NonNull FirebaseStorage firebaseStorage,
                              @NonNull FirebaseJobDispatcher jobDispatcher,
                              @NonNull ReceiptOcr receiptOcr,
                              @NonNull ExchangeRates exchangeRates,
                              @NonNull SharedPrefsHelper sharedPrefsHelper) {
        mJobDispatcher = jobDispatcher;
        mReceiptOcr = receiptOcr;
        mDatabaseRef = database.getReference();
        mStorageRef = firebaseStorage.getReferenceFromUrl(Constants.STORAGE_URL).child("receipts");
        mExchangeRates = exchangeRates;
        mSharedPrefsHelper = sharedPrefsHelper;
    }

    public Observable<RxChildEvent<Purchase>> observePurchaseChildren(@NonNull final String groupId,
                                                                      @NonNull final String currentIdentityId,
                                                                      final boolean getDrafts) {
        final String path = getDrafts ? Purchase.PATH_DRAFTS : Purchase.PATH_PURCHASES;
        final Query query = mDatabaseRef.child(path).orderByChild(Purchase.PATH_GROUP).equalTo(groupId);
        return RxFirebaseDatabase.observeChildren(query, Purchase.class)
                .filter(new Func1<RxChildEvent<Purchase>, Boolean>() {
                    @Override
                    public Boolean call(RxChildEvent<Purchase> event) {
                        final Purchase purchase = event.getValue();
                        return Objects.equals(purchase.getBuyer(), currentIdentityId)
                                || purchase.getIdentitiesIds().contains(currentIdentityId);
                    }
                });
    }

    public Observable<Purchase> observePurchase(@NonNull String purchaseId, boolean isDraft) {
        final String path = isDraft ? Purchase.PATH_DRAFTS : Purchase.PATH_PURCHASES;
        final Query query = mDatabaseRef.child(path).child(purchaseId);
        return RxFirebaseDatabase.observeValue(query, Purchase.class);
    }


    public Observable<Purchase> getPurchases(@NonNull String groupId,
                                             @NonNull final String currentIdentityId,
                                             boolean getDrafts) {
        final String path = getDrafts ? Purchase.PATH_DRAFTS : Purchase.PATH_PURCHASES;
        final Query query = mDatabaseRef.child(path).orderByChild(Purchase.PATH_GROUP).equalTo(groupId);
        return RxFirebaseDatabase.observeValuesOnce(query, Purchase.class)
                .filter(new Func1<Purchase, Boolean>() {
                    @Override
                    public Boolean call(Purchase purchase) {
                        return Objects.equals(purchase.getBuyer(), currentIdentityId)
                                || purchase.getIdentitiesIds().contains(currentIdentityId);
                    }
                });
    }

    public Single<Purchase> getPurchase(@NonNull String purchaseId, boolean isDraft) {
        final String path = isDraft ? Purchase.PATH_DRAFTS : Purchase.PATH_PURCHASES;
        final Query query = mDatabaseRef.child(path).child(purchaseId);
        return RxFirebaseDatabase.observeValueOnce(query, Purchase.class);
    }

    public Observable<Boolean> isDraftsAvailable(@NonNull final String groupId,
                                                 @NonNull final String currentIdentityId) {
        final Query query = mDatabaseRef.child(Purchase.PATH_DRAFTS).orderByChild(Purchase.PATH_GROUP).equalTo(groupId);
        return RxFirebaseDatabase.observeValues(query, Purchase.class)
                .map(new Func1<List<Purchase>, Boolean>() {
                    @Override
                    public Boolean call(List<Purchase> drafts) {
                        for (Purchase draft : drafts) {
                            if (Objects.equals(draft.getBuyer(), currentIdentityId)) {
                                return true;
                            }
                        }

                        return false;
                    }
                });
    }

    public Observable<Void> uploadReceiptForOcr(@NonNull byte[] receipt,
                                                @NonNull String idToken) {
        final RequestBody tokenPart =
                RequestBody.create(MediaType.parse("text/plain"), idToken);

        final RequestBody receiptPart = RequestBody.create(MediaType.parse("image/jpeg"), receipt);
        final MultipartBody.Part body =
                MultipartBody.Part.createFormData("receipt", "receipt.jpg", receiptPart);

        return mReceiptOcr.uploadReceipt(tokenPart, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void saveDraft(@NonNull Purchase purchase, @Nullable String purchaseId) {
        final String key = TextUtils.isEmpty(purchaseId)
                ? mDatabaseRef.child(Purchase.PATH_DRAFTS).push().getKey()
                : purchaseId;
        mDatabaseRef.child(Purchase.PATH_DRAFTS).child(key).setValue(purchase);
        final String receipt = purchase.getReceipt();
        if (!TextUtils.isEmpty(receipt) && !Utils.isHttpsUrl(receipt)) {
            UploadReceiptJob.schedule(mJobDispatcher, purchase.getId(), true, receipt);
        }
    }

    public void savePurchase(@NonNull Purchase purchase, @Nullable String purchaseId,
                             boolean wasDraft) {
        final String key = TextUtils.isEmpty(purchaseId)
                ? mDatabaseRef.child(Purchase.PATH_PURCHASES).push().getKey()
                : purchaseId;

        if (wasDraft) {
            // TODO: use updateChildren
//            final Map<String, Object> childUpdates = new HashMap<>();
//            childUpdates.put(Purchase2.PATH_DRAFTS + "/" + key, null);
//            childUpdates.put(Purchase2.PATH_PURCHASES + "/" + key, purchase.toMap());
//            mDatabaseRef.updateChildren(childUpdates);
            mDatabaseRef.child(Purchase.PATH_DRAFTS).child(key).removeValue();
        }
        mDatabaseRef.child(Purchase.PATH_PURCHASES).child(key).setValue(purchase);

        final String receipt = purchase.getReceipt();
        if (!TextUtils.isEmpty(receipt) && !Utils.isHttpsUrl(receipt)) {
            UploadReceiptJob.schedule(mJobDispatcher, key, false, receipt);
        }
    }

    public Single<UploadTask.TaskSnapshot> uploadReceipt(@NonNull final String purchaseId,
                                                         final boolean isDraft,
                                                         @NonNull String receipt) {
        final File file = new File(receipt);
        final StorageReference receiptRef = mStorageRef.child(purchaseId);
        return RxFirebaseStorage.putFile(receiptRef, Uri.fromFile(file))
                .doOnSuccess(new Action1<UploadTask.TaskSnapshot>() {
                    @Override
                    public void call(UploadTask.TaskSnapshot taskSnapshot) {
                        final Uri url = taskSnapshot.getDownloadUrl();
                        if (url != null) {
                            final String path = isDraft ? Purchase.PATH_DRAFTS : Purchase.PATH_PURCHASES;
                            mDatabaseRef.child(path)
                                    .child(purchaseId)
                                    .child(Purchase.PATH_RECEIPT)
                                    .setValue(url.toString());
                        }
                    }
                });
    }

    public void deletePurchase(@NonNull String purchaseId, boolean isDraft) {
        final String path = isDraft ? Purchase.PATH_DRAFTS : Purchase.PATH_PURCHASES;
        mDatabaseRef.child(path).child(purchaseId).removeValue();
    }

    public void updateReadBy(@NonNull String purchaseId, @NonNull String currentIdentityId) {
        mDatabaseRef.child(Purchase.PATH_PURCHASES).child(purchaseId)
                .child(Purchase.PATH_READ_BY).child(currentIdentityId).setValue(true);
    }

    public Single<OcrData> getOcrData(@NonNull String ocrDataId) {
        final Query query = mDatabaseRef.child(OcrData.PATH).child(ocrDataId);
        return RxFirebaseDatabase.observeValueOnce(query, OcrData.class);
    }

    public void saveOcrRating(int satisfaction, int ratingNames, int ratingPrices,
                              int ratingMissing, int ratingSpeed, @NonNull String ocrDataId) {
        final OcrRating ocrRating = new OcrRating(satisfaction, ratingNames, ratingPrices,
                ratingMissing, ratingSpeed, ocrDataId);
        mDatabaseRef.child(OcrRating.PATH).push().setValue(ocrRating);
    }

    public Single<Float> getExchangeRate(@NonNull String baseCurrency, @NonNull String currency) {
        if (Objects.equals(currency, baseCurrency)) {
            return Single.just(1f);
        }

        final float rate = mSharedPrefsHelper.getExchangeRate(currency);
        if (rate == 1) {
            return loadExchangeRates(baseCurrency, currency);
        } else {
            if (mSharedPrefsHelper.isExchangeRatesFetchNeeded()) {
                return loadExchangeRates(baseCurrency, currency);
            } else {
                return Single.just(rate);
            }
        }
    }

    @NonNull
    private Single<Float> loadExchangeRates(@NonNull String baseCurrency,
                                            @NonNull final String currency) {
        return mExchangeRates.getRates(baseCurrency)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<CurrencyRates, Map<String, Float>>() {
                    @Override
                    public Map<String, Float> call(CurrencyRates currencyRates) {
                        return currencyRates.getRates();
                    }
                })
                .doOnSuccess(new Action1<Map<String, Float>>() {
                    @Override
                    public void call(Map<String, Float> exchangeRates) {
                        mSharedPrefsHelper.saveExchangeRates(exchangeRates);
                    }
                })
                .map(new Func1<Map<String, Float>, Float>() {
                    @Override
                    public Float call(Map<String, Float> exchangeRates) {
                        return exchangeRates.get(currency);
                    }
                });
    }
}
