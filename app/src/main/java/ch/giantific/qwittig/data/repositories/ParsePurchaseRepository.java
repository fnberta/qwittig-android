/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.data.rest.CurrencyRates;
import ch.giantific.qwittig.data.rest.ExchangeRates;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Item;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

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

    public ParsePurchaseRepository(@NonNull SharedPreferences sharedPreferences,
                                   @NonNull ExchangeRates exchangeRates) {
        super();

        mSharedPrefs = sharedPreferences;
        mExchangeRates = exchangeRates;
    }

    @Override
    protected String getClassName() {
        return Purchase.CLASS;
    }

    @Override
    public Observable<Purchase> getPurchases(@NonNull Identity identity,
                                             boolean getDrafts) {
        final ParseQuery<Purchase> buyerQuery = ParseQuery.getQuery(Purchase.CLASS);
        buyerQuery.whereEqualTo(Purchase.BUYER, identity);
        final ParseQuery<Purchase> involvedQuery = ParseQuery.getQuery(Purchase.CLASS);
        involvedQuery.whereEqualTo(Purchase.IDENTITIES, identity);

        final List<ParseQuery<Purchase>> queries = new ArrayList<>();
        queries.add(buyerQuery);
        queries.add(involvedQuery);

        final ParseQuery<Purchase> query = ParseQuery.or(queries);
        query.whereEqualTo(Purchase.GROUP, identity.getGroup());
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.include(Purchase.ITEMS);
        query.include(Purchase.BUYER);
        query.include(Purchase.IDENTITIES);
        query.orderByDescending(Purchase.DATE);
        if (getDrafts) {
            query.whereExists(Purchase.DRAFT_ID);
            query.whereEqualTo(Purchase.BUYER, identity);
        } else {
            query.whereDoesNotExist(Purchase.DRAFT_ID);
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
    public Single<Purchase> getPurchase(@NonNull String purchaseId, boolean isDraft) {
        final ParseQuery<Purchase> query = ParseQuery.getQuery(Purchase.CLASS);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.include(Purchase.ITEMS);
        query.include(Purchase.BUYER);
        query.include(Purchase.IDENTITIES);
        query.orderByDescending(Purchase.DATE);
        if (isDraft) {
            query.whereEqualTo(Purchase.DRAFT_ID, purchaseId);
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
    public Single<Purchase> removeDraft(@NonNull final Purchase purchase) {
        return unpin(purchase, Purchase.PIN_LABEL_DRAFT)
                .flatMap(new Func1<Purchase, Single<Integer>>() {
                    @Override
                    public Single<Integer> call(Purchase purchase) {
                        return countDrafts(purchase.getGroup());
                    }
                })
                .map(new Func1<Integer, Purchase>() {
                    @Override
                    public Purchase call(Integer count) {
                        toggleDraftsAvailable(purchase.getBuyer(), count > 0);
                        return purchase;
                    }
                });
    }

    private Single<Integer> countDrafts(@NonNull Group currentGroup) {
        final ParseQuery<Purchase> query = ParseQuery.getQuery(Purchase.CLASS);
        query.fromPin(Purchase.PIN_LABEL_DRAFT);
        query.whereEqualTo(Purchase.GROUP, currentGroup);
        return count(query);
    }

    @Override
    public boolean removePurchase(@NonNull String purchaseId, @NonNull String groupId) {
        final ParseObject purchase = ParseObject.createWithoutData(Purchase.CLASS, purchaseId);
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
        try {
            final Purchase purchase = getPurchaseOnline(purchaseId);
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

    private Purchase getPurchaseOnline(@NonNull String objectId) throws ParseException {
        final ParseQuery<ParseObject> query = ParseQuery.getQuery(Purchase.CLASS);
        query.include(Purchase.ITEMS);
        query.include(Purchase.IDENTITIES);
        query.include(Purchase.BUYER);
        return (Purchase) query.get(objectId);
    }

    @Override
    public Single<Purchase> savePurchase(@NonNull final Purchase purchase,
                                         @NonNull final String tag,
                                         @Nullable byte[] receiptImage,
                                         final boolean isDraft) {
        if (receiptImage == null) {
            return saveAndPinPurchase(purchase, tag, isDraft);
        }

        final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpg");
        final ParseFile receipt = new ParseFile(PurchaseRepository.FILE_NAME, receiptImage, mimeType);
        return saveFile(receipt)
                .flatMap(new Func1<ParseFile, Single<? extends Purchase>>() {
                    @Override
                    public Single<? extends Purchase> call(ParseFile parseFile) {
                        purchase.setReceipt(parseFile);
                        return saveAndPinPurchase(purchase, tag, isDraft);
                    }
                });
    }

    private Single<Purchase> saveAndPinPurchase(@NonNull final Purchase purchase,
                                                @NonNull final String tag, final boolean isDraft) {
        convertPrices(purchase, true);
        return save(purchase)
                .flatMap(new Func1<Purchase, Single<? extends Purchase>>() {
                    @Override
                    public Single<? extends Purchase> call(Purchase purchase) {
                        if (isDraft) {
                            return unpin(purchase, Purchase.PIN_LABEL_DRAFT)
                                    .flatMap(new Func1<Purchase, Single<? extends Purchase>>() {
                                        @Override
                                        public Single<? extends Purchase> call(Purchase purchase) {
                                            return pin(purchase, tag);
                                        }
                                    });
                        } else {
                            return pin(purchase, tag);
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

        List<Item> items = purchase.getItems();
        for (Item item : items) {
            item.convertPrice(exchangeRate, toGroupCurrency);
        }

        purchase.convertTotalPrice(toGroupCurrency);
    }

    @Override
    public Single<Purchase> savePurchaseAsDraft(@NonNull Purchase purchase,
                                                @NonNull String tag) {
        return pin(purchase, tag)
                .doOnSuccess(new Action1<Purchase>() {
                    @Override
                    public void call(Purchase purchase) {
                        toggleDraftsAvailable(purchase.getBuyer(), true);
                    }
                });
    }

    @Override
    public Single<String> deleteReceipt(@NonNull String fileName) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_FILE_NAME, fileName);

        return callFunctionInBackground(DELETE_PARSE_FILE, params);
    }

    @Override
    public void deleteItemsByIds(@NonNull List<String> itemIds) {
        for (String itemId : itemIds) {
            final Item item = (Item) Item.createWithoutData(Item.CLASS, itemId);
            item.deleteEventually();
        }
    }

    @Override
    public void deletePurchase(@NonNull Purchase purchase) {
        purchase.deleteEventually();
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
}
