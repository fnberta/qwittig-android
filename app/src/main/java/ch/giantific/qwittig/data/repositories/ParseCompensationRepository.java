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
import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import rx.Observable;
import rx.Single;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an implementation of {@link CompensationRepository} that uses the Parse.com framework
 * as the local and online data store.
 */
public class ParseCompensationRepository extends ParseBaseRepository<Compensation> implements
        CompensationRepository {

    private static final String DATE_CREATED = "createdAt";
    private static final String DATE_UPDATED = "updatedAt";
    private static final String LOG_TAG = ParseCompensationRepository.class.getSimpleName();

    public ParseCompensationRepository() {
        super();
    }

    @Override
    protected String getClassName() {
        return Compensation.CLASS;
    }

    @Override
    public Observable<Compensation> getCompensationsLocalUnpaidAsync(@NonNull User currentUser,
                                                                     @NonNull final Group group) {
        ParseQuery<Compensation> query = getCompensationsLocalQuery(currentUser, group);
        query.whereEqualTo(Compensation.IS_PAID, false);
        query.orderByAscending(DATE_CREATED);

        return find(query).flatMap(new Func1<List<Compensation>, Observable<Compensation>>() {
            @Override
            public Observable<Compensation> call(List<Compensation> compensations) {
                return Observable.from(compensations);
            }
        });
    }

    @Override
    public Observable<Compensation> getCompensationsLocalPaidAsync(@NonNull final User currentUser,
                                                                   @NonNull final Group group) {
        ParseQuery<Compensation> query = getCompensationsLocalQuery(currentUser, group);
        query.whereEqualTo(Compensation.IS_PAID, true);
        query.orderByDescending(DATE_UPDATED);

        return find(query).flatMap(new Func1<List<Compensation>, Observable<Compensation>>() {
            @Override
            public Observable<Compensation> call(List<Compensation> compensations) {
                return Observable.from(compensations);
            }
        });
    }

    @NonNull
    private ParseQuery<Compensation> getCompensationsLocalQuery(@NonNull User currentUser,
                                                                @NonNull final Group group) {
        ParseQuery<Compensation> payerQuery = ParseQuery.getQuery(Compensation.CLASS);
        payerQuery.whereEqualTo(Compensation.PAYER, currentUser);

        ParseQuery<Compensation> beneficiaryQuery = ParseQuery.getQuery(Compensation.CLASS);
        beneficiaryQuery.whereEqualTo(Compensation.BENEFICIARY, currentUser);

        List<ParseQuery<Compensation>> queries = new ArrayList<>();
        queries.add(payerQuery);
        queries.add(beneficiaryQuery);

        ParseQuery<Compensation> query = ParseQuery.or(queries);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.whereEqualTo(Compensation.GROUP, group);
        return query;
    }

    @Override
    public Single<Compensation> saveCompensationAsync(@NonNull final Compensation compensation) {
        return save(compensation).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                compensation.setPaid(false);
            }
        });
    }

    @Override
    public boolean removeCompensationLocal(@NonNull String compensationId) {
        ParseObject compensation = ParseObject.createWithoutData(Compensation.CLASS, compensationId);
        try {
            compensation.unpin(Compensation.PIN_LABEL_UNPAID);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    @Override
    public Observable<Compensation> updateCompensationsUnpaidAsync(@NonNull final List<ParseObject> groups) {
        ParseQuery<Compensation> query = getCompensationsOnlineQuery();
        query.whereContainedIn(Compensation.GROUP, groups);
        query.whereEqualTo(Compensation.IS_PAID, false);
        return find(query)
                .flatMap(new Func1<List<Compensation>, Observable<List<Compensation>>>() {
                    @Override
                    public Observable<List<Compensation>> call(List<Compensation> compensations) {
                        return unpinAll(compensations, Compensation.PIN_LABEL_UNPAID);
                    }
                })
                .flatMap(new Func1<List<Compensation>, Observable<List<Compensation>>>() {
                    @Override
                    public Observable<List<Compensation>> call(List<Compensation> compensations) {
                        return pinAll(compensations, Compensation.PIN_LABEL_UNPAID);
                    }
                })
                .flatMap(new Func1<List<Compensation>, Observable<Compensation>>() {
                    @Override
                    public Observable<Compensation> call(List<Compensation> compensations) {
                        return Observable.from(compensations);
                    }
                });
    }

    @Override
    public Observable<Compensation> updateCompensationsPaidAsync(@NonNull List<ParseObject> groups,
                                                                 @NonNull final String currentGroupId) {
        return Observable.from(groups)
                .flatMap(new Func1<ParseObject, Observable<Compensation>>() {
                    @Override
                    public Observable<Compensation> call(final ParseObject parseObject) {
                        final String groupId = parseObject.getObjectId();
                        final String pinLabel = Compensation.PIN_LABEL_PAID + groupId;

                        ParseQuery<Compensation> query = getCompensationsOnlineQuery();
                        query.whereEqualTo(Compensation.GROUP, parseObject);
                        query.whereEqualTo(Compensation.IS_PAID, true);
                        query.setLimit(QUERY_ITEMS_PER_PAGE);
                        return find(query)
                                .flatMap(new Func1<List<Compensation>, Observable<List<Compensation>>>() {
                                    @Override
                                    public Observable<List<Compensation>> call(List<Compensation> compensations) {
                                        return unpinAll(compensations, pinLabel);
                                    }
                                })
                                .flatMap(new Func1<List<Compensation>, Observable<List<Compensation>>>() {
                                    @Override
                                    public Observable<List<Compensation>> call(List<Compensation> compensations) {
                                        return pinAll(compensations, pinLabel);
                                    }
                                })
                                .flatMap(new Func1<List<Compensation>, Observable<Compensation>>() {
                                    @Override
                                    public Observable<Compensation> call(List<Compensation> compensations) {
                                        return Observable.from(compensations);
                                    }
                                })
                                .filter(new Func1<Compensation, Boolean>() {
                                    @Override
                                    public Boolean call(Compensation compensation) {
                                        return groupId.equals(currentGroupId);
                                    }
                                });
                    }
                });
    }

    @Override
    public Observable<Compensation> getCompensationsPaidOnlineAsync(@NonNull final Group group,
                                                                    final int skip) {
        final ParseQuery<Compensation> query = getCompensationsOnlineQuery();
        query.setSkip(skip);
        query.whereEqualTo(Compensation.GROUP, group);
        query.whereEqualTo(Compensation.IS_PAID, true);

        return find(query)
                .flatMap(new Func1<List<Compensation>, Observable<List<Compensation>>>() {
                    @Override
                    public Observable<List<Compensation>> call(List<Compensation> compensations) {
                        final String tag = Compensation.PIN_LABEL_PAID + group.getObjectId();
                        return pinAll(compensations, tag);
                    }
                })
                .flatMap(new Func1<List<Compensation>, Observable<Compensation>>() {
                    @Override
                    public Observable<Compensation> call(List<Compensation> compensations) {
                        return Observable.from(compensations);
                    }
                });
    }

    @Override
    public boolean updateCompensations(@NonNull List<ParseObject> groups) {
        if (groups.isEmpty()) {
            return false;
        }

        try {
            updateCompensationsUnpaid(groups);
            updateCompensationsPaid(groups);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    private void updateCompensationsUnpaid(@NonNull List<ParseObject> groups) throws ParseException {
        ParseQuery<Compensation> query = getCompensationsOnlineQuery();
        query.whereContainedIn(Compensation.GROUP, groups);
        query.whereEqualTo(Compensation.IS_PAID, false);

        List<Compensation> compensationsUnpaid = query.find();
        ParseObject.unpinAll(Compensation.PIN_LABEL_UNPAID);
        ParseObject.pinAll(Compensation.PIN_LABEL_UNPAID, compensationsUnpaid);
    }

    private void updateCompensationsPaid(@NonNull final List<ParseObject> groups) throws ParseException {
        for (ParseObject group : groups) {
            ParseQuery<Compensation> query = getCompensationsOnlineQuery();
            query.whereEqualTo(Compensation.GROUP, group);
            query.whereEqualTo(Compensation.IS_PAID, true);
            query.setLimit(ParseBaseRepository.QUERY_ITEMS_PER_PAGE);

            List<Compensation> compensationsPaid = query.find();
            final String groupId = group.getObjectId();
            final String pinLabel = Compensation.PIN_LABEL_PAID + groupId;
            ParseObject.unpinAll(pinLabel);
            ParseObject.pinAll(pinLabel, compensationsPaid);
        }
    }

    @NonNull
    private ParseQuery<Compensation> getCompensationsOnlineQuery() {
        ParseQuery<Compensation> query = ParseQuery.getQuery(Compensation.CLASS);
        query.orderByDescending(DATE_CREATED);
        return query;
    }

    @Override
    @Nullable
    public Boolean updateCompensation(@NonNull String compensationId, boolean isNew) {
        try {
            Compensation compensation = getCompensationOnline(compensationId);
            boolean isPaid = compensation.isPaid();
            if (isNew) {
                String groupId = compensation.getGroup().getObjectId();
                String pinLabel;
                if (isPaid) {
                    pinLabel = Compensation.PIN_LABEL_PAID + groupId;
                } else {
                    pinLabel = Compensation.PIN_LABEL_UNPAID;
                }

                compensation.pin(pinLabel);
            } else if (isPaid) {
                compensation.unpin(Compensation.PIN_LABEL_UNPAID);

                String groupId = compensation.getGroup().getObjectId();
                String pinLabel = Compensation.PIN_LABEL_PAID + groupId;
                compensation.pin(pinLabel);
            }

            return isPaid;
        } catch (ParseException e) {
            return null;
        }
    }

    private Compensation getCompensationOnline(@NonNull String compensationId)
            throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Compensation.CLASS);
        return (Compensation) query.get(compensationId);
    }

}
