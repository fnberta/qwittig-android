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
import ch.giantific.qwittig.Constants;
import ch.giantific.qwittig.data.queues.GroupJoinQueue;
import ch.giantific.qwittig.data.rest.dynamiclinks.AndroidInfo;
import ch.giantific.qwittig.data.rest.dynamiclinks.DynamicLink;
import ch.giantific.qwittig.data.rest.dynamiclinks.IosInfo;
import ch.giantific.qwittig.data.rest.dynamiclinks.LinkInfo;
import ch.giantific.qwittig.data.rest.dynamiclinks.LinkRequest;
import ch.giantific.qwittig.data.rest.dynamiclinks.LinkResult;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxFirebaseDatabase;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by fabio on 04.07.16.
 */
public class GroupRepository {

    public static final String INVITATION_IDENTITY = "iid";
    public static final String INVITATION_GROUP = "ig";
    public static final String INVITATION_INVITER = "iin";
    private static final String FIREBASE_APP_CODE = "u4fa2";
    private static final String QWITTIG_AUTHORITY = "qwittig.com";

    private final DatabaseReference databaseRef;
    private final FirebaseMessaging messaging;
    private final DynamicLink dynamicLink;

    @Inject
    public GroupRepository(@NonNull FirebaseDatabase firebaseDatabase,
                           @NonNull FirebaseMessaging messaging,
                           @NonNull DynamicLink dynamicLink) {
        databaseRef = firebaseDatabase.getReference();
        this.messaging = messaging;
        this.dynamicLink = dynamicLink;
    }

    public Observable<Group> observeGroup(@NonNull String groupId) {
        final Query query = databaseRef.child(Group.BASE_PATH).child(groupId);
        return RxFirebaseDatabase.observeValue(query, Group.class);
    }

    public Single<Group> getGroup(@NonNull String groupId) {
        final Query query = databaseRef.child(Group.BASE_PATH).child(groupId);
        return RxFirebaseDatabase.observeValueOnce(query, Group.class);
    }

    public Observable<RxChildEvent<Identity>> observeGroupIdentityChildren(@NonNull String groupId) {
        final Query query = databaseRef
                .child(Identity.BASE_PATH)
                .child(Identity.BASE_PATH_ACTIVE)
                .orderByChild(Identity.PATH_GROUP)
                .equalTo(groupId);
        return RxFirebaseDatabase.observeChildren(query, Identity.class);
    }

    public Observable<Identity> getGroupIdentities(@NonNull String groupId,
                                                   final boolean includePending) {
        final Query query = databaseRef
                .child(Identity.BASE_PATH)
                .child(Identity.BASE_PATH_ACTIVE)
                .orderByChild(Identity.PATH_GROUP)
                .equalTo(groupId);
        return RxFirebaseDatabase.observeValueListOnce(query, Identity.class)
                .filter(identity -> includePending || !identity.isPending());
    }

    public Single<Group> updateGroupDetails(@NonNull final String groupId,
                                            @NonNull final String name,
                                            @Nullable final String currency) {
        final Query query = databaseRef.child(Group.BASE_PATH).child(groupId);
        return RxFirebaseDatabase.observeValueOnce(query, Group.class)
                .doOnSuccess(group -> {
                    final Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put(String.format("%s/%s/%s", Group.BASE_PATH, groupId, Group.PATH_NAME), name);
                    if (!TextUtils.isEmpty(currency)) {
                        childUpdates.put(String.format("%s/%s/%s", Group.BASE_PATH, groupId, Group.PATH_CURRENCY), currency);
                    }

                    final Set<String> identitiesIds = group.getIdentitiesIds();
                    for (String identityId : identitiesIds) {
                        childUpdates.put(String.format("%s/%s/%s/%s", Identity.BASE_PATH, Identity.BASE_PATH_ACTIVE, identityId, Identity.PATH_GROUP_NAME), name);
                        if (!TextUtils.isEmpty(currency)) {
                            childUpdates.put(String.format("%s/%s/%s/%s", Identity.BASE_PATH, Identity.BASE_PATH_ACTIVE, identityId, Identity.PATH_GROUP_CURRENCY), currency);
                        }
                    }

                    databaseRef.updateChildren(childUpdates);
                });
    }

    public void createGroup(@NonNull final String userId,
                            @NonNull String name,
                            @NonNull String currency,
                            @NonNull String identityNickname,
                            @Nullable String identityAvatar) {
        final Map<String, Object> childUpdates = new HashMap<>();

        // get identity id
        final String identityId = databaseRef.child(Identity.BASE_PATH).child(Identity.BASE_PATH_ACTIVE).push().getKey();

        // create group
        final String groupId = databaseRef.child(Group.BASE_PATH).push().getKey();
        final Group group = new Group(name, currency, Collections.singletonList(identityId));
        childUpdates.put(Group.BASE_PATH + "/" + groupId, group.toMap());

        // create identity
        final Map<String, Long> balance = new HashMap<>(2);
        balance.put(Identity.NUMERATOR, 0L);
        balance.put(Identity.DENOMINATOR, 1L);
        final Identity identity = new Identity(true, groupId, name, currency, userId,
                identityNickname, identityAvatar, balance);
        childUpdates.put(String.format("%s/%s/%s", Identity.BASE_PATH, Identity.BASE_PATH_ACTIVE, identityId), identity.toMap());

        // addItemAtPosition to user
        childUpdates.put(String.format("%s/%s/%s/%s", User.BASE_PATH, userId, User.PATH_IDENTITIES, identityId), true);
        childUpdates.put(String.format("%s/%s/%s", User.BASE_PATH, userId, User.PATH_CURRENT_IDENTITY), identityId);

        databaseRef.updateChildren(childUpdates);
        messaging.subscribeToTopic(groupId);
    }

    public void joinGroup(@NonNull Identity joinIdentity,
                          @NonNull String userId,
                          @Nullable String currentNickname,
                          @Nullable String currentAvatar) {
        final String queueKey = databaseRef.child(Constants.PATH_PUSH_QUEUE).push().getKey();
        final String joinIdentityId = joinIdentity.getId();
        final String joinGroupId = joinIdentity.getGroup();
        final Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(String.format("%s/%s/%s/%s", User.BASE_PATH, userId, User.PATH_IDENTITIES, joinIdentityId), true);
        childUpdates.put(String.format("%s/%s/%s", User.BASE_PATH, userId, User.PATH_CURRENT_IDENTITY), joinIdentityId);
        childUpdates.put(String.format("%s/%s/%s/%s", Identity.BASE_PATH, Identity.BASE_PATH_ACTIVE, joinIdentityId, Identity.PATH_USER), userId);
        if (!TextUtils.isEmpty(currentNickname)) {
            childUpdates.put(String.format("%s/%s/%s/%s", Identity.BASE_PATH, Identity.BASE_PATH_ACTIVE, joinIdentityId, Identity.PATH_NICKNAME), currentNickname);
        }
        if (!TextUtils.isEmpty(currentAvatar)) {
            childUpdates.put(String.format("%s/%s/%s/%s", Identity.BASE_PATH, Identity.BASE_PATH_ACTIVE, joinIdentityId, Identity.PATH_AVATAR), currentAvatar);
        }
        childUpdates.put(String.format("%s/%s", Constants.PATH_PUSH_QUEUE, queueKey), new GroupJoinQueue(joinGroupId, joinIdentityId).toMap());

        databaseRef.updateChildren(childUpdates);
        messaging.subscribeToTopic(joinGroupId);
    }

    public Single<String> leaveGroup(@NonNull final Identity identity) {
        final String identityId = identity.getId();
        final String userId = identity.getUser();
        final String groupId = identity.getGroup();
        return RxFirebaseDatabase.observeValueOnce(databaseRef.child(User.BASE_PATH).child(userId), User.class)
                .flatMapObservable(user -> Observable.from(user.getIdentitiesIds()))
                .first(id -> !Objects.equals(identityId, id))
                .toSingle()
                .doOnSuccess(newCurrentIdentity -> {
                    final Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put(String.format("%s/%s/%s", Identity.BASE_PATH, Identity.BASE_PATH_ACTIVE, identityId), null);
                    final Map<String, Object> identityMap = identity.toMap();
                    identityMap.put(Identity.PATH_ACTIVE, false);
                    childUpdates.put(String.format("%s/%s/%s", Identity.BASE_PATH, Identity.BASE_PATH_INACTIVE, identityId), identityMap);
                    childUpdates.put(String.format("%s/%s/%s/%s", Group.BASE_PATH, groupId, Group.PATH_IDENTITIES, identityId), null);
                    childUpdates.put(String.format("%s/%s/%s/%s", User.BASE_PATH, userId, User.PATH_IDENTITIES, identityId), null);
                    childUpdates.put(String.format("%s/%s/%s/%s", User.BASE_PATH, userId, User.PATH_ARCHIVED_IDENTITIES, identityId), true);
                    childUpdates.put(String.format("%s/%s/%s", User.BASE_PATH, userId, User.PATH_CURRENT_IDENTITY), newCurrentIdentity);
                    databaseRef.updateChildren(childUpdates);
                    messaging.unsubscribeFromTopic(identity.getGroup());
                });
    }

    public void addPendingIdentity(@NonNull Identity currentIdentity,
                                   @NonNull String nickname) {
        final String identityId = databaseRef.child(Identity.BASE_PATH).child(Identity.BASE_PATH_ACTIVE).push().getKey();
        final String groupId = currentIdentity.getGroup();
        final String groupName = currentIdentity.getGroupName();
        final Map<String, Long> balance = new HashMap<>();
        balance.put(Identity.NUMERATOR, 0L);
        balance.put(Identity.DENOMINATOR, 1L);
        final Identity identity = new Identity(true, groupId, groupName,
                currentIdentity.getGroupCurrency(), null, nickname, null, balance);

        final Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(String.format("%s/%s/%s", Identity.BASE_PATH, Identity.BASE_PATH_ACTIVE, identityId), identity.toMap());
        childUpdates.put(String.format("%s/%s/%s/%s", Group.BASE_PATH, groupId, Group.PATH_IDENTITIES, identityId), true);
        databaseRef.updateChildren(childUpdates);
    }

    public Single<Identity> removePendingIdentity(@NonNull final String identityId,
                                                  @NonNull final String groupId) {
        final Query query = databaseRef.child(Identity.BASE_PATH).child(Identity.BASE_PATH_ACTIVE).child(identityId);
        return RxFirebaseDatabase.observeValueOnce(query, Identity.class)
                .doOnSuccess(identity -> {
                    final Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put(String.format("%s/%s/%s", Identity.BASE_PATH, Identity.BASE_PATH_ACTIVE, identityId), null);
                    final Map<String, Object> identityMap = identity.toMap();
                    identityMap.put(Identity.PATH_ACTIVE, false);
                    childUpdates.put(String.format("%s/%s/%s", Identity.BASE_PATH, Identity.BASE_PATH_INACTIVE, identityId), identityMap);
                    childUpdates.put(String.format("%s/%s/%s/%s", Group.BASE_PATH, groupId, Group.PATH_IDENTITIES, identityId), null);
                    databaseRef.updateChildren(childUpdates);
                });
    }

    public Single<String> getInvitationLink(@NonNull final String identityId,
                                            @NonNull String groupName,
                                            @NonNull String inviterNickname,
                                            @NonNull String apiKey) {
        final Uri link = new Uri.Builder()
                .scheme("https")
                .authority(QWITTIG_AUTHORITY)
                .path("invitation")
                .appendQueryParameter(INVITATION_IDENTITY, identityId)
                .appendQueryParameter(INVITATION_GROUP, groupName)
                .appendQueryParameter(INVITATION_INVITER, inviterNickname)
                .build();

        final LinkInfo info = new LinkInfo(FIREBASE_APP_CODE + ".app.goo.gl", link.toString(),
                new AndroidInfo(BuildConfig.APPLICATION_ID),
                new IosInfo(BuildConfig.APPLICATION_ID));

        return dynamicLink.getDynamicLink(apiKey, new LinkRequest(info))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(LinkResult::getShortLink);
    }
}
