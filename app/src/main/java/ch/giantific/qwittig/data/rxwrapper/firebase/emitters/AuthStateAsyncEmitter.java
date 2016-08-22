package ch.giantific.qwittig.data.rxwrapper.firebase.emitters;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import rx.AsyncEmitter;
import rx.functions.Action1;

/**
 * Created by fabio on 20.08.16.
 */
public class AuthStateAsyncEmitter implements Action1<AsyncEmitter<FirebaseUser>> {

    private final FirebaseAuth firebaseAuth;

    public AuthStateAsyncEmitter(@NonNull FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public void call(final AsyncEmitter<FirebaseUser> asyncEmitter) {
        final FirebaseAuth.AuthStateListener authStateListener = new RxAuthStateListener(asyncEmitter);
        firebaseAuth.addAuthStateListener(authStateListener);
        asyncEmitter.setCancellation(new AsyncEmitter.Cancellable() {
            @Override
            public void cancel() throws Exception {
                firebaseAuth.removeAuthStateListener(authStateListener);
            }
        });
    }

    private static class RxAuthStateListener implements FirebaseAuth.AuthStateListener {

        private final AsyncEmitter<FirebaseUser> asyncEmitter;

        RxAuthStateListener(@NonNull AsyncEmitter<FirebaseUser> asyncEmitter) {
            this.asyncEmitter = asyncEmitter;
        }

        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            asyncEmitter.onNext(firebaseAuth.getCurrentUser());
        }
    }
}
