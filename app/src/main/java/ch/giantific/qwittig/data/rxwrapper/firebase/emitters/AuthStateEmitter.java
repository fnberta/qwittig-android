package ch.giantific.qwittig.data.rxwrapper.firebase.emitters;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ch.giantific.qwittig.data.rxwrapper.firebase.listeners.RxAuthStateListener;
import rx.AsyncEmitter;
import rx.functions.Action1;

/**
 * Created by fabio on 20.08.16.
 */
public class AuthStateEmitter implements Action1<AsyncEmitter<FirebaseUser>> {

    private final FirebaseAuth firebaseAuth;

    public AuthStateEmitter(@NonNull FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public void call(final AsyncEmitter<FirebaseUser> asyncEmitter) {
        final FirebaseAuth.AuthStateListener authStateListener = new RxAuthStateListener(asyncEmitter);
        firebaseAuth.addAuthStateListener(authStateListener);
        asyncEmitter.setCancellation(() -> firebaseAuth.removeAuthStateListener(authStateListener));
    }
}
