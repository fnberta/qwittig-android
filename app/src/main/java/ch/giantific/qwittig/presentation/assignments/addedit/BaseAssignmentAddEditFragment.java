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

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentAssignmentAddEditBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.common.dialogs.DatePickerDialogFragment;
import ch.giantific.qwittig.presentation.common.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.listadapters.StringResSpinnerAdapter;
import ch.giantific.qwittig.presentation.common.listadapters.interactions.ListDragInteraction;

/**
 * Provides an interface for the user to add a new task. Allows the selection of the time
 * frame, the deadline and the users involved. The title of the task is set in the {@link Toolbar}
 * of the hosting {@link Activity}.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public abstract class BaseAssignmentAddEditFragment<T> extends BaseFragment<T, AssignmentAddEditContract.Presenter, BaseFragment.ActivityListener<T>>
        implements AssignmentAddEditContract.ViewListener {

    private FragmentAssignmentAddEditBinding binding;
    private ListDragInteraction itemTouchHelper;

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
        itemTouchHelper = new ListDragHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source,
                                  @NonNull RecyclerView.ViewHolder target) {
                if (source.getItemViewType() != target.getItemViewType()) {
                    return false;
                }

                presenter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                presenter.onItemDismiss(viewHolder.getAdapterPosition());
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

        final AssignmentAddEditIdentitiesRecyclerAdapter adapter = setupRecyclerView();
        presenter.attachView(this);
        presenter.setListInteraction(adapter);
        presenter.setListDragInteraction(itemTouchHelper);
        binding.setPresenter(presenter);
        binding.setViewModel(presenter.getViewModel());
        setupTimeFrameSpinner();
    }

    private AssignmentAddEditIdentitiesRecyclerAdapter setupRecyclerView() {
        binding.rvAssignmentIdentities.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvAssignmentIdentities.setHasFixedSize(true);
        final AssignmentAddEditIdentitiesRecyclerAdapter adapter = new AssignmentAddEditIdentitiesRecyclerAdapter(presenter);
        binding.rvAssignmentIdentities.setAdapter(adapter);

        return adapter;
    }

    private void setupTimeFrameSpinner() {
        final StringResSpinnerAdapter timeFrameAdapter =
                new StringResSpinnerAdapter(getActivity(), R.layout.spinner_item,
                        presenter.getTimeFrames());
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

    private static class ListDragHelper extends ItemTouchHelper implements ListDragInteraction {

        public ListDragHelper(Callback callback) {
            super(callback);
        }
    }
}
