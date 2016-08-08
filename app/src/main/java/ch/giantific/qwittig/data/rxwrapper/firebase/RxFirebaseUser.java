package ch.giantific.qwittig.data.rxwrapper.firebase;

import android.support.annotation.NonNull;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.UserProfileChangeRequest;

import ch.giantific.qwittig.data.rxwrapper.firebase.subscribers.ListenToTaskOnCompleteOnSubscribe;
import rx.Single;

/**
 * Created by Nick Moskalenko on 24/05/2016.
 */
public class RxFirebaseUser {

    @NonNull
    public static Single<GetTokenResult> getToken(@NonNull final FirebaseUser firebaseUser,
                                                  final boolean forceRefresh) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(firebaseUser.getToken(forceRefresh)));
    }

    @NonNull
    public static Single<Void> updateEmail(@NonNull final FirebaseUser firebaseUser,
                                           @NonNull final String email) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(firebaseUser.updateEmail(email)));
    }

    @NonNull
    public static Single<Void> updatePassword(@NonNull final FirebaseUser firebaseUser,
                                              @NonNull final String password) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(firebaseUser.updatePassword(password)));
    }

    @NonNull
    public static Single<Void> updateProfile(@NonNull final FirebaseUser firebaseUser,
                                             @NonNull final UserProfileChangeRequest request) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(firebaseUser.updateProfile(request)));
    }

    @NonNull
    public static Single<Void> reauthenticate(@NonNull final FirebaseUser firebaseUser,
                                              @NonNull final AuthCredential credential) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(firebaseUser.reauthenticate(credential)));
    }

    @NonNull
    public static Single<AuthResult> linkWithCredential(@NonNull final FirebaseUser firebaseUser,
                                                        @NonNull final AuthCredential credential) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(firebaseUser.linkWithCredential(credential)));
    }

    @NonNull
    public static Single<AuthResult> unlinkFromProvider(@NonNull final FirebaseUser firebaseUser,
                                                        @NonNull final String providerId) {
        return Single.create(new ListenToTaskOnCompleteOnSubscribe<>(firebaseUser.unlink(providerId)));
    }
}
