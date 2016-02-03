/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.data.rest.ExchangeRates;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Item;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.models.rates.CurrencyRates;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Provides an implementation of {@link PurchaseRepository} that uses the Parse.com framework as
 * the local and online data store.
 */
public class ParsePurchaseRepository extends ParseBaseRepository<Purchase> implements
        PurchaseRepository {

    private static final String DRAFTS_AVAILABLE = "DRAFTS_AVAILABLE";
    private static final String EXCHANGE_RATE_LAST_FETCHED_TIME = "EXCHANGE_RATE_LAST_FETCHED_TIME";
    private static final long EXCHANGE_RATE_REFRESH_INTERVAL = 24 * 60 * 60 * 1000;
    private SharedPreferences mSharedPreferences;
    private ExchangeRates mExchangeRates;

    public ParsePurchaseRepository() {
    }

    public ParsePurchaseRepository(@NonNull SharedPreferences sharedPreferences,
                                   @NonNull ExchangeRates exchangeRates) {
        super();

        mSharedPreferences = sharedPreferences;
        mExchangeRates = exchangeRates;
    }

    @Override
    protected String getClassName() {
        return Purchase.CLASS;
    }

    @Override
    public Observable<Purchase> getPurchasesLocalAsync(@NonNull User currentUser,
                                                       @NonNull Group group, boolean getDrafts) {
        final ParseQuery<Purchase> query = getPurchasesLocalQuery();
        query.whereEqualTo(Purchase.GROUP, currentUser.getCurrentGroup());
        if (getDrafts) {
            query.whereExists(Purchase.DRAFT_ID);
            query.whereEqualTo(Purchase.BUYER, currentUser);
        } else {
            query.whereDoesNotExist(Purchase.DRAFT_ID);
        }

        return find(query).flatMap(new Func1<List<Purchase>, Observable<Purchase>>() {
            @Override
            public Observable<Purchase> call(List<Purchase> purchases) {
                return Observable.from(purchases);
            }
        });
    }

    @Override
    public Single<Purchase> getPurchaseLocalAsync(@NonNull String purchaseId, boolean isDraft) {
        final ParseQuery<Purchase> query = getPurchasesLocalQuery();
        if (isDraft) {
            query.whereEqualTo(Purchase.DRAFT_ID, purchaseId);
            return first(query);
        }

        return get(query, purchaseId);
    }

    @NonNull
    private ParseQuery<Purchase> getPurchasesLocalQuery() {
        final ParseQuery<Purchase> query = ParseQuery.getQuery(Purchase.CLASS);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.include(Purchase.ITEMS);
        query.include(Purchase.BUYER);
        query.include(Purchase.USERS_INVOLVED);
        query.orderByDescending(Purchase.DATE);
        return query;
    }

    @Override
    public Single<Purchase> fetchPurchaseDataLocalAsync(@NonNull String purchaseId) {
        final Purchase purchase = (Purchase) Purchase.createWithoutData(Purchase.CLASS, purchaseId);
        return fetchLocal(purchase);
    }

    @Override
    public Single<Purchase> removePurchaseLocalAsync(@NonNull final Purchase purchase,
                                                     @NonNull String tag) {
        return unpin(purchase, tag)
                .flatMap(new Func1<Purchase, Single<Integer>>() {
                    @Override
                    public Single<Integer> call(Purchase purchase) {
                        return countDrafts(purchase.getGroup());
                    }
                })
                .map(new Func1<Integer, Purchase>() {
                    @Override
                    public Purchase call(Integer integer) {
                        final SharedPreferences.Editor editor = mSharedPreferences.edit();
                        if (integer > 0) {
                            editor.putBoolean(DRAFTS_AVAILABLE, true);
                        } else {
                            editor.putBoolean(DRAFTS_AVAILABLE, false);
                        }
                        editor.apply();

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
    public boolean removePurchaseLocal(@NonNull String purchaseId, @NonNull String groupId) {
        final ParseObject purchase = ParseObject.createWithoutData(Purchase.CLASS, purchaseId);
        try {
            purchase.unpin(Purchase.PIN_LABEL + groupId);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    @Override
    public Observable<Purchase> updatePurchasesAsync(@NonNull final User currentUser,
                                                     @NonNull List<ParseObject> groups,
                                                     @NonNull final String currentGroupId) {
        return Observable.from(groups)
                .flatMap(new Func1<ParseObject, Observable<Purchase>>() {
                    @Override
                    public Observable<Purchase> call(ParseObject parseObject) {
                        final String groupId = parseObject.getObjectId();
                        final String pinLabel = Purchase.PIN_LABEL + groupId;

                        ParseQuery<Purchase> query = getPurchasesOnlineQuery(currentUser);
                        query.whereEqualTo(Purchase.GROUP, parseObject);
                        return find(query)
                                .flatMap(new Func1<List<Purchase>, Observable<List<Purchase>>>() {
                                    @Override
                                    public Observable<List<Purchase>> call(List<Purchase> purchases) {
                                        return unpinAll(purchases, pinLabel);
                                    }
                                })
                                .flatMap(new Func1<List<Purchase>, Observable<List<Purchase>>>() {
                                    @Override
                                    public Observable<List<Purchase>> call(List<Purchase> purchases) {
                                        return pinAll(purchases, pinLabel);
                                    }
                                })
                                .flatMap(new Func1<List<Purchase>, Observable<Purchase>>() {
                                    @Override
                                    public Observable<Purchase> call(List<Purchase> purchases) {
                                        return Observable.from(purchases);
                                    }
                                })
                                .filter(new Func1<Purchase, Boolean>() {
                                    @Override
                                    public Boolean call(Purchase purchase) {
                                        return groupId.equals(currentGroupId);
                                    }
                                });
                    }
                });
    }

    @NonNull
    private ParseQuery<Purchase> getPurchasesOnlineQuery(@NonNull User currentUser) {
        final ParseQuery<Purchase> buyerQuery = ParseQuery.getQuery(Purchase.CLASS);
        buyerQuery.whereEqualTo(Purchase.BUYER, currentUser);

        final ParseQuery<Purchase> involvedQuery = ParseQuery.getQuery(Purchase.CLASS);
        involvedQuery.whereEqualTo(Purchase.USERS_INVOLVED, currentUser);

        final List<ParseQuery<Purchase>> queries = new ArrayList<>();
        queries.add(buyerQuery);
        queries.add(involvedQuery);

        final ParseQuery<Purchase> query = ParseQuery.or(queries);
        query.include(Purchase.ITEMS);
        query.include(Purchase.BUYER);
        query.include(Purchase.USERS_INVOLVED);
        query.setLimit(QUERY_ITEMS_PER_PAGE);
        query.orderByDescending(Purchase.DATE);
        return query;
    }

    @Override
    public Observable<Purchase> getPurchasesOnlineAsync(@NonNull User currentUser,
                                                        @NonNull final Group group, int skip) {
        final ParseQuery<Purchase> query = getPurchasesOnlineQuery(currentUser);
        query.setSkip(skip);
        query.whereEqualTo(Purchase.GROUP, group);

        return find(query)
                .flatMap(new Func1<List<Purchase>, Observable<List<Purchase>>>() {
                    @Override
                    public Observable<List<Purchase>> call(List<Purchase> purchases) {
                        final String tag = Purchase.PIN_LABEL + group.getObjectId();
                        return pinAll(purchases, tag);
                    }
                })
                .flatMap(new Func1<List<Purchase>, Observable<Purchase>>() {
                    @Override
                    public Observable<Purchase> call(List<Purchase> purchases) {
                        return Observable.from(purchases);
                    }
                });
    }

    @Override
    public boolean updatePurchases(@NonNull User currentUser, @NonNull List<ParseObject> groups) {
        if (groups.isEmpty()) {
            return false;
        }

        for (ParseObject group : groups) {
            try {
                final List<Purchase> purchases = getPurchasesForGroupOnline(currentUser, group);
                final String groupId = group.getObjectId();
                final String label = Purchase.PIN_LABEL + groupId;

                ParseObject.unpinAll(label);
                ParseObject.pinAll(label, purchases);
            } catch (ParseException e) {
                return false;
            }
        }

        return true;
    }

    private List<Purchase> getPurchasesForGroupOnline(@NonNull User currentUser,
                                                      @NonNull ParseObject group)
            throws ParseException {
        final ParseQuery<Purchase> query = getPurchasesOnlineQuery(currentUser);
        query.whereEqualTo(Purchase.GROUP, group);
        return query.find();
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
                // although we only update an existing purchase, we need to unpin and repin it
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
        query.include(Purchase.USERS_INVOLVED);
        query.include(Purchase.BUYER);
        return (Purchase) query.get(objectId);
    }

    @Override
    public Single<Purchase> savePurchaseAsync(@NonNull final Purchase purchase,
                                              @NonNull final String tag,
                                              @Nullable byte[] receiptImage, final boolean isDraft) {
        if (receiptImage == null) {
            return saveAndPinPurchase(purchase, tag, isDraft);
        }

        return saveReceiptFile(receiptImage).flatMap(new Func1<ParseFile, Single<? extends Purchase>>() {
            @Override
            public Single<? extends Purchase> call(ParseFile parseFile) {
                purchase.setReceiptParseFile(parseFile);
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

    private Single<ParseFile> saveReceiptFile(@NonNull byte[] receiptImage) {
        final ParseFile parseFile = new ParseFile(receiptImage);
        return Single.create(new Single.OnSubscribe<ParseFile>() {
            @Override
            public void call(final SingleSubscriber<? super ParseFile> singleSubscriber) {
                parseFile.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            singleSubscriber.onError(e);
                        } else {
                            singleSubscriber.onSuccess(parseFile);
                        }
                    }
                });
            }
        });
    }

    private void convertPrices(@NonNull Purchase purchase, boolean toGroupCurrency) {
        final float exchangeRate = purchase.getExchangeRate();
        if (exchangeRate == 1) {
            return;
        }

        List<ParseObject> items = purchase.getItems();
        for (ParseObject parseObject : items) {
            final Item item = (Item) parseObject;
            item.convertPrice(exchangeRate, toGroupCurrency);
        }

        purchase.convertTotalPrice(toGroupCurrency);
    }

    @Override
    public Single<Purchase> savePurchaseAsDraftAsync(@NonNull Purchase purchase,
                                                     @NonNull String tag) {
        return pin(purchase, tag)
                .doOnSuccess(new Action1<Purchase>() {
                    @Override
                    public void call(Purchase purchase) {
                        final SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putBoolean(DRAFTS_AVAILABLE, true);
                        editor.apply();
                    }
                });
    }

    @Override
    public Single<byte[]> getPurchaseReceiptImageAsync(@NonNull final Purchase purchase) {
        return Single.create(new Single.OnSubscribe<byte[]>() {
            @Override
            public void call(final SingleSubscriber<? super byte[]> singleSubscriber) {
                final ParseFile file = purchase.getReceiptParseFile();
                file.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        if (singleSubscriber.isUnsubscribed()) {
                            return;
                        }

                        if (e != null) {
                            singleSubscriber.onError(e);
                        } else {
                            singleSubscriber.onSuccess(data);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void deleteItemsByIds(@NonNull List<String> itemIds) {
        for (String itemId : itemIds) {
            final ParseObject item = ParseObject.createWithoutData(Item.CLASS, itemId);
            item.deleteEventually();
        }
    }

    @Override
    public void deletePurchase(@NonNull Purchase purchase) {
        purchase.deleteEventually();
    }

    @Override
    public boolean isPurchaseDraftsAvailable() {
        return mSharedPreferences.getBoolean(DRAFTS_AVAILABLE, false);
    }

    @Override
    public Single<Float> getExchangeRate(@NonNull String baseCurrency, @NonNull String currency) {
        if (currency.equals(baseCurrency)) {
            return Single.just(1f);
        }

        final float rate = mSharedPreferences.getFloat(currency, 1);
        if (rate == 1) {
            return loadExchangeRates(baseCurrency, currency);
        } else {
            long lastFetched = mSharedPreferences.getLong(EXCHANGE_RATE_LAST_FETCHED_TIME, 0);
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
                        final SharedPreferences.Editor editor = mSharedPreferences.edit();
                        for (Map.Entry<String, Float> exchangeRate : exchangeRates.entrySet()) {
                            final BigDecimal roundedExchangeRate = MoneyUtils.roundToFractionDigits(
                                    MoneyUtils.EXCHANGE_RATE_FRACTION_DIGITS, 1 / exchangeRate.getValue());
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
