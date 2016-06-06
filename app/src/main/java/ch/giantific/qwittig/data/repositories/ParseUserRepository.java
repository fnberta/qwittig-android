/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseSession;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ch.giantific.qwittig.BuildConfig;
import ch.giantific.qwittig.data.push.PushBroadcastReceiver;
import ch.giantific.qwittig.data.services.SaveIdentityTaskService;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.utils.googleapi.GoogleApiClientSignOut;
import ch.giantific.qwittig.utils.googleapi.GoogleApiClientUnlink;
import ch.giantific.qwittig.utils.parse.ParseInstallationUtils;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.util.LinkProperties;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Provides an implementation of {@link UserRepository} that uses the Parse.com framework as
 * the local and online data store.
 */
public class ParseUserRepository extends ParseBaseRepository implements UserRepository {

    private static final String VERIFY_GOOGLE_LOGIN = "loginWithGoogle";
    private static final String ADD_IDENTITY_TO_USER = "addIdentityToUser";
    private static final String PARAM_ID_TOKEN = "idToken";
    private static final String PARAM_IDENTITY_ID = "identityId";
    private static final String CALCULATE_BALANCES = "calculateBalances";
    private static final String ADD_NEW_GROUP = "addGroup";
    private static final String FIRST_GROUP_NAME = "Qwittig";
    private static final String FIRST_GROUP_CURRENCY = "CHF";
    private final GcmNetworkManager mGcmNetworkManager;

    public ParseUserRepository(@NonNull GcmNetworkManager gcmNetworkManager) {
        super();

        mGcmNetworkManager = gcmNetworkManager;
    }

    @Override
    protected String getClassName() {
        return User.CLASS;
    }

    @Nullable
    @Override
    public User getCurrentUser() {
        return (User) ParseUser.getCurrentUser();
    }

    @Override
    public Single<User> updateUser(@NonNull User user) {
        return fetch(user);
    }

    @Override
    public Single<String> getUserSessionToken() {
        return Single.create(new Single.OnSubscribe<String>() {
            @Override
            public void call(final SingleSubscriber<? super String> singleSubscriber) {
                ParseSession.getCurrentSessionInBackground(new GetCallback<ParseSession>() {
                    @Override
                    public void done(@NonNull ParseSession parseSession, @Nullable ParseException e) {
                        if (singleSubscriber.isUnsubscribed()) {
                            return;
                        }

                        if (e != null) {
                            singleSubscriber.onError(e);
                        } else {
                            singleSubscriber.onSuccess(parseSession.getSessionToken());
                        }
                    }
                });
            }
        });
    }

    @Override
    public Single<User> loginEmail(@NonNull final String username, @NonNull final String password,
                                   @NonNull final String identityId) {
        return login(username, password)
                .flatMap(new Func1<User, Single<? extends User>>() {
                    @Override
                    public Single<? extends User> call(final User user) {
                        if (TextUtils.isEmpty(identityId)) {
                            return Single.just(user);
                        }

                        return addIdentityToUser(identityId)
                                .flatMap(new Func1<String, Single<User>>() {
                                    @Override
                                    public Single<User> call(String s) {
                                        return updateUser(user);
                                    }
                                });
                    }
                })
                .flatMap(new Func1<User, Single<User>>() {
                    @Override
                    public Single<User> call(final User user) {
                        return fetchIdentitiesData(user.getIdentities())
                                .toList()
                                .toSingle()
                                .flatMap(new Func1<List<Identity>, Single<? extends User>>() {
                                    @Override
                                    public Single<? extends User> call(List<Identity> identities) {
                                        return setupInstallation(user);
                                    }
                                });
                    }
                });
    }

    @Override
    public Single<String> requestPasswordReset(@NonNull final String email) {
        return Single.create(new Single.OnSubscribe<String>() {
            @Override
            public void call(final SingleSubscriber<? super String> singleSubscriber) {
                ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
                    @Override
                    public void done(@Nullable ParseException e) {
                        if (singleSubscriber.isUnsubscribed()) {
                            return;
                        }

                        if (e != null) {
                            singleSubscriber.onError(e);
                        } else {
                            singleSubscriber.onSuccess(email);
                        }
                    }
                });
            }
        });
    }

    @Override
    public Single<User> signUpEmail(@NonNull final String username,
                                    @NonNull final String password, @NonNull final String identityId) {
        return Single
                .create(new Single.OnSubscribe<User>() {
                    @Override
                    public void call(final SingleSubscriber<? super User> singleSubscriber) {
                        final User user = new User(username, password);
                        user.signUpInBackground(new SignUpCallback() {
                            @Override
                            public void done(@Nullable ParseException e) {
                                if (singleSubscriber.isUnsubscribed()) {
                                    return;
                                }

                                if (e != null) {
                                    singleSubscriber.onError(e);
                                } else {
                                    singleSubscriber.onSuccess(user);
                                }
                            }
                        });
                    }
                })
                .flatMap(new Func1<User, Single<? extends User>>() {
                    @Override
                    public Single<? extends User> call(final User user) {
                        return addFirstGroup(user, identityId)
                                .flatMap(new Func1<Identity, Single<User>>() {
                                    @Override
                                    public Single<User> call(Identity identity) {
                                        return setupInstallation(user);
                                    }
                                });
                    }
                });
    }

    private Single<Identity> addFirstGroup(@NonNull final User user, @NonNull String identityId) {
        final Single<String> addGroup = TextUtils.isEmpty(identityId) ? addGroup(FIRST_GROUP_NAME, FIRST_GROUP_CURRENCY) : addIdentityToUser(identityId);
        return addGroup
                .flatMap(new Func1<String, Single<? extends User>>() {
                    @Override
                    public Single<? extends User> call(String result) {
                        return updateUser(user);
                    }
                })
                .flatMap(new Func1<User, Single<Identity>>() {
                    @Override
                    public Single<Identity> call(final User user) {
                        return fetchIdentityData(user.getCurrentIdentity());
                    }
                })
                .flatMap(new Func1<Identity, Single<Identity>>() {
                    @Override
                    public Single<Identity> call(Identity identity) {
                        return saveIdentityLocal(identity);
                    }
                });
    }

    @Override
    public Single<User> loginFacebook(@NonNull final Fragment fragment, @NonNull final String identityId) {
        return Single
                .create(new Single.OnSubscribe<User>() {
                    @Override
                    public void call(final SingleSubscriber<? super User> singleSubscriber) {
                        final List<String> permissions = Arrays.asList("public_profile", "email");
                        ParseFacebookUtils.logInWithReadPermissionsInBackground(fragment, permissions, new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if (singleSubscriber.isUnsubscribed()) {
                                    return;
                                }

                                if (e != null) {
                                    singleSubscriber.onError(e);
                                } else {
                                    singleSubscriber.onSuccess((User) user);
                                }
                            }
                        });
                    }
                })
                .flatMap(new Func1<User, Single<User>>() {
                    @Override
                    public Single<User> call(final User user) {
                        if (user.isNew()) {
                            return addFirstGroup(user, identityId)
                                    .flatMap(new Func1<Identity, Single<? extends User>>() {
                                        @Override
                                        public Single<? extends User> call(Identity identity) {
                                            return setFacebookData(user, identity, fragment);
                                        }
                                    });
                        }

                        return fetchIdentitiesData(user.getIdentities())
                                .toList()
                                .toSingle()
                                .map(new Func1<List<Identity>, User>() {
                                    @Override
                                    public User call(List<Identity> identities) {
                                        return user;
                                    }
                                });
                    }
                })
                .flatMap(new Func1<User, Single<User>>() {
                    @Override
                    public Single<User> call(final User user) {
                        return setupInstallation(user);
                    }
                });
    }

    private Single<User> setFacebookData(@NonNull final User user, @NonNull final Identity identity,
                                         @NonNull final Fragment fragment) {
        return Single
                .create(new Single.OnSubscribe<JSONObject>() {
                    @Override
                    public void call(final SingleSubscriber<? super JSONObject> singleSubscriber) {
                        final GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        if (singleSubscriber.isUnsubscribed()) {
                                            return;
                                        }

                                        if (object == null) {
                                            singleSubscriber.onError(response.getError().getException());
                                        } else {
                                            singleSubscriber.onSuccess(object);
                                        }
                                    }
                                });
                        final Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,email,first_name");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }
                })
                .flatMap(new Func1<JSONObject, Single<ParseFile>>() {
                    @Override
                    public Single<ParseFile> call(JSONObject facebookData) {
                        final String email = facebookData.optString("email");
                        if (!TextUtils.isEmpty(email)) {
                            user.setUsername(email);
                        }
                        final String name = facebookData.optString("first_name");
                        if (!TextUtils.isEmpty(name)) {
                            identity.setNickname(name);
                        }

                        final String id = facebookData.optString("id");
                        return getFacebookUserAvatar(fragment, id);
                    }
                })
                .flatMap(new Func1<ParseFile, Single<? extends User>>() {
                    @Override
                    public Single<? extends User> call(ParseFile avatar) {
                        identity.setAvatar(avatar);
                        return save(user);
                    }
                });
    }

    private Single<ParseFile> getFacebookUserAvatar(@NonNull final Fragment fragment,
                                                    @NonNull String facebookId) {
        final String pictureUrl = "http://graph.facebook.com/" + facebookId + "/picture?type=large";
        return Single
                .create(new Single.OnSubscribe<byte[]>() {
                    @Override
                    public void call(final SingleSubscriber<? super byte[]> singleSubscriber) {
                        Glide.with(fragment)
                                .load(pictureUrl)
                                .asBitmap()
                                .toBytes(Bitmap.CompressFormat.JPEG, UserRepository.JPEG_COMPRESSION_RATE)
                                .centerCrop()
                                .into(new SimpleTarget<byte[]>(UserRepository.WIDTH, UserRepository.HEIGHT) {
                                    @Override
                                    public void onResourceReady(byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                                        if (!singleSubscriber.isUnsubscribed()) {
                                            singleSubscriber.onSuccess(resource);
                                        }
                                    }

                                    @Override
                                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                        super.onLoadFailed(e, errorDrawable);

                                        if (!singleSubscriber.isUnsubscribed()) {
                                            singleSubscriber.onError(e);
                                        }
                                    }
                                });
                    }
                })
                .flatMap(new Func1<byte[], Single<? extends ParseFile>>() {
                    @Override
                    public Single<? extends ParseFile> call(byte[] bytes) {
                        final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpg");
                        final ParseFile avatar = new ParseFile(UserRepository.FILE_NAME, bytes, mimeType);
                        return saveFile(avatar);
                    }
                });
    }

    @Override
    public Single<User> loginGoogle(@NonNull String idToken,
                                    @NonNull final String username,
                                    @NonNull final Uri photoUrl,
                                    @NonNull final String identityId, @NonNull final Fragment fragment) {
        return verifyGoogleLogin(idToken)
                .flatMap(new Func1<String, Single<? extends User>>() {
                    @Override
                    public Single<? extends User> call(String sessionToken) {
                        return becomeUser(sessionToken);
                    }
                })
                .flatMap(new Func1<User, Single<User>>() {
                    @Override
                    public Single<User> call(final User user) {
                        if (user.isNew()) {
                            return addFirstGroup(user, identityId)
                                    .flatMap(new Func1<Identity, Single<? extends User>>() {
                                        @Override
                                        public Single<? extends User> call(Identity identity) {
                                            return setGoogleData(user, identity,
                                                    username, photoUrl, fragment);
                                        }
                                    });
                        }

                        return fetchIdentitiesData(user.getIdentities())
                                .toList()
                                .toSingle()
                                .map(new Func1<List<Identity>, User>() {
                                    @Override
                                    public User call(List<Identity> identities) {
                                        return user;
                                    }
                                });
                    }
                })
                .flatMap(new Func1<User, Single<User>>() {
                    @Override
                    public Single<User> call(final User user) {
                        return setupInstallation(user);
                    }
                });
    }

    private Single<String> verifyGoogleLogin(@NonNull String idToken) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_ID_TOKEN, idToken);
        return callFunctionInBackground(VERIFY_GOOGLE_LOGIN, params);
    }

    private Single<User> becomeUser(@NonNull final String sessionToken) {
        return Single.create(new Single.OnSubscribe<User>() {
            @Override
            public void call(final SingleSubscriber<? super User> singleSubscriber) {
                ParseUser.becomeInBackground(sessionToken, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (singleSubscriber.isUnsubscribed()) {
                            return;
                        }

                        if (e != null) {
                            singleSubscriber.onError(e);
                        } else {
                            singleSubscriber.onSuccess((User) user);
                        }
                    }
                });
            }
        });
    }

    private Single<User> setGoogleData(@NonNull final User user, @NonNull final Identity identity,
                                       @NonNull final String displayName,
                                       @NonNull final Uri photoUrl,
                                       @NonNull final Fragment fragment) {
        return Single
                .create(new Single.OnSubscribe<byte[]>() {
                    @Override
                    public void call(final SingleSubscriber<? super byte[]> singleSubscriber) {
                        Glide.with(fragment)
                                .load(photoUrl)
                                .asBitmap()
                                .toBytes(Bitmap.CompressFormat.JPEG, UserRepository.JPEG_COMPRESSION_RATE)
                                .centerCrop()
                                .into(new SimpleTarget<byte[]>(UserRepository.WIDTH, UserRepository.HEIGHT) {
                                    @Override
                                    public void onResourceReady(byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                                        if (!singleSubscriber.isUnsubscribed()) {
                                            singleSubscriber.onSuccess(resource);
                                        }
                                    }

                                    @Override
                                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                        super.onLoadFailed(e, errorDrawable);

                                        if (!singleSubscriber.isUnsubscribed()) {
                                            singleSubscriber.onError(e);
                                        }
                                    }
                                });
                    }
                })
                .flatMap(new Func1<byte[], Single<? extends ParseFile>>() {
                    @Override
                    public Single<? extends ParseFile> call(byte[] bytes) {
                        final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpg");
                        final ParseFile avatar = new ParseFile(UserRepository.FILE_NAME, bytes, mimeType);
                        return saveFile(avatar);
                    }
                })
                .flatMap(new Func1<ParseFile, Single<? extends User>>() {
                    @Override
                    public Single<? extends User> call(ParseFile parseFile) {
                        identity.setNickname(displayName);
                        identity.setAvatar(parseFile);
                        return save(user);
                    }
                });
    }

    @Override
    public Single<Identity> handleInvitation(@NonNull final User user, @NonNull String identityId) {
        return addIdentityToUser(identityId)
                .flatMap(new Func1<String, Single<User>>() {
                    @Override
                    public Single<User> call(String s) {
                        return updateUser(user);
                    }
                })
                .flatMap(new Func1<User, Single<Identity>>() {
                    @Override
                    public Single<Identity> call(User user) {
                        return fetchIdentityData(user.getCurrentIdentity());
                    }
                })
                .flatMap(new Func1<Identity, Single<Identity>>() {
                    @Override
                    public Single<Identity> call(Identity identity) {
                        return saveIdentityLocal(identity);
                    }
                })
                .flatMap(new Func1<Identity, Single<Group>>() {
                    @Override
                    public Single<Group> call(Identity identity) {
                        return subscribeGroup(identity.getGroup());
                    }
                })
                .map(new Func1<Group, Identity>() {
                    @Override
                    public Identity call(Group group) {
                        return user.getCurrentIdentity();
                    }
                });
    }

    private Single<String> addIdentityToUser(@NonNull String identityId) {
        final Map<String, Object> params = new HashMap<>();
        params.put(PARAM_IDENTITY_ID, identityId);
        return callFunctionInBackground(ADD_IDENTITY_TO_USER, params);
    }

    @Override
    public Single<User> logoutUser(@NonNull final User user, final boolean deleteUser) {
        return clearInstallation()
                .flatMap(new Func1<ParseInstallation, Single<? extends User>>() {
                    @Override
                    public Single<? extends User> call(ParseInstallation installation) {
                        if (deleteUser) {
                            return delete(user)
                                    .flatMap(new Func1<User, Single<? extends User>>() {
                                        @Override
                                        public Single<? extends User> call(User user) {
                                            return logout(user);
                                        }
                                    });
                        }

                        return logout(user);
                    }
                });
    }

    @Override
    public Single<User> unlinkFacebook(@NonNull final User user, final boolean deleteUser) {
        return Single
                .create(new Single.OnSubscribe<User>() {
                    @Override
                    public void call(final SingleSubscriber<? super User> singleSubscriber) {
                        ParseFacebookUtils.unlinkInBackground(user, new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (singleSubscriber.isUnsubscribed()) {
                                    return;
                                }

                                if (e != null) {
                                    singleSubscriber.onError(e);
                                } else {
                                    singleSubscriber.onSuccess(user);
                                }
                            }
                        });
                    }
                })
                .flatMap(new Func1<User, Single<? extends User>>() {
                    @Override
                    public Single<? extends User> call(User user) {
                        return deleteUser ? logoutUser(user, true) : Single.just(user);
                    }
                });
    }

    @Override
    public Single<User> signOutGoogle(@NonNull Context context, @NonNull final User user) {
        return GoogleApiClientSignOut.create(context)
                .flatMap(new Func1<Void, Single<ParseInstallation>>() {
                    @Override
                    public Single<ParseInstallation> call(Void aVoid) {
                        return clearInstallation();
                    }
                })
                .flatMap(new Func1<ParseInstallation, Single<User>>() {
                    @Override
                    public Single<User> call(ParseInstallation parseInstallation) {
                        return logout(user);
                    }
                });
    }

    @Override
    public Single<User> unlinkGoogle(@NonNull Context context, @NonNull final User user,
                                     final boolean deleteUser) {
        return GoogleApiClientUnlink.create(context)
                .flatMap(new Func1<Void, Single<? extends User>>() {
                    @Override
                    public Single<? extends User> call(Void aVoid) {
                        if (deleteUser) {
                            return logoutUser(user, true);
                        }

                        user.removeGoogleId();
                        return save(user);
                    }
                });
    }

    @Override
    public Single<User> setupInstallation(@NonNull final User user) {
        return Observable.from(user.getIdentities())
                .map(new Func1<Identity, String>() {
                    @Override
                    public String call(Identity identity) {
                        return identity.getGroup().getObjectId();
                    }
                })
                .toList()
                .toSingle()
                .flatMap(new Func1<List<String>, Single<ParseInstallation>>() {
                    @Override
                    public Single<ParseInstallation> call(List<String> channels) {
                        final ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                        installation.addAllUnique(ParseInstallationUtils.CHANNELS, channels);
                        installation.put(ParseInstallationUtils.USER, user);
                        return save(installation);
                    }
                })
                .map(new Func1<ParseInstallation, User>() {
                    @Override
                    public User call(ParseInstallation installation) {
                        return user;
                    }
                });
    }

    @Override
    public Single<Group> subscribeGroup(@NonNull final Group group) {
        return Single.create(new Single.OnSubscribe<Group>() {
            @Override
            public void call(final SingleSubscriber<? super Group> singleSubscriber) {
                ParsePush.subscribeInBackground(group.getObjectId(), new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (singleSubscriber.isUnsubscribed()) {
                            return;
                        }

                        if (e != null) {
                            singleSubscriber.onError(e);
                        } else {
                            singleSubscriber.onSuccess(group);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void unSubscribeGroup(@NonNull Group group) {
        ParsePush.unsubscribeInBackground(group.getObjectId());
    }

    @Override
    public Single<ParseInstallation> clearInstallation() {
        final ParseInstallation installation = ParseInstallationUtils.getResetInstallation();
        return save(installation);
    }

    @Override
    public Single<String> calcUserBalances() {
        return callFunctionInBackground(CALCULATE_BALANCES, Collections.<String, Object>emptyMap());
    }

    @Override
    public Single<Identity> addNewGroup(@NonNull final User user, @NonNull String groupName,
                                        @NonNull String groupCurrency) {
        return addGroup(groupName, groupCurrency)
                .flatMap(new Func1<String, Single<User>>() {
                    @Override
                    public Single<User> call(String result) {
                        return updateUser(user);
                    }
                })
                .flatMap(new Func1<User, Single<Identity>>() {
                    @Override
                    public Single<Identity> call(User user) {
                        final Identity newIdentity = user.getCurrentIdentity();
                        return fetchIdentityData(newIdentity);
                    }
                })
                .flatMap(new Func1<Identity, Single<Identity>>() {
                    @Override
                    public Single<Identity> call(Identity identity) {
                        return saveIdentityLocal(identity);
                    }
                })
                .flatMap(new Func1<Identity, Single<Group>>() {
                    @Override
                    public Single<Group> call(Identity identity) {
                        return subscribeGroup(identity.getGroup());
                    }
                })
                .map(new Func1<Group, Identity>() {
                    @Override
                    public Identity call(Group group) {
                        return user.getCurrentIdentity();
                    }
                });
    }

    private Single<String> addGroup(@NonNull String groupName,
                                    @NonNull String groupCurrency) {
        final Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP_NAME, groupName);
        params.put(PushBroadcastReceiver.PUSH_PARAM_CURRENCY_CODE, groupCurrency);
        return callFunctionInBackground(ADD_NEW_GROUP, params);
    }

    @Override
    public Single<Identity> addIdentity(@NonNull final Context context, @NonNull String nickname,
                                        @NonNull String groupId, @NonNull final String groupName,
                                        @NonNull final String inviterNickname) {
        final Group group = (Group) ParseObject.createWithoutData(Group.CLASS, groupId);
        final Identity identity = new Identity(group, nickname, true);
        return save(identity)
                .flatMap(new Func1<Identity, Single<? extends Identity>>() {
                    @Override
                    public Single<? extends Identity> call(Identity identity) {
                        return pin(identity, Identity.PIN_LABEL);
                    }
                })
                .flatMap(new Func1<Identity, Single<String>>() {
                    @Override
                    public Single<String> call(Identity identity) {
                        return getInvitationUrl(context, identity, groupName, inviterNickname);
                    }
                })
                .flatMap(new Func1<String, Single<? extends Identity>>() {
                    @Override
                    public Single<? extends Identity> call(String link) {
                        identity.setInvitationLink(link);
                        return save(identity);
                    }
                });
    }

    @Override
    public Single<Identity> saveIdentityLocal(@NonNull Identity identity) {
        return pin(identity, Identity.PIN_LABEL);
    }

    @NonNull
    @Override
    public Single<String> getInvitationUrl(@NonNull final Context context,
                                           @NonNull final Identity identity,
                                           @NonNull String groupName,
                                           @NonNull String inviterNickname) {
        final BranchUniversalObject universalObject = new BranchUniversalObject()
                .setCanonicalIdentifier("invitation")
                .setTitle(String.format("You are invited to join %s", groupName))
                .setContentDescription("Click on this link to open or install Qwittig and accept the invitation.")
                .addContentMetadata("identityId", identity.getObjectId())
                .addContentMetadata("groupName", groupName)
                .addContentMetadata("inviterNickname", inviterNickname);

        final LinkProperties properties = new LinkProperties()
                .setFeature("invitation")
                .setChannel("all");

        return Single
                .create(new Single.OnSubscribe<String>() {
                    @Override
                    public void call(final SingleSubscriber<? super String> singleSubscriber) {
                        universalObject.generateShortUrl(context, properties, new Branch.BranchLinkCreateListener() {
                            @Override
                            public void onLinkCreate(String url, BranchError error) {
                                if (singleSubscriber.isUnsubscribed()) {
                                    return;
                                }

                                if (error != null) {
                                    singleSubscriber.onError(new Throwable(error.getMessage()));
                                } else {
                                    singleSubscriber.onSuccess(url);
                                }
                            }
                        });
                    }
                });
    }

    @Override
    public Single<Identity> fetchIdentityData(@NonNull final Identity identity) {
        if (identity.isDataAvailable() && identity.getGroup().isDataAvailable()) {
            return Single.just(identity);
        }

        return fetchLocal(identity)
                .onErrorResumeNext(fetchIfNeeded(identity))
                .flatMap(new Func1<Identity, Single<? extends Group>>() {
                    @Override
                    public Single<? extends Group> call(Identity identity) {
                        final Group group = identity.getGroup();
                        return fetchLocal(group)
                                .onErrorResumeNext(fetchIfNeeded(group));
                    }
                })
                .map(new Func1<Group, Identity>() {
                    @Override
                    public Identity call(Group group) {
                        return identity;
                    }
                });
    }

    @Override
    public Observable<Identity> fetchIdentitiesData(@NonNull List<Identity> identities) {
        return Observable.from(identities)
                .flatMap(new Func1<Identity, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(Identity identity) {
                        return fetchIdentityData(identity).toObservable();
                    }
                });
    }

    @Override
    public Observable<Identity> getIdentities(@NonNull Group group, boolean includePending) {
        final ParseQuery<Identity> query = ParseQuery.getQuery(Identity.CLASS);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.whereEqualTo(Identity.GROUP, group);
        query.whereEqualTo(Identity.ACTIVE, true);
        if (!includePending) {
            query.whereEqualTo(Identity.PENDING, false);
        }
        return find(query)
                .concatMap(new Func1<List<Identity>, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(List<Identity> identities) {
                        return Observable.from(identities);
                    }
                });
    }

    @Override
    public boolean updateIdentities(@NonNull List<Identity> identities) {
        try {
            final List<Group> groups = new ArrayList<>();
            for (Identity identity : identities) {
                groups.add(identity.getGroup());
            }

            final ParseQuery<Identity> query = getIdentitiesOnlineQuery(groups);
            final List<Identity> onlineIdentities = query.find();

            ParseObject.unpinAll(Identity.PIN_LABEL);
            ParseObject.pinAll(Identity.PIN_LABEL, onlineIdentities);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    @NonNull
    private ParseQuery<Identity> getIdentitiesOnlineQuery(@NonNull List<Group> groups) {
        final ParseQuery<Identity> query = ParseQuery.getQuery(Identity.CLASS);
        query.whereContainedIn(Identity.GROUP, groups);
        query.whereEqualTo(Identity.ACTIVE, true);
        query.include(Identity.GROUP);
        return query;
    }

    @Override
    public Observable<Identity> saveCurrentUserIdentitiesWithAvatar(@Nullable final String newNickname,
                                                                    @NonNull final String localAvatarPath) {
        final User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Observable.error(new Exception("currentUser is null"));
        }

        final List<Identity> identities = currentUser.getIdentities();
        return Observable.from(identities)
                .flatMap(new Func1<Identity, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(Identity identity) {
                        if (!TextUtils.isEmpty(newNickname)) {
                            identity.setNickname(newNickname);
                        }
                        identity.setAvatarLocal(localAvatarPath);
                        return unpin(identity, Identity.PIN_LABEL)
                                .flatMap(new Func1<Identity, Single<Identity>>() {
                                    @Override
                                    public Single<Identity> call(Identity identity) {
                                        return pin(identity, Identity.PIN_LABEL_TEMP);
                                    }
                                })
                                .toObservable();
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        SaveIdentityTaskService.scheduleCurrentUser(mGcmNetworkManager);
                    }
                });
    }

    @Override
    public boolean uploadIdentities(@NonNull Context context, @NonNull List<Identity> identities) {
        final String localAvatarPath = identities.get(0).getAvatarLocal();
        final ParseFile avatar = new ParseFile(new File(localAvatarPath));
        try {
            avatar.save();
        } catch (ParseException e) {
            return false;
        }

        for (Identity identity : identities) {
            try {
                identity.setAvatar(avatar);
                identity.removeAvatarLocal();
                identity.save();
            } catch (ParseException e) {
                identity.setAvatarLocal(localAvatarPath);
                return false;
            }
        }

        deleteLocalAvatar(localAvatarPath);

        for (Identity identity : identities) {
            try {
                identity.unpin(Identity.PIN_LABEL_TEMP);
                identity.pin(Identity.PIN_LABEL);
            } catch (ParseException e) {
                return false;
            }
        }

        return true;
    }

    private void deleteLocalAvatar(@NonNull String localAvatarPath) {
        final File avatarFile = new File(localAvatarPath);
        final boolean deleteSuccessful = avatarFile.delete();
        if (!deleteSuccessful && BuildConfig.DEBUG) {
            Timber.e("Failed to delete local avatar file");
        }
    }

    @Override
    public Single<Identity> saveIdentityWithAvatar(@NonNull Identity identity,
                                                   @Nullable String newNickname,
                                                   @NonNull String localAvatarPath) {
        if (!TextUtils.isEmpty(newNickname)) {
            identity.setNickname(newNickname);
        }
        identity.setAvatarLocal(localAvatarPath);
        return pin(identity, Identity.PIN_LABEL_TEMP)
                .doOnSuccess(new Action1<Identity>() {
                    @Override
                    public void call(Identity identity) {
                        SaveIdentityTaskService.scheduleIdentity(mGcmNetworkManager, identity.getObjectId());
                    }
                });
    }

    @Override
    public boolean uploadIdentityId(@NonNull Context context, @NonNull String identityId) {
        final Identity identity = (Identity) ParseObject.createWithoutData(Identity.CLASS, identityId);
        try {
            identity.fetchFromLocalDatastore();
        } catch (ParseException e) {
            return false;
        }

        final String localAvatarPath = identity.getAvatarLocal();
        final ParseFile avatar = new ParseFile(new File(localAvatarPath));
        try {
            avatar.save();
        } catch (ParseException e) {
            return false;
        }

        try {
            identity.setAvatar(avatar);
            identity.removeAvatarLocal();
            identity.save();
        } catch (ParseException e) {
            identity.setAvatarLocal(localAvatarPath);
            return false;
        }

        deleteLocalAvatar(localAvatarPath);

        try {
            identity.unpin(Identity.PIN_LABEL_TEMP);
            identity.pin(Identity.PIN_LABEL);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    @Override
    public Single<Identity> removePendingIdentity(@NonNull Identity identity) {
        identity.setActive(false);
        return unpin(identity, Identity.PIN_LABEL)
                .doOnSuccess(new Action1<Identity>() {
                    @Override
                    public void call(Identity identity) {
                        identity.saveEventually();
                    }
                });
    }
}
