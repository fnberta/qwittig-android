package ch.giantific.qwittig.presentation.settings.groupusers.addgroup;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

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

    private final SettingsAddGroupViewModel viewModel;
    private final RemoteConfigHelper configHelper;
    private final GroupRepository groupRepo;
    private final List<String> groupNames;
    private Identity currentIdentity;

    @Inject
    public SettingsAddGroupPresenter(@NonNull Navigator navigator,
                                     @NonNull SettingsAddGroupViewModel viewModel,
                                     @NonNull UserRepository userRepo,
                                     @NonNull GroupRepository groupRepo,
                                     @NonNull RemoteConfigHelper configHelper) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
        this.groupRepo = groupRepo;
        this.configHelper = configHelper;
        groupNames = new ArrayList<>();
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
    public void onCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final Currency currency = (Currency) parent.getItemAtPosition(position);
        viewModel.setCurrency(currency.getCode());
    }

    @Override
    public void onCreateClick(View v) {
        if (!viewModel.isInputValid()) {
            return;
        }

        final String name = viewModel.name.get();
        boolean newGroup = true;
        for (String groupName : groupNames) {
            if (name.equalsIgnoreCase(groupName)) {
                newGroup = false;
            }
        }
        if (newGroup) {
            groupRepo.createGroup(currentIdentity.getUser(), name, viewModel.getCurrency(),
                    currentIdentity.getNickname(), currentIdentity.getAvatar());
            view.setScreenResult(name);
            view.showAddUsers();
        } else {
            view.showMessage(R.string.toast_group_already_in_list);
        }
    }
}
