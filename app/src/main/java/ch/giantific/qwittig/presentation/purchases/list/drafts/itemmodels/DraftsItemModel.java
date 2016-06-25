/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.drafts.itemmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import java.text.NumberFormat;

import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Provides a view model for a draft in the list of drafts screen.
 */
public class DraftsItemModel extends BaseObservable {

    private final NumberFormat mMoneyFormatter;
    private Purchase mDraft;
    private boolean mDraftSelected;

    public DraftsItemModel(@NonNull Purchase draft, boolean draftSelected,
                           @NonNull String groupCurrency) {
        mMoneyFormatter = MoneyUtils.getMoneyFormatter(groupCurrency, false, true);
        setDraftInfo(draft, draftSelected);
    }

    public void updateDraftInfo(@NonNull Purchase draft, boolean draftSelected) {
        setDraftInfo(draft, draftSelected);
        notifyChange();
    }

    private void setDraftInfo(@NonNull Purchase draft, boolean draftSelected) {
        mDraft = draft;
        mDraftSelected = draftSelected;
    }

    @Bindable
    public Purchase getDraft() {
        return mDraft;
    }

    @Bindable
    public String getDraftDate() {
        return DateUtils.formatMonthDayLineSeparated(mDraft.getDate());
    }

    @Bindable
    public String getDraftStore() {
        return mDraft.getStore();
    }

    @Bindable
    public String getDraftTotalPrice() {
        return mMoneyFormatter.format(mDraft.getTotalPrice());
    }

    @Bindable
    public boolean isDraftSelected() {
        return mDraftSelected;
    }
}
