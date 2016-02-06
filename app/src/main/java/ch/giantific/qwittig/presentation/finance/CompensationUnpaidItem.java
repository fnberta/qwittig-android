/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.presentation.common.viewmodels.HeaderRowViewModel;

/**
 * Created by fabio on 30.01.16.
 */
public class CompensationUnpaidItem extends BaseObservable
        implements HeaderRowViewModel {

    @Type
    private int mType;
    @StringRes
    private int mHeader;
    private Compensation mCompensation;

    private CompensationUnpaidItem(@Type int type, @StringRes int header) {
        mType = type;
        mHeader = header;
    }

    private CompensationUnpaidItem(@Type int type, @NonNull Compensation compensation) {
        mType = type;
        mCompensation = compensation;
    }

    public static CompensationUnpaidItem createNewHeaderInstance(@StringRes int header) {
        return new CompensationUnpaidItem(Type.HEADER, header);
    }

    public static CompensationUnpaidItem createNewCreditInstance(@NonNull Compensation compensation) {
        return new CompensationUnpaidItem(Type.CREDIT, compensation);
    }

    public static CompensationUnpaidItem createNewDebtInstance(@NonNull Compensation compensation) {
        return new CompensationUnpaidItem(Type.DEBT, compensation);
    }

    public int getType() {
        return mType;
    }

    @Bindable
    public int getHeader() {
        return mHeader;
    }

    public Compensation getCompensation() {
        return mCompensation;
    }

    public void setCompensation(Compensation compensation) {
        mCompensation = compensation;
    }

    @IntDef({Type.HEADER, Type.CREDIT, Type.DEBT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        int HEADER = 0;
        int CREDIT = 1;
        int DEBT = 2;
    }
}
