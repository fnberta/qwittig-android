/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.parse.ParseObject;

import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.models.parse.User;

/**
 * Provides the methods to get, update and remove purchases from the local and online data store.
 */
public interface PurchaseRepository {

    /**
     * Queries the local data store for purchases .
     *
     * @param currentUser the current user
     * @param getDrafts   whether to query for drafts or purchases
     * @param listener    the callback when the query finishes
     */
    void getPurchasesLocalAsync(@NonNull User currentUser, boolean getDrafts,
                                @NonNull GetPurchasesLocalListener listener);

    /**
     * Queries the local data store for a single purchase.
     *
     * @param purchaseId the object id of the purchase to query
     * @param listener   the callback when the query finishes
     */
    void getPurchaseLocalAsync(@NonNull String purchaseId, boolean isDraft,
                               @NonNull GetPurchaseLocalListener listener);

    /**
     * Fetches the data of a purchase from the local data store.
     *
     * @param purchaseId the object id of the purchase to fetch
     * @param listener   the callback when the fetch finishes
     */
    void fetchPurchaseDataLocalAsync(@NonNull String purchaseId,
                                     @NonNull GetPurchaseLocalListener listener);

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
     * @param listener       the callback when a query finishes, fails or all queries are finished
     */
    void updatePurchasesAsync(@NonNull User currentUser, @NonNull List<ParseObject> groups,
                              @NonNull String currentGroupId,
                              @NonNull UpdatePurchasesListener listener);

    /**
     * Queries purchases from the online data store and saves them in the local data store.
     *
     * @param currentUser the current user
     * @param group       the group for which to get purchases for
     * @param skip        the number of purchases to skip for the query
     * @param listener    the callback when the new purchases are saved in the local data store
     */
    void getPurchasesOnlineAsync(@NonNull User currentUser, @NonNull Group group, int skip,
                                 @NonNull GetPurchasesOnlineListener listener);

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
     * Defines the callback when purchases are loaded from the local data store.
     */
    interface GetPurchasesLocalListener {
        /**
         * Called when local purchases were successfully loaded.
         *
         * @param purchases the loaded purchases
         */
        void onPurchasesLocalLoaded(@NonNull List<ParseObject> purchases);
    }

    /**
     * Defines the callback when a purchase is loaded from the local data store.
     */
    interface GetPurchaseLocalListener {
        /**
         * Called when a local purchase was successfully loaded.
         *
         * @param purchase the loaded purchase
         */
        void onPurchaseLocalLoaded(@NonNull Purchase purchase);
    }

    /**
     * Defines the callback when purchases are loaded from the online data store.
     */
    interface GetPurchasesOnlineListener {
        /**
         * Called when online purchases were successfully loaded.
         *
         * @param purchases the loaded purchases
         */
        void onPurchasesOnlineLoaded(@NonNull List<ParseObject> purchases);

        /**
         * Called when the purchases load failed.
         *
         * @param errorMessage the error message from the exception thrown in the process
         */
        void onPurchaseOnlineLoadFailed(@StringRes int errorMessage);
    }

    /**
     * Defines the callback when purchases in the local data store are updated from the online data
     * store.
     */
    interface UpdatePurchasesListener {
        /**
         * Called when local purchases were successfully updated.
         *
         * @param isCurrentGroup whether the updated purchases belong to the current group
         */
        void onPurchasesUpdated(boolean isCurrentGroup);

        /**
         * Called when purchases update failed.
         *
         * @param errorMessage the error message from the exception thrown in the process
         */
        void onPurchaseUpdateFailed(@StringRes int errorMessage);

        /**
         * Called when all purchases from all group were successfully updated.
         */
        void onAllPurchasesUpdated();
    }
}
