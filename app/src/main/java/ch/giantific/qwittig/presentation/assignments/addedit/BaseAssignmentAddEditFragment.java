/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentAssignmentAddEditBinding;
import ch.giantific.qwittig.presentation.assignments.addedit.viewmodels.AssignmentAddEditViewModel;
import ch.giantific.qwittig.presentation.assignments.addedit.viewmodels.items.AssignmentAddEditIdentityItemViewModel;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.common.dialogs.DatePickerDialogFragment;
import ch.giantific.qwittig.presentation.common.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.listadapters.StringResSpinnerAdapter;

/**
 * Provides an interface for the user to addItemAtPosition a new task. Allows the selection of the time
 * frame, the deadline and the users involved. The title of the task is set in the {@link Toolbar}
 * of the hosting {@link Activity}.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public abstract class BaseAssignmentAddEditFragment<T> extends BaseFragment<T, AssignmentAddEditContract.Presenter, BaseFragment.ActivityListener<T>>
        implements AssignmentAddEditContract.ViewListener {

    @Inject
    AssignmentAddEditViewModel viewModel;
    private FragmentAssignmentAddEditBinding binding;
    private ItemTouchHelper itemTouchHelper;
    private AssignmentAddEditIdentitiesRecyclerAdapter recyclerAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAssignmentAddEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupIdentitiesSwipeHelper();
    }

    private void setupIdentitiesSwipeHelper() {
        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source,
                                  @NonNull RecyclerView.ViewHolder target) {
                if (source.getItemViewType() != target.getItemViewType()) {
                    return false;
                }

                presenter.onIdentityMove(source.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                presenter.onIdentityDismiss(viewHolder.getAdapterPosition());
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }
        });
        itemTouchHelper.attachToRecyclerView(binding.rvAssignmentIdentities);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupRecyclerView();
        presenter.attachView(this);
        binding.setPresenter(presenter);
        binding.setViewModel(viewModel);
        setupTimeFrameSpinner();
    }

    private void setupRecyclerView() {
        binding.rvAssignmentIdentities.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvAssignmentIdentities.setHasFixedSize(true);
        recyclerAdapter = new AssignmentAddEditIdentitiesRecyclerAdapter(presenter,
                viewModel.getIdentities());
        binding.rvAssignmentIdentities.setAdapter(recyclerAdapter);
    }

    private void setupTimeFrameSpinner() {
        final StringResSpinnerAdapter timeFrameAdapter =
                new StringResSpinnerAdapter(getActivity(), R.layout.spinner_item,
                        viewModel.getTimeFrames());
        binding.spAssignmentTimeFrame.setAdapter(timeFrameAdapter);
    }

    @Override
    protected View getSnackbarView() {
        return binding.rvAssignmentIdentities;
    }

    @Override
    public void showDiscardChangesDialog() {
        DiscardChangesDialogFragment.display(getFragmentManager());
    }

    @Override
    public void showDatePickerDialog() {
        DatePickerDialogFragment.display(getFragmentManager());
    }

    @Override
    public boolean isIdentitiesEmpty() {
        return recyclerAdapter.getItemCount() == 0;
    }

    @Override
    public void addIdentity(@NonNull AssignmentAddEditIdentityItemViewModel item) {
        recyclerAdapter.addItem(item);
    }

    @Override
    public void startDragIdentity(@NonNull RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void swapIdentity(int fromPosition, int toPosition) {
        recyclerAdapter.swapItem(fromPosition, toPosition);
    }

    @Override
    public void removeIdentityAtPosition(int position) {
        recyclerAdapter.removeItemAtPosition(position);
    }

    @Override
    public void notifyIdentityChanged(@NonNull AssignmentAddEditIdentityItemViewModel item) {
        recyclerAdapter.notifyItemChanged(recyclerAdapter.getPositionForItem(item));
    }
}
