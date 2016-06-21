/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.Set;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.OcrData;
import ch.giantific.qwittig.domain.models.Purchase;
import rx.Observable;
import rx.Single;

/**
 * Provides the methods to get, update and remove purchases from the local and online data store.
 */
public interface PurchaseRepository extends BaseRepository {

    String FILE_NAME = "receipt.jpg";
    int JPEG_COMPRESSION_RATE = 100;
    int HEIGHT = 2048;
    int WIDTH = 1024;

    /**
     * Queries the local data store for purchases.
     *
     * @param currentIdentity the current identity of the user
     * @param getDrafts       whether to query for drafts or purchases
     * @return a {@link Observable} emitting the results
     */
    Observable<Purchase> getPurchases(@NonNull Identity currentIdentity, boolean getDrafts);

    /**
     * Queries the local data store for a single purchase.
     *
     * @param purchaseId the object id of the purchase to query
     * @return a {@link Single} emitting the result
     */
    Single<Purchase> getPurchase(@NonNull String purchaseId);

    /**
     * Fetches the data of a purchase from the local data store.
     *
     * @param purchaseId the object id of the purchase to fetch
     * @return a {@link Single} emitting the result
     */
    Single<Purchase> fetchPurchaseData(@NonNull String purchaseId);

    /**
     * Removes a purchase from the local data store.
     *
     * @param purchase the purchase to remove
     * @return a {@link Single} emitting the result
     */
    Single<Purchase> deleteDraft(@NonNull Purchase purchase);

    /**
     * Removes a purchase from the local data store.
     *
     * @param purchaseId the object id of the purchase to remove
     * @param groupId    the object id of the group the purchase belongs to
     * @return whether the removal was successful or not
     */
    boolean deletePurchaseLocal(@NonNull String purchaseId, @NonNull String groupId);

    /**
     * Queries purchases from the online data store and saves them in the local data store.
     *
     * @param currentIdentity the current identity of the user
     * @param skip            the number of purchases to skip for the query
     * @return a {@link Observable} emitting the results
     */
    Observable<Purchase> queryMorePurchases(@NonNull Identity currentIdentity, int skip);

    /**
     * Deletes all purchases from the local data store and saves new ones.
     *
     * @param identities      the identities of the current user
     * @param currentIdentity the user's current identity
     * @return whether the update was successful or not
     */
    boolean updatePurchases(@NonNull final List<Identity> identities, @NonNull final Identity currentIdentity);

    /**
     * Updates a purchase if is already available in the local data store (by simply querying it) or
     * saves it for the first time to the local data store if not.
     *
     * @param purchaseId the object id of the purchase to query
     * @param isNew      whether the purchase is already available in the local data store or not
     * @return whether the update was successful or not
     */
    boolean updatePurchase(@NonNull String purchaseId, boolean isNew);

    /**
     * Saves a {@link Purchase} object to the online and offline storage.
     *
     * @param purchase the purchase to save
     * @return a {@link Single} emitting the result
     */
    Single<Purchase> savePurchase(@NonNull Purchase purchase);

    Single<Purchase> savePurchaseEdit(@NonNull final Purchase purchase,
                                      final boolean deleteOldReceipt);

    boolean uploadPurchase(@NonNull Context context, @NonNull String tempId);

    boolean uploadPurchaseEdit(@NonNull Context context, @NonNull String purchaseId,
                               boolean wasDraft, boolean deleteOldReceipt);

    /**
     * Deletes the purchase.
     *
     * @param purchase the purchase to delete
     */
    void deletePurchase(@NonNull Purchase purchase);

    Observable<Void> uploadReceipt(@NonNull String sessionToken, @NonNull byte[] receipt);

    /**
     * Returns whether drafts are available for the current identity of the user.
     *
     * @param identity the identity for which to check for drafts
     * @return whether drafts are available
     */
    boolean isDraftsAvailable(@NonNull Identity identity);

    /**
     * Toggle the save setting whether drafts are available or not.
     *
     * @param identity  the identity to toggle the drafts availability for
     * @param available whether drafts are available or not
     */
    void toggleDraftsAvailable(@NonNull Identity identity, boolean available);

    /**
     * Returns the exchange rate for the given currency.
     *
     * @param baseCurrency the base currency of which to calculate the rate
     * @param currency     the currency to get the rate for
     * @return a {@link Single} emitting the result
     */
    Single<Float> getExchangeRate(@NonNull String baseCurrency, @NonNull String currency);

    Single<OcrData> fetchOcrData(@NonNull String ocrPurchaseId);

    boolean updateOcrPurchase(@NonNull String ocrPurchaseId);

    void cacheOldEditItems(@NonNull Set<String> itemIds);
}
