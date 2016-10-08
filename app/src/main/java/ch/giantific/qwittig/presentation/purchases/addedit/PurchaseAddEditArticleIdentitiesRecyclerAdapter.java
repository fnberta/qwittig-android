/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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
public class PurchaseAddEditArticleIdentitiesRecyclerAdapter extends BaseRecyclerAdapter<BindingRow<RowPurchaseAddEditArticleIdentityBinding>> {

    private final PurchaseAddEditContract.Presenter presenter;
    private final List<PurchaseAddEditArticleIdentityItemViewModel> identities;

    public PurchaseAddEditArticleIdentitiesRecyclerAdapter(@NonNull PurchaseAddEditContract.Presenter presenter,
                                                           @NonNull List<PurchaseAddEditArticleIdentityItemViewModel> identities) {
        super();

        this.presenter = presenter;
        this.identities = identities;
    }

    @Override
    public BindingRow<RowPurchaseAddEditArticleIdentityBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowPurchaseAddEditArticleIdentityBinding binding =
                RowPurchaseAddEditArticleIdentityBinding.inflate(inflater, parent, false);
        return new BindingRow<>(binding);
    }

    @Override
    public void onBindViewHolder(BindingRow<RowPurchaseAddEditArticleIdentityBinding> holder, int position) {
        final RowPurchaseAddEditArticleIdentityBinding binding = holder.getBinding();
        final PurchaseAddEditArticleIdentityItemViewModel viewModel = identities.get(position);
        binding.setViewModel(viewModel);
        binding.setPresenter(presenter);
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return identities.size();
    }
}
