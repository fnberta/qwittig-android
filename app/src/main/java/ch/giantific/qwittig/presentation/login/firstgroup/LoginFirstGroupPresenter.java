package ch.giantific.qwittig.presentation.login.firstgroup;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.models.Currency;
import rx.SingleSubscriber;

/**
 * Created by fabio on 13.05.16.
 */
public class LoginFirstGroupPresenter extends BasePresenterImpl<LoginFirstGroupContract.ViewListener>
        implements LoginFirstGroupContract.Presenter {

    private final LoginFirstGroupViewModel viewModel;
    private final GroupRepository groupRepo;
    private final List<Currency> currencies;
    private Identity identity;

    @Inject
    public LoginFirstGroupPresenter(@NonNull Navigator navigator,
                                    @NonNull LoginFirstGroupViewModel viewModel,
                                    @NonNull UserRepository userRepo,
                                    @NonNull GroupRepository groupRepo,
                                    @NonNull RemoteConfigHelper configHelper) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
        this.groupRepo = groupRepo;
        currencies = configHelper.getSupportedCurrencies();
    }

    @Override
    public List<Currency> getSupportedCurrencies() {
        return currencies;
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        subscriptions.add(userRepo.observeCurrentIdentityId(currentUser.getUid())
                .flatMap(currentIdentityId -> userRepo.getIdentity(currentIdentityId).toObservable())
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        LoginFirstGroupPresenter.this.identity = identity;

                        if (TextUtils.isEmpty(viewModel.groupName.get())) {
                            viewModel.groupName.set(identity.getGroupName());
                        }

                        if (TextUtils.isEmpty(viewModel.getGroupCurrency())) {
                            int selected = 0;
                            for (Currency currency : currencies) {
                                if (Objects.equals(currency.getCode(), viewModel.getGroupCurrency())) {
                                    selected = currencies.indexOf(currency);
                                    break;
                                }
                            }
                            viewModel.setGroupCurrency(identity.getGroupCurrency());
                            viewModel.setSelectedGroupCurrency(selected);
                        }
                    }
                })
        );
    }

    @Override
    public void onGroupCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final Currency currency = (Currency) parent.getItemAtPosition(position);
        viewModel.setGroupCurrency(currency.getCode());
    }

    @Override
    public void onDoneClick(View v) {
        final String groupName = viewModel.groupName.get();
        final String groupCurrency = viewModel.getGroupCurrency();
        if (!Objects.equals(groupName, identity.getGroupName()) ||
                !Objects.equals(groupCurrency, identity.getGroupCurrency())) {
            subscriptions.add(groupRepo.updateGroupDetails(identity.getGroup(), groupName, groupCurrency)
                    .subscribe(new SingleSubscriber<Group>() {
                        @Override
                        public void onSuccess(Group value) {
                            navigator.finish(Activity.RESULT_OK);
                        }

                        @Override
                        public void onError(Throwable error) {
                            view.showMessage(R.string.toast_error_profile);
                        }
                    })
            );
        } else {
            navigator.finish(Activity.RESULT_OK);
        }
    }
}
