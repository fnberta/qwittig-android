/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ch.giantific.qwittig.databinding.RowPurchaseAddEditArticleIdentityBinding;
import ch.giantific.qwittig.presentation.common.listadapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.listadapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditArticleIdentityItemViewModel;

/**
 * Provides an adapter for a {@link RecyclerView} showing a list of users.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchaseAddEditArticleIdentitiesRecyclerAdapter extends BaseRecyclerAdapter<PurchaseAddEditArticleIdentitiesRecyclerAdapter.ItemUserRow>
        implements PurchaseAddEditArticleIdentitiesClickListener {

    private final PurchaseAddEditContract.Presenter presenter;
    private final List<PurchaseAddEditArticleIdentityItemViewModel> identities;

    public PurchaseAddEditArticleIdentitiesRecyclerAdapter(@NonNull PurchaseAddEditContract.Presenter presenter,
                                                           @NonNull List<PurchaseAddEditArticleIdentityItemViewModel> identities) {
        super();

        this.presenter = presenter;
        this.identities = identities;
    }

    @Override
    public ItemUserRow onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowPurchaseAddEditArticleIdentityBinding binding =
                RowPurchaseAddEditArticleIdentityBinding.inflate(inflater, parent, false);
        return new ItemUserRow(binding, this);
    }

    @Override
    public void onBindViewHolder(ItemUserRow holder, int position) {
        final RowPurchaseAddEditArticleIdentityBinding binding = holder.getBinding();
        final PurchaseAddEditArticleIdentityItemViewModel viewModel = identities.get(position);
        binding.setViewModel(viewModel);
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return identities.size();
    }

    @Override
    public void onItemRowIdentityClick(int position) {
        final PurchaseAddEditArticleIdentityItemViewModel viewModel = identities.get(position);
        viewModel.setSelected(!viewModel.isSelected());
        notifyItemChanged(position);

        // notify main view model because total and my share values need to be updated
        presenter.onArticleRowIdentityClick();
    }

    @Override
    public void onItemRowIdentityLongClick(int position) {
        final PurchaseAddEditArticleIdentityItemViewModel viewModel = identities.get(position);
        viewModel.setSelected(!viewModel.isSelected());
        notifyItemChanged(position);

        presenter.onArticleRowIdentityLongClick(viewModel);
    }

    /**
     * Provides a {@link RecyclerView} row showing a user.
     * <p/>
     * Subclass of {@link BindingRow}.
     */
    public static class ItemUserRow extends BindingRow<RowPurchaseAddEditArticleIdentityBinding> {

        public ItemUserRow(@NonNull RowPurchaseAddEditArticleIdentityBinding binding,
                           @NonNull final PurchaseAddEditArticleIdentitiesClickListener listener) {
            super(binding);

            final View root = binding.getRoot();
            root.setOnClickListener(v -> listener.onItemRowIdentityClick(getAdapterPosition()));
            root.setOnLongClickListener(view -> {
                listener.onItemRowIdentityLongClick(getAdapterPosition());
                return true;
            });
        }
    }
}
