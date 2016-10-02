/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.FragmentPurchaseAddEditBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.BasePurchaseAddEditItemViewModel.ViewType;

/**
 * Displays the interface where the user can add a new purchase by setting store, date, users
 * involved and the different items.
 */
public abstract class BasePurchaseAddEditFragment<U, T extends PurchaseAddEditContract.Presenter, S extends BaseFragment.ActivityListener<U>>
        extends BaseFragment<U, T, S> {

    private FragmentPurchaseAddEditBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPurchaseAddEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final PurchaseAddEditRecyclerAdapter adapter = setupRecyclerView();
        presenter.setListInteraction(adapter);
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
                if (presenter.getItemAtPosition(position).getViewType() != ViewType.ARTICLE) {
                    return 0;
                }

                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        });
        touchHelper.attachToRecyclerView(binding.rvPurchaseAddEdit);
    }

    private PurchaseAddEditRecyclerAdapter setupRecyclerView() {
        binding.rvPurchaseAddEdit.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvPurchaseAddEdit.setHasFixedSize(true);
        final PurchaseAddEditRecyclerAdapter adapter = new PurchaseAddEditRecyclerAdapter(presenter);
        binding.rvPurchaseAddEdit.setAdapter(adapter);

        return adapter;
    }

    @Override
    protected View getSnackbarView() {
        return binding.rvPurchaseAddEdit;
    }
}
