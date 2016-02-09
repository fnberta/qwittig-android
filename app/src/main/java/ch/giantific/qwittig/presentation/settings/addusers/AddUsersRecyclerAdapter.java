/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addusers;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowSettingsAddUsersIntroBinding;
import ch.giantific.qwittig.databinding.RowSettingsAddUsersNicknameBinding;
import ch.giantific.qwittig.databinding.RowSettingsAddUsersUserBinding;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.settings.addusers.listitems.ListItem;
import ch.giantific.qwittig.presentation.settings.addusers.listitems.NicknameItem;
import ch.giantific.qwittig.presentation.settings.addusers.listitems.UserItem;

/**
 * Created by fabio on 08.02.16.
 */
public class AddUsersRecyclerAdapter extends RecyclerView.Adapter {

    private SettingsAddUsersViewModel mViewModel;

    public AddUsersRecyclerAdapter(@NonNull SettingsAddUsersViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, @ListItem.Type int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ListItem.Type.INTRO: {
                final RowSettingsAddUsersIntroBinding binding =
                        RowSettingsAddUsersIntroBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case ListItem.Type.NICKNAME: {
                final RowSettingsAddUsersNicknameBinding binding =
                        RowSettingsAddUsersNicknameBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case ListItem.Type.USER: {
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
        final ListItem item = mViewModel.getItemAtPosition(position);
        @ListItem.Type final int type = getItemViewType(position);
        switch (type) {
            case ListItem.Type.INTRO:
                // do nothing
                break;
            case ListItem.Type.NICKNAME: {
                final BindingRow<RowSettingsAddUsersNicknameBinding> row =
                        (BindingRow<RowSettingsAddUsersNicknameBinding>) holder;
                final RowSettingsAddUsersNicknameBinding binding = row.getBinding();
                final NicknameItem nicknameItem = (NicknameItem) item;

                nicknameItem.setAddListener(mViewModel);
                binding.setNicknameItem(nicknameItem);
                binding.executePendingBindings();
                break;
            }
            case ListItem.Type.USER: {
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
