/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowUsersBinding;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.presentation.ui.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.viewmodels.FinanceUsersViewModel;
import ch.giantific.qwittig.presentation.viewmodels.rows.UserRowViewModel;


/**
 * Handles the display of users with their avatar images, nicknames and current balances.
 * <p/>
 * Subclass of {@link BaseRecyclerAdapter}.
 */
public class UsersRecyclerAdapter extends RecyclerView.Adapter<BindingRow> {

    private FinanceUsersViewModel mViewModel;

    /**
     * Constructs a new {@link UsersRecyclerAdapter}.
     *
     * @param viewModel the model of the view
     */
    public UsersRecyclerAdapter(@NonNull FinanceUsersViewModel viewModel) {
        super();

        mViewModel = viewModel;
    }

    @NonNull
    @Override
    public BindingRow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowUsersBinding binding = RowUsersBinding.inflate(inflater, parent, false);
        return new BindingRow<>(binding);
    }

    @Override
    public void onBindViewHolder(BindingRow holder, int position) {
        final User user = mViewModel.getItemAtPosition(position);
        final RowUsersBinding binding = (RowUsersBinding) holder.getBinding();
        UserRowViewModel viewModel = binding.getViewModel();

        if (viewModel == null) {
            viewModel = new UserRowViewModel(user, mViewModel.getCurrentUser().getCurrentGroup());
            binding.setViewModel(viewModel);
        } else {
            viewModel.setUser(user);
        }

        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mViewModel.getItemCount();
    }
}
