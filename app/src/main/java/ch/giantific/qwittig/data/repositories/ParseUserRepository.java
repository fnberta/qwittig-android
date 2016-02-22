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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseSession;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.utils.googleapi.GoogleApiClientSignOut;
import ch.giantific.qwittig.utils.googleapi.GoogleApiClientUnlink;
import ch.giantific.qwittig.utils.parse.ParseInstallationUtils;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.exceptions.Exceptions;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an implementation of {@link UserRepository} that uses the Parse.com framework as
 * the local and online data store.
 */
public class ParseUserRepository extends ParseBaseRepository implements UserRepository {

    private static final String VERIFY_GOOGLE_LOGIN = "loginWithGoogle";
    private static final String HANDLE_INVITATION = "checkIdentity";
    private static final String PARAM_ID_TOKEN = "idToken";
    private static final String PARAM_IDENTITY_ID = "identityId";

    public ParseUserRepository() {
        super();
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
    public Single<User> loginEmail(@NonNull final String username, @NonNull final String password) {
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
                })
                .doOnSuccess(new Action1<User>() {
                    @Override
                    public void call(User user) {
//                        addUserToInstallation(user);
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
    public Single<User> signUpEmail(@NonNull final String username, @NonNull final String password) {
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
                    public Single<? extends User> call(User user) {
                        return loginEmail(username, password);
                    }
                });
    }

    @Override
    public Single<User> loginFacebook(@NonNull final Fragment fragment) {
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
                }).flatMap(new Func1<User, Single<? extends User>>() {
                    @Override
                    public Single<? extends User> call(final User user) {
                        if (!user.isNew()) {
                            return Single.just(user);
                        } else {
                            return getFacebookUserData()
                                    .flatMap(new Func1<JSONObject, Single<? extends User>>() {
                                        @Override
                                        public Single<? extends User> call(JSONObject facebookData) {
                                            final String name = facebookData.optString("first_name");
                                            if (!TextUtils.isEmpty(name)) {
//                                                user.setNickname(name);
                                            }
                                            final String email = facebookData.optString("email");
                                            if (!TextUtils.isEmpty(email)) {
                                                user.setUsername(email);
                                            }

                                            // TODO: prompt for email is not available (do we really need the email?)

                                            final String id = facebookData.optString("id");
                                            return getFacebookUserProfileImage(fragment, id)
                                                    .map(new Func1<ParseFile, User>() {
                                                        @Override
                                                        public User call(ParseFile avatar) {
//                                                            user.setAvatar(avatar);
                                                            return user;
                                                        }
                                                    });
                                        }
                                    });

                        }
                    }
                })
                .doOnSuccess(new Action1<User>() {
                    @Override
                    public void call(User user) {
//                        addUserToInstallation(user);
                    }
                });
    }

    private Single<JSONObject> getFacebookUserData() {
        return Single.create(new Single.OnSubscribe<JSONObject>() {
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
        });
    }

    private Single<ParseFile> getFacebookUserProfileImage(@NonNull final Fragment fragment,
                                                          @NonNull String facebookId) {
        final String pictureUrl = "http://graph.facebook.com/" + facebookId + "/picture?type=large";
        return Single
                .create(new Single.OnSubscribe<byte[]>() {
                    @Override
                    public void call(final SingleSubscriber<? super byte[]> singleSubscriber) {
                        Glide.with(fragment)
                                .load(pictureUrl)
                                .asBitmap()
                                .toBytes(Bitmap.CompressFormat.JPEG, IdentityRepository.JPEG_COMPRESSION_RATE)
                                .centerCrop()
                                .into(new SimpleTarget<byte[]>(IdentityRepository.WIDTH, IdentityRepository.HEIGHT) {
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
                        final ParseFile avatar = new ParseFile(IdentityRepository.FILE_NAME, bytes);
                        return saveFile(avatar);
                    }
                });
    }

    @Override
    public Single<User> loginGoogle(@NonNull final Fragment fragment,
                                    @NonNull String idToken,
                                    @NonNull final String displayName,
                                    @NonNull final Uri photoUrl) {
        return verifyGoogleLogin(idToken)
                .flatMap(new Func1<JSONObject, Single<? extends User>>() {
                    @Override
                    public Single<? extends User> call(JSONObject token) {
                        final String sessionToken = token.optString("sessionToken");
                        final boolean isNew = token.optBoolean("isNew");
                        if (!isNew) {
                            return becomeUser(sessionToken);
                        }

                        return becomeUser(sessionToken)
                                .flatMap(new Func1<User, Single<? extends User>>() {
                                    @Override
                                    public Single<? extends User> call(final User user) {
//                                        user.setNickname(displayName);
                                        return getGoogleProfileImage(fragment, photoUrl)
                                                .map(new Func1<ParseFile, User>() {
                                                    @Override
                                                    public User call(ParseFile avatar) {
//                                                    user.setAvatar(avatar);
                                                        return user;
                                                    }
                                                });
                                    }
                                });
                    }
                })
                .doOnSuccess(new Action1<User>() {
                    @Override
                    public void call(User user) {
//                        addUserToInstallation(user);
                    }
                });
    }

    @Override
    public Single<JSONObject> verifyGoogleLogin(@NonNull String idToken) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_ID_TOKEN, idToken);

        return this.<String>callFunctionInBackground(VERIFY_GOOGLE_LOGIN, params)
                .map(new Func1<String, JSONObject>() {
                    @Override
                    public JSONObject call(String s) {
                        try {
                            return new JSONObject(s);
                        } catch (JSONException e) {
                            throw Exceptions.propagate(e);
                        }
                    }
                });
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

    private Single<ParseFile> getGoogleProfileImage(@NonNull final Fragment fragment,
                                                    @NonNull final Uri photoUrl) {
        return Single
                .create(new Single.OnSubscribe<byte[]>() {
                    @Override
                    public void call(final SingleSubscriber<? super byte[]> singleSubscriber) {
                        Glide.with(fragment)
                                .load(photoUrl)
                                .asBitmap()
                                .toBytes(Bitmap.CompressFormat.JPEG, IdentityRepository.JPEG_COMPRESSION_RATE)
                                .centerCrop()
                                .into(new SimpleTarget<byte[]>(IdentityRepository.WIDTH, IdentityRepository.HEIGHT) {
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
                        final ParseFile avatar = new ParseFile(IdentityRepository.FILE_NAME, bytes);
                        return saveFile(avatar);
                    }
                });
    }

    @Override
    public Single<String> handleInvitation(@NonNull String identityId) {
        final Map<String, Object> params = new HashMap<>();
        params.put(PARAM_IDENTITY_ID, identityId);
        return callFunctionInBackground(HANDLE_INVITATION, params);
    }

    @Override
    public Single<User> logOut(@NonNull final User user) {
        return Single.create(new Single.OnSubscribe<User>() {
            @Override
            public void call(final SingleSubscriber<? super User> singleSubscriber) {
                ParseUser.logOutInBackground(new LogOutCallback() {
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
        });
    }

    @Override
    public Single<User> unlinkFacebook(@NonNull final User user) {
        return Single.create(new Single.OnSubscribe<User>() {
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
        });
    }

    @Override
    public Single<Void> signOutGoogle(@NonNull Context context) {
        return GoogleApiClientSignOut.create(context);
    }

    @Override
    public Single<User> unlinkGoogle(@NonNull Context context, @NonNull final User user) {
        return GoogleApiClientUnlink.create(context)
                .flatMap(new Func1<Void, Single<? extends User>>() {
                    @Override
                    public Single<? extends User> call(Void aVoid) {
                        user.removeGoogleId();
                        return save(user);
                    }
                });
    }

    @Override
    public Single<User> deleteUser(@NonNull User user) {
        return delete(user);
    }

    @Override
    public Observable<ParseInstallation> setupInstallation(@NonNull final User user) {
        return Observable.from(user.getIdentities())
                .map(new Func1<Identity, String>() {
                    @Override
                    public String call(Identity identity) {
                        return identity.getGroup().getObjectId();
                    }
                })
                .toList()
                .flatMap(new Func1<List<String>, Observable<ParseInstallation>>() {
                    @Override
                    public Observable<ParseInstallation> call(List<String> channels) {
                        final ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                        installation.addAllUnique(ParseInstallationUtils.CHANNELS, channels);
                        installation.put(ParseInstallationUtils.USER, user);
                        return save(installation).toObservable();
                    }
                });
    }

    @Override
    public Single<ParseInstallation> clearInstallation() {
        final ParseInstallation installation = ParseInstallationUtils.getResetInstallation();
        return save(installation);
    }
}
