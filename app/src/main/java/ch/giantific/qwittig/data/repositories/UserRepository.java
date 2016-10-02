package ch.giantific.qwittig.data.repositories;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.facebook.login.LoginManager;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import ch.giantific.qwittig.Constants;
import ch.giantific.qwittig.data.jobs.UploadAvatarJob;
import ch.giantific.qwittig.data.rest.DeleteUserData;
import ch.giantific.qwittig.data.rest.UserIdToken;
import ch.giantific.qwittig.utils.rxwrapper.firebase.DataMapper;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxFirebaseAuth;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxFirebaseDatabase;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxFirebaseStorage;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxFirebaseUser;
import ch.giantific.qwittig.utils.rxwrapper.googleapi.GoogleApiClientSignOut;
import ch.giantific.qwittig.utils.rxwrapper.googleapi.GoogleApiClientUnlink;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.utils.Utils;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by fabio on 02.07.16.
 */
public class UserRepository {

    public static final int JPEG_COMPRESSION_RATE = 60;
    public static final int HEIGHT = 720;
    public static final int WIDTH = 720;

    private final FirebaseAuth auth;
    private final DatabaseReference databaseRef;
    private final StorageReference storageRef;
    private final FirebaseJobDispatcher jobDispatcher;
    private final DeleteUserData deleteUserData;

    @Inject
    public UserRepository(@NonNull FirebaseAuth auth,
                          @NonNull FirebaseDatabase firebaseDatabase,
                          @NonNull FirebaseStorage firebaseStorage,
                          @NonNull FirebaseJobDispatcher jobDispatcher,
                          @NonNull DeleteUserData deleteUserData) {
        this.auth = auth;
        databaseRef = firebaseDatabase.getReference();
        storageRef = firebaseStorage.getReferenceFromUrl(Constants.STORAGE_URL).child("avatars");
        this.jobDispatcher = jobDispatcher;
        this.deleteUserData = deleteUserData;
    }

    @Nullable
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public Single<String> getAuthToken() {
        final FirebaseUser firebaseUser = getCurrentUser();
        if (firebaseUser == null) {
            return Single.error(new Throwable("no user is logged in."));
        }

        return RxFirebaseUser.getToken(firebaseUser, false)
                .map(GetTokenResult::getToken);
    }

    public Observable<FirebaseUser> observeAuthStatus() {
        return RxFirebaseAuth.observeAuthState(auth);
    }

    public Observable<User> observeUser(@NonNull String userId) {
        final Query query = databaseRef.child(User.BASE_PATH).child(userId);
        return RxFirebaseDatabase.observeValue(query, User.class);
    }

    public Single<User> getUser(@NonNull String userId) {
        final Query query = databaseRef.child(User.BASE_PATH).child(userId);
        return RxFirebaseDatabase.observeValueOnce(query, User.class);
    }

    public Observable<String> observeCurrentIdentityId(@NonNull String userId) {
        final Query query = databaseRef.child(User.BASE_PATH).child(userId).child(User.PATH_CURRENT_IDENTITY);
        return RxFirebaseDatabase.observeValue(query, DataMapper.map(String.class));
    }

    public Observable<Identity> observeIdentity(@NonNull String identityId) {
        final Query query = databaseRef.child(Identity.BASE_PATH).child(Identity.BASE_PATH_ACTIVE).child(identityId);
        return RxFirebaseDatabase.observeValue(query, Identity.class);
    }

    public Single<Identity> getIdentity(@NonNull String identityId) {
        final Query query = databaseRef.child(Identity.BASE_PATH).child(Identity.BASE_PATH_ACTIVE).child(identityId);
        return RxFirebaseDatabase.observeValueOnce(query, Identity.class);
    }

    public void updateCurrentIdentity(@NonNull String userId, @NonNull String newCurrentIdentityId) {
        databaseRef.child(User.BASE_PATH).child(userId).child(User.PATH_CURRENT_IDENTITY).setValue(newCurrentIdentityId);
    }

    public Observable<Identity> switchGroup(@NonNull final String userId,
                                            @NonNull final String newGroupId) {
        return getUser(userId)
                .flatMapObservable(user -> Observable.from(user.getIdentitiesIds()))
                .flatMap(identityId -> getIdentity(identityId).toObservable())
                .filter(identity -> Objects.equals(identity.getGroup(), newGroupId))
                .doOnNext(identity -> updateCurrentIdentity(userId, identity.getId()));
    }

    public Single<Void> updateEmailPassword(@NonNull final FirebaseUser firebaseUser,
                                            @NonNull AuthCredential authCredential,
                                            @Nullable final String email,
                                            @Nullable final String password) {
        return RxFirebaseUser.reAuthenticate(firebaseUser, authCredential)
                .flatMap(aVoid -> {
                    if (!TextUtils.isEmpty(email)) {
                        return RxFirebaseUser.updateEmail(firebaseUser, email);
                    }

                    return Single.just(aVoid);
                })
                .flatMap(aVoid -> {
                    if (!TextUtils.isEmpty(password)) {
                        return RxFirebaseUser.updatePassword(firebaseUser, password);
                    }

                    return Single.just(aVoid);
                });
    }

    public Single<User> updateProfile(@NonNull final String nickname,
                                      @Nullable final String avatar,
                                      final boolean avatarChanged) {
        final FirebaseUser firebaseUser = getCurrentUser();
        if (firebaseUser == null) {
            return Single.error(new Throwable("user is not logged in"));
        }

        return getUser(firebaseUser.getUid())
                .doOnSuccess(user -> {
                    final Map<String, Object> childUpdates = new HashMap<>();
                    final Set<String> identities = user.getIdentitiesIds();
                    for (String identityId : identities) {
                        childUpdates.put(String.format("%s/%s/%s/%s", Identity.BASE_PATH, Identity.BASE_PATH_ACTIVE, identityId, Identity.PATH_NICKNAME), nickname);
                        if (avatarChanged) {
                            childUpdates.put(String.format("%s/%s/%s/%s", Identity.BASE_PATH, Identity.BASE_PATH_ACTIVE, identityId, Identity.PATH_AVATAR),
                                    !TextUtils.isEmpty(avatar)
                                    ? avatar : null);
                        }
                    }
                    databaseRef.updateChildren(childUpdates);

                    if (avatarChanged && !TextUtils.isEmpty(avatar) && !Utils.isHttpsUrl(avatar)) {
                        UploadAvatarJob.schedule(jobDispatcher, avatar, null);
                    }
                });
    }

    public Single<UploadTask.TaskSnapshot> uploadAvatar(@NonNull final String avatar,
                                                        @Nullable final String identityId) {
        if (!TextUtils.isEmpty(identityId)) {
            final File file = new File(avatar);
            final StorageReference avatarRef = storageRef.child(identityId);
            return RxFirebaseStorage.putFile(avatarRef, Uri.fromFile(file))
                    .doOnSuccess(taskSnapshot -> {
                        final Uri url = taskSnapshot.getDownloadUrl();
                        if (url != null) {
                            databaseRef.child(Identity.BASE_PATH)
                                    .child(Identity.BASE_PATH_ACTIVE)
                                    .child(identityId)
                                    .child(Identity.PATH_AVATAR)
                                    .setValue(url.toString());
                        }
                    });
        }

        final FirebaseUser firebaseUser = getCurrentUser();
        if (firebaseUser == null) {
            return Single.error(new Throwable("user is not logged in"));
        }

        return getUser(firebaseUser.getUid())
                .flatMap(user -> {
                    final File file = new File(avatar);
                    final StorageReference avatarRef = storageRef.child(user.getId());
                    return RxFirebaseStorage.putFile(avatarRef, Uri.fromFile(file))
                            .doOnSuccess(taskSnapshot -> {
                                final Uri url = taskSnapshot.getDownloadUrl();
                                if (url != null) {
                                    final Set<String> identities = user.getIdentitiesIds();
                                    final Map<String, Object> childUpdates = new HashMap<>();
                                    for (String identityId1 : identities) {
                                        childUpdates.put(String.format("%s/%s/%s/%s", Identity.BASE_PATH, Identity.BASE_PATH_ACTIVE, identityId1, Identity.PATH_AVATAR), url.toString());
                                    }
                                    databaseRef.updateChildren(childUpdates);
                                }
                            });
                });
    }

    public void updatePendingIdentityNickname(@NonNull String identityId, @NonNull String nickname) {
        databaseRef.child(Identity.BASE_PATH).child(Identity.BASE_PATH_ACTIVE).child(identityId).child(Identity.PATH_NICKNAME).setValue(nickname);
    }

    public void updatePendingIdentityAvatar(@NonNull final String identityId,
                                            @NonNull String avatar) {
        databaseRef.child(Identity.BASE_PATH).child(Identity.BASE_PATH_ACTIVE).child(identityId).child(Identity.PATH_AVATAR).setValue(avatar);
        UploadAvatarJob.schedule(jobDispatcher, avatar, identityId);
    }

    public Single<FirebaseUser> loginEmail(@NonNull final String username,
                                           @NonNull final String password) {
        return RxFirebaseAuth.signInWithEmailAndPassword(auth, username, password)
                .map(AuthResult::getUser)
                .doOnSuccess(firebaseUser -> {
                    final String token = FirebaseInstanceId.getInstance().getToken();
                    if (!TextUtils.isEmpty(token)) {
                        updateToken(firebaseUser.getUid(), token);
                    }
                });
    }

    public Single<FirebaseUser> signUpEmail(@NonNull final String username,
                                            @NonNull final String password) {
        return RxFirebaseAuth.createUserWithEmailAndPassword(auth, username, password)
                .map(AuthResult::getUser)
                .doOnSuccess(firebaseUser -> {
                    final String token = FirebaseInstanceId.getInstance().getToken();
                    if (!TextUtils.isEmpty(token)) {
                        updateToken(firebaseUser.getUid(), token);
                    }
                });
    }

    public Single<Void> requestPasswordReset(@NonNull String email) {
        return RxFirebaseAuth.sendPasswordResetEmail(auth, email);
    }

    public Single<FirebaseUser> loginGoogle(@NonNull String idToken) {
        final AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        return RxFirebaseAuth.signInWithCredential(auth, credential)
                .map(AuthResult::getUser)
                .doOnSuccess(firebaseUser -> {
                    final String token = FirebaseInstanceId.getInstance().getToken();
                    if (!TextUtils.isEmpty(token)) {
                        updateToken(firebaseUser.getUid(), token);
                    }
                });
    }

    public Single<Void> signOutGoogle(@NonNull Context context) {
        return GoogleApiClientSignOut.create(context);
    }

    public Single<Void> unlinkGoogle(@NonNull final Context context,
                                     @NonNull final FirebaseUser firebaseUser,
                                     @NonNull AuthCredential authCredential) {
        return RxFirebaseUser.reAuthenticate(firebaseUser, authCredential)
                .flatMapObservable(aVoid -> Observable.from(firebaseUser.getProviderData()))
                .map(UserInfo::getProviderId)
                .first(providerId -> Objects.equals(providerId, GoogleAuthProvider.PROVIDER_ID))
                .toSingle()
                .flatMap(providerId -> RxFirebaseUser.unlinkFromProvider(firebaseUser, providerId))
                .flatMap(authResult -> GoogleApiClientUnlink.create(context));
    }

    public boolean isGoogleUser(@NonNull FirebaseUser firebaseUser) {
        for (UserInfo userInfo : firebaseUser.getProviderData()) {
            if (Objects.equals(userInfo.getProviderId(), GoogleAuthProvider.PROVIDER_ID)) {
                return true;
            }
        }

        return false;
    }

    public Single<FirebaseUser> loginFacebook(@NonNull String idToken) {
        final AuthCredential credential = FacebookAuthProvider.getCredential(idToken);
        return RxFirebaseAuth.signInWithCredential(auth, credential)
                .map(AuthResult::getUser)
                .doOnSuccess(firebaseUser -> {
                    final String token = FirebaseInstanceId.getInstance().getToken();
                    if (!TextUtils.isEmpty(token)) {
                        updateToken(firebaseUser.getUid(), token);
                    }
                });
    }

    public Single<Void> unlinkFacebook() {
        // TODO: not available in facebook sdk, use REST api
        return null;
    }

    public boolean isFacebookUser(@NonNull FirebaseUser firebaseUser) {
        for (UserInfo userInfo : firebaseUser.getProviderData()) {
            if (Objects.equals(userInfo.getProviderId(), FacebookAuthProvider.PROVIDER_ID)) {
                return true;
            }
        }

        return false;
    }

    public void signOut(@NonNull FirebaseUser firebaseUser) {
        for (UserInfo userInfo : firebaseUser.getProviderData()) {
            if (Objects.equals(userInfo.getProviderId(), FacebookAuthProvider.PROVIDER_ID)) {
                LoginManager.getInstance().logOut();
                break;
            }
        }

        databaseRef.child(User.BASE_PATH).child(firebaseUser.getUid()).child(User.PATH_TOKENS).removeValue();
        auth.signOut();
    }

    public Single<AuthResult> linkUserWithCredential(@NonNull final FirebaseUser firebaseUser,
                                                     @NonNull AuthCredential oldCredential,
                                                     @NonNull final AuthCredential newCredential) {
        return RxFirebaseUser.reAuthenticate(firebaseUser, oldCredential)
                .flatMap(aVoid -> RxFirebaseUser.linkWithCredential(firebaseUser, newCredential));
    }

    public Single<Void> deleteUser(@NonNull final FirebaseUser firebaseUser,
                                   @NonNull AuthCredential authCredential) {
        return RxFirebaseUser.reAuthenticate(firebaseUser, authCredential)
                .flatMap(aVoid -> RxFirebaseUser.getToken(firebaseUser, false))
                .flatMap(getTokenResult -> {
                    final String idToken = getTokenResult.getToken();
                    if (TextUtils.isEmpty(idToken)) {
                        return Single.error(new Throwable("could not get id token!"));
                    }

                    return deleteUserData.deleteUserData(new UserIdToken(idToken))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread());
                })
                .flatMap(aVoid -> RxFirebaseAuth.deleteUser(firebaseUser))
                .doOnSuccess(aVoid -> auth.signOut());
    }

    public void updateToken(@NonNull String userId, @NonNull String token) {
        databaseRef.child(User.BASE_PATH).child(userId).child(User.PATH_TOKENS).child(token).setValue(true);
    }

    public String getNicknameFromEmail(@Nullable String email) {
        return !TextUtils.isEmpty(email) ? email.substring(0, email.indexOf("@")) : "";
    }
}
