/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid;

import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowCompUnpaidCreditBinding;
import ch.giantific.qwittig.databinding.RowCompUnpaidDebtBinding;
import ch.giantific.qwittig.presentation.common.listadapters.BaseSortedListRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.listadapters.SortedListCallback;
import ch.giantific.qwittig.presentation.common.listadapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.finance.unpaid.viewmodels.items.CompUnpaidItemViewModel;
import ch.giantific.qwittig.presentation.finance.unpaid.viewmodels.items.CompUnpaidItemViewModel.ViewType;


/**
 * Handles the display of different unpaid compensations.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class CompsUnpaidRecyclerAdapter extends BaseSortedListRecyclerAdapter<CompUnpaidItemViewModel,
        CompsUnpaidContract.Presenter,
        RecyclerView.ViewHolder> {

    /**
     * Constructs a new {@link CompsUnpaidRecyclerAdapter}.
     *
     * @param presenter the view's presenter
     */
    public CompsUnpaidRecyclerAdapter(@NonNull CompsUnpaidContract.Presenter presenter) {
        super(presenter);
    }

    @Override
    protected SortedList<CompUnpaidItemViewModel> createList() {
        return new SortedList<>(CompUnpaidItemViewModel.class,
                new SortedListCallback<>(this, presenter));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ViewType.CREDIT: {
                final RowCompUnpaidCreditBinding binding =
                        RowCompUnpaidCreditBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case ViewType.DEBT: {
                final RowCompUnpaidDebtBinding binding =
                        RowCompUnpaidDebtBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            default:
                return super.onCreateViewHolder(parent, viewType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final CompUnpaidItemViewModel viewModel = getItemAtPosition(position);
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case ViewType.CREDIT: {
                final BindingRow<RowCompUnpaidCreditBinding> row = (BindingRow<RowCompUnpaidCreditBinding>) holder;
                final RowCompUnpaidCreditBinding binding = row.getBinding();

                binding.setViewModel(viewModel);
                binding.setPresenter(presenter);
                binding.executePendingBindings();
                break;
            }
            case ViewType.DEBT: {
                final BindingRow<RowCompUnpaidDebtBinding> row =
                        (BindingRow<RowCompUnpaidDebtBinding>) holder;
                final RowCompUnpaidDebtBinding binding = row.getBinding();

                binding.setViewModel(viewModel);
                binding.executePendingBindings();
                break;
            }
        }
    }
}
