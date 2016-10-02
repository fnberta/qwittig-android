/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.paid;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowCompPaidBinding;
import ch.giantific.qwittig.presentation.common.listadapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.listadapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.finance.paid.viewmodels.items.CompPaidItemViewViewModel;


/**
 * Handles the display of different paid compensations.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class CompsPaidRecyclerAdapter extends BaseRecyclerAdapter<BindingRow<RowCompPaidBinding>> {

    private final CompsPaidContract.Presenter presenter;

    /**
     * Constructs a new {@link CompsPaidRecyclerAdapter}.
     *
     * @param presenter the view's model
     */
    public CompsPaidRecyclerAdapter(@NonNull CompsPaidContract.Presenter presenter) {
        super();

        this.presenter = presenter;
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
        final CompPaidItemViewViewModel viewModel = presenter.getItemAtPosition(position);

        binding.setViewModel(viewModel);
        binding.executePendingBindings();
    }

    @Override
    public int getItemViewType(int position) {
        return presenter.getItemAtPosition(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return presenter.getItemCount();
    }
}
