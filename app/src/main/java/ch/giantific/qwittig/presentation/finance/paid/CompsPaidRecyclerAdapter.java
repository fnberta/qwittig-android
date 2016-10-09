/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.paid;

import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowCompPaidBinding;
import ch.giantific.qwittig.presentation.common.listadapters.BaseSortedListRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.listadapters.SortedListCallback;
import ch.giantific.qwittig.presentation.common.listadapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.finance.paid.viewmodels.items.CompPaidItemViewModel;


/**
 * Handles the display of different paid compensations.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class CompsPaidRecyclerAdapter extends BaseSortedListRecyclerAdapter<CompPaidItemViewModel,
        CompsPaidContract.Presenter,
        BindingRow<RowCompPaidBinding>> {

    /**
     * Constructs a new {@link CompsPaidRecyclerAdapter}.
     *
     * @param presenter the view's model
     */
    public CompsPaidRecyclerAdapter(@NonNull CompsPaidContract.Presenter presenter) {
        super(presenter);
    }

    @Override
    protected SortedList<CompPaidItemViewModel> createList() {
        return new SortedList<>(CompPaidItemViewModel.class,
                new SortedListCallback<>(this, presenter));
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
        final CompPaidItemViewModel viewModel = getItemAtPosition(position);

        binding.setViewModel(viewModel);
        binding.executePendingBindings();
    }
}
