package ch.giantific.qwittig.presentation.common.listadapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;

import java.util.Objects;

import ch.giantific.qwittig.presentation.common.viewmodels.items.ChildItemViewModel;
import ch.giantific.qwittig.presentation.common.presenters.SortedListPresenter;

/**
 * Created by fabio on 06.10.16.
 */

public class SortedListCallback<T extends ChildItemViewModel> extends SortedListAdapterCallback<T> {

    private final SortedListPresenter<T> presenter;

    public SortedListCallback(@NonNull RecyclerView.Adapter adapter,
                              @NonNull SortedListPresenter<T> presenter) {
        super(adapter);

        this.presenter = presenter;
    }

    @Override
    public int compare(T o1, T o2) {
        return presenter.compareItemViewModels(o1, o2);
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
