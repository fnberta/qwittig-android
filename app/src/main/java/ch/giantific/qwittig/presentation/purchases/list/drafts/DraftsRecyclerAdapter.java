/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.drafts;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowDraftsBinding;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.purchases.list.drafts.itemmodels.DraftItemModel;

/**
 * Handles the display of the user's drafts.
 * <p>
 * Subclass of {@link android.support.v7.widget.RecyclerView.Adapter}.
 */
public class DraftsRecyclerAdapter extends BaseRecyclerAdapter<BindingRow<RowDraftsBinding>> {

    private final DraftsViewModel viewModel;

    /**
     * Constructs a new {@link DraftsRecyclerAdapter}.
     *
     * @param viewModel the view's model
     */
    public DraftsRecyclerAdapter(@NonNull DraftsViewModel viewModel) {
        super();

        this.viewModel = viewModel;
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
        final DraftItemModel itemModel = viewModel.getItemAtPosition(position);

        binding.setItemModel(itemModel);
        binding.setViewModel(viewModel);
        binding.executePendingBindings();
    }

    @Override
    public void scrollToPosition(final int position) {
        // override to let RecyclerView layout its items first
        recyclerView.post(() -> recyclerView.scrollToPosition(position));
    }

    @Override
    public int getItemCount() {
        return viewModel.getItemCount();
    }
}
