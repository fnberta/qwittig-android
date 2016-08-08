/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.drafts.itemmodels;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.NumberFormat;
import java.util.Date;

import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.itemmodels.BaseChildItemModel;
import ch.giantific.qwittig.utils.DateUtils;

/**
 * Provides a view model for a purchase in the list of purchases screen.
 */
public class DraftsItemModel extends BaseChildItemModel
        implements Comparable<DraftsItemModel> {

    private final Date mDate;
    private final String mDateMonthDay;
    private final String mStore;
    private final String mTotal;
    private boolean mSelected;

    public DraftsItemModel(@EventType int eventType,
                           @NonNull Purchase draft,
                           boolean isSelected,
                           @NonNull NumberFormat numberFormat) {
        super(eventType, draft.getId());

        mDate = draft.getDateDate();
        mDateMonthDay = DateUtils.formatMonthDayLineSeparated(mDate);
        mStore = draft.getStore();
        mTotal = numberFormat.format(draft.getTotal());
        mSelected = isSelected;
    }

    public Date getDate() {
        return mDate;
    }

    @Bindable
    public String getDateMonthDay() {
        return mDateMonthDay;
    }

    @Bindable
    public String getStore() {
        return mStore;
    }

    @Bindable
    public String getTotal() {
        return mTotal;
    }

    @Bindable
    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }

    @Override
    public int compareTo(@NonNull DraftsItemModel itemModel) {
        return mDate.compareTo(itemModel.getDate());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DraftsItemModel that = (DraftsItemModel) o;

        if (mDate != null ? !mDate.equals(that.getDate()) : that.getDate() != null) return false;
        if (mStore != null ? !mStore.equals(that.getStore()) : that.getStore() != null)
            return false;
        return mTotal != null ? mTotal.equals(that.getTotal()) : that.getTotal() == null;

    }

    @Override
    public int hashCode() {
        int result = mDate != null ? mDate.hashCode() : 0;
        result = 31 * result + (mStore != null ? mStore.hashCode() : 0);
        result = 31 * result + (mTotal != null ? mTotal.hashCode() : 0);
        return result;
    }
}
