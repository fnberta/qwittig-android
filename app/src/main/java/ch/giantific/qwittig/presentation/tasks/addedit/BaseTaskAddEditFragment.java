/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentTaskAddEditBinding;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.presentation.common.ListDragInteraction;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.StringResSpinnerAdapter;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;
import ch.giantific.qwittig.presentation.common.fragments.DatePickerDialogFragment;
import ch.giantific.qwittig.presentation.common.fragments.DiscardChangesDialogFragment;

/**
 * Provides an interface for the user to add a new {@link Task}. Allows the selection of the time
 * frame, the deadline and the users involved. The title of the task is set in the {@link Toolbar}
 * of the hosting {@link Activity}.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public abstract class BaseTaskAddEditFragment<T> extends BaseRecyclerViewFragment<T, TaskAddEditViewModel, BaseFragment.ActivityListener<T>>
        implements TaskAddEditViewModel.ViewListener {

    private FragmentTaskAddEditBinding mBinding;
    private ListDragInteraction mItemTouchHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentTaskAddEditBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.attachView(this);
        mViewModel.setListDragInteraction(mItemTouchHelper);
        mBinding.setViewModel(mViewModel);
        setupTimeFrameSpinner();
        setupIdentitiesSwipeHelper();
    }

    private void setupTimeFrameSpinner() {
        final StringResSpinnerAdapter timeFrameAdapter =
                new StringResSpinnerAdapter(getActivity(), R.layout.spinner_item,
                        mViewModel.getTimeFrames());
        mBinding.spTaskTimeFrame.setAdapter(timeFrameAdapter);
    }

    private void setupIdentitiesSwipeHelper() {
        mItemTouchHelper = new ListDragHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source,
                                  @NonNull RecyclerView.ViewHolder target) {
                if (source.getItemViewType() != target.getItemViewType()) {
                    return false;
                }

                mViewModel.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                mViewModel.onItemDismiss(viewHolder.getAdapterPosition());
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }
        });
        mItemTouchHelper.attachToRecyclerView(mBinding.rvTaskUsersInvolved);
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.rvTaskUsersInvolved;
    }

    @Override
    protected BaseRecyclerAdapter getRecyclerAdapter() {
        return new TaskAddEditUsersRecyclerAdapter(mViewModel);
    }

    @Override
    public void showDiscardChangesDialog() {
        DiscardChangesDialogFragment.display(getFragmentManager());
    }

    @Override
    public void showDatePickerDialog() {
        DatePickerDialogFragment.display(getFragmentManager());
    }

    private static class ListDragHelper extends ItemTouchHelper implements ListDragInteraction {

        public ListDragHelper(Callback callback) {
            super(callback);
        }
    }
}
