/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.purchases;

import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowPurchasesBinding;
import ch.giantific.qwittig.presentation.common.listadapters.BaseSortedListRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.listadapters.SortedListCallback;
import ch.giantific.qwittig.presentation.common.listadapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.purchases.list.purchases.viewmodels.items.PurchaseItemViewModel;

/**
 * Handles the display of recent purchases.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchasesRecyclerAdapter extends BaseSortedListRecyclerAdapter<PurchaseItemViewModel,
        PurchasesContract.Presenter,
        BindingRow<RowPurchasesBinding>> {

    /**
     * Constructs a new {@link PurchasesRecyclerAdapter}.
     *
     * @param presenter the view's model
     */
    public PurchasesRecyclerAdapter(@NonNull PurchasesContract.Presenter presenter) {
        super(presenter);
    }

    @Override
    protected SortedList<PurchaseItemViewModel> createList() {
        return new SortedList<>(PurchaseItemViewModel.class,
                new SortedListCallback<>(this, presenter));
    }

    @NonNull
    @Override
    public BindingRow<RowPurchasesBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowPurchasesBinding binding = RowPurchasesBinding.inflate(inflater, parent, false);
        return new BindingRow<>(binding);
    }

    @Override
    public void onBindViewHolder(BindingRow<RowPurchasesBinding> holder, int position) {
        final RowPurchasesBinding binding = holder.getBinding();
        final PurchaseItemViewModel viewModel = getItemAtPosition(position);
        binding.setPresenter(presenter);
        binding.setViewModel(viewModel);
        binding.executePendingBindings();
    }
}
