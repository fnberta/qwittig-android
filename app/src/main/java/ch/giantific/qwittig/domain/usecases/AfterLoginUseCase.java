package ch.giantific.qwittig.domain.usecases;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import rx.Single;

/**
 * Created by fabio on 11.09.16.
 */

public class AfterLoginUseCase {

    private final RemoteConfigHelper configHelper;
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private Single<FirebaseUser> loginResult;
    private String joinIdentityId;

    @Inject
    public AfterLoginUseCase(@NonNull RemoteConfigHelper configHelper,
                             @NonNull UserRepository userRepo,
                             @NonNull GroupRepository groupRepo) {
        this.configHelper = configHelper;
        this.userRepo = userRepo;
        this.groupRepo = groupRepo;
    }

    public void setLoginResult(@NonNull Single<FirebaseUser> loginResult) {
        this.loginResult = loginResult;
    }

    public void setJoinIdentityId(@Nullable String joinIdentityId) {
        this.joinIdentityId = joinIdentityId;
    }

    public Single<Boolean> execute() {
        if (loginResult == null) {
            return Single.error(new Throwable("login result is null, can't process."));
        }

        return loginResult.flatMap(firebaseUser -> userRepo.getUser(firebaseUser.getUid())
                .doOnSuccess(user -> {
                    final String currentIdentityId = user.getCurrentIdentity();
                    if (TextUtils.isEmpty(currentIdentityId) && TextUtils.isEmpty(joinIdentityId)) {
                        createInitialGroup(firebaseUser);
                    }
                })
                .flatMap(user -> {
                    final String currentIdentityId = user.getCurrentIdentity();
                    final boolean userIsNew = TextUtils.isEmpty(currentIdentityId);
                    if (TextUtils.isEmpty(joinIdentityId)) {
                        return Single.just(userIsNew);
                    }

                    final String userId = user.getId();
                    return userRepo.getIdentity(joinIdentityId)
                            .doOnSuccess(identity -> {
                                if (userIsNew) {
                                    groupRepo.joinGroup(identity, userId, null, null);
                                }
                            })
                            .flatMap(joinIdentity -> {
                                if (userIsNew) {
                                    return Single.just(true);
                                }

                                return userRepo.getIdentity(currentIdentityId)
                                        .doOnSuccess(identity -> groupRepo.joinGroup(
                                                joinIdentity,
                                                userId,
                                                identity.getNickname(),
                                                identity.getAvatar()))
                                        .map(identity -> false);
                            });
                })
        );
    }

    private void createInitialGroup(@NonNull FirebaseUser firebaseUser) {
        String nickname = "";
        String avatar = "";
        final List<? extends UserInfo> userInfos = firebaseUser.getProviderData();
        if (!userInfos.isEmpty()) {
            final UserInfo userInfo = userInfos.get(0);
            nickname = userInfo.getDisplayName();
            final Uri uri = userInfo.getPhotoUrl();
            if (uri != null) {
                avatar = uri.toString();
            }
        }

        if (TextUtils.isEmpty(nickname)) {
            nickname = userRepo.getNicknameFromEmail(firebaseUser.getEmail());
        }

        groupRepo.createGroup(firebaseUser.getUid(), configHelper.getDefaultGroupName(),
                configHelper.getDefaultGroupCurrency(), nickname, avatar);
    }
}
