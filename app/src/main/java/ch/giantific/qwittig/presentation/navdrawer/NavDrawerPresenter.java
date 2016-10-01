package ch.giantific.qwittig.presentation.navdrawer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.listadapters.interactions.SpinnerInteraction;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import rx.Observable;

/**
 * Created by fabio on 29.09.16.
 */
public class NavDrawerPresenter extends BasePresenterImpl<NavDrawerContract.ViewListener>
        implements NavDrawerContract.Presenter {

    private final NavDrawerViewModel viewModel;
    private final List<Identity> identities;
    private SpinnerInteraction spinnerInteraction;
    private Identity currentIdentity;

    public NavDrawerPresenter(@Nullable Bundle savedState,
                              @NonNull Navigator navigator,
                              @NonNull UserRepository userRepo) {
        super(savedState, navigator, userRepo);

        identities = new ArrayList<>();
        viewModel = new NavDrawerViewModel();
    }

    @Override
    public NavDrawerViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public List<Identity> getIdentities() {
        return identities;
    }

    @Override
    public void setSpinnerInteraction(@NonNull SpinnerInteraction spinnerInteraction) {
        this.spinnerInteraction = spinnerInteraction;
    }

    @Override
    protected void onUserLoggedIn(@NonNull final FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        subscriptions.add(userRepo.observeCurrentIdentityId(currentUser.getUid())
                .flatMap(currentIdentityId -> userRepo.observeIdentity(currentIdentityId)
                        .doOnNext(identity -> {
                            currentIdentity = identity;
                            viewModel.setNickname(identity.getNickname());
                            viewModel.setAvatar(identity.getAvatar());
                        }))
                .flatMap(identity -> userRepo.getUser(identity.getUser()).toObservable())
                .flatMap(user -> Observable.from(user.getIdentitiesIds())
                        .flatMap(identityId -> userRepo.getIdentity(identityId).toObservable())
                        .toList())
                .subscribe(new IndefiniteSubscriber<List<Identity>>() {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        view.showMessage(R.string.toast_error_load_groups);
                    }

                    @Override
                    public void onNext(List<Identity> newIdentities) {
                        identities.clear();
                        identities.addAll(newIdentities);
                        spinnerInteraction.notifyDataSetChanged();

                        int selected = 0;
                        for (int i = 0, size = identities.size(); i < size; i++) {
                            final Identity identity = identities.get(i);
                            if (Objects.equals(currentIdentity.getId(), identity.getId())) {
                                selected = i;
                                break;
                            }
                        }

                        viewModel.setSelectedIdentity(selected);
                    }
                })
        );
    }

    @Override
    public boolean isUserLoggedIn() {
        if (userRepo.getCurrentUser() == null) {
            navigator.startLogin();
            return false;
        }

        return true;
    }

    @Override
    public void afterLogout() {
        navigator.startHome();
        navigator.finish();
    }

    @Override
    public void onIdentitySelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final Identity identity = (Identity) parent.getItemAtPosition(position);
        final String identityId = identity.getId();
        if (Objects.equals(currentIdentity.getId(), identityId)) {
            return;
        }

        userRepo.updateCurrentIdentity(currentIdentity.getUser(), identityId);
    }

    @Override
    public void onAvatarClick(View view) {
        navigator.startProfileSettings();
    }
}
