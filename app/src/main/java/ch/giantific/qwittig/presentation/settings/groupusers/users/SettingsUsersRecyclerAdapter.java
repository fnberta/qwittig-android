/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.users;

import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.RowSettingsUsersUserBinding;
import ch.giantific.qwittig.presentation.common.listadapters.BaseSortedListRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.listadapters.SortedListCallback;
import ch.giantific.qwittig.presentation.common.listadapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.settings.groupusers.users.viewmodels.items.SettingsUsersItemViewModel;

/**
 * Provides a {@link RecyclerView} adapter that manages the list of items on the manage users
 * settings screen.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class SettingsUsersRecyclerAdapter extends BaseSortedListRecyclerAdapter<SettingsUsersItemViewModel,
        SettingsUsersContract.Presenter,
        SettingsUsersRecyclerAdapter.GroupUserRow> {

    public SettingsUsersRecyclerAdapter(@NonNull SettingsUsersContract.Presenter presenter) {
        super(presenter);
    }

    @Override
    protected SortedList<SettingsUsersItemViewModel> createList() {
        return new SortedList<>(SettingsUsersItemViewModel.class,
                new SortedListCallback<>(this, presenter));
    }

    @Override
    public GroupUserRow onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowSettingsUsersUserBinding binding =
                RowSettingsUsersUserBinding.inflate(inflater, parent, false);
        return new GroupUserRow(binding, presenter);
    }

    @Override
    public void onBindViewHolder(GroupUserRow holder, int position) {
        final RowSettingsUsersUserBinding binding = holder.getBinding();
        final SettingsUsersItemViewModel viewModel = getItemAtPosition(position);

        holder.setMenuVisibility(viewModel.isPending());
        binding.setViewModel(viewModel);
        binding.executePendingBindings();
    }

    public interface AdapterInteractionListener {

        void onInviteClick(int position);

        void onEditNicknameClick(int position);

        void onEditAvatarClick(int position);

        void onRemoveClick(int position);
    }

    public static class GroupUserRow extends BindingRow<RowSettingsUsersUserBinding> {

        public GroupUserRow(@NonNull RowSettingsUsersUserBinding binding,
                            @NonNull final AdapterInteractionListener listener) {
            super(binding);

            binding.tbAddUsersUser.inflateMenu(R.menu.menu_settings_users_user);
            binding.tbAddUsersUser.setOnMenuItemClickListener(item -> {
                final int id = item.getItemId();
                switch (id) {
                    case R.id.action_settings_users_user_invite:
                        listener.onInviteClick(getAdapterPosition());
                        return true;
                    case R.id.action_settings_users_user_edit_nickname:
                        listener.onEditNicknameClick(getAdapterPosition());
                        return true;
                    case R.id.action_settings_users_user_edit_avatar:
                        listener.onEditAvatarClick(getAdapterPosition());
                        return true;
                    case R.id.action_settings_users_user_remove:
                        listener.onRemoveClick(getAdapterPosition());
                        return true;
                    default:
                        return false;
                }
            });
        }

        public void setMenuVisibility(boolean show) {
            final Toolbar toolbar = getBinding().tbAddUsersUser;
            final Menu menu = toolbar.getMenu();
            final MenuItem share = menu.findItem(R.id.action_settings_users_user_invite);
            share.setVisible(show);
            final MenuItem nickname = menu.findItem(R.id.action_settings_users_user_edit_nickname);
            nickname.setVisible(show);
            final MenuItem avatar = menu.findItem(R.id.action_settings_users_user_edit_avatar);
            avatar.setVisible(show);
            final MenuItem remove = menu.findItem(R.id.action_settings_users_user_remove);
            remove.setVisible(show);
        }
    }
}
