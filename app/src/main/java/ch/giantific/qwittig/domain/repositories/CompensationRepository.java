/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import rx.Observable;
import rx.Single;

/**
 * Provides the methods to get, update and remove compensations from the local and online data
 * store.
 */
public interface CompensationRepository extends BaseRepository {

    /**
     * Calculates the compensations for the group.
     *
     * @param group the group to calculate for
     * @return the response from the server
     */
    Single<String> calculateCompensations(@NonNull Group group);

    /**
     * Queries the local data store for unpaid compensations.
     *
     * @param currentIdentity the current user
     * @return a {@link Observable} emitting the results
     */
    Observable<Compensation> getCompensationsLocalUnpaidAsync(@NonNull Identity currentIdentity);

    /**
     * Queries the local data store for paid compensations where the current user is either the
     * buyer or the beneficiary.
     *
     * @param currentIdentity the current user
     * @return a {@link Observable} emitting the results
     */
    Observable<Compensation> getCompensationsLocalPaidAsync(@NonNull Identity currentIdentity);

    /**
     * Saves a {@link Compensation} object to the online and offline storage
     *
     * @param compensation the compensation to save
     * @return a {@link Single} emitting the result
     */
    Single<Compensation> saveCompensationAsync(@NonNull Compensation compensation);

    /**
     * Removes a compensation from the local data store.
     *
     * @param compensationId the object id of the compensation to remove
     * @return whether the removal was successful or not
     */
    boolean removeCompensationLocal(@NonNull String compensationId);

    /**
     * Updates all unpaid compensations in the local data store by deleting all compensations from the
     * local data store, querying and saving new ones.
     *
     * @param identities all identities from the current user
     * @return a {@link Observable} emitting the results
     */
    Observable<Compensation> updateCompensationsUnpaidAsync(@NonNull List<Identity> identities);

    /**
     * Updates all paid compensations in the local data store by deleting all compensations from the
     * local data store, querying and saving new ones.
     *
     * @param currentIdentity the current identity
     * @param identities      all identities from the current user
     * @return a {@link Observable} emitting the results
     */
    Observable<Compensation> updateCompensationsPaidAsync(@NonNull Identity currentIdentity,
                                                          @NonNull List<Identity> identities);

    /**
     * Queries paid compensations from the online data store and saves them in the local data store.
     *
     * @param currentIdentity the group for which to get compensations for
     * @param skip            the number of compensations to skip for the query
     * @return a {@link Observable} emitting the results
     */
    Observable<Compensation> getCompensationsPaidOnlineAsync(@NonNull Identity currentIdentity, int skip);

    /**
     * Deletes all compensations from the local data store and saves new ones.
     *
     * @param identities the groups for which to update the compensations
     * @return whether the update was successful or not
     */
    boolean updateCompensations(@NonNull List<Identity> identities);

    /**
     * Updates a compensation if is already available in the local data store (by simply querying
     * it) or saves it for the first time to the local data store if not.
     *
     * @param compensationId the object id of the compensations to query
     * @param isNew          whether the compensations is already available in the local data store
     *                       or not
     * @return whether the update was successful or not
     */
    @Nullable
    Boolean updateCompensation(@NonNull String compensationId, boolean isNew);

    /**
     * Marks the compensation as paid and saves it to the offline and online data stores.
     *
     * @param compensation the compensation to save
     * @return a {@link Single} emitting the result
     */
    Single<Compensation> saveCompensationPaid(@NonNull Compensation compensation);

    /**
     * Sends a push notification to remind a user to pay a compensation.
     *
     * @param compensationId the object id of the compensation that needs to be paid
     * @param currencyCode   the currency code to format the price in the push notification
     * @return a {@link Single} emitting the result
     */
    Single<String> pushCompensationReminder(@NonNull String compensationId,
                                            @NonNull String currencyCode);
}
