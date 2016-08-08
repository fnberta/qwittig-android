package ch.giantific.qwittig.data.repositories;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import ch.giantific.qwittig.BuildConfig;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxFirebaseDatabase;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import rx.Observable;
import rx.Single;
import rx.functions.Action1;
import rx.functions.Func1;


/**
 * Created by fabio on 04.07.16.
 */
public class GroupRepository {

    public static final String INVITATION_IDENTITY = "iid";
    public static final String INVITATION_GROUP = "ig";
    public static final String INVITATION_INVITER = "iin";
    private static final String FIREBASE_APP_CODE = "u4fa2";
    private static final String QWITTIG_AUTHORITY = "qwittig.com";
    private final DatabaseReference mDatabaseRef;
    private final FirebaseMessaging mMessaging;

    @Inject
    public GroupRepository(@NonNull FirebaseDatabase firebaseDatabase,
                           @NonNull FirebaseMessaging messaging) {
        mDatabaseRef = firebaseDatabase.getReference();
        mMessaging = messaging;
    }

    public Observable<Group> observeGroup(@NonNull String groupId) {
        final Query query = mDatabaseRef.child(Group.PATH).child(groupId);
        return RxFirebaseDatabase.observeValue(query, Group.class);
    }

    public Single<Group> getGroup(@NonNull String groupId) {
        final Query query = mDatabaseRef.child(Group.PATH).child(groupId);
        return RxFirebaseDatabase.observeValueOnce(query, Group.class);
    }

    public Single<Group> updateGroupDetails(@NonNull final String groupId,
                                            @NonNull final String name,
                                            @Nullable final String currency) {
        final Query query = mDatabaseRef.child(Group.PATH).child(groupId);
        return RxFirebaseDatabase.observeValueOnce(query, Group.class)
                .doOnSuccess(new Action1<Group>() {
                    @Override
                    public void call(Group group) {
                        final Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put(Group.PATH + "/" + groupId + "/" + Group.PATH_NAME, name);
                        if (!TextUtils.isEmpty(currency)) {
                            childUpdates.put(Group.PATH + "/" + groupId + "/" + Group.PATH_CURRENCY, currency);
                        }

                        final Set<String> identitiesIds = group.getIdentitiesIds();
                        for (String identityId : identitiesIds) {
                            childUpdates.put(Identity.PATH + "/" + identityId + "/" + Identity.PATH_GROUP_NAME, name);
                            if (!TextUtils.isEmpty(currency)) {
                                childUpdates.put(Identity.PATH + "/" + identityId + "/" + Identity.PATH_GROUP_CURRENCY, currency);
                            }
                        }

                        mDatabaseRef.updateChildren(childUpdates);
                    }
                });
    }

    public void createGroup(@NonNull final String userId,
                            @NonNull String name,
                            @NonNull String currency,
                            @NonNull String identityNickname,
                            @Nullable String identityAvatar) {
        final Map<String, Object> childUpdates = new HashMap<>();

        // get identity id
        final String identityId = mDatabaseRef.child(Identity.PATH).push().getKey();

        // create group
        final String groupId = mDatabaseRef.child(Group.PATH).push().getKey();
        final Group group = new Group(name, currency, Collections.singletonList(identityId));
        childUpdates.put(Group.PATH + "/" + groupId, group.toMap());

        // create identity
        final Map<String, Long> balance = new HashMap<>(2);
        balance.put(Identity.NUMERATOR, 0L);
        balance.put(Identity.DENOMINATOR, 1L);
        final Identity identity = new Identity(true, groupId, name, currency, userId,
                identityNickname, identityAvatar, balance, null);
        childUpdates.put(Identity.PATH + "/" + identityId, identity.toMap());

        // add to user
        childUpdates.put(User.PATH + "/" + userId + "/" + User.PATH_IDENTITIES + "/" + identityId, true);
        childUpdates.put(User.PATH + "/" + userId + "/" + User.PATH_CURRENT_IDENTITY, identityId);

        mDatabaseRef.updateChildren(childUpdates);
        mMessaging.subscribeToTopic(groupId);
    }

    public void joinGroup(@NonNull final String userId,
                          @NonNull String identityId,
                          @NonNull String groupId) {
        final Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(User.PATH + "/" + userId + "/" + User.PATH_IDENTITIES + "/" + identityId, true);
        childUpdates.put(User.PATH + "/" + userId + "/" + User.PATH_CURRENT_IDENTITY, identityId);
        childUpdates.put(Identity.PATH + "/" + identityId + "/" + Identity.PATH_USER, userId);
        childUpdates.put(Identity.PATH + "/" + identityId + "/" + Identity.PATH_INVITATION_LINK, null);

        mDatabaseRef.updateChildren(childUpdates);
        mMessaging.subscribeToTopic(groupId);
    }

    public Single<String> leaveGroup(@NonNull final Identity identity) {
        final String identityId = identity.getId();
        final String userId = identity.getUser();
        final String groupId = identity.getGroup();
        return RxFirebaseDatabase.observeValueOnce(mDatabaseRef.child(User.PATH).child(userId), User.class)
                .flatMapObservable(new Func1<User, Observable<String>>() {
                    @Override
                    public Observable<String> call(User user) {
                        return Observable.from(user.getIdentitiesIds());
                    }
                })
                .first(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String id) {
                        return !Objects.equals(identityId, id);
                    }
                })
                .toSingle()
                .doOnSuccess(new Action1<String>() {
                    @Override
                    public void call(String newCurrentIdentity) {
                        final Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put(Identity.PATH + "/" + identityId + "/" + Identity.PATH_ACTIVE, false);
                        childUpdates.put(Group.PATH + "/" + groupId + "/" + Group.PATH_IDENTITIES + "/" + identityId, null);
                        childUpdates.put(User.PATH + "/" + userId + "/" + User.PATH_IDENTITIES + "/" + identityId, null);
                        childUpdates.put(User.PATH + "/" + userId + "/" + User.PATH_ARCHIVED_IDENTITIES + "/" + identityId, true);
                        childUpdates.put(User.PATH + "/" + userId + "/" + User.PATH_CURRENT_IDENTITY, newCurrentIdentity);
                        mDatabaseRef.updateChildren(childUpdates);
                        mMessaging.unsubscribeFromTopic(identity.getGroup());
                    }
                });
    }

    public void addIdentityToGroup(@NonNull Identity currentIdentity,
                                   @NonNull String nickname) {
        final String identityId = mDatabaseRef.child(Identity.PATH).push().getKey();
        final String groupId = currentIdentity.getGroup();
        final String groupName = currentIdentity.getGroupName();
        final String invitationLink = getInvitationLink(identityId, groupName, currentIdentity.getNickname());
        final Map<String, Long> balance = new HashMap<>();
        balance.put(Identity.NUMERATOR, 0L);
        balance.put(Identity.DENOMINATOR, 1L);
        final Identity identity = new Identity(true, groupId, groupName,
                currentIdentity.getGroupCurrency(), null, nickname, null, balance, invitationLink);

        final Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(Identity.PATH + "/" + identityId, identity.toMap());
        childUpdates.put(Group.PATH + "/" + groupId + "/" + Group.PATH_IDENTITIES + "/" + identityId, true);
        mDatabaseRef.updateChildren(childUpdates);
    }

    private String getInvitationLink(@NonNull final String identityId,
                                     @NonNull String groupName,
                                     @NonNull String inviterNickname) {
        final Uri link = new Uri.Builder()
                .scheme("https")
                .authority(QWITTIG_AUTHORITY)
                .path("invitation/")
                .appendQueryParameter(INVITATION_IDENTITY, identityId)
                .appendQueryParameter(INVITATION_GROUP, groupName)
                .appendQueryParameter(INVITATION_INVITER, inviterNickname)
                .build();
        final Uri uri = new Uri.Builder()
                .scheme("https")
                .authority(FIREBASE_APP_CODE + ".app.goo.gl")
                .path("/")
                .appendQueryParameter("link", link.toString())
                .appendQueryParameter("apn", BuildConfig.APPLICATION_ID)
                .appendQueryParameter("ibi", BuildConfig.APPLICATION_ID)
                .build();

        // TODO: shorten
        return uri.toString();
    }
}
