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
import ch.giantific.qwittig.presentation.purchases.list.drafts.itemmodels.DraftsItemModel;

/**
 * Handles the display of the user's drafts.
 * <p>
 * Subclass of {@link android.support.v7.widget.RecyclerView.Adapter}.
 */
public class DraftsRecyclerAdapter extends BaseRecyclerAdapter<BindingRow<RowDraftsBinding>> {

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
    public BindingRow<RowDraftsBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowDraftsBinding binding = RowDraftsBinding.inflate(inflater, parent, false);
        return new BindingRow<>(binding);
    }

    @Override
    public void onBindViewHolder(BindingRow<RowDraftsBinding> holder, int position) {
        final RowDraftsBinding binding = holder.getBinding();
        final DraftsItemModel itemModel = mViewModel.getItemAtPosition(position);

        binding.setItemModel(itemModel);
        binding.setViewModel(mViewModel);
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
