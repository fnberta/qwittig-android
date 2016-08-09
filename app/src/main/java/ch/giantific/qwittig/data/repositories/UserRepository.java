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
import ch.giantific.qwittig.data.rxwrapper.firebase.RxFirebaseAuth;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxFirebaseDatabase;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxFirebaseStorage;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxFirebaseUser;
import ch.giantific.qwittig.data.rxwrapper.googleapi.GoogleApiClientSignOut;
import ch.giantific.qwittig.data.rxwrapper.googleapi.GoogleApiClientUnlink;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.utils.Utils;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by fabio on 02.07.16.
 */
public class UserRepository {

    public static final int JPEG_COMPRESSION_RATE = 60;
    public static final int HEIGHT = 720;
    public static final int WIDTH = 720;
    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabaseRef;
    private final StorageReference mStorageRef;
    private final FirebaseJobDispatcher mJobDispatcher;
    private final DeleteUserData mDeleteUserData;

    @Inject
    public UserRepository(@NonNull FirebaseAuth auth,
                          @NonNull FirebaseDatabase firebaseDatabase,
                          @NonNull FirebaseStorage firebaseStorage,
                          @NonNull FirebaseJobDispatcher jobDispatcher,
                          @NonNull DeleteUserData deleteUserData) {
        mAuth = auth;
        mDatabaseRef = firebaseDatabase.getReference();
        mStorageRef = firebaseStorage.getReferenceFromUrl(Constants.STORAGE_URL).child("avatars");
        mJobDispatcher = jobDispatcher;
        mDeleteUserData = deleteUserData;
    }

    @Nullable
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public Single<String> getAuthToken() {
        final FirebaseUser firebaseUser = getCurrentUser();
        if (firebaseUser == null) {
            return Single.error(new Throwable("no user is logged in."));
        }

        return RxFirebaseUser.getToken(firebaseUser, false)
                .map(new Func1<GetTokenResult, String>() {
                    @Override
                    public String call(GetTokenResult getTokenResult) {
                        return getTokenResult.getToken();
                    }
                });
    }

    public Observable<FirebaseUser> observeAuthStatus() {
        return RxFirebaseAuth.observeAuthState(mAuth);
    }

    public Observable<User> observeUser(@NonNull String userId) {
        final Query query = mDatabaseRef.child(User.PATH).child(userId);
        return RxFirebaseDatabase.observeValue(query, User.class);
    }

    public Single<User> getUser(@NonNull String userId) {
        final Query query = mDatabaseRef.child(User.PATH).child(userId);
        return RxFirebaseDatabase.observeValueOnce(query, User.class);
    }

    public Observable<Identity> observeIdentity(@NonNull String identityId) {
        final Query query = mDatabaseRef.child(Identity.PATH).child(identityId);
        return RxFirebaseDatabase.observeValue(query, Identity.class);
    }

    public Single<Identity> getIdentity(@NonNull String identityId) {
        final Query query = mDatabaseRef.child(Identity.PATH).child(identityId);
        return RxFirebaseDatabase.observeValueOnce(query, Identity.class);
    }

    public void updateCurrentIdentity(@NonNull String userId, @NonNull String newCurrentIdentityId) {
        mDatabaseRef.child(User.PATH).child(userId).child(User.PATH_CURRENT_IDENTITY).setValue(newCurrentIdentityId);
    }

    public Observable<Identity> switchGroup(@NonNull final User user,
                                            @NonNull final String newGroupId) {
        return Observable.from(user.getIdentitiesIds())
                .flatMap(new Func1<String, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(String identityId) {
                        return getIdentity(identityId).toObservable();
                    }
                })
                .filter(new Func1<Identity, Boolean>() {
                    @Override
                    public Boolean call(Identity identity) {
                        return Objects.equals(identity.getGroup(), newGroupId);
                    }
                })
                .doOnNext(new Action1<Identity>() {
                    @Override
                    public void call(Identity identity) {
                        updateCurrentIdentity(user.getId(), identity.getId());
                    }
                });
    }

    public Single<Void> updateEmailPassword(@NonNull final FirebaseUser firebaseUser,
                                            @NonNull AuthCredential authCredential,
                                            @Nullable final String email,
                                            @Nullable final String password) {
        return RxFirebaseUser.reauthenticate(firebaseUser, authCredential)
                .flatMap(new Func1<Void, Single<? extends Void>>() {
                    @Override
                    public Single<? extends Void> call(Void aVoid) {
                        if (!TextUtils.isEmpty(email)) {
                            return RxFirebaseUser.updateEmail(firebaseUser, email);
                        }

                        return Single.just(aVoid);
                    }
                })
                .flatMap(new Func1<Void, Single<? extends Void>>() {
                    @Override
                    public Single<? extends Void> call(Void aVoid) {
                        if (!TextUtils.isEmpty(password)) {
                            return RxFirebaseUser.updatePassword(firebaseUser, password);
                        }

                        return Single.just(aVoid);
                    }
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
                .doOnSuccess(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        final Map<String, Object> childUpdates = new HashMap<>();
                        final Set<String> identities = user.getIdentitiesIds();
                        for (String identityId : identities) {
                            childUpdates.put(Identity.PATH + "/" + identityId + "/" + Identity.PATH_NICKNAME, nickname);
                            if (avatarChanged) {
                                childUpdates.put(Identity.PATH + "/" + identityId + "/" +
                                        Identity.PATH_AVATAR, !TextUtils.isEmpty(avatar)
                                        ? avatar : null);
                            }
                        }
                        mDatabaseRef.updateChildren(childUpdates);

                        if (avatarChanged && !TextUtils.isEmpty(avatar) && !Utils.isHttpsUrl(avatar)) {
                            UploadAvatarJob.schedule(mJobDispatcher, avatar, null);
                        }
                    }
                });
    }

    public Single<UploadTask.TaskSnapshot> uploadAvatar(@NonNull final String avatar,
                                                        @Nullable final String identityId) {
        if (!TextUtils.isEmpty(identityId)) {
            final File file = new File(avatar);
            final StorageReference avatarRef = mStorageRef.child(identityId);
            return RxFirebaseStorage.putFile(avatarRef, Uri.fromFile(file))
                    .doOnSuccess(new Action1<UploadTask.TaskSnapshot>() {
                        @Override
                        public void call(UploadTask.TaskSnapshot taskSnapshot) {
                            final Uri url = taskSnapshot.getDownloadUrl();
                            if (url != null) {
                                mDatabaseRef.child(Identity.PATH)
                                        .child(identityId)
                                        .child(Identity.PATH_AVATAR)
                                        .setValue(url.toString());
                            }
                        }
                    });
        }

        final FirebaseUser firebaseUser = getCurrentUser();
        if (firebaseUser == null) {
            return Single.error(new Throwable("user is not logged in"));
        }

        return getUser(firebaseUser.getUid())
                .flatMap(new Func1<User, Single<? extends UploadTask.TaskSnapshot>>() {
                    @Override
                    public Single<? extends UploadTask.TaskSnapshot> call(final User user) {
                        final File file = new File(avatar);
                        final StorageReference avatarRef = mStorageRef.child(user.getId());
                        return RxFirebaseStorage.putFile(avatarRef, Uri.fromFile(file))
                                .doOnSuccess(new Action1<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void call(UploadTask.TaskSnapshot taskSnapshot) {
                                        final Uri url = taskSnapshot.getDownloadUrl();
                                        if (url != null) {
                                            final Set<String> identities = user.getIdentitiesIds();
                                            final Map<String, Object> childUpdates = new HashMap<>();
                                            for (String identityId : identities) {
                                                childUpdates.put(Identity.PATH + "/"
                                                        + identityId + "/"
                                                        + Identity.PATH_AVATAR, url.toString());
                                            }
                                            mDatabaseRef.updateChildren(childUpdates);
                                        }
                                    }
                                });
                    }
                });
    }

    public void updatePendingIdentityNickname(@NonNull String identityId, @NonNull String nickname) {
        mDatabaseRef.child(Identity.PATH).child(identityId).child(Identity.PATH_NICKNAME).setValue(nickname);
    }

    public void updatePendingIdentityAvatar(@NonNull final String identityId,
                                            @NonNull String avatar) {
        mDatabaseRef.child(Identity.PATH).child(identityId).child(Identity.PATH_AVATAR).setValue(avatar);
        UploadAvatarJob.schedule(mJobDispatcher, avatar, identityId);
    }

    public Single<FirebaseUser> loginEmail(@NonNull final String username,
                                           @NonNull final String password) {
        return RxFirebaseAuth.signInWithEmailAndPassword(mAuth, username, password)
                .map(new Func1<AuthResult, FirebaseUser>() {
                    @Override
                    public FirebaseUser call(AuthResult authResult) {
                        return authResult.getUser();
                    }
                })
                .doOnSuccess(new Action1<FirebaseUser>() {
                    @Override
                    public void call(FirebaseUser firebaseUser) {
                        final String token = FirebaseInstanceId.getInstance().getToken();
                        if (!TextUtils.isEmpty(token)) {
                            updateToken(firebaseUser.getUid(), token);
                        }
                    }
                });
    }

    public Single<FirebaseUser> signUpEmail(@NonNull final String username,
                                            @NonNull final String password) {
        return RxFirebaseAuth.createUserWithEmailAndPassword(mAuth, username, password)
                .map(new Func1<AuthResult, FirebaseUser>() {
                    @Override
                    public FirebaseUser call(AuthResult authResult) {
                        return authResult.getUser();
                    }
                })
                .doOnSuccess(new Action1<FirebaseUser>() {
                    @Override
                    public void call(FirebaseUser firebaseUser) {
                        final String token = FirebaseInstanceId.getInstance().getToken();
                        if (!TextUtils.isEmpty(token)) {
                            updateToken(firebaseUser.getUid(), token);
                        }
                    }
                });
    }

    public Single<Void> requestPasswordReset(@NonNull String email) {
        return RxFirebaseAuth.sendPasswordResetEmail(mAuth, email);
    }

    public Single<FirebaseUser> loginGoogle(@NonNull String idToken) {
        final AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        return RxFirebaseAuth.signInWithCredential(mAuth, credential)
                .map(new Func1<AuthResult, FirebaseUser>() {
                    @Override
                    public FirebaseUser call(AuthResult authResult) {
                        return authResult.getUser();
                    }
                })
                .doOnSuccess(new Action1<FirebaseUser>() {
                    @Override
                    public void call(FirebaseUser firebaseUser) {
                        final String token = FirebaseInstanceId.getInstance().getToken();
                        if (!TextUtils.isEmpty(token)) {
                            updateToken(firebaseUser.getUid(), token);
                        }
                    }
                });
    }

    public Single<Void> signOutGoogle(@NonNull Context context) {
        return GoogleApiClientSignOut.create(context);
    }

    public Single<Void> unlinkGoogle(@NonNull final Context context,
                                     @NonNull final FirebaseUser firebaseUser,
                                     @NonNull AuthCredential authCredential) {
        return RxFirebaseUser.reauthenticate(firebaseUser, authCredential)
                .flatMapObservable(new Func1<Void, Observable<UserInfo>>() {
                    @Override
                    public Observable<UserInfo> call(Void aVoid) {
                        return Observable.from(firebaseUser.getProviderData());
                    }
                })
                .map(new Func1<UserInfo, String>() {
                    @Override
                    public String call(UserInfo userInfo) {
                        return userInfo.getProviderId();
                    }
                })
                .first(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String providerId) {
                        return Objects.equals(providerId, GoogleAuthProvider.PROVIDER_ID);
                    }
                })
                .toSingle()
                .flatMap(new Func1<String, Single<AuthResult>>() {
                    @Override
                    public Single<AuthResult> call(String providerId) {
                        return RxFirebaseUser.unlinkFromProvider(firebaseUser, providerId);
                    }
                })
                .flatMap(new Func1<AuthResult, Single<? extends Void>>() {
                    @Override
                    public Single<? extends Void> call(AuthResult authResult) {
                        return GoogleApiClientUnlink.create(context);
                    }
                });
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
        return RxFirebaseAuth.signInWithCredential(mAuth, credential)
                .map(new Func1<AuthResult, FirebaseUser>() {
                    @Override
                    public FirebaseUser call(AuthResult authResult) {
                        return authResult.getUser();
                    }
                })
                .doOnSuccess(new Action1<FirebaseUser>() {
                    @Override
                    public void call(FirebaseUser firebaseUser) {
                        final String token = FirebaseInstanceId.getInstance().getToken();
                        if (!TextUtils.isEmpty(token)) {
                            updateToken(firebaseUser.getUid(), token);
                        }
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

    public Single<Boolean> isUserNew(@NonNull String userId) {
        return RxFirebaseDatabase.checkForChild(mDatabaseRef.child(User.PATH).child(userId), User.PATH_IDENTITIES)
                .map(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean hasChild) {
                        return !hasChild;
                    }
                });
    }

    public void signOut(@NonNull FirebaseUser firebaseUser) {
        for (UserInfo userInfo : firebaseUser.getProviderData()) {
            if (Objects.equals(userInfo.getProviderId(), FacebookAuthProvider.PROVIDER_ID)) {
                LoginManager.getInstance().logOut();
                break;
            }
        }

        mDatabaseRef.child(User.PATH).child(firebaseUser.getUid()).child(User.PATH_TOKENS).removeValue();
        mAuth.signOut();
    }

    public Single<AuthResult> linkUserWithCredential(@NonNull final FirebaseUser firebaseUser,
                                                     @NonNull AuthCredential oldCredential,
                                                     @NonNull final AuthCredential newCredential) {
        return RxFirebaseUser.reauthenticate(firebaseUser, oldCredential)
                .flatMap(new Func1<Void, Single<? extends AuthResult>>() {
                    @Override
                    public Single<? extends AuthResult> call(Void aVoid) {
                        return RxFirebaseUser.linkWithCredential(firebaseUser, newCredential);
                    }
                });
    }

    public Single<Void> deleteUser(@NonNull final FirebaseUser firebaseUser,
                                   @NonNull AuthCredential authCredential) {
        return RxFirebaseUser.reauthenticate(firebaseUser, authCredential)
                .flatMap(new Func1<Void, Single<GetTokenResult>>() {
                    @Override
                    public Single<GetTokenResult> call(Void aVoid) {
                        return RxFirebaseUser.getToken(firebaseUser, false);
                    }
                })
                .flatMap(new Func1<GetTokenResult, Single<? extends Void>>() {
                    @Override
                    public Single<? extends Void> call(GetTokenResult getTokenResult) {
                        final String idToken = getTokenResult.getToken();
                        if (TextUtils.isEmpty(idToken)) {
                            return Single.error(new Throwable("could not get id token!"));
                        }

                        return mDeleteUserData.deleteUserData(new UserIdToken(idToken))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread());
                    }
                })
                .flatMap(new Func1<Void, Single<? extends Void>>() {
                    @Override
                    public Single<? extends Void> call(Void aVoid) {
                        return RxFirebaseAuth.deleteUser(firebaseUser);
                    }
                })
                .doOnSuccess(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        mAuth.signOut();
                    }
                });
    }

    public void updateToken(@NonNull String userId, @NonNull String token) {
        mDatabaseRef.child(User.PATH).child(userId).child(User.PATH_TOKENS).child(token).setValue(true);
    }
}
