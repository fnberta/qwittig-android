/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.parse.ParseUser;

import java.util.List;

import ch.giantific.qwittig.databinding.RowPurchaseDetailsUsersUserBinding;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.presentation.ui.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.viewmodels.PurchaseDetailsViewModel;
import ch.giantific.qwittig.presentation.viewmodels.rows.PurchaseDetailsUsersUserRowViewModel;

/**
 * Created by fabio on 30.01.16.
 */
public class PurchaseDetailsUsersRecyclerAdapter extends RecyclerView.Adapter<BindingRow<RowPurchaseDetailsUsersUserBinding>> {

    private PurchaseDetailsViewModel mViewModel;
    private List<ParseUser> mUsers;

    public PurchaseDetailsUsersRecyclerAdapter(@NonNull PurchaseDetailsViewModel viewModel,
                                               @NonNull List<ParseUser> users) {
        mViewModel = viewModel;
        mUsers = users;
    }

    @Override
    public BindingRow<RowPurchaseDetailsUsersUserBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowPurchaseDetailsUsersUserBinding binding =
                RowPurchaseDetailsUsersUserBinding.inflate(inflater, parent, false);
        return new BindingRow<>(binding);
    }

    @Override
    public void onBindViewHolder(BindingRow<RowPurchaseDetailsUsersUserBinding> holder, int position) {
        final User user = (User) mUsers.get(position);
        final RowPurchaseDetailsUsersUserBinding binding = holder.getBinding();
        PurchaseDetailsUsersUserRowViewModel viewModel = binding.getViewModel();

        final boolean boldName = mViewModel.getPurchaseBuyer().getObjectId().equals(user.getObjectId());
        if (viewModel == null) {
            viewModel = new PurchaseDetailsUsersUserRowViewModel(user, boldName);
            binding.setViewModel(viewModel);
        } else {
            viewModel.setUser(user);
            viewModel.setUserNameBold(boldName);
        }

        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}
