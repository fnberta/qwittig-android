/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;

import com.parse.ParseFile;

import org.json.JSONObject;

import java.util.List;

import ch.giantific.qwittig.domain.models.stats.Stats;
import rx.Single;

/**
 * Created by fabio on 11.01.16.
 */
public interface ApiRepository {
    /**
     * Re-calculates the balances all users of the current user's groups.
     */
    Single<String> calcUserBalances();

    /**
     * Invites new users to join a group by sending them an email and a push notification if the
     * there is already an account for the email address.
     *
     * @param emails    the emails to which to send the invitations too
     * @param groupName the name of the group the users are invited to
     */
    Single<String> inviteUsers(@NonNull List<String> emails,
                               @NonNull String groupName);

    /**
     * Sends a push notification to remind a user to finish a task.
     *
     * @param taskId the object id of the task that should be finished
     */
    Single<String> pushTaskReminder(@NonNull String taskId);

    /**
     * Sends a push notification to remind a user to pay a compensation.
     *
     * @param compensationId the object id of the compensation that needs to be paid
     * @param currencyCode   the currency code to format the price in the push notification
     */
    Single<String> pushCompensationReminder(@NonNull String compensationId,
                                            @NonNull String currencyCode);

    /**
     * Adds the current user to the group role, giving him/her access to all the objects of the
     * group.
     *
     * @param groupId the object id of the group to whose role the user should be added
     */
    Single<String> addUserToGroupRole(@NonNull String groupId);

    /**
     * Removes the current user from the group role, preventing him from accessing any objects that
     * belong to the group.
     *
     * @param groupId the object id of the group from whose role the user should be removed
     */
    Single<String> removeUserFromGroupRole(@NonNull String groupId);

    /**
     * Deletes the the specified {@link ParseFile}, probably a receipt image that is no longer
     * needed.
     *
     * @param fileName the file name of the {@link ParseFile} to delete
     */
    Single<String> deleteParseFile(@NonNull String fileName);

    /**
     * Deletes a users account by setting all his/her fields to empty. Does not actually delete
     * the user because that would cause all purchases the users is involved in to be corrupted.
     */
    Single<String> deleteAccount();

    /**
     * Calculates the stats that show how much each users of the group spent in a month/year.
     *
     * @param groupId the object id of the group to calculate stats for
     * @param year    the year for which to calculate stats for
     * @param month   the month for which to calculate stats for, 0 means the whole year
     */
    Single<Stats> calcStatsSpending(@NonNull String groupId, @NonNull String year, int month);

    /**
     * Calculates the stats that shows the percentages of stores used in purchases.
     *
     * @param groupId the object id of the group to calculate stats for
     * @param year    the year for which to calculate stats for
     * @param month   the month for which to calculate stats for, 0 means the whole year
     */
    Single<Stats> calcStatsStores(@NonNull String groupId, @NonNull String year, int month);

    /**
     * Calculates the stats that the percentages of currencies used in purchases.
     *
     * @param groupId the object id of the group to calculate stats for
     * @param year    the year for which to calculate stats for
     * @param month   the month for which to calculate stats for, 0 means the whole year
     */
    Single<Stats> calcStatsCurrencies(@NonNull String groupId, @NonNull String year, int month);

    /**
     * Verifies the idToken obtained from Google and if successful logs in the user attached to the
     * email address in the token. If no such user exists yet, creates a new one.
     *
     * @param idToken the token obtained from the Google login
     */
    Single<JSONObject> loginWithGoogle(@NonNull String idToken);
}
