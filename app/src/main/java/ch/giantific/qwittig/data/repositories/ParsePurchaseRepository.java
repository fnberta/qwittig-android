/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.bumptech.glide.Glide;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ch.giantific.qwittig.BuildConfig;
import ch.giantific.qwittig.data.rest.CurrencyRates;
import ch.giantific.qwittig.data.rest.ExchangeRates;
import ch.giantific.qwittig.data.rest.ReceiptOcr;
import ch.giantific.qwittig.data.services.SavePurchaseTaskService;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Item;
import ch.giantific.qwittig.domain.models.OcrPurchase;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.parse.ParseUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Provides an implementation of {@link PurchaseRepository} that uses the Parse.com framework as
 * the local and online data store.
 */
public class ParsePurchaseRepository extends ParseBaseRepository implements
        PurchaseRepository {

    private static final String DELETE_PARSE_FILE = "deleteParseFile";
    private static final String PARAM_FILE_NAME = "fileName";
    private static final String DRAFTS_AVAILABLE = "DRAFTS_AVAILABLE_";
    private static final String EXCHANGE_RATE_LAST_FETCHED_TIME = "EXCHANGE_RATE_LAST_FETCHED_TIME";
    private static final long EXCHANGE_RATE_REFRESH_INTERVAL = 24 * 60 * 60 * 1000;
    private final SharedPreferences mSharedPrefs;
    private final ExchangeRates mExchangeRates;
    private final ReceiptOcr mReceiptOcr;
    private final GcmNetworkManager mGcmNetworkManager;

    public ParsePurchaseRepository(@NonNull SharedPreferences sharedPreferences,
                                   @NonNull ExchangeRates exchangeRates,
                                   @NonNull ReceiptOcr receiptOcr,
                                   @NonNull GcmNetworkManager gcmNetworkManager) {
        super();

        mSharedPrefs = sharedPreferences;
        mExchangeRates = exchangeRates;
        mReceiptOcr = receiptOcr;
        mGcmNetworkManager = gcmNetworkManager;
    }

    @Override
    protected String getClassName() {
        return Purchase.CLASS;
    }

    @Override
    public Observable<Purchase> getPurchases(@NonNull Identity identity, boolean getDrafts) {
        final ParseQuery<Purchase> buyerQuery = ParseQuery.getQuery(Purchase.CLASS);
        buyerQuery.whereEqualTo(Purchase.BUYER, identity);
        final ParseQuery<Purchase> involvedQuery = ParseQuery.getQuery(Purchase.CLASS);
        involvedQuery.whereEqualTo(Purchase.IDENTITIES, identity);

        final List<ParseQuery<Purchase>> queries = new ArrayList<>();
        queries.add(buyerQuery);
        queries.add(involvedQuery);

        final ParseQuery<Purchase> query = ParseQuery.or(queries);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.whereEqualTo(Purchase.GROUP, identity.getGroup());
        query.include(Purchase.ITEMS);
        query.include(Purchase.BUYER);
        query.include(Purchase.IDENTITIES);
        query.orderByDescending(Purchase.DATE);
        if (getDrafts) {
            query.whereEqualTo(Purchase.DRAFT, true);
            query.whereEqualTo(Purchase.BUYER, identity);
        } else {
            query.whereDoesNotExist(Purchase.DRAFT);
        }

        return find(query)
                .concatMap(new Func1<List<Purchase>, Observable<Purchase>>() {
                    @Override
                    public Observable<Purchase> call(List<Purchase> purchases) {
                        return Observable.from(purchases);
                    }
                });
    }

    @Override
    public Single<Purchase> getPurchase(@NonNull String purchaseId) {
        final ParseQuery<Purchase> query = ParseQuery.getQuery(Purchase.CLASS);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.include(Purchase.ITEMS);
        query.include(Purchase.BUYER);
        query.include(Purchase.IDENTITIES);
        if (!ParseUtils.isObjectId(purchaseId)) {
            query.whereEqualTo(Purchase.TEMP_ID, purchaseId);
            return first(query);
        }

        return get(query, purchaseId);
    }

    @Override
    public Single<Purchase> fetchPurchaseData(@NonNull String purchaseId) {
        final Purchase purchase = (Purchase) Purchase.createWithoutData(Purchase.CLASS, purchaseId);
        return fetchLocal(purchase);
    }

    @Override
    public Single<Purchase> deleteDraft(@NonNull Purchase purchase) {
        return unpin(purchase, Purchase.PIN_LABEL_TEMP)
                .flatMap(new Func1<Purchase, Single<? extends Purchase>>() {
                    @Override
                    public Single<? extends Purchase> call(final Purchase purchase) {
                        final Identity buyer = purchase.getBuyer();
                        return countDrafts(buyer)
                                .map(new Func1<Integer, Purchase>() {
                                    @Override
                                    public Purchase call(Integer count) {
                                        toggleDraftsAvailable(buyer, count > 0);
                                        return purchase;
                                    }
                                });
                    }
                });
    }

    private Single<Integer> countDrafts(@NonNull Identity identity) {
        final ParseQuery<Purchase> query = ParseQuery.getQuery(Purchase.CLASS);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.whereEqualTo(Purchase.DRAFT, true);
        query.whereEqualTo(Purchase.GROUP, identity.getGroup());
        query.whereEqualTo(Purchase.BUYER, identity);
        return count(query);
    }

    @Override
    public boolean deletePurchaseLocal(@NonNull String purchaseId, @NonNull String groupId) {
        final Purchase purchase = (Purchase) ParseObject.createWithoutData(Purchase.CLASS, purchaseId);
        try {
            purchase.unpin(Purchase.PIN_LABEL + groupId);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    @Override
    public Observable<Purchase> queryMorePurchases(@NonNull Identity currentIdentity,
                                                   int skip) {
        final Group currentGroup = currentIdentity.getGroup();
        final String pinLabel = Purchase.PIN_LABEL + currentGroup.getObjectId();
        final ParseQuery<Purchase> query = getPurchasesOnlineQuery(currentIdentity);
        query.setSkip(skip);

        return find(query)
                .concatMap(new Func1<List<Purchase>, Observable<List<Purchase>>>() {
                    @Override
                    public Observable<List<Purchase>> call(List<Purchase> purchases) {
                        return pinAll(purchases, pinLabel);
                    }
                })
                .concatMap(new Func1<List<Purchase>, Observable<Purchase>>() {
                    @Override
                    public Observable<Purchase> call(List<Purchase> purchases) {
                        return Observable.from(purchases);
                    }
                });
    }

    @NonNull
    private ParseQuery<Purchase> getPurchasesOnlineQuery(@NonNull Identity identity) {
        final ParseQuery<Purchase> buyerQuery = ParseQuery.getQuery(Purchase.CLASS);
        buyerQuery.whereEqualTo(Purchase.BUYER, identity);
        final ParseQuery<Purchase> involvedQuery = ParseQuery.getQuery(Purchase.CLASS);
        involvedQuery.whereEqualTo(Purchase.IDENTITIES, identity);

        final List<ParseQuery<Purchase>> queries = new ArrayList<>();
        queries.add(buyerQuery);
        queries.add(involvedQuery);

        final ParseQuery<Purchase> query = ParseQuery.or(queries);
        query.include(Purchase.ITEMS);
        query.include(Purchase.BUYER);
        query.include(Purchase.IDENTITIES);
        query.setLimit(QUERY_ITEMS_PER_PAGE);
        query.orderByDescending(Purchase.DATE);
        return query;
    }

    @Override
    public boolean updatePurchases(@NonNull final List<Identity> identities,
                                   @NonNull final Identity currentIdentity) {
        for (Identity identity : identities) {
            try {
                final ParseQuery<Purchase> query = getPurchasesOnlineQuery(identity);
                final List<Purchase> purchases = query.find();
                final Group group = identity.getGroup();
                final String pinLabel = Purchase.PIN_LABEL + group.getObjectId();

                ParseObject.unpinAll(pinLabel);
                ParseObject.pinAll(pinLabel, purchases);
            } catch (ParseException e) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean updatePurchase(@NonNull String purchaseId, boolean isNew) {
        final ParseQuery<Purchase> query = ParseQuery.getQuery(Purchase.CLASS);
        query.include(Purchase.ITEMS);
        query.include(Purchase.IDENTITIES);
        query.include(Purchase.BUYER);

        try {
            final Purchase purchase = query.get(purchaseId);
            final String groupId = purchase.getGroup().getObjectId();
            final String pinLabel = Purchase.PIN_LABEL + groupId;

            if (isNew) {
                purchase.pin(pinLabel);
            } else {
                // although we only update an existing purchase, we need to unpin and re-pin it
                // because the items have changed
                purchase.unpin(pinLabel);
                purchase.pin(pinLabel);
            }
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    @Override
    public Single<Purchase> savePurchase(@NonNull final Purchase purchase) {
        purchase.setRandomTempId();
        convertPrices(purchase, true);

        return pin(purchase, Purchase.PIN_LABEL_TEMP)
                .doOnSuccess(new Action1<Purchase>() {
                    @Override
                    public void call(Purchase purchase) {
                        if (purchase.isDraft()) {
                            toggleDraftsAvailable(purchase.getBuyer(), true);
                        } else {
                            SavePurchaseTaskService.scheduleSaveNew(mGcmNetworkManager,
                                    purchase.getTempId());
                        }
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        convertPrices(purchase, false);
                    }
                });
    }

    private void convertPrices(@NonNull Purchase purchase, boolean toGroupCurrency) {
        final double exchangeRate = purchase.getExchangeRate();
        if (exchangeRate == 1) {
            return;
        }

        final List<Item> items = purchase.getItems();
        for (Item item : items) {
            item.convertPrice(exchangeRate, toGroupCurrency);
        }

        purchase.convertTotalPrice(toGroupCurrency);
    }

    @Override
    public Single<Purchase> savePurchaseEdit(@NonNull final Purchase purchase,
                                             final boolean deleteOldReceipt) {
        final boolean wasDraft = purchase.isDraft();
        if (wasDraft) {
            purchase.removeDraft();
        }

        convertPrices(purchase, true);
        // we need to unpin and re-pin also for a non draft because the items have changed
        final String groupId = purchase.getGroup().getObjectId();
        final String pinLabel = Purchase.PIN_LABEL + groupId;
        return unpin(purchase, wasDraft ? Purchase.PIN_LABEL_TEMP : pinLabel)
                .flatMap(new Func1<Purchase, Single<? extends Purchase>>() {
                    @Override
                    public Single<? extends Purchase> call(final Purchase purchase) {
                        return pin(purchase, Purchase.PIN_LABEL_TEMP);
                    }
                })
                .flatMap(new Func1<Purchase, Single<? extends Purchase>>() {
                    @Override
                    public Single<? extends Purchase> call(final Purchase purchase) {
                        if (wasDraft) {
                            final Identity identity = purchase.getBuyer();
                            return countDrafts(identity)
                                    .map(new Func1<Integer, Purchase>() {
                                        @Override
                                        public Purchase call(Integer count) {
                                            toggleDraftsAvailable(identity, count > 0);
                                            return purchase;
                                        }
                                    });
                        }

                        return Single.just(purchase);
                    }
                })
                .doOnSuccess(new Action1<Purchase>() {
                    @Override
                    public void call(Purchase purchase) {
                        final String id = wasDraft
                                ? purchase.getTempId()
                                : purchase.getObjectId();
                        SavePurchaseTaskService.scheduleSaveEdit(mGcmNetworkManager, id, wasDraft,
                                deleteOldReceipt);
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        convertPrices(purchase, false);
                        if (wasDraft) {
                            purchase.setDraft(true);
                        }
                    }
                });
    }

    @Override
    public boolean uploadPurchase(@NonNull Context context, @NonNull String tempId) {
        final Purchase purchase;
        try {
            purchase = getPurchaseForUpload(tempId);
        } catch (ParseException e) {
            return false;
        }

        final String localReceiptPath = purchase.getReceiptLocal();
        if (purchase.getReceipt() == null && !TextUtils.isEmpty(localReceiptPath)) {
            try {
                final ParseFile receipt = saveReceiptFile(context, purchase, localReceiptPath);
                purchase.removeReceiptLocal();
                purchase.setReceipt(receipt);
            } catch (ParseException | ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

            deleteLocalReceipt(localReceiptPath);
        }

        try {
            purchase.removeTempId();
            purchase.save();
        } catch (ParseException e) {
            purchase.setTempId(tempId);
            return false;
        }


        try {
            purchase.unpin(Purchase.PIN_LABEL_TEMP);
            final String groupId = purchase.getGroup().getObjectId();
            final String pinLabel = Purchase.PIN_LABEL + groupId;
            purchase.pin(pinLabel);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    private Purchase getPurchaseForUpload(@NonNull String purchaseId) throws ParseException {
        final ParseQuery<Purchase> query = ParseQuery.getQuery(Purchase.CLASS);
        query.fromLocalDatastore();
        query.ignoreACLs();
        if (!ParseUtils.isObjectId(purchaseId)) {
            query.whereEqualTo(Purchase.TEMP_ID, purchaseId);
            return query.getFirst();
        }

        return query.get(purchaseId);
    }

    private ParseFile saveReceiptFile(@NonNull Context context, @NonNull Purchase purchase,
                                 @NonNull String localReceiptPath)
            throws ParseException, ExecutionException, InterruptedException {
        final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpg");
        final byte[] bytes = Glide.with(context)
                .load(localReceiptPath)
                .asBitmap()
                .toBytes(Bitmap.CompressFormat.JPEG, PurchaseRepository.JPEG_COMPRESSION_RATE)
                .into(PurchaseRepository.WIDTH, PurchaseRepository.HEIGHT)
                .get();
        final ParseFile receipt = new ParseFile(PurchaseRepository.FILE_NAME, bytes, mimeType);
        receipt.save();
        return receipt;
    }

    private void deleteLocalReceipt(@NonNull String localReceiptPath) {
        final File file = new File(localReceiptPath);
        final boolean fileDeleted = file.delete();
        if (!fileDeleted && BuildConfig.DEBUG) {
            Timber.e("Failed to delete local receipt file");
        }
    }

    @Override
    public boolean uploadPurchaseEdit(@NonNull Context context, @NonNull String purchaseId,
                                      boolean wasDraft,
                                      boolean deleteOldReceipt) {
        final Purchase purchase;
        try {
            purchase = getPurchaseForUpload(purchaseId);
            if (deleteOldReceipt) {
                deleteOldReceipt(purchase.getReceipt().getName());
            }
        } catch (ParseException e) {
            return false;
        }

        final String localReceiptPath = purchase.getReceiptLocal();
        if (!TextUtils.isEmpty(localReceiptPath)) {
            try {
                final ParseFile receipt = saveReceiptFile(context, purchase, localReceiptPath);
                purchase.removeReceiptLocal();
                purchase.setReceipt(receipt);
            } catch (ParseException | ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

            deleteLocalReceipt(localReceiptPath);
        }

        try {
            purchase.removeTempId();
            purchase.save();
        } catch (ParseException e) {
            if (wasDraft) {
                purchase.setTempId(purchaseId);
                purchase.setDraft(true);
                toggleDraftsAvailable(purchase.getBuyer(), true);
            }
            return false;
        }

        try {
            purchase.unpin(Purchase.PIN_LABEL_TEMP);
            final String groupId = purchase.getGroup().getObjectId();
            final String pinLabel = Purchase.PIN_LABEL + groupId;
            purchase.pin(pinLabel);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    private void deleteOldReceipt(@NonNull String receiptName) throws ParseException {
        final Map<String, Object> params = new HashMap<>();
        params.put(PARAM_FILE_NAME, receiptName);
        ParseCloud.callFunction(DELETE_PARSE_FILE, params);
    }

    @Override
    public void deletePurchase(@NonNull Purchase purchase) {
        purchase.deleteEventually();
    }

    @Override
    public Observable<String> uploadReceipt(@NonNull String sessionToken,
                                            @NonNull final String receiptPath) {
        final RequestBody tokenPart = RequestBody.create(
                MediaType.parse("text/plain"), sessionToken);

        final File file = new File(receiptPath);
        final RequestBody receiptPart = RequestBody.create(
                MediaType.parse("image/jpeg"), file);
        final MultipartBody.Part body =
                MultipartBody.Part.createFormData("receipt", file.getName(), receiptPart);

        return mReceiptOcr.uploadReceipt(tokenPart, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Void, String>() {
                    @Override
                    public String call(Void aVoid) {
                        return receiptPath;
                    }
                });
    }

    @Override
    public boolean isDraftsAvailable(@NonNull Identity identity) {
        return mSharedPrefs.getBoolean(DRAFTS_AVAILABLE + identity.getObjectId(), false);
    }

    @Override
    public void toggleDraftsAvailable(@NonNull Identity identity, boolean available) {
        mSharedPrefs.edit()
                .putBoolean(DRAFTS_AVAILABLE + identity.getObjectId(), available)
                .apply();
    }

    @Override
    public Single<Float> getExchangeRate(@NonNull String baseCurrency, @NonNull String currency) {
        if (currency.equals(baseCurrency)) {
            return Single.just(1f);
        }

        final float rate = mSharedPrefs.getFloat(currency, 1);
        if (rate == 1) {
            return loadExchangeRates(baseCurrency, currency);
        } else {
            long lastFetched = mSharedPrefs.getLong(EXCHANGE_RATE_LAST_FETCHED_TIME, 0);
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFetched > EXCHANGE_RATE_REFRESH_INTERVAL) {
                return loadExchangeRates(baseCurrency, currency);
            } else {
                return Single.just(rate);
            }
        }
    }

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
                        final SharedPreferences.Editor editor = mSharedPrefs.edit();
                        for (Map.Entry<String, Float> exchangeRate : exchangeRates.entrySet()) {
                            final BigDecimal roundedExchangeRate =
                                    MoneyUtils.roundExchangeRate(1 / exchangeRate.getValue());
                            editor.putFloat(exchangeRate.getKey(), roundedExchangeRate.floatValue());
                        }
                        final long currentTime = System.currentTimeMillis();
                        editor.putLong(ParsePurchaseRepository.EXCHANGE_RATE_LAST_FETCHED_TIME, currentTime);

                        editor.apply();
                    }
                })
                .map(new Func1<Map<String, Float>, Float>() {
                    @Override
                    public Float call(Map<String, Float> exchangeRates) {
                        return exchangeRates.get(currency);
                    }
                });
    }

    @Override
    public Single<OcrPurchase> fetchOcrPurchaseData(@NonNull String ocrPurchaseId) {
        final OcrPurchase purchase = (OcrPurchase) ParseObject.createWithoutData(OcrPurchase.CLASS, ocrPurchaseId);
        return fetchLocal(purchase);
    }

    @Override
    public boolean updateOcrPurchase(@NonNull String ocrPurchaseId) {
        final ParseQuery<OcrPurchase> query = ParseQuery.getQuery(OcrPurchase.CLASS);
        try {
            final OcrPurchase ocrPurchase = query.get(ocrPurchaseId);
            ocrPurchase.pin();
        } catch (ParseException e) {
            return false;
        }

        return true;
    }
}
