/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

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
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseSession;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.ApiRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.utils.AvatarUtils;
import ch.giantific.qwittig.utils.parse.ParseInstallationUtils;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an implementation of {@link UserRepository} that uses the Parse.com framework as
 * the local and online data store.
 */
public class ParseUserRepository extends ParseBaseRepository<ParseUser> implements UserRepository {

    private ApiRepository mApiRepo;

    public ParseUserRepository(@NonNull ApiRepository apiRepo) {
        super();

        mApiRepo = apiRepo;
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
    public Single<User> udpateCurrentUser() {
        return ;
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
                        addUserToInstallation(user);
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
                        return addFirstIdentity(user);
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
                                                    .map(new Func1<byte[], User>() {
                                                        @Override
                                                        public User call(byte[] bytes) {
//                                                            user.setAvatar(bytes);
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
                        addUserToInstallation(user);
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

    private Single<byte[]> getFacebookUserProfileImage(@NonNull final Fragment fragment,
                                                       @NonNull String facebookId) {
        final String pictureUrl = "http://graph.facebook.com/" + facebookId + "/picture?type=large";
        return Single.create(new Single.OnSubscribe<byte[]>() {
            @Override
            public void call(final SingleSubscriber<? super byte[]> singleSubscriber) {
                Glide.with(fragment)
                        .load(pictureUrl)
                        .asBitmap()
                        .toBytes(Bitmap.CompressFormat.JPEG, AvatarUtils.JPEG_COMPRESSION_RATE)
                        .centerCrop()
                        .into(new SimpleTarget<byte[]>(AvatarUtils.WIDTH, AvatarUtils.HEIGHT) {
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
        });
    }

    @Override
    public Single<User> loginGoogle(@NonNull final Fragment fragment,
                                    @NonNull String idToken,
                                    @NonNull final String displayName,
                                    @NonNull final Uri photoUrl) {
        return mApiRepo.loginWithGoogle(idToken)
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
                                                .map(new Func1<byte[], User>() {
                                                    @Override
                                                    public User call(byte[] bytes) {
//                                                    user.setAvatar(bytes);
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
                        addUserToInstallation(user);
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

    private Single<byte[]> getGoogleProfileImage(@NonNull final Fragment fragment,
                                                 @NonNull final Uri photoUrl) {
        return Single.create(new Single.OnSubscribe<byte[]>() {
            @Override
            public void call(final SingleSubscriber<? super byte[]> singleSubscriber) {
                Glide.with(fragment)
                        .load(photoUrl)
                        .asBitmap()
                        .toBytes(Bitmap.CompressFormat.JPEG, AvatarUtils.JPEG_COMPRESSION_RATE)
                        .centerCrop()
                        .into(new SimpleTarget<byte[]>(AvatarUtils.WIDTH, AvatarUtils.HEIGHT) {
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
        });
    }

    private Single<User> addFirstIdentity(@NonNull User user) {
        final Group defaultGroup = new Group("Qwittig rocks", "CHF");
        final Identity defaultIdentity = new Identity(defaultGroup);
        user.addIdentity(defaultIdentity);
        user.setCurrentIdentity(defaultIdentity);

        return save(user).toObservable().cast(User.class).toSingle();
    }

    private void addUserToInstallation(@NonNull User user) {
        final List<ParseObject> identities = user.getIdentities();
        final List<String> channels = new ArrayList<>();
        for (ParseObject parseObject : identities) {
            final Identity identity = (Identity) parseObject;
            channels.add(identity.getGroup().getObjectId());
        }

        final ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.addAllUnique(ParseInstallationUtils.CHANNELS, channels);
        installation.put(ParseInstallationUtils.USER, user);
        installation.saveEventually();
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
    public Observable<User> addNewIdentity(@NonNull String groupName,
                                           @NonNull String groupCurrency) {
        final User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Observable.empty();
        }
        final Identity currentIdentity = currentUser.getCurrentIdentity();

        final Group group = new Group(groupName, groupCurrency);
        return Single
                .create(new Single.OnSubscribe<Group>() {
                    @Override
                    public void call(final SingleSubscriber<? super Group> singleSubscriber) {
                        group.saveInBackground(new SaveCallback() {
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
                })
                .flatMapObservable(new Func1<Group, Observable<? super ParseUser>>() {
                    @Override
                    public Observable<? super ParseUser> call(Group group) {
                        final Identity newIdentity = new Identity(group, currentIdentity.getNickname());
                        final byte[] avatar = currentIdentity.getAvatar();
                        if (avatar != null) {
                            newIdentity.setAvatar(avatar);
                        }
                        currentUser.addIdentity(newIdentity);
                        currentUser.setCurrentIdentity(newIdentity);
                        return save(currentUser)
                                .toObservable()
                                .doOnError(new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        currentUser.removeIdentity(newIdentity);
                                        currentUser.setCurrentIdentity(currentIdentity);
                                    }
                                });
                    }
                })
                .cast(User.class);
    }
}
