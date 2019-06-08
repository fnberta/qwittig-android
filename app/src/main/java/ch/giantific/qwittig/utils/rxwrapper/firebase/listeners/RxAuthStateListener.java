package ch.giantific.qwittig.utils.rxwrapper.firebase.listeners;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import rx.AsyncEmitter;

/**
 * Created by fabio on 24.09.16.
 */
public class RxAuthStateListener implements FirebaseAuth.AuthStateListener {

    private final AsyncEmitter<FirebaseUser> asyncEmitter;

    public RxAuthStateListener(@NonNull AsyncEmitter<FirebaseUser> asyncEmitter) {
        this.asyncEmitter = asyncEmitter;
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        asyncEmitter.onNext(firebaseAuth.getCurrentUser());
    }
}
