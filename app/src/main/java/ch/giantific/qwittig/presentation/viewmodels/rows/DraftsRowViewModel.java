/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels.rows;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Created by fabio on 22.01.16.
 */
public class DraftsRowViewModel extends BaseObservable {

    private String mGroupCurrency;
    private String mDraftDate;
    private String mDraftStore;
    private String mDraftTotalPrice;
    private boolean mDraftSelected;

    public DraftsRowViewModel(@NonNull Purchase draft, boolean draftSelected,
                              @NonNull String groupCurrency) {
        mGroupCurrency = groupCurrency;
        setDraftInfo(draft, draftSelected);
    }

    public void updateDraftInfo(@NonNull Purchase draft, boolean draftSelected) {
        setDraftInfo(draft, draftSelected);
        notifyChange();
    }

    private void setDraftInfo(@NonNull Purchase draft, boolean draftSelected) {
        mDraftDate = DateUtils.formatMonthDayLineSeparated(draft.getDate());
        mDraftStore = draft.getStore();
        double totalPrice = draft.getTotalPrice();
        mDraftTotalPrice = MoneyUtils.formatMoneyNoSymbol(totalPrice, mGroupCurrency);
        mDraftSelected = draftSelected;
    }

    @Bindable
    public String getDraftDate() {
        return mDraftDate;
    }

    @Bindable
    public String getDraftStore() {
        return mDraftStore;
    }

    @Bindable
    public String getDraftTotalPrice() {
        return mDraftTotalPrice;
    }

    @Bindable
    public boolean isDraftSelected() {
        return mDraftSelected;
    }
}
