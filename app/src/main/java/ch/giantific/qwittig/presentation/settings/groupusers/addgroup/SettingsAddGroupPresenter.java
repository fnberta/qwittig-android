package ch.giantific.qwittig.presentation.settings.groupusers.addgroup;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.models.Currency;
import rx.Observable;

/**
 * Created by fabio on 30.09.16.
 */

public class SettingsAddGroupPresenter extends BasePresenterImpl<SettingsAddGroupContract.ViewListener>
        implements SettingsAddGroupContract.Presenter {

    private static final String STATE_VIEW_MODEL = SettingsAddGroupViewModel.class.getCanonicalName();
    private final SettingsAddGroupViewModel viewModel;
    private final RemoteConfigHelper configHelper;
    private final GroupRepository groupRepo;
    private final List<String> groupNames;
    private Identity currentIdentity;

    public SettingsAddGroupPresenter(@Nullable Bundle savedState,
                                     @NonNull Navigator navigator,
                                     @NonNull UserRepository userRepo,
                                     @NonNull GroupRepository groupRepo,
                                     @NonNull RemoteConfigHelper configHelper) {
        super(savedState, navigator, userRepo);

        this.groupRepo = groupRepo;
        this.configHelper = configHelper;
        groupNames = new ArrayList<>();

        if (savedState != null) {
            viewModel = savedState.getParcelable(STATE_VIEW_MODEL);
        } else {
            viewModel = new SettingsAddGroupViewModel();
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelable(STATE_VIEW_MODEL, viewModel);
    }

    @Override
    public SettingsAddGroupViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public List<Currency> getSupportedCurrencies() {
        return configHelper.getSupportedCurrencies();
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        subscriptions.add(userRepo.observeCurrentIdentityId(currentUser.getUid())
                .flatMap(currentIdentityId -> userRepo.getIdentity(currentIdentityId)
                        .doOnSuccess(identity -> currentIdentity = identity)
                        .flatMap(identity -> userRepo.getUser(identity.getUser()))
                        .flatMapObservable(user -> Observable.from(user.getIdentitiesIds())))
                .flatMap(userRepo::observeIdentity)
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        groupNames.add(identity.getGroupName());
                    }
                })
        );
    }

    @Override
    public void onNameChanged(CharSequence s, int start, int before, int count) {
        viewModel.setName(s.toString());
    }

    @Override
    public void onCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final Currency currency = (Currency) parent.getItemAtPosition(position);
        viewModel.setCurrency(currency.getCode());
    }

    @Override
    public void onCreateClick(View view) {
        if (!viewModel.isInputValid()) {
            return;
        }

        final String name = viewModel.getName();
        boolean newGroup = true;
        for (String groupName : groupNames) {
            if (name.equalsIgnoreCase(groupName)) {
                newGroup = false;
            }
        }
        if (newGroup) {
            groupRepo.createGroup(currentIdentity.getUser(), name, viewModel.getCurrency(),
                    currentIdentity.getNickname(), currentIdentity.getAvatar());
            this.view.setScreenResult(name);
            this.view.showAddUsers();
        } else {
            this.view.showMessage(R.string.toast_group_already_in_list);
        }
    }
}
