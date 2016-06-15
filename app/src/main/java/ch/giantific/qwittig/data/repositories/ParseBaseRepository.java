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
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.LogOutCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.BaseRepository;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import timber.log.Timber;

/**
 * Provides a base class for repository implementations that use the Parse.com framework.
 */
public abstract class ParseBaseRepository implements BaseRepository {

    static final int QUERY_ITEMS_PER_PAGE = 15;

    ParseBaseRepository() {
    }

    @StringRes
    public int getErrorMessage(@NonNull Throwable error) {
        Timber.e(error, "error");

        final ParseException exception;
        try {
            exception = ((ParseException) error);
        } catch (ClassCastException e) {
            return R.string.toast_error_unknown;
        }

        final int code = exception.getCode();
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
                return R.string.toast_error_unknown;
        }
    }

//    /**
//     * Forces the user to the login screen because his/her session token is no longer valid and a
//     * new login is required.
//     * TODO: can we un-subscribe from notification channels before logging out?
//     *
//     * @param context the context used to construct the intent, may be an application context as
//     *                we start the activity as a new task
//     */
//    private static void forceNewLogin(final Context context) {
//        ParseUser.logOutInBackground(new LogOutCallback() {
//            @Override
//            public void done(ParseException e) {
//                // ignore possible exception, currentUser will always be null now
//                Intent intent = new Intent(context, HomeActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
//            }
//        });
//    }

    @Override
    public boolean isAlreadySavedLocal(@NonNull String objectId) {
        final ParseQuery<ParseObject> query = ParseQuery.getQuery(getClassName());
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

    final <T> Single<T> callFunctionInBackground(@NonNull final String function,
                                                 @NonNull final Map<String, ?> params) {
        return Single.create(new Single.OnSubscribe<T>() {
            @Override
            public void call(final SingleSubscriber<? super T> singleSubscriber) {
                ParseCloud.callFunctionInBackground(function, params, new FunctionCallback<T>() {
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
    final <T extends ParseFile> Single<T> saveFile(@NonNull final T object) {
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
    final <T extends ParseObject> Single<T> save(@NonNull final T object) {
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
    final <T extends ParseObject> Observable<List<T>> find(@NonNull final ParseQuery<T> query) {
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
    final <T extends ParseObject> Single<T> first(@NonNull final ParseQuery<T> query) {
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
    final <T extends ParseObject> Single<T> unpin(@NonNull final T object, @NonNull final String tag) {
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
    final <T extends ParseObject> Observable<List<T>> unpinAll(@NonNull final List<T> objects,
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
    final <T extends ParseObject> Observable<List<T>> pinAll(@NonNull final List<T> objects,
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
    final <T extends ParseObject> Single<T> pin(@NonNull final T object, @NonNull final String tag) {
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
    final <T extends ParseObject> Single<T> get(@NonNull final ParseQuery<T> query, @NonNull final String objectId) {
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
    final <T extends ParseObject> Single<T> fetchLocal(@NonNull final T object) {
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
    final <T extends ParseObject> Single<T> fetch(@NonNull final T object) {
        return Single.create(new Single.OnSubscribe<T>() {
            @Override
            public void call(final SingleSubscriber<? super T> singleSubscriber) {
                object.fetchInBackground(new GetCallback<T>() {
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
    final <T extends ParseObject> Single<T> fetchIfNeeded(@NonNull final T object) {
        return Single.create(new Single.OnSubscribe<T>() {
            @Override
            public void call(final SingleSubscriber<? super T> singleSubscriber) {
                object.fetchIfNeededInBackground(new GetCallback<T>() {
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
    final <T extends ParseObject> Single<Integer> count(@NonNull final ParseQuery<T> query) {
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

    @NonNull
    final <T extends ParseObject> Single<T> delete(@NonNull final T object) {
        return Single.create(new Single.OnSubscribe<T>() {
            @Override
            public void call(final SingleSubscriber<? super T> singleSubscriber) {
                object.deleteInBackground(new DeleteCallback() {
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
    final Single<User> login(@NonNull final String username, @NonNull final String password) {
        return Single
                .create(new Single.OnSubscribe<User>() {
                    @Override
                    public void call(final SingleSubscriber<? super User> singleSubscriber) {
                        ParseUser.logInInBackground(username, password, new LogInCallback() {
                            @Override
                            public void done(ParseUser parseUser, @Nullable ParseException e) {
                                if (singleSubscriber.isUnsubscribed()) {
                                    return;
                                }

                                if (e != null) {
                                    singleSubscriber.onError(e);
                                } else {
                                    singleSubscriber.onSuccess((User) parseUser);
                                }
                            }
                        });
                    }
                });
    }

    @NonNull
    final Single<User> logout(@NonNull final User user) {
        return Single.create(new Single.OnSubscribe<User>() {
            @Override
            public void call(final SingleSubscriber<? super User> singleSubscriber) {
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (singleSubscriber.isUnsubscribed()) {
                            return;
                        }

                        // ignore exception, currentUser will always be null now
                        singleSubscriber.onSuccess(user);
                    }
                });
            }
        });
    }
}
