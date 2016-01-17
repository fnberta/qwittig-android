/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentTaskAddBinding;
import ch.giantific.qwittig.di.components.DaggerTaskAddEditComponent;
import ch.giantific.qwittig.di.components.TaskAddEditComponent;
import ch.giantific.qwittig.di.modules.TaskAddViewModelModule;
import ch.giantific.qwittig.di.modules.TaskEditViewModelModule;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.presentation.ui.adapters.StringResSpinnerAdapter;
import ch.giantific.qwittig.presentation.ui.adapters.TaskUsersInvolvedRecyclerAdapter;
import ch.giantific.qwittig.presentation.ui.fragments.dialogs.DatePickerDialogFragment;
import ch.giantific.qwittig.presentation.ui.fragments.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.viewmodels.TaskAddEditViewModel;

/**
 * Provides an interface for the user to add a new {@link Task}. Allows the selection of the time
 * frame, the deadline and the users involved. The title of the task is set in the {@link Toolbar}
 * of the hosting {@link Activity}.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class TaskAddEditFragment extends BaseFragment<TaskAddEditViewModel, TaskAddEditFragment.ActivityListener>
        implements TaskAddEditViewModel.ViewListener {

    public static final int RESULT_TASK_SAVED = 2;
    public static final int RESULT_TASK_DISCARDED = 3;
    private static final String KEY_EDIT_TASK_ID = "EDIT_TASK_ID";
    private static final String DATE_PICKER_DIALOG = "DATE_PICKER_DIALOG";
    private static final String DISCARD_TASK_CHANGES_DIALOG = "DISCARD_TASK_CHANGES_DIALOG";
    StringResSpinnerAdapter mTimeFrameAdapter;
    TaskUsersInvolvedRecyclerAdapter mUsersRecyclerAdapter;
    private FragmentTaskAddBinding mBinding;
    private ItemTouchHelper mUsersItemTouchHelper;

    public TaskAddEditFragment() {
        // required empty constructor
    }

    /**
     * Returns a new instance of a {@link TaskAddEditFragment} in add mode withouth any params.
     *
     * @return a new instance of a {@link TaskAddEditFragment} in add mode
     */
    public static TaskAddEditFragment newAddInstance() {
        return new TaskAddEditFragment();
    }

    /**
     * a new instance of a {@link TaskAddEditFragment} in edit mode with the id of the task to edit
     * as a param.
     *
     * @param taskId the object id of the task to edit
     * @return a new instance of a {@link TaskAddEditFragment} in edit mode
     */
    public static TaskAddEditFragment newEditInstance(@NonNull String taskId) {
        TaskAddEditFragment fragment = new TaskAddEditFragment();
        Bundle args = new Bundle();
        args.putString(KEY_EDIT_TASK_ID, taskId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String editTaskId = getArguments().getString(KEY_EDIT_TASK_ID, "");
        final TaskAddEditComponent component;
        if (TextUtils.isEmpty(editTaskId)) {
            component = DaggerTaskAddEditComponent.builder()
                    .taskAddViewModelModule(new TaskAddViewModelModule(savedInstanceState))
                    .build();
        } else {
            component = DaggerTaskAddEditComponent.builder()
                    .taskEditViewModelModule(new TaskEditViewModelModule(savedInstanceState, editTaskId))
                    .build();
        }

        component.inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentTaskAddBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupUsersInvolvedRecyclerView();
        setupTimeFrameSpinner();
    }

    private void setupUsersInvolvedRecyclerView() {
        mBinding.rvTaskUsersInvolved.setHasFixedSize(true);
        mBinding.rvTaskUsersInvolved.setLayoutManager(new LinearLayoutManager(getActivity()));
        mUsersRecyclerAdapter = new TaskUsersInvolvedRecyclerAdapter(mViewModel);
        mBinding.rvTaskUsersInvolved.setAdapter(mUsersRecyclerAdapter);

        mUsersItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source, @NonNull RecyclerView.ViewHolder target) {
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
        mUsersItemTouchHelper.attachToRecyclerView(mBinding.rvTaskUsersInvolved);
    }

    private void setupTimeFrameSpinner() {
        final int[] timeFrames = new int[]{
                R.string.time_frame_daily,
                R.string.time_frame_weekly,
                R.string.time_frame_monthly,
                R.string.time_frame_yearly,
                R.string.time_frame_as_needed,
                R.string.time_frame_one_time};
        mTimeFrameAdapter = new StringResSpinnerAdapter(getActivity(), R.layout.spinner_item, timeFrames);
        mBinding.spTaskTimeFrame.setAdapter(mTimeFrameAdapter);
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.rvTaskUsersInvolved;
    }

    @Override
    public void showDiscardChangesDialog() {
        final DiscardChangesDialogFragment dialog = new DiscardChangesDialogFragment();
        dialog.show(getFragmentManager(), DISCARD_TASK_CHANGES_DIALOG);
    }

    @Override
    public void showDatePickerDialog() {
        final DatePickerDialogFragment dialog = new DatePickerDialogFragment();
        dialog.show(getFragmentManager(), DATE_PICKER_DIALOG);
    }

    @Override
    public String getTaskTitle() {
        return mActivity.getTaskTitle();
    }

    @Override
    public void finishScreen(@TaskResult int taskResult) {
        final Activity activity = getActivity();
        activity.setResult(taskResult);
        ActivityCompat.finishAfterTransition(activity);
    }

    @Override
    public void setUserListMinimumHeight(int numberOfUsers) {
        mBinding.rvTaskUsersInvolved.setMinimumHeight(numberOfUsers *
                getResources().getDimensionPixelSize(R.dimen.list_avatar_with_text));
    }

    @Override
    public void onStartUserDrag(@NonNull RecyclerView.ViewHolder viewHolder) {
        mUsersItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void notifyDataSetChanged() {
        mUsersRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyItemChanged(int position) {
        mUsersRecyclerAdapter.notifyItemChanged(position);
    }

    @Override
    public void notifyItemMoved(int fromPosition, int toPosition) {
        mUsersRecyclerAdapter.notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void notifyItemRemoved(int position) {
        mUsersRecyclerAdapter.notifyItemRemoved(position);
    }

    @Override
    public void setTimeFramePosition(int timeFrame) {
        int position = mTimeFrameAdapter.getPosition(timeFrame);
        mBinding.spTaskTimeFrame.setSelection(position);
    }

    @IntDef({RESULT_TASK_SAVED, RESULT_TASK_DISCARDED, Activity.RESULT_CANCELED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TaskResult {
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     * </p>
     * Extends {@link BaseFragment.ActivityListener}.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener<TaskAddEditViewModel> {
        /**
         * Gets the title of task.
         *
         * @return the title of the task
         */
        @NonNull
        String getTaskTitle();
    }
}
