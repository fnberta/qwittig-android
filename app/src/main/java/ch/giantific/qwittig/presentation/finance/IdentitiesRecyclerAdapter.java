/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowIdentitiesBinding;
import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;


/**
 * Handles the display of users with their avatar images, nicknames and current balances.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class IdentitiesRecyclerAdapter extends RecyclerView.Adapter<BindingRow> {

    private IdentitiesViewModel mViewModel;

    /**
     * Constructs a new {@link IdentitiesRecyclerAdapter}.
     *
     * @param viewModel the model of the view
     */
    public IdentitiesRecyclerAdapter(@NonNull IdentitiesViewModel viewModel) {
        super();

        mViewModel = viewModel;
    }

    @NonNull
    @Override
    public BindingRow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowIdentitiesBinding binding = RowIdentitiesBinding.inflate(inflater, parent, false);
        return new BindingRow<>(binding);
    }

    @Override
    public void onBindViewHolder(BindingRow holder, int position) {
        final Identity identity = mViewModel.getItemAtPosition(position);
        final RowIdentitiesBinding binding = (RowIdentitiesBinding) holder.getBinding();
        IdentityRowViewModel viewModel = binding.getViewModel();

        if (viewModel == null) {
            viewModel = new IdentityRowViewModel(identity);
            binding.setViewModel(viewModel);
        } else {
            viewModel.updateIdentity(identity);
        }

        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mViewModel.getItemCount();
    }
}
