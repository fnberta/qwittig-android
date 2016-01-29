/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.ParseObject;

import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.models.parse.User;
import rx.Observable;
import rx.Single;

/**
 * Provides the methods to get, update and remove purchases from the local and online data store.
 */
public interface PurchaseRepository extends Repository {

    /**
     * Queries the local data store for purchases.
     *
     * @param currentUser the current user
     * @param group       the group for which to get purchases for
     * @param getDrafts   whether to query for drafts or purchases
     */
    Observable<Purchase> getPurchasesLocalAsync(@NonNull User currentUser, @NonNull Group group,
                                                boolean getDrafts);

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
     * @param groupId  the object id of the group the purchase belongs to
     * @return a {@link Single} representing the save action
     */
    Single<Purchase> removePurchaseLocalAsync(@NonNull Purchase purchase, @Nullable String groupId);

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
     * @param currentUser    the current user
     * @param groups         the groups for which to update the purchases
     * @param currentGroupId the object id of the user's current group
     */
    Observable<Purchase> updatePurchasesAsync(@NonNull User currentUser,
                                              @NonNull List<ParseObject> groups,
                                              @NonNull String currentGroupId);

    /**
     * Queries purchases from the online data store and saves them in the local data store.
     *
     * @param currentUser the current user
     * @param group       the group for which to get purchases for
     * @param skip        the number of purchases to skip for the query
     */
    Observable<Purchase> getPurchasesOnlineAsync(@NonNull User currentUser, @NonNull Group group,
                                                 int skip);

    /**
     * Deletes all purchases from the local data store and saves new ones.
     *
     * @param currentUser the current user
     * @param groups      the groups for which to update the purchases
     * @return whether the update was successful or not
     */
    boolean updatePurchases(@NonNull User currentUser, @NonNull List<ParseObject> groups);

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
     * @param isDraft
     * @return a {@link Single} emitting the save stream
     */
    Single<Purchase> savePurchaseAsync(@NonNull Purchase purchase, @NonNull String tag,
                                       @Nullable byte[] receiptImage, boolean isDraft);

    /**
     * Saves a {@link Purchase} object as a draft, meaning only to the local offline datastore.
     *
     * @param purchase the purchase to save
     * @param tag      the tag save the purche in the offline storage
     * @return a {@link Single} emitting the save stream
     */
    Single<Purchase> savePurchaseAsDraftAsync(@NonNull Purchase purchase, @NonNull String tag);

    void deleteItemsByIds(@NonNull List<String> itemIds);
}
