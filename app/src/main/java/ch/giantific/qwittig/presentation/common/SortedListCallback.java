package ch.giantific.qwittig.presentation.common;

import android.support.v7.util.SortedList;

import java.util.Objects;

import ch.giantific.qwittig.presentation.common.itemmodels.ChildItemModel;

/**
 * Created by fabio on 22.07.16.
 */
public abstract class SortedListCallback<T extends ChildItemModel> extends SortedList.Callback<T> {

    private ListInteraction listInteraction;

    public SortedListCallback() {
    }

    public void setListInteraction(ListInteraction listInteraction) {
        this.listInteraction = listInteraction;
    }

    @Override
    public void onInserted(int position, int count) {
        if (listInteraction != null) {
            listInteraction.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void onRemoved(int position, int count) {
        if (listInteraction != null) {
            listInteraction.notifyItemRangeRemoved(position, count);
        }
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
        if (listInteraction != null) {
            listInteraction.notifyItemMoved(fromPosition, toPosition);
        }
    }

    @Override
    public void onChanged(int position, int count) {
        if (listInteraction != null) {
            listInteraction.notifyItemRangeChanged(position, count);
        }
    }

    @Override
    public boolean areContentsTheSame(T oldItem, T newItem) {
        return Objects.equals(oldItem, newItem);
    }

    @Override
    public boolean areItemsTheSame(T item1, T item2) {
        return Objects.equals(item1.getId(), item2.getId());
    }
}
