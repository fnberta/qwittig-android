package ch.giantific.qwittig.presentation.login;

import android.app.Activity;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.Currency;
import rx.SingleSubscriber;

/**
 * Created by fabio on 13.05.16.
 */
public class LoginFirstGroupViewModelImpl extends ViewModelBaseImpl<LoginFirstGroupViewModel.ViewListener>
        implements LoginFirstGroupViewModel {

    private static final String STATE_VALIDATE = "STATE_VALIDATE";
    private static final String STATE_GROUP_NAME = "STATE_GROUP_NAME";
    private static final String STATE_GROUP_CURRENCY = "STATE_GROUP_CURRENCY";

    private final RemoteConfigHelper configHelper;
    private final GroupRepository groupRepo;
    private final List<Currency> currencies;
    private Identity identity;
    private boolean validate;
    private String groupName;
    private String groupCurrency;

    public LoginFirstGroupViewModelImpl(@Nullable Bundle savedState,
                                        @NonNull Navigator navigator,
                                        @NonNull RxBus<Object> eventBus,
                                        @NonNull RemoteConfigHelper configHelper,
                                        @NonNull UserRepository userRepo,
                                        @Nullable GroupRepository groupRepo) {
        super(savedState, navigator, eventBus, userRepo);

        this.configHelper = configHelper;
        this.groupRepo = groupRepo;
        currencies = configHelper.getSupportedCurrencies();

        if (savedState != null) {
            validate = savedState.getBoolean(STATE_VALIDATE);
            groupName = savedState.getString(STATE_GROUP_NAME);
            groupCurrency = savedState.getString(STATE_GROUP_CURRENCY);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_VALIDATE, validate);
        outState.putString(STATE_GROUP_NAME, groupName);
    }

    @Override
    @Bindable
    public boolean isValidate() {
        return validate;
    }

    @Override
    public void setValidate(boolean validate) {
        this.validate = validate;
        notifyPropertyChanged(BR.validate);
    }

    @Override
    @Bindable
    public String getGroupName() {
        return groupName;
    }

    @Override
    public void setGroupName(@NonNull String groupName) {
        this.groupName = groupName;
        notifyPropertyChanged(BR.groupName);
    }

    @Override
    @Bindable
    public boolean isGroupNameComplete() {
        return !TextUtils.isEmpty(groupName);
    }

    @Override
    public void onGroupNameChanged(CharSequence s, int start, int before, int count) {
        groupName = s.toString();
        if (validate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Override
    @Bindable
    public int getSelectedGroupCurrency() {
        for (Currency currency : currencies) {
            if (Objects.equals(currency.getCode(), groupCurrency)) {
                return currencies.indexOf(currency);
            }
        }

        return 0;
    }

    @Override
    public void setGroupCurrency(@NonNull String groupCurrency) {
        this.groupCurrency = groupCurrency;
        notifyPropertyChanged(BR.selectedGroupCurrency);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(userRepo.observeUser(currentUser.getUid())
                .flatMap(user -> userRepo.getIdentity(user.getCurrentIdentity()).toObservable())
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        LoginFirstGroupViewModelImpl.this.identity = identity;

                        if (TextUtils.isEmpty(groupName)) {
                            setGroupName(identity.getGroupName());
                        }

                        if (TextUtils.isEmpty(groupCurrency)) {
                            setGroupCurrency(identity.getGroupCurrency());
                        }
                    }
                })
        );
    }

    @Override
    public List<Currency> getSupportedCurrencies() {
        return currencies;
    }

    @Override
    public void onGroupCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final Currency currency = (Currency) parent.getItemAtPosition(position);
        groupCurrency = currency.getCode();
    }

    @Override
    public void onFabDoneClick(View view) {
        if (!Objects.equals(groupName, identity.getGroupName()) ||
                !Objects.equals(groupCurrency, identity.getGroupCurrency())) {
            getSubscriptions().add(groupRepo.updateGroupDetails(identity.getGroup(), groupName, groupCurrency)
                    .subscribe(new SingleSubscriber<Group>() {
                        @Override
                        public void onSuccess(Group value) {
                            navigator.finish(Activity.RESULT_OK);
                        }

                        @Override
                        public void onError(Throwable error) {
                            LoginFirstGroupViewModelImpl.this.view.showMessage(R.string.toast_error_profile);
                        }
                    })
            );
        } else {
            navigator.finish(Activity.RESULT_OK);
        }
    }
}
