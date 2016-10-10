/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import ch.giantific.qwittig.databinding.FragmentPurchaseAddEditBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditRecyclerAdapter.ArticleIdentitiesRow;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.BasePurchaseAddEditItemViewModel.ViewType;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditArticleIdentityItemViewModel;

/**
 * Displays the interface where the user can addItemAtPosition a new purchase by setting store, date, users
 * involved and the different items.
 */
public abstract class BasePurchaseAddEditFragment<U, T extends PurchaseAddEditContract.Presenter, S extends BaseFragment.ActivityListener<U>>
        extends BaseFragment<U, T, S> {

    @Inject
    PurchaseAddEditViewModel viewModel;
    private FragmentPurchaseAddEditBinding binding;
    private PurchaseAddEditRecyclerAdapter recyclerAdapter;

    public PurchaseAddEditRecyclerAdapter getRecyclerAdapter() {
        return recyclerAdapter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPurchaseAddEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupRecyclerView();
        final ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.END) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                presenter.onArticleDismiss(viewHolder.getAdapterPosition());
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                final int position = viewHolder.getAdapterPosition();
                if (viewModel.getItemAtPosition(position).getViewType() != ViewType.ARTICLE) {
                    return 0;
                }

                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        });
        touchHelper.attachToRecyclerView(binding.rvPurchaseAddEdit);
    }

    private void setupRecyclerView() {
        binding.rvPurchaseAddEdit.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvPurchaseAddEdit.setHasFixedSize(true);
        recyclerAdapter = new PurchaseAddEditRecyclerAdapter(presenter, viewModel);
        binding.rvPurchaseAddEdit.setAdapter(recyclerAdapter);
    }

    @Override
    protected View getSnackbarView() {
        return binding.rvPurchaseAddEdit;
    }

    public void scrollToPosition(int position) {
        binding.rvPurchaseAddEdit.scrollToPosition(position);
    }

    public void notifyItemIdentityChanged(int position,
                                          @NonNull PurchaseAddEditArticleIdentityItemViewModel identityViewModel) {
        final ArticleIdentitiesRow row =
                (ArticleIdentitiesRow) binding.rvPurchaseAddEdit.findViewHolderForAdapterPosition(position);
        row.notifyItemChanged(identityViewModel);
    }
}
