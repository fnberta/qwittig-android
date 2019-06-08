/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.drafts;

import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowDraftsBinding;
import ch.giantific.qwittig.presentation.common.listadapters.BaseSortedListRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.listadapters.SortedListCallback;
import ch.giantific.qwittig.presentation.common.listadapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.purchases.list.drafts.viewmodels.items.DraftItemViewModel;

/**
 * Handles the display of the user's drafts.
 * <p>
 * Subclass of {@link android.support.v7.widget.RecyclerView.Adapter}.
 */
public class DraftsRecyclerAdapter extends BaseSortedListRecyclerAdapter<DraftItemViewModel,
        DraftsContract.Presenter,
        BindingRow<RowDraftsBinding>> {

    /**
     * Constructs a new {@link DraftsRecyclerAdapter}.
     *
     * @param presenter the main view's presenter
     */
    public DraftsRecyclerAdapter(@NonNull DraftsContract.Presenter presenter) {
        super(presenter);
    }

    @Override
    protected SortedList<DraftItemViewModel> createList() {
        return new SortedList<>(DraftItemViewModel.class,
                new SortedListCallback<>(this, presenter));
    }

    @NonNull
    @Override
    public BindingRow<RowDraftsBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowDraftsBinding binding = RowDraftsBinding.inflate(inflater, parent, false);
        return new BindingRow<>(binding);
    }

    @Override
    public void onBindViewHolder(BindingRow<RowDraftsBinding> holder, int position) {
        final RowDraftsBinding binding = holder.getBinding();
        final DraftItemViewModel viewModel = getItemAtPosition(position);

        binding.setPresenter(presenter);
        binding.setViewModel(viewModel);
        binding.executePendingBindings();
    }
}
