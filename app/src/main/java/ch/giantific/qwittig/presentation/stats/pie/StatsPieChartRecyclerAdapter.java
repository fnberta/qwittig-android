/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.pie;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowStatsStoresUserBinding;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;

/**
 * Handles the display of separate stats pie charts for each user of a group.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class StatsPieChartRecyclerAdapter extends RecyclerView.Adapter<BindingRow<RowStatsStoresUserBinding>> {

    private final StatsPieViewModel mViewModel;

    /**
     * Constructs a new {@link StatsPieChartRecyclerAdapter}.
     *
     * @param viewModel the model for the view
     */
    public StatsPieChartRecyclerAdapter(@NonNull StatsPieViewModel viewModel) {
        super();

        mViewModel = viewModel;
    }

    @Override
    public BindingRow<RowStatsStoresUserBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowStatsStoresUserBinding binding =
                RowStatsStoresUserBinding.inflate(inflater, parent, false);
        return new BindingRow<>(binding);
    }

    @Override
    public void onBindViewHolder(BindingRow<RowStatsStoresUserBinding> holder, int position) {
        final RowStatsStoresUserBinding binding = holder.getBinding();
        binding.setViewModel(new PieChartRowViewModel(mViewModel.getDataAtPosition(position),
                mViewModel.isShowPercentage(), mViewModel.getNicknameAtPosition(position)));
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mViewModel.getItemCount();
    }
}
