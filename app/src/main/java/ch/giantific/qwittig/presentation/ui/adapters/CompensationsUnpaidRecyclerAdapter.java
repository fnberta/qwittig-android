/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowCompensationsUnpaidPendingNegBinding;
import ch.giantific.qwittig.databinding.RowCompensationsUnpaidPendingPosBinding;
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
            case FinanceCompsUnpaidViewModel.TYPE_PENDING_POS: {
                final RowCompensationsUnpaidPendingPosBinding binding =
                        RowCompensationsUnpaidPendingPosBinding.inflate(inflater, parent, false);
                return new CompensationPendingPosRow(binding, mViewModel);
            }
            case FinanceCompsUnpaidViewModel.TYPE_PENDING_NEG: {
                final RowCompensationsUnpaidPendingNegBinding binding =
                        RowCompensationsUnpaidPendingNegBinding.inflate(inflater, parent, false);
                return new CompensationPendingNegRow(binding, mViewModel);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case FinanceCompsUnpaidViewModel.TYPE_PENDING_POS: {
                final CompensationPendingPosRow row = (CompensationPendingPosRow) viewHolder;
                final RowCompensationsUnpaidPendingPosBinding binding = row.getBinding();
                final Compensation compensation = mViewModel.getItemAtPosition(position);

                CompUnpaidRowViewModel viewModel = binding.getViewModel();
                if (viewModel == null) {
                    viewModel = new CompUnpaidRowViewModel(compensation, mGroupCurrency);
                    binding.setViewModel(viewModel);
                } else {
                    viewModel.updateCompInfo(compensation);
                }

                binding.executePendingBindings();
                break;
            }
            case FinanceCompsUnpaidViewModel.TYPE_PENDING_NEG: {
                final CompensationPendingNegRow row = (CompensationPendingNegRow) viewHolder;
                final RowCompensationsUnpaidPendingNegBinding binding = row.getBinding();
                final Compensation compensation = mViewModel.getItemAtPosition(position);

                CompUnpaidRowViewModel viewModel = binding.getViewModel();
                if (viewModel == null) {
                    viewModel = new CompUnpaidRowViewModel(compensation, mGroupCurrency);
                    binding.setViewModel(viewModel);
                } else {
                    viewModel.updateCompInfo(compensation);
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

//        /**
//         * Handles the click on the change amount button of a compensation.
//         *
//         * @param position the adapter position of the compensation
//         */
//        void onChangeAmountButtonClick(int position);
    }

    /**
     * Provides a {@link RecyclerView} row that displays unpaid compensations.
     * <p/>
     * Subclass of {@link BindingRow}.
     */
    private static class CompensationPendingPosRow extends BindingRow<RowCompensationsUnpaidPendingPosBinding> {

        public CompensationPendingPosRow(@NonNull RowCompensationsUnpaidPendingPosBinding binding,
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

    /**
     * Provides a {@link RecyclerView} row that displays unpaid compensations, where the current
     * user receives money.
     * <p/>
     * Subclass of {@link BindingRow}.
     */
    private static class CompensationPendingNegRow extends BindingRow<RowCompensationsUnpaidPendingNegBinding> {

        public CompensationPendingNegRow(@NonNull RowCompensationsUnpaidPendingNegBinding binding,
                                         @NonNull final AdapterInteractionListener listener) {
            super(binding);

//            binding.btCompUnpaidChangeAmount.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    listener.onChangeAmountButtonClick(getAdapterPosition());
//                }
//            });
        }
    }
}
