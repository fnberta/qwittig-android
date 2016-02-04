/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowCompensationsUnpaidCreditBinding;
import ch.giantific.qwittig.databinding.RowCompensationsUnpaidDebtBinding;
import ch.giantific.qwittig.databinding.RowGenericHeaderBinding;
import ch.giantific.qwittig.domain.models.CompensationUnpaidItem;
import ch.giantific.qwittig.domain.models.CompensationUnpaidItem.Type;
import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.presentation.ui.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.viewmodels.FinanceCompsUnpaidViewModel;
import ch.giantific.qwittig.presentation.viewmodels.rows.CompUnpaidRowViewModel;


/**
 * Handles the display of different unpaid compensations.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class CompensationsUnpaidRecyclerAdapter extends RecyclerView.Adapter {

    private FinanceCompsUnpaidViewModel mViewModel;
    private String mGroupCurrency;

    /**
     * Constructs a new {@link CompensationsUnpaidRecyclerAdapter}.
     *
     * @param viewModel the view's model
     */
    public CompensationsUnpaidRecyclerAdapter(@NonNull FinanceCompsUnpaidViewModel viewModel) {
        super();

        mViewModel = viewModel;
        mGroupCurrency = mViewModel.getCurrentUser().getCurrentGroup().getCurrency();
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
                final RowCompensationsUnpaidCreditBinding binding =
                        RowCompensationsUnpaidCreditBinding.inflate(inflater, parent, false);
                return new CompensationCreditRow(binding, mViewModel);
            }
            case Type.DEBT: {
                final RowCompensationsUnpaidDebtBinding binding =
                        RowCompensationsUnpaidDebtBinding.inflate(inflater, parent, false);
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
        final CompensationUnpaidItem compItem = mViewModel.getItemAtPosition(position);
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case Type.HEADER: {
                final BindingRow<RowGenericHeaderBinding> row = (BindingRow<RowGenericHeaderBinding>) holder;
                final RowGenericHeaderBinding binding = row.getBinding();
                binding.setViewModel(compItem);

                binding.executePendingBindings();
                break;
            }
            case Type.CREDIT: {
                final CompensationCreditRow row = (CompensationCreditRow) holder;
                final RowCompensationsUnpaidCreditBinding binding = row.getBinding();
                final Compensation compensation = mViewModel.getItemAtPosition(position).getCompensation();

                CompUnpaidRowViewModel viewModel = binding.getViewModel();
                if (viewModel == null) {
                    viewModel = new CompUnpaidRowViewModel(compensation, mGroupCurrency, true);
                    binding.setViewModel(viewModel);
                } else {
                    viewModel.updateCompInfo(compensation, true);
                }

                binding.executePendingBindings();
                break;
            }
            case Type.DEBT: {
                final BindingRow<RowCompensationsUnpaidDebtBinding> row =
                        (BindingRow<RowCompensationsUnpaidDebtBinding>) holder;
                final RowCompensationsUnpaidDebtBinding binding = row.getBinding();
                final Compensation compensation = mViewModel.getItemAtPosition(position).getCompensation();

                CompUnpaidRowViewModel viewModel = binding.getViewModel();
                if (viewModel == null) {
                    viewModel = new CompUnpaidRowViewModel(compensation, mGroupCurrency, false);
                    binding.setViewModel(viewModel);
                } else {
                    viewModel.updateCompInfo(compensation, false);
                }

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
    private static class CompensationCreditRow extends BindingRow<RowCompensationsUnpaidCreditBinding> {

        public CompensationCreditRow(@NonNull RowCompensationsUnpaidCreditBinding binding,
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
