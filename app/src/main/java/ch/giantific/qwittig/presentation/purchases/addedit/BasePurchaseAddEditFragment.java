/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.FragmentPurchaseAddEditBinding;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemModel.Type;

/**
 * Displays the interface where the user can add a new purchase by setting store, date, users
 * involved and the different items.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
 */
public abstract class BasePurchaseAddEditFragment<U, T extends PurchaseAddEditViewModel, S extends BaseRecyclerViewFragment.ActivityListener<U>>
        extends BaseRecyclerViewFragment<U, T, S> {

    private FragmentPurchaseAddEditBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentPurchaseAddEditBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.setListInteraction(mRecyclerAdapter);
        mBinding.setViewModel(mViewModel);
        final ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.END) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                mViewModel.onItemDismiss(viewHolder.getAdapterPosition());
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                final int position = viewHolder.getAdapterPosition();
                if (mViewModel.getItemViewType(position) != Type.ITEM) {
                    return 0;
                }

                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        });
        touchHelper.attachToRecyclerView(mBinding.rvPurchaseAddEdit);
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.rvPurchaseAddEdit;
    }

    @Override
    protected BaseRecyclerAdapter getRecyclerAdapter() {
        return new PurchaseAddEditRecyclerAdapter(mViewModel);
    }
}
