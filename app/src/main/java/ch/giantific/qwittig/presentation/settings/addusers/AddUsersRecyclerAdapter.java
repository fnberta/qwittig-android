/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addusers;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowGenericHeaderBinding;
import ch.giantific.qwittig.databinding.RowSettingsAddUsersIntroBinding;
import ch.giantific.qwittig.databinding.RowSettingsAddUsersNicknameBinding;
import ch.giantific.qwittig.databinding.RowSettingsAddUsersUserBinding;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.settings.addusers.items.AddUsersItem;
import ch.giantific.qwittig.presentation.settings.addusers.items.HeaderItem;
import ch.giantific.qwittig.presentation.settings.addusers.items.AddUsersItem.Type;
import ch.giantific.qwittig.presentation.settings.addusers.items.NicknameItem;
import ch.giantific.qwittig.presentation.settings.addusers.items.UserItem;

/**
 * Created by fabio on 08.02.16.
 */
public class AddUsersRecyclerAdapter extends RecyclerView.Adapter {

    private SettingsAddUsersViewModel mViewModel;

    public AddUsersRecyclerAdapter(@NonNull SettingsAddUsersViewModel viewModel) {
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
                final RowSettingsAddUsersIntroBinding binding =
                        RowSettingsAddUsersIntroBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.NICKNAME: {
                final RowSettingsAddUsersNicknameBinding binding =
                        RowSettingsAddUsersNicknameBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case Type.USER: {
                final RowSettingsAddUsersUserBinding binding =
                        RowSettingsAddUsersUserBinding.inflate(inflater, parent, false);
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
        final AddUsersItem item = mViewModel.getItemAtPosition(position);
        @Type final int type = getItemViewType(position);
        switch (type) {
            case Type.HEADER: {
                final BindingRow<RowGenericHeaderBinding> row =
                        (BindingRow<RowGenericHeaderBinding>) holder;
                final RowGenericHeaderBinding binding = row.getBinding();
                final HeaderItem headerItem = (HeaderItem) item;

                binding.setViewModel(headerItem);
                binding.executePendingBindings();
                break;
            }
            case Type.INTRO:
                // do nothing
                break;
            case Type.NICKNAME: {
                final BindingRow<RowSettingsAddUsersNicknameBinding> row =
                        (BindingRow<RowSettingsAddUsersNicknameBinding>) holder;
                final RowSettingsAddUsersNicknameBinding binding = row.getBinding();
                final NicknameItem nicknameItem = (NicknameItem) item;

                nicknameItem.setAddListener(mViewModel);
                binding.setNicknameItem(nicknameItem);
                binding.executePendingBindings();
                break;
            }
            case Type.USER: {
                final BindingRow<RowSettingsAddUsersUserBinding> row =
                        (BindingRow<RowSettingsAddUsersUserBinding>) holder;
                final RowSettingsAddUsersUserBinding binding = row.getBinding();
                final UserItem userItem = (UserItem) item;

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
