/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.repositories.Repository;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import timber.log.Timber;

/**
 * Provides a base class for repository implementations that use the Parse.com framework.
 */
public abstract class ParseBaseRepository<T extends ParseObject> implements Repository {

    public static final int QUERY_ITEMS_PER_PAGE = 15;

    public ParseBaseRepository() {
    }

    @StringRes
    public int getErrorMessage(@NonNull Throwable e) {
        final int code = ((ParseException) e).getCode();
        switch (code) {
            case ParseException.INVALID_SESSION_TOKEN:
                return R.string.toast_invalid_session;
            case ParseException.USERNAME_TAKEN:
                return R.string.toast_email_address_taken;
            case ParseException.OBJECT_NOT_FOUND:
                return R.string.toast_login_failed_credentials;
            case ParseException.CONNECTION_FAILED:
                return R.string.toast_no_connection;
            default:
                Timber.e(e, "unknown error");
                return R.string.toast_unknown_error;
        }
    }

    @Override
    public boolean isAlreadySavedLocal(@NonNull String objectId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(getClassName());
        query.ignoreACLs();
        query.fromLocalDatastore();
        try {
            query.get(objectId);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    protected abstract String getClassName();

    @NonNull
    final Single<T> save(@NonNull final T object) {
        return Single.create(new Single.OnSubscribe<T>() {
            @Override
            public void call(final SingleSubscriber<? super T> singleSubscriber) {
                object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(@Nullable ParseException e) {
                        if (singleSubscriber.isUnsubscribed()) {
                            return;
                        }

                        if (e != null) {
                            singleSubscriber.onError(e);
                        } else {
                            singleSubscriber.onSuccess(object);
                        }
                    }
                });
            }
        });
    }

    @NonNull
    final Observable<List<T>> find(@NonNull final ParseQuery<T> query) {
        return Observable.create(new Observable.OnSubscribe<List<T>>() {
            @Override
            public void call(final Subscriber<? super List<T>> subscriber) {
                query.findInBackground(new FindCallback<T>() {
                    @Override
                    public void done(List<T> objects, ParseException e) {
                        if (subscriber.isUnsubscribed()) {
                            return;
                        }

                        if (e != null) {
                            subscriber.onError(e);
                        } else {
                            subscriber.onNext(objects);
                            subscriber.onCompleted();
                        }
                    }
                });
            }
        });
    }

    @NonNull
    final Single<T> first(@NonNull final ParseQuery<T> query) {
        return Single.create(new Single.OnSubscribe<T>() {
            @Override
            public void call(final SingleSubscriber<? super T> singleSubscriber) {
                query.getFirstInBackground(new GetCallback<T>() {
                    @Override
                    public void done(T object, ParseException e) {
                        if (singleSubscriber.isUnsubscribed()) {
                            return;
                        }

                        if (e != null) {
                            singleSubscriber.onError(e);
                        } else {
                            singleSubscriber.onSuccess(object);
                        }
                    }
                });
            }
        });
    }

    @NonNull
    final Single<T> unpin(@NonNull final T object, @NonNull final String tag) {
        return Single.create(new Single.OnSubscribe<T>() {
            @Override
            public void call(final SingleSubscriber<? super T> singleSubscriber) {
                object.unpinInBackground(tag, new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (singleSubscriber.isUnsubscribed()) {
                            return;
                        }

                        if (e != null) {
                            singleSubscriber.onError(e);
                        } else {
                            singleSubscriber.onSuccess(object);
                        }
                    }
                });
            }
        });
    }

    @NonNull
    final Observable<List<T>> unpinAll(@NonNull final List<T> objects,
                                       @NonNull final String tag) {
        return Observable.create(new Observable.OnSubscribe<List<T>>() {
            @Override
            public void call(final Subscriber<? super List<T>> subscriber) {
                ParseObject.unpinAllInBackground(tag, new DeleteCallback() {
                    @Override
                    public void done(@Nullable ParseException e) {
                        if (subscriber.isUnsubscribed()) {
                            return;
                        }

                        if (e != null) {
                            subscriber.onError(e);
                        } else {
                            subscriber.onNext(objects);
                            subscriber.onCompleted();
                        }
                    }
                });
            }
        });
    }

    @NonNull
    final Observable<List<T>> pinAll(@NonNull final List<T> objects,
                                     @NonNull final String tag) {
        return Observable.create(new Observable.OnSubscribe<List<T>>() {
            @Override
            public void call(final Subscriber<? super List<T>> subscriber) {
                ParseObject.pinAllInBackground(tag, objects, new SaveCallback() {
                    @Override
                    public void done(@Nullable ParseException e) {
                        if (subscriber.isUnsubscribed()) {
                            return;
                        }

                        if (e != null) {
                            subscriber.onError(e);
                        } else {
                            subscriber.onNext(objects);
                            subscriber.onCompleted();
                        }
                    }
                });
            }
        });
    }

    @NonNull
    final Single<T> pin(@NonNull final T object, @NonNull final String tag) {
        return Single.create(new Single.OnSubscribe<T>() {
            @Override
            public void call(final SingleSubscriber<? super T> singleSubscriber) {
                object.pinInBackground(tag, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (singleSubscriber.isUnsubscribed()) {
                            return;
                        }

                        if (e != null) {
                            singleSubscriber.onError(e);
                        } else {
                            singleSubscriber.onSuccess(object);
                        }
                    }
                });
            }
        });
    }

    @NonNull
    final Single<T> get(@NonNull final ParseQuery<T> query, @NonNull final String objectId) {
        return Single.create(new Single.OnSubscribe<T>() {
            @Override
            public void call(final SingleSubscriber<? super T> singleSubscriber) {
                query.getInBackground(objectId, new GetCallback<T>() {
                    @Override
                    public void done(T object, @Nullable ParseException e) {
                        if (singleSubscriber.isUnsubscribed()) {
                            return;
                        }

                        if (e != null) {
                            singleSubscriber.onError(e);
                        } else {
                            singleSubscriber.onSuccess(object);
                        }
                    }
                });
            }
        });
    }

    @NonNull
    final Single<T> fetchLocal(@NonNull final T object) {
        return Single.create(new Single.OnSubscribe<T>() {
            @Override
            public void call(final SingleSubscriber<? super T> singleSubscriber) {
                object.fetchFromLocalDatastoreInBackground(new GetCallback<T>() {
                    @Override
                    public void done(T object, @Nullable ParseException e) {
                        if (singleSubscriber.isUnsubscribed()) {
                            return;
                        }

                        if (e != null) {
                            singleSubscriber.onError(e);
                        } else {
                            singleSubscriber.onSuccess(object);
                        }
                    }
                });
            }
        });
    }

    @NonNull
    final Single<Integer> count(@NonNull final ParseQuery<T> query) {
        return Single.create(new Single.OnSubscribe<Integer>() {
            @Override
            public void call(final SingleSubscriber<? super Integer> singleSubscriber) {
                query.countInBackground(new CountCallback() {
                    @Override
                    public void done(int count, ParseException e) {
                        if (singleSubscriber.isUnsubscribed()) {
                            return;
                        }

                        if (e != null) {
                            singleSubscriber.onError(e);
                        } else {
                            singleSubscriber.onSuccess(count);
                        }
                    }
                });
            }
        });
    }
}
