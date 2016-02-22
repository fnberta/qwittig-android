/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowGenericHeaderBinding;
import ch.giantific.qwittig.databinding.RowSettingsUsersIntroBinding;
import ch.giantific.qwittig.databinding.RowSettingsUsersNicknameBinding;
import ch.giantific.qwittig.databinding.RowSettingsUsersUserBinding;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.settings.users.items.SettingsUsersHeaderItem;
import ch.giantific.qwittig.presentation.settings.users.items.SettingsUsersNicknameItem;
import ch.giantific.qwittig.presentation.settings.users.items.SettingsUsersBaseItem;
import ch.giantific.qwittig.presentation.settings.users.items.SettingsUsersBaseItem.Type;
import ch.giantific.qwittig.presentation.settings.users.items.SettingsUsersUserItem;

/**
 * Provides a {@link RecyclerView} adapter that manages the list of items on the manage users
 * settings screen.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class SettingsUsersRecyclerAdapter extends RecyclerView.Adapter {

    private final SettingsUsersViewModel mViewModel;

    public SettingsUsersRecyclerAdapter(@NonNull SettingsUsersViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, @Type int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case Type.HEADER: {
                final RowGenericHeaderBinding binding =
                        RowGenericHeaderBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.INTRO: {
                final RowSettingsUsersIntroBinding binding =
                        RowSettingsUsersIntroBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.NICKNAME: {
                final RowSettingsUsersNicknameBinding binding =
                        RowSettingsUsersNicknameBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.USER: {
                final RowSettingsUsersUserBinding binding =
                        RowSettingsUsersUserBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }

            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final SettingsUsersBaseItem item = mViewModel.getItemAtPosition(position);
        @Type final int type = getItemViewType(position);
        switch (type) {
            case Type.HEADER: {
                final BindingRow<RowGenericHeaderBinding> row =
                        (BindingRow<RowGenericHeaderBinding>) holder;
                final RowGenericHeaderBinding binding = row.getBinding();
                final SettingsUsersHeaderItem headerItem = (SettingsUsersHeaderItem) item;

                binding.setViewModel(headerItem);
                binding.executePendingBindings();
                break;
            }
            case Type.INTRO:
                // do nothing
                break;
            case Type.NICKNAME: {
                final BindingRow<RowSettingsUsersNicknameBinding> row =
                        (BindingRow<RowSettingsUsersNicknameBinding>) holder;
                final RowSettingsUsersNicknameBinding binding = row.getBinding();
                final SettingsUsersNicknameItem nicknameItem = (SettingsUsersNicknameItem) item;

                nicknameItem.setAddListener(mViewModel);
                binding.setNicknameItem(nicknameItem);
                binding.executePendingBindings();
                break;
            }
            case Type.USER: {
                final BindingRow<RowSettingsUsersUserBinding> row =
                        (BindingRow<RowSettingsUsersUserBinding>) holder;
                final RowSettingsUsersUserBinding binding = row.getBinding();
                final SettingsUsersUserItem userItem = (SettingsUsersUserItem) item;

                userItem.setShareListener(mViewModel);
                binding.setUserItem(userItem);
                binding.executePendingBindings();
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mViewModel.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        return mViewModel.getItemViewType(position);
    }
}
