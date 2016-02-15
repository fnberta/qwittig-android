/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowPurchasesBinding;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.common.adapters.rows.ProgressRow;

/**
 * Handles the display of recent purchases.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchasesRecyclerAdapter extends RecyclerView.Adapter {

    private PurchasesViewModel mViewModel;

    /**
     * Constructs a new {@link PurchasesRecyclerAdapter}.
     *
     * @param viewModel the view's model
     */
    public PurchasesRecyclerAdapter(@NonNull PurchasesViewModel viewModel) {
        super();

        mViewModel = viewModel;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case PurchasesViewModel.TYPE_ITEM: {
                final RowPurchasesBinding binding = RowPurchasesBinding.inflate(inflater, parent, false);
                return new PurchaseRow(binding, mViewModel);
            }
            case PurchasesViewModel.TYPE_PROGRESS: {
                View view = inflater
                        .inflate(ProgressRow.VIEW_RESOURCE, parent, false);
                return new ProgressRow(view);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case PurchasesViewModel.TYPE_ITEM: {
                final PurchaseRow purchaseRow = (PurchaseRow) viewHolder;
                final RowPurchasesBinding binding = purchaseRow.getBinding();
                final Purchase purchase = mViewModel.getItemAtPosition(position);

                PurchaseRowViewModel viewModel = binding.getViewModel();
                if (viewModel == null) {
                    viewModel = new PurchaseRowViewModel(purchase, mViewModel.getCurrentIdentity());
                    binding.setViewModel(viewModel);
                } else {
                    viewModel.updatePurchaseInfo(purchase);
                }

                binding.executePendingBindings();
                break;
            }
            case PurchasesViewModel.TYPE_PROGRESS:
                // do nothing
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mViewModel.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mViewModel.getItemCount();
    }

    /**
     * Defines the actions to take when a user clicks on a purchase.
     */
    public interface AdapterInteractionListener {
        /**
         * Handles the click on a purchase.
         *
         * @param position the adapter position of the purchase
         */
        void onPurchaseRowItemClick(int position);
    }

    /**
     * Provides a {@link RecyclerView} row that displays a purchase.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class PurchaseRow extends BindingRow<RowPurchasesBinding> {

        /**
         * Constructs a new {@link PurchaseRow} and sets the click listener.
         *
         * @param binding  the binding of the view
         * @param listener the callback for user clicks on the purchase
         */
        public PurchaseRow(@NonNull RowPurchasesBinding binding,
                           @NonNull final AdapterInteractionListener listener) {
            super(binding);

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onPurchaseRowItemClick(getAdapterPosition());
                }
            });
        }
    }
}
