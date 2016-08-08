package ch.giantific.qwittig.presentation.common;

import android.support.v7.util.SortedList;

import java.util.Objects;

import ch.giantific.qwittig.presentation.common.itemmodels.ChildItemModel;

/**
 * Created by fabio on 22.07.16.
 */
public abstract class SortedListCallback<T extends ChildItemModel> extends SortedList.Callback<T> {

    private ListInteraction mListInteraction;

    public SortedListCallback() {
    }

    public void setListInteraction(ListInteraction listInteraction) {
        mListInteraction = listInteraction;
    }

    @Override
    public void onInserted(int position, int count) {
        if (mListInteraction != null) {
            mListInteraction.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void onRemoved(int position, int count) {
        if (mListInteraction != null) {
            mListInteraction.notifyItemRangeRemoved(position, count);
        }
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
        if (mListInteraction != null) {
            mListInteraction.notifyItemMoved(fromPosition, toPosition);
        }
    }

    @Override
    public void onChanged(int position, int count) {
        if (mListInteraction != null) {
            mListInteraction.notifyItemRangeChanged(position, count);
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
