/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.ParseFile;

import java.util.List;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import rx.Observable;
import rx.Single;

/**
 * Provides the methods to get, update and remove purchases from the local and online data store.
 */
public interface PurchaseRepository extends BaseRepository {

    String FILE_NAME = "receipt.jpg";
    int JPEG_COMPRESSION_RATE = 80; // TODO: figure out real value
    int HEIGHT = 720; // TODO: figure out real height and width
    int WIDTH = 720;

    /**
     * Queries the local data store for purchases.
     *
     * @param currentIdentity the current identity of the user
     * @param getDrafts       whether to query for drafts or purchases
     */
    Observable<Purchase> getPurchasesLocalAsync(@NonNull Identity currentIdentity, boolean getDrafts);

    /**
     * Queries the local data store for a single purchase.
     *
     * @param purchaseId the object id of the purchase to query
     */
    Single<Purchase> getPurchaseLocalAsync(@NonNull String purchaseId, boolean isDraft);

    /**
     * Fetches the data of a purchase from the local data store.
     *
     * @param purchaseId the object id of the purchase to fetch
     */
    Single<Purchase> fetchPurchaseDataLocalAsync(@NonNull String purchaseId);

    /**
     * Removes a purchase from the local data store.
     *
     * @param purchase the purchase to remove
     * @param tag      the pin tag the purchase uses
     * @return a {@link Single} representing the save action
     */
    Single<Purchase> removePurchaseLocalAsync(@NonNull Purchase purchase, @NonNull String tag);

    /**
     * Removes a purchase from the local data store.
     *
     * @param purchaseId the object id of the purchase to remove
     * @param groupId    the object id of the group the purchase belongs to
     * @return whether the removal was successful or not
     */
    boolean removePurchaseLocal(@NonNull String purchaseId, @NonNull String groupId);

    /**
     * Updates all purchases in the local data store by deleting all purchases from the local data
     * store, querying and saving new ones.
     *
     * @param identities      the identities of the current user
     * @param currentIdentity the user's current identity
     */
    Observable<Purchase> updatePurchasesAsync(@NonNull List<Identity> identities, @NonNull Identity currentIdentity);

    /**
     * Queries purchases from the online data store and saves them in the local data store.
     *
     * @param currentIdentity the current identity of the user
     * @param skip            the number of purchases to skip for the query
     */
    Observable<Purchase> getPurchasesOnlineAsync(@NonNull Identity currentIdentity,
                                                 int skip);

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
     * @param purchase     the purchase to save
     * @param tag          the tag to save the purchase in the offline storage
     * @param receiptImage the receipt image to attach to the purchase
     * @param isDraft      whether to save the purchase as a draft
     * @return a {@link Single} emitting the save stream
     */
    Single<Purchase> savePurchaseAsync(@NonNull Purchase purchase, @NonNull String tag,
                                       @Nullable byte[] receiptImage, boolean isDraft);

    /**
     * Saves a {@link Purchase} object as a draft, meaning only to the local offline datastore.
     *
     * @param purchase the purchase to save
     * @param tag      the tag save the purchase in the offline storage
     * @return a {@link Single} emitting the save stream
     */
    Single<Purchase> savePurchaseAsDraftAsync(@NonNull Purchase purchase, @NonNull String tag);

    /**
     * Deletes the the specified {@link ParseFile}, probably a receipt image that is no longer
     * needed.
     *
     * @param fileName the file name of the {@link ParseFile} to delete
     */
    Single<String> deleteReceipt(@NonNull String fileName);

    void deleteItemsByIds(@NonNull List<String> itemIds);

    void deletePurchase(@NonNull Purchase purchase);

    boolean isDraftsAvailable();

    void toggleDraftsAvailable(boolean available);

    Single<Float> getExchangeRate(@NonNull String baseCurrency, @NonNull String currency);
}
