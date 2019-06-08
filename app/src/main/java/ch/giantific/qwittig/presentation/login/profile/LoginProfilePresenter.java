package ch.giantific.qwittig.presentation.login.profile;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import rx.SingleSubscriber;
import timber.log.Timber;

/**
 * Created by fabio on 01.05.16.
 */
public class LoginProfilePresenter extends BasePresenterImpl<LoginProfileContract.ViewListener>
        implements LoginProfileContract.Presenter {

    private final LoginProfileViewModel viewModel;
    private boolean withInvitation;

    @Inject
    public LoginProfilePresenter(@NonNull Navigator navigator,
                                 LoginProfileViewModel viewModel,
                                 @NonNull UserRepository userRepo) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
    }

    @Override
    public void setWithInvitation(boolean withInvitation) {
        this.withInvitation = withInvitation;
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        subscriptions.add(userRepo.observeCurrentIdentityId(currentUser.getUid())
                .flatMap(currentIdentityId -> userRepo.getIdentity(currentIdentityId).toObservable())
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        if (TextUtils.isEmpty(viewModel.nickname.get())) {
                            viewModel.nickname.set(identity.getNickname());
                        }

                        if (TextUtils.isEmpty(viewModel.getAvatar())) {
                            viewModel.setAvatar(identity.getAvatar());
                        }
                    }
                })
        );
    }

    @Override
    public void onAvatarClick(View view) {
        navigator.startImagePicker();
    }

    @Override
    public void onNewAvatarTaken(@NonNull String avatarPath) {
        viewModel.setAvatar(avatarPath);
    }

    @Override
    public void onDoneClick(View v) {
        if (!viewModel.isInputValid()) {
            return;
        }

        subscriptions.add(userRepo.updateProfile(viewModel.nickname.get(), viewModel.getAvatar(), true)
                .subscribe(new SingleSubscriber<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (withInvitation) {
                            navigator.finish(Activity.RESULT_OK);
                        } else {
                            view.showFirstGroupAdjust();
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to save profile with error:");
                        view.showMessage(R.string.toast_error_profile);
                    }
                })
        );
    }
}
