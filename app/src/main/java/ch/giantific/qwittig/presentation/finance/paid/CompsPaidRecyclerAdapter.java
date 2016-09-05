/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.paid;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowCompPaidBinding;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.finance.paid.itemmodels.CompPaidItemModel;


/**
 * Handles the display of different paid compensations.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class CompsPaidRecyclerAdapter extends BaseRecyclerAdapter<BindingRow<RowCompPaidBinding>> {

    private final CompsPaidViewModel viewModel;

    /**
     * Constructs a new {@link CompsPaidRecyclerAdapter}.
     *
     * @param viewModel the view's model
     */
    public CompsPaidRecyclerAdapter(@NonNull CompsPaidViewModel viewModel) {
        super();

        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public BindingRow<RowCompPaidBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowCompPaidBinding binding = RowCompPaidBinding.inflate(inflater, parent, false);
        return new BindingRow<>(binding);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(BindingRow<RowCompPaidBinding> holder, int position) {
        final RowCompPaidBinding binding = holder.getBinding();
        final CompPaidItemModel itemModel = viewModel.getItemAtPosition(position);

        binding.setItemModel(itemModel);
        binding.executePendingBindings();
    }

    @Override
    public int getItemViewType(int position) {
        return viewModel.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return viewModel.getItemCount();
    }
}
