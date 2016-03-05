/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.data.receivers.PushBroadcastReceiver;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import rx.Observable;
import rx.Single;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an implementation of {@link CompensationRepository} that uses the Parse.com framework
 * as the local and online data store.
 */
public class ParseCompensationRepository extends ParseBaseRepository implements
        CompensationRepository {

    private static final String CALCULATE_COMPENSATIONS = "calculateCompensations";
    private static final String PUSH_COMPENSATION_REMIND = "pushCompensationRemind";
    private static final String DATE_CREATED = "createdAt";
    private static final String DATE_UPDATED = "updatedAt";

    public ParseCompensationRepository() {
        super();
    }

    @Override
    protected String getClassName() {
        return Compensation.CLASS;
    }

    @Override
    public Single<String> calculateCompensations(@NonNull Group group) {
        final Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP_ID, group.getObjectId());
        return callFunctionInBackground(CALCULATE_COMPENSATIONS, params);
    }

    @Override
    public Observable<Compensation> getCompensationsLocalUnpaidAsync(@NonNull Identity identity) {
        ParseQuery<Compensation> query = getCompensationsLocalQuery(identity);
        query.whereEqualTo(Compensation.PAID, false);
        query.orderByAscending(DATE_CREATED);

        return find(query)
                .concatMap(new Func1<List<Compensation>, Observable<Compensation>>() {
                    @Override
                    public Observable<Compensation> call(List<Compensation> compensations) {
                        return Observable.from(compensations);
                    }
                });
    }

    @Override
    public Observable<Compensation> getCompensationsLocalPaidAsync(@NonNull Identity identity) {
        final ParseQuery<Compensation> query = getCompensationsLocalQuery(identity);
        query.whereEqualTo(Compensation.PAID, true);
        query.orderByDescending(DATE_UPDATED);

        return find(query)
                .concatMap(new Func1<List<Compensation>, Observable<Compensation>>() {
                    @Override
                    public Observable<Compensation> call(List<Compensation> compensations) {
                        return Observable.from(compensations);
                    }
                });
    }

    @NonNull
    private ParseQuery<Compensation> getCompensationsLocalQuery(@NonNull Identity identity) {
        final ParseQuery<Compensation> debtorQuery = ParseQuery.getQuery(Compensation.CLASS);
        debtorQuery.whereEqualTo(Compensation.DEBTOR, identity);
        final ParseQuery<Compensation> creditorQuery = ParseQuery.getQuery(Compensation.CLASS);
        creditorQuery.whereEqualTo(Compensation.CREDITOR, identity);

        final List<ParseQuery<Compensation>> queries = new ArrayList<>();
        queries.add(debtorQuery);
        queries.add(creditorQuery);

        final ParseQuery<Compensation> query = ParseQuery.or(queries);
        query.whereEqualTo(Compensation.GROUP, identity.getGroup());
        query.fromLocalDatastore();
        query.ignoreACLs();
        return query;
    }

    @Override
    public Single<Compensation> saveCompensationAsync(@NonNull final Compensation compensation) {
        return save(compensation)
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        compensation.setPaid(false);
                    }
                });
    }

    @Override
    public boolean removeCompensationLocal(@NonNull String compensationId) {
        final Compensation compensation = (Compensation)
                ParseObject.createWithoutData(Compensation.CLASS, compensationId);
        try {
            compensation.unpin(Compensation.PIN_LABEL_UNPAID);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    @Override
    public Observable<Compensation> updateCompensationsUnpaidAsync(@NonNull List<Identity> identities) {
        return Observable.from(identities)
                .toList()
                .map(new Func1<List<Identity>, ParseQuery<Compensation>>() {
                    @Override
                    public ParseQuery<Compensation> call(List<Identity> identities) {
                        final ParseQuery<Compensation> query = getCompensationsOnlineQuery(identities);
                        query.whereEqualTo(Compensation.PAID, false);
                        return query;
                    }
                })
                .flatMap(new Func1<ParseQuery<Compensation>, Observable<List<Compensation>>>() {
                    @Override
                    public Observable<List<Compensation>> call(ParseQuery<Compensation> query) {
                        return find(query);
                    }
                })
                .concatMap(new Func1<List<Compensation>, Observable<List<Compensation>>>() {
                    @Override
                    public Observable<List<Compensation>> call(List<Compensation> compensations) {
                        return unpinAll(compensations, Compensation.PIN_LABEL_UNPAID);
                    }
                })
                .concatMap(new Func1<List<Compensation>, Observable<List<Compensation>>>() {
                    @Override
                    public Observable<List<Compensation>> call(List<Compensation> compensations) {
                        return pinAll(compensations, Compensation.PIN_LABEL_UNPAID);
                    }
                })
                .concatMap(new Func1<List<Compensation>, Observable<Compensation>>() {
                    @Override
                    public Observable<Compensation> call(List<Compensation> compensations) {
                        return Observable.from(compensations);
                    }
                });
    }

    @NonNull
    private ParseQuery<Compensation> getCompensationsOnlineQuery(@NonNull List<Identity> identities) {
        final ParseQuery<Compensation> debtorQuery = ParseQuery.getQuery(Compensation.CLASS);
        debtorQuery.whereContainedIn(Compensation.DEBTOR, identities);

        final ParseQuery<Compensation> creditorQuery = ParseQuery.getQuery(Compensation.CLASS);
        creditorQuery.whereContainedIn(Compensation.CREDITOR, identities);

        final List<ParseQuery<Compensation>> queries = new ArrayList<>();
        queries.add(debtorQuery);
        queries.add(creditorQuery);

        final ParseQuery<Compensation> query = ParseQuery.or(queries);
        query.orderByDescending(DATE_CREATED);
        return query;
    }

    @NonNull
    private ParseQuery<Compensation> getCompensationsOnlineQuery(@NonNull Identity identity) {
        final ParseQuery<Compensation> debtorQuery = ParseQuery.getQuery(Compensation.CLASS);
        debtorQuery.whereEqualTo(Compensation.DEBTOR, identity);

        final ParseQuery<Compensation> creditorQuery = ParseQuery.getQuery(Compensation.CLASS);
        creditorQuery.whereEqualTo(Compensation.CREDITOR, identity);

        final List<ParseQuery<Compensation>> queries = new ArrayList<>();
        queries.add(debtorQuery);
        queries.add(creditorQuery);

        final ParseQuery<Compensation> query = ParseQuery.or(queries);
        query.orderByDescending(DATE_CREATED);
        return query;
    }

    @Override
    public Observable<Compensation> updateCompensationsPaidAsync(@NonNull Identity currentIdentity,
                                                                 @NonNull List<Identity> identities) {
        final String currentIdentityGroupId = currentIdentity.getGroup().getObjectId();
        return Observable.from(identities)
                .flatMap(new Func1<Identity, Observable<Compensation>>() {
                    @Override
                    public Observable<Compensation> call(final Identity identity) {
                        final Group group = identity.getGroup();
                        final String groupId = group.getObjectId();
                        final String pinLabel = Compensation.PIN_LABEL_PAID + groupId;

                        final ParseQuery<Compensation> query = getCompensationsOnlineQuery(identity);
                        query.whereEqualTo(Compensation.PAID, true);
                        query.setLimit(QUERY_ITEMS_PER_PAGE);
                        return find(query)
                                .concatMap(new Func1<List<Compensation>, Observable<List<Compensation>>>() {
                                    @Override
                                    public Observable<List<Compensation>> call(List<Compensation> compensations) {
                                        return unpinAll(compensations, pinLabel);
                                    }
                                })
                                .concatMap(new Func1<List<Compensation>, Observable<List<Compensation>>>() {
                                    @Override
                                    public Observable<List<Compensation>> call(List<Compensation> compensations) {
                                        return pinAll(compensations, pinLabel);
                                    }
                                })
                                .concatMap(new Func1<List<Compensation>, Observable<Compensation>>() {
                                    @Override
                                    public Observable<Compensation> call(List<Compensation> compensations) {
                                        return Observable.from(compensations);
                                    }
                                })
                                .filter(new Func1<Compensation, Boolean>() {
                                    @Override
                                    public Boolean call(Compensation compensation) {
                                        return groupId.equals(currentIdentityGroupId);
                                    }
                                });
                    }
                });
    }

    @Override
    public Observable<Compensation> getCompensationsPaidOnlineAsync(@NonNull final Identity currentIdentity,
                                                                    final int skip) {
        final Group currentGroup = currentIdentity.getGroup();
        final ParseQuery<Compensation> query = getCompensationsOnlineQuery(currentIdentity);
        query.setSkip(skip);
        query.whereEqualTo(Compensation.PAID, true);

        return find(query)
                .concatMap(new Func1<List<Compensation>, Observable<List<Compensation>>>() {
                    @Override
                    public Observable<List<Compensation>> call(List<Compensation> compensations) {
                        final String tag = Compensation.PIN_LABEL_PAID + currentGroup.getObjectId();
                        return pinAll(compensations, tag);
                    }
                })
                .concatMap(new Func1<List<Compensation>, Observable<Compensation>>() {
                    @Override
                    public Observable<Compensation> call(List<Compensation> compensations) {
                        return Observable.from(compensations);
                    }
                });
    }

    @Override
    public boolean updateCompensations(@NonNull List<Identity> identities) {
        try {
            updateCompensationsUnpaid(identities);
            updateCompensationsPaid(identities);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    private void updateCompensationsUnpaid(@NonNull List<Identity> identities) throws ParseException {
        final ParseQuery<Compensation> query = getCompensationsOnlineQuery(identities);
        query.whereEqualTo(Compensation.PAID, false);

        final List<Compensation> compensationsUnpaid = query.find();
        ParseObject.unpinAll(Compensation.PIN_LABEL_UNPAID);
        ParseObject.pinAll(Compensation.PIN_LABEL_UNPAID, compensationsUnpaid);
    }

    private void updateCompensationsPaid(@NonNull final List<Identity> identities) throws ParseException {
        for (Identity identity : identities) {
            final ParseQuery<Compensation> query = getCompensationsOnlineQuery(identity);
            query.whereEqualTo(Compensation.PAID, true);
            query.setLimit(ParseBaseRepository.QUERY_ITEMS_PER_PAGE);

            final List<Compensation> compensationsPaid = query.find();
            final Group group = identity.getGroup();
            final String pinLabel = Compensation.PIN_LABEL_PAID + group.getObjectId();
            ParseObject.unpinAll(pinLabel);
            ParseObject.pinAll(pinLabel, compensationsPaid);
        }
    }

    @Override
    @Nullable
    public Boolean updateCompensation(@NonNull String compensationId, boolean isNew) {
        try {
            Compensation compensation = getCompensationOnline(compensationId);
            boolean isPaid = compensation.isPaid();
            final String groupId = compensation.getGroup().getObjectId();
            if (isNew) {
                final String pinLabel = isPaid
                        ? Compensation.PIN_LABEL_PAID + groupId
                        : Compensation.PIN_LABEL_UNPAID;
                compensation.pin(pinLabel);
            } else if (isPaid) {
                compensation.unpin(Compensation.PIN_LABEL_UNPAID);
                final String pinLabel = Compensation.PIN_LABEL_PAID + groupId;
                compensation.pin(pinLabel);
            }

            return isPaid;
        } catch (ParseException e) {
            return null;
        }
    }

    private Compensation getCompensationOnline(@NonNull String compensationId)
            throws ParseException {
        final ParseQuery<ParseObject> query = ParseQuery.getQuery(Compensation.CLASS);
        return (Compensation) query.get(compensationId);
    }

    @Override
    public Single<Compensation> saveCompensationPaid(@NonNull Compensation compensation) {
        return unpin(compensation, Compensation.PIN_LABEL_UNPAID)
                .flatMap(new Func1<Compensation, Single<Compensation>>() {
                    @Override
                    public Single<Compensation> call(Compensation compensation) {
                        return pin(compensation, Compensation.PIN_LABEL_PAID + compensation.getGroup().getObjectId());
                    }
                })
                .doOnSuccess(new Action1<Compensation>() {
                    @Override
                    public void call(Compensation compensation) {
                        compensation.setPaid(true);
                        compensation.saveEventually();
                    }
                });
    }

    @Override
    public Single<String> pushCompensationReminder(@NonNull final String compensationId,
                                                   @NonNull final String currencyCode) {
        final Map<String, Object> params = getCompensationPushParams(compensationId, currencyCode);
        return callFunctionInBackground(PUSH_COMPENSATION_REMIND, params);
    }

    @NonNull
    private Map<String, Object> getCompensationPushParams(@NonNull String compensationId,
                                                          @NonNull String currencyCode) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_COMPENSATION_ID, compensationId);
        params.put(PushBroadcastReceiver.PUSH_PARAM_CURRENCY_CODE, currencyCode);

        return params;
    }
}
