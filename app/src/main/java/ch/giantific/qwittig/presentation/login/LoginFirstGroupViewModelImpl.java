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
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.Currency;
import rx.Observable;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Created by fabio on 13.05.16.
 */
public class LoginFirstGroupViewModelImpl extends ViewModelBaseImpl<LoginFirstGroupViewModel.ViewListener>
        implements LoginFirstGroupViewModel {

    private static final String STATE_VALIDATE = "STATE_VALIDATE";
    private static final String STATE_GROUP_NAME = "STATE_GROUP_NAME";
    private static final String STATE_GROUP_CURRENCY = "STATE_GROUP_CURRENCY";
    private final RemoteConfigHelper mConfigHelper;
    private final GroupRepository mGroupRepo;
    private final List<Currency> mCurrencies;
    private Identity mIdentity;
    private boolean mValidate;
    private String mGroupName;
    private String mGroupCurrency;

    public LoginFirstGroupViewModelImpl(@Nullable Bundle savedState,
                                        @NonNull Navigator navigator,
                                        @NonNull RxBus<Object> eventBus,
                                        @NonNull RemoteConfigHelper configHelper,
                                        @NonNull UserRepository userRepository,
                                        @Nullable GroupRepository groupRepository) {
        super(savedState, navigator, eventBus, userRepository);

        mConfigHelper = configHelper;
        mGroupRepo = groupRepository;
        mCurrencies = mConfigHelper.getSupportedCurrencies();

        if (savedState != null) {
            mValidate = savedState.getBoolean(STATE_VALIDATE);
            mGroupName = savedState.getString(STATE_GROUP_NAME);
            mGroupCurrency = savedState.getString(STATE_GROUP_CURRENCY);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_VALIDATE, mValidate);
        outState.putString(STATE_GROUP_NAME, mGroupName);
    }

    @Override
    @Bindable
    public boolean isValidate() {
        return mValidate;
    }

    @Override
    public void setValidate(boolean validate) {
        mValidate = validate;
        notifyPropertyChanged(BR.validate);
    }

    @Override
    @Bindable
    public String getGroupName() {
        return mGroupName;
    }

    @Override
    public void setGroupName(@NonNull String groupName) {
        mGroupName = groupName;
        notifyPropertyChanged(BR.groupName);
    }

    @Override
    @Bindable
    public boolean isGroupNameComplete() {
        return !TextUtils.isEmpty(mGroupName);
    }

    @Override
    public void onGroupNameChanged(CharSequence s, int start, int before, int count) {
        mGroupName = s.toString();
        if (mValidate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Override
    @Bindable
    public int getSelectedGroupCurrency() {
        for (Currency currency : mCurrencies) {
            if (Objects.equals(currency.getCode(), mGroupCurrency)) {
                return mCurrencies.indexOf(currency);
            }
        }

        return 0;
    }

    @Override
    public void setGroupCurrency(@NonNull String groupCurrency) {
        mGroupCurrency = groupCurrency;
        notifyPropertyChanged(BR.selectedGroupCurrency);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(mUserRepo.observeUser(currentUser.getUid())
                .flatMap(new Func1<User, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(User user) {
                        return mUserRepo.getIdentity(user.getCurrentIdentity()).toObservable();
                    }
                })
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        mIdentity = identity;

                        if (TextUtils.isEmpty(mGroupName)) {
                            setGroupName(identity.getGroupName());
                        }

                        if (TextUtils.isEmpty(mGroupCurrency)) {
                            setGroupCurrency(identity.getGroupCurrency());
                        }
                    }
                })
        );
    }

    @Override
    public List<Currency> getSupportedCurrencies() {
        return mCurrencies;
    }

    @Override
    public void onGroupCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final Currency currency = (Currency) parent.getItemAtPosition(position);
        mGroupCurrency = currency.getCode();
    }

    @Override
    public void onFabDoneClick(View view) {
        if (!Objects.equals(mGroupName, mIdentity.getGroupName()) ||
                !Objects.equals(mGroupCurrency, mIdentity.getGroupCurrency())) {
            getSubscriptions().add(mGroupRepo.updateGroupDetails(mIdentity.getGroup(), mGroupName, mGroupCurrency)
                    .subscribe(new SingleSubscriber<Group>() {
                        @Override
                        public void onSuccess(Group value) {
                            mNavigator.finish(Activity.RESULT_OK);
                        }

                        @Override
                        public void onError(Throwable error) {
                            mView.showMessage(R.string.toast_error_profile);
                        }
                    })
            );
        } else {
            mNavigator.finish(Activity.RESULT_OK);
        }
    }
}
