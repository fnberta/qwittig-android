package ch.giantific.qwittig.data.rxwrapper.firebase;

import android.support.annotation.NonNull;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;

import ch.giantific.qwittig.data.rxwrapper.firebase.subscribers.ListenToTaskOnCompleteOnSubscribe;
import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * Created by Nick Moskalenko on 15/05/2016.
 */
public class RxFirebaseAuth {

    @NonNull
    public static Single<AuthResult> signInAnonymously(@NonNull final FirebaseAuth firebaseAuth) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(firebaseAuth.signInAnonymously()));
    }

    @NonNull
    public static Single<AuthResult> signInWithEmailAndPassword(@NonNull final FirebaseAuth firebaseAuth,
                                                                @NonNull final String email,
                                                                @NonNull final String password) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(firebaseAuth.signInWithEmailAndPassword(email, password)));
    }

    @NonNull
    public static Single<AuthResult> signInWithCredential(@NonNull final FirebaseAuth firebaseAuth,
                                                          @NonNull final AuthCredential credential) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(firebaseAuth.signInWithCredential(credential)));
    }

    @NonNull
    public static Single<AuthResult> signInWithCustomToken(@NonNull final FirebaseAuth firebaseAuth,
                                                           @NonNull final String token) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(firebaseAuth.signInWithCustomToken(token)));
    }

    @NonNull
    public static Single<AuthResult> createUserWithEmailAndPassword(@NonNull final FirebaseAuth firebaseAuth,
                                                                    @NonNull final String email,
                                                                    @NonNull final String password) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(firebaseAuth.createUserWithEmailAndPassword(email, password)));
    }

    @NonNull
    public static Single<ProviderQueryResult> fetchProvidersForEmail(@NonNull final FirebaseAuth firebaseAuth,
                                                                     @NonNull final String email) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(firebaseAuth.fetchProvidersForEmail(email)));
    }

    @NonNull
    public static Single<Void> sendPasswordResetEmail(@NonNull final FirebaseAuth firebaseAuth,
                                                      @NonNull final String email) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(firebaseAuth.sendPasswordResetEmail(email)));
    }

    @NonNull
    public static Single<Void> deleteUser(@NonNull final FirebaseUser firebaseUser) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(firebaseUser.delete()));
    }

    @NonNull
    public static Observable<FirebaseUser> observeAuthState(@NonNull final FirebaseAuth firebaseAuth) {

        return Observable.create(new Observable.OnSubscribe<FirebaseUser>() {
            @Override
            public void call(final Subscriber<? super FirebaseUser> subscriber) {
                final FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(firebaseAuth.getCurrentUser());
                        }
                    }
                };
                firebaseAuth.addAuthStateListener(authStateListener);

                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        firebaseAuth.removeAuthStateListener(authStateListener);
                    }
                }));
            }
        });
    }
}
