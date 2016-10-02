/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.drafts.viewmodels.items;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import java.text.NumberFormat;
import java.util.Date;

import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.viewmodels.items.BaseChildItemViewModel;
import ch.giantific.qwittig.utils.DateUtils;

/**
 * Provides a view model for a purchase in the list of purchases screen.
 */
public class DraftItemViewModel extends BaseChildItemViewModel
        implements Comparable<DraftItemViewModel> {

    private final Date date;
    private final String dateMonthDay;
    private final String store;
    private final String total;
    private final String buyer;
    private boolean selected;

    public DraftItemViewModel(@EventType int eventType,
                              @NonNull Purchase draft,
                              boolean selected,
                              @NonNull NumberFormat numberFormat) {
        super(eventType, draft.getId());

        date = draft.getDateDate();
        dateMonthDay = DateUtils.formatMonthDayLineSeparated(date);
        store = draft.getStore();
        total = numberFormat.format(draft.getTotal());
        buyer = draft.getBuyer();
        this.selected = selected;
    }

    public Date getDate() {
        return date;
    }

    @Bindable
    public String getDateMonthDay() {
        return dateMonthDay;
    }

    @Bindable
    public String getStore() {
        return store;
    }

    @Bindable
    public String getTotal() {
        return total;
    }

    public String getBuyer() {
        return buyer;
    }

    @Bindable
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int compareTo(@NonNull DraftItemViewModel itemViewModel) {
        return date.compareTo(itemViewModel.getDate());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DraftItemViewModel that = (DraftItemViewModel) o;

        if (date != null ? !date.equals(that.getDate()) : that.getDate() != null) return false;
        if (store != null ? !store.equals(that.getStore()) : that.getStore() != null)
            return false;
        return total != null ? total.equals(that.getTotal()) : that.getTotal() == null;

    }

    @Override
    public int hashCode() {
        int result = date != null ? date.hashCode() : 0;
        result = 31 * result + (store != null ? store.hashCode() : 0);
        result = 31 * result + (total != null ? total.hashCode() : 0);
        return result;
    }
}
