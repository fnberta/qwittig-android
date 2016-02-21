/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowCompUnpaidCreditBinding;
import ch.giantific.qwittig.databinding.RowCompUnpaidDebtBinding;
import ch.giantific.qwittig.databinding.RowGenericHeaderBinding;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.finance.items.HeaderItem;
import ch.giantific.qwittig.presentation.finance.items.UnpaidCreditItem;
import ch.giantific.qwittig.presentation.finance.items.UnpaidDebtItem;
import ch.giantific.qwittig.presentation.finance.items.UnpaidItem;
import ch.giantific.qwittig.presentation.finance.items.UnpaidItem.Type;


/**
 * Handles the display of different unpaid compensations.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class CompsUnpaidRecyclerAdapter extends RecyclerView.Adapter {

    private final CompsUnpaidViewModel mViewModel;

    /**
     * Constructs a new {@link CompsUnpaidRecyclerAdapter}.
     *
     * @param viewModel the view's model
     */
    public CompsUnpaidRecyclerAdapter(@NonNull CompsUnpaidViewModel viewModel) {
        super();

        mViewModel = viewModel;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case Type.HEADER: {
                final RowGenericHeaderBinding binding =
                        RowGenericHeaderBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.CREDIT: {
                final RowCompUnpaidCreditBinding binding =
                        RowCompUnpaidCreditBinding.inflate(inflater, parent, false);
                return new CompensationCreditRow(binding, mViewModel);
            }
            case Type.DEBT: {
                final RowCompUnpaidDebtBinding binding =
                        RowCompUnpaidDebtBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final UnpaidItem unpaidItem = mViewModel.getItemAtPosition(position);
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case Type.HEADER: {
                final BindingRow<RowGenericHeaderBinding> row = (BindingRow<RowGenericHeaderBinding>) holder;
                final RowGenericHeaderBinding binding = row.getBinding();

                binding.setViewModel((HeaderItem) unpaidItem);
                binding.executePendingBindings();
                break;
            }
            case Type.CREDIT: {
                final CompensationCreditRow row = (CompensationCreditRow) holder;
                final RowCompUnpaidCreditBinding binding = row.getBinding();

                binding.setItem((UnpaidCreditItem) unpaidItem);
                binding.executePendingBindings();
                break;
            }
            case Type.DEBT: {
                final BindingRow<RowCompUnpaidDebtBinding> row =
                        (BindingRow<RowCompUnpaidDebtBinding>) holder;
                final RowCompUnpaidDebtBinding binding = row.getBinding();

                binding.setItem((UnpaidDebtItem) unpaidItem);
                binding.executePendingBindings();
                break;
            }
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
     * Defines the actions to take when user clicks on the compensations.
     */
    public interface AdapterInteractionListener {

        /**
         * Handles the click on the done button of a compensation.
         *
         * @param position the adapter position of the compensation
         */
        void onConfirmButtonClick(int position);

        /**
         * Handles the click on the remind to pay button of a compensation.
         *
         * @param position the adapter position of the compensation
         */
        void onRemindButtonClick(int position);
    }

    /**
     * Provides a {@link RecyclerView} row that displays unpaid compensations.
     * <p/>
     * Subclass of {@link BindingRow}.
     */
    private static class CompensationCreditRow extends BindingRow<RowCompUnpaidCreditBinding> {

        public CompensationCreditRow(@NonNull RowCompUnpaidCreditBinding binding,
                                     @NonNull final AdapterInteractionListener listener) {
            super(binding);

            binding.btCompUnpaidConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onConfirmButtonClick(getAdapterPosition());
                }
            });
            binding.btCompUnpaidRemind.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onRemindButtonClick(getAdapterPosition());
                }
            });
        }
    }
}
