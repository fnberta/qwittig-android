/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.addgroup;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import ch.giantific.qwittig.BR;

/**
 * Provides an implementation of the {@link SettingsAddGroupContract}.
 */
public class SettingsAddGroupViewModel extends BaseObservable
        implements Parcelable {

    public static final Creator<SettingsAddGroupViewModel> CREATOR = new Creator<SettingsAddGroupViewModel>() {
        @Override
        public SettingsAddGroupViewModel createFromParcel(Parcel in) {
            return new SettingsAddGroupViewModel(in);
        }

        @Override
        public SettingsAddGroupViewModel[] newArray(int size) {
            return new SettingsAddGroupViewModel[size];
        }
    };
    public final ObservableField<String> name = new ObservableField<>();
    private boolean validate;
    private String currency;

    public SettingsAddGroupViewModel() {
        addChangedListeners();
    }

    private SettingsAddGroupViewModel(Parcel in) {
        validate = in.readByte() != 0;
        name.set(in.readString());
        currency = in.readString();

        addChangedListeners();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (validate ? 1 : 0));
        dest.writeString(name.get());
        dest.writeString(currency);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private void addChangedListeners() {
        name.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                if (validate) {
                    notifyPropertyChanged(BR.nameComplete);
                }
            }
        });
    }

    @Bindable
    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
        notifyPropertyChanged(BR.validate);
    }

    @Bindable
    public boolean isNameComplete() {
        return !TextUtils.isEmpty(name.get());
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isInputValid() {
        setValidate(true);
        return isNameComplete();
    }

}
