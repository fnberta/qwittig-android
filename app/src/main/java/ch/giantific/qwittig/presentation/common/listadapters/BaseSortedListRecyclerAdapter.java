/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.listadapters;

import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.viewmodels.items.ChildItemViewModel;
import ch.giantific.qwittig.presentation.common.views.SortedListView;


/**
 * Subclass of {@link RecyclerView.Adapter}.
 */
public abstract class BaseSortedListRecyclerAdapter<T extends ChildItemViewModel,
        S extends BasePresenter,
        U extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<U>
        implements SortedListView<T> {

    protected final S presenter;
    private final SortedList<T> items;

    public BaseSortedListRecyclerAdapter(@NonNull S presenter) {
        super();

        this.presenter = presenter;
        items = createList();
    }

    protected abstract SortedList<T> createList();

    @Override
    public U onCreateViewHolder(ViewGroup parent, int viewType) {
        throw new RuntimeException("There is no type that matches the type " + viewType +
                ", make sure your using types correctly!");
    }

    @Override
    public int getItemViewType(int position) {
        return getItemAtPosition(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public boolean isItemsEmpty() {
        return getItemCount() == 0;
    }

    @Override
    public void addItem(@NonNull T item) {
        items.add(item);
    }

    public void addItems(@NonNull List<T> items) {
        this.items.addAll(items);
    }

    @Override
    public void updateItemAt(int pos, @NonNull T item) {
        items.updateItemAt(pos, item);
    }

    @Override
    public void removeItem(@NonNull T item) {
        items.remove(item);
    }

    @Override
    public void removeItemAtPosition(int position) {
        items.removeItemAt(position);
    }

    public void clearItems() {
        items.clear();
    }

    @Override
    public T getItemAtPosition(int position) {
        return items.get(position);
    }

    @Override
    public int getItemPositionForId(@NonNull String id) {
        for (int i = 0, size = getItemCount(); i < size; i++) {
            final T item = getItemAtPosition(i);
            if (Objects.equals(item.getId(), id)) {
                return i;
            }
        }

        throw new IllegalArgumentException(String.format("not item found with id: %s", id));
    }

    @Override
    public int getItemPositionForItem(@NonNull T item) {
        return items.indexOf(item);
    }
}
