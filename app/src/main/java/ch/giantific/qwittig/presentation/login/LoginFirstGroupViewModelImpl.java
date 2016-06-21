package ch.giantific.qwittig.presentation.login;

import android.app.Activity;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.Currency;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Created by fabio on 13.05.16.
 */
public class LoginFirstGroupViewModelImpl extends ViewModelBaseImpl<LoginFirstGroupViewModel.ViewListener>
        implements LoginFirstGroupViewModel {

    private static final String STATE_VALIDATE = "STATE_VALIDATE";
    private static final String STATE_GROUP_NAME = "STATE_GROUP_NAME";
    private static final String STATE_GROUP_CURRENCY = "STATE_GROUP_CURRENCY";
    private final Navigator mNavigator;
    private boolean mValidate;
    private String mGroupName;
    private String mGroupCurrency;
    private List<Currency> mCurrencies;

    public LoginFirstGroupViewModelImpl(@Nullable Bundle savedState,
                                        @NonNull Navigator navigator,
                                        @NonNull RxBus<Object> eventBus,
                                        @NonNull UserRepository userRepository) {
        super(savedState, eventBus, userRepository);

        mNavigator = navigator;
        mCurrencies = MoneyUtils.getSupportedCurrencies();

        if (savedState != null) {
            mValidate = savedState.getBoolean(STATE_VALIDATE);
            mGroupName = savedState.getString(STATE_GROUP_NAME);
            mGroupCurrency = savedState.getString(STATE_GROUP_CURRENCY);
        } else {
            final Group group = mCurrentIdentity.getGroup();
            mGroupName = group.getName();
            mGroupCurrency = group.getCurrency();
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
    public void onGroupNameChanged(CharSequence s, int start, int before, int count) {
        mGroupName = s.toString();
        if (mValidate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Override
    @Bindable
    public boolean isGroupNameComplete() {
        return !TextUtils.isEmpty(mGroupName);
    }

    @Override
    public List<Currency> getSupportedCurrencies() {
        return mCurrencies;
    }

    @Override
    public int getSelectedGroupCurrency() {
        for (Currency currency : mCurrencies) {
            if (Objects.equals(currency.getCode(), mGroupCurrency)) {
                return mCurrencies.indexOf(currency);
            }
        }

        return 0;
    }

    @Override
    public void onGroupCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final Currency currency = (Currency) parent.getItemAtPosition(position);
        mGroupCurrency = currency.getCode();
    }

    @Override
    public void onFabDoneClick(View view) {
        final Group group = mCurrentIdentity.getGroup();
        boolean changesMade = false;
        if (!Objects.equals(mGroupName, group.getName())) {
            group.setName(mGroupName);
            changesMade = true;
        }
        if (!Objects.equals(mGroupCurrency, group.getCurrency())) {
            group.setCurrency(mGroupCurrency);
            changesMade = true;
        }

        if (changesMade) {
            group.saveEventually();
        }

        mNavigator.finish(Activity.RESULT_OK);
    }
}
