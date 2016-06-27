/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.drafts;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowDraftsBinding;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.purchases.list.drafts.itemmodels.DraftsItemModel;

/**
 * Handles the display of the user's drafts.
 * <p/>
 * Subclass of {@link android.support.v7.widget.RecyclerView.Adapter}.
 */
public class DraftsRecyclerAdapter extends BaseRecyclerAdapter {

    private final DraftsViewModel mViewModel;

    /**
     * Constructs a new {@link DraftsRecyclerAdapter}.
     *
     * @param viewModel the view's model
     */
    public DraftsRecyclerAdapter(@NonNull DraftsViewModel viewModel) {
        super();

        mViewModel = viewModel;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowDraftsBinding binding = RowDraftsBinding.inflate(inflater, parent, false);
        return new BindingRow<>(binding);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final BindingRow<RowDraftsBinding> row = (BindingRow<RowDraftsBinding>) viewHolder;
        final RowDraftsBinding binding = row.getBinding();
        final Purchase draft = mViewModel.getItemAtPosition(position);

        DraftsItemModel itemModel = binding.getItemModel();
        final boolean selected = mViewModel.isSelected(draft);
        final String currency = mViewModel.getCurrentIdentity().getGroup().getCurrency();
        if (itemModel == null) {
            itemModel = new DraftsItemModel(draft, selected, currency);
            binding.setItemModel(itemModel);
            binding.setViewModel(mViewModel);
        } else {
            itemModel.updateDraftInfo(draft, selected, currency);
        }

        binding.executePendingBindings();
    }

    @Override
    public void scrollToPosition(final int position) {
        // override to let RecyclerView layout its items first
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.scrollToPosition(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mViewModel.getItemCount();
    }
}
