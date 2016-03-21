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
     * @return a {@link Observable} emitting the results
     */
    Observable<Purchase> getPurchases(@NonNull Identity currentIdentity, boolean getDrafts);

    /**
     * Queries the local data store for a single purchase.
     *
     * @param purchaseId the object id of the purchase to query
     * @return a {@link Single} emitting the result
     */
    Single<Purchase> getPurchase(@NonNull String purchaseId, boolean isDraft);

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
    Single<Purchase> removeDraft(@NonNull Purchase purchase);

    /**
     * Removes a purchase from the local data store.
     *
     * @param purchaseId the object id of the purchase to remove
     * @param groupId    the object id of the group the purchase belongs to
     * @return whether the removal was successful or not
     */
    boolean removePurchaseLocal(@NonNull String purchaseId, @NonNull String groupId);

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
     * @param purchase     the purchase to save
     * @param tag          the tag to save the purchase in the offline storage
     * @param receiptImage the receipt image to attach to the purchase
     * @param isDraft      whether to save the purchase as a draft
     * @return a {@link Single} emitting the result
     */
    Single<Purchase> savePurchase(@NonNull Purchase purchase, @NonNull String tag,
                                  @Nullable byte[] receiptImage, boolean isDraft);

    /**
     * Saves a {@link Purchase} object as a draft, meaning only to the local offline datastore.
     *
     * @param purchase the purchase to save
     * @param tag      the tag save the purchase in the offline storage
     * @return a {@link Single} emitting the result
     */
    Single<Purchase> savePurchaseAsDraft(@NonNull Purchase purchase, @NonNull String tag);

    /**
     * Deletes the the specified {@link ParseFile}, probably a receipt image that is no longer
     * needed.
     *
     * @param fileName the file name of the {@link ParseFile} to delete
     * @return a {@link Single} emitting the result
     */
    Single<String> deleteReceipt(@NonNull String fileName);

    /**
     * Deletes the items with the specified object ids.
     *
     * @param itemIds the ids of the items to delete
     */
    void deleteItemsByIds(@NonNull List<String> itemIds);

    /**
     * Deletes the purchase.
     *
     * @param purchase the purchase to delete
     */
    void deletePurchase(@NonNull Purchase purchase);

    /**
     * Returns whether drafts are available for the current identity of the user.
     *
     * @return whether drafts are available
     * @param identity
     */
    boolean isDraftsAvailable(@NonNull Identity identity);

    /**
     * Toggle the save setting whether drafts are available or not.
     *
     * @param identity
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
}
