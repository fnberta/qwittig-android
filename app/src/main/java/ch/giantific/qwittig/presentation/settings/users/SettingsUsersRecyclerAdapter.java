/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowSettingsUsersUserBinding;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.settings.users.models.SettingsUsersUserRowViewModel;

/**
 * Provides a {@link RecyclerView} adapter that manages the list of items on the manage users
 * settings screen.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class SettingsUsersRecyclerAdapter extends RecyclerView.Adapter<BindingRow<RowSettingsUsersUserBinding>> {

    private final SettingsUsersViewModel mViewModel;

    public SettingsUsersRecyclerAdapter(@NonNull SettingsUsersViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public BindingRow<RowSettingsUsersUserBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowSettingsUsersUserBinding binding =
                RowSettingsUsersUserBinding.inflate(inflater, parent, false);
        return new BindingRow<>(binding);
    }

    @Override
    public void onBindViewHolder(BindingRow<RowSettingsUsersUserBinding> holder, int position) {
        final RowSettingsUsersUserBinding binding = holder.getBinding();
        final SettingsUsersUserRowViewModel viewModel = mViewModel.getItemAtPosition(position);

        binding.setViewModel(viewModel);
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mViewModel.getItemCount();
    }
}
