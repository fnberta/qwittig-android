/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentTaskAddEditBinding;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.presentation.common.adapters.StringResSpinnerAdapter;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;
import ch.giantific.qwittig.presentation.common.fragments.DatePickerDialogFragment;
import ch.giantific.qwittig.presentation.common.fragments.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.tasks.addedit.di.DaggerTaskAddComponent;
import ch.giantific.qwittig.presentation.tasks.addedit.di.DaggerTaskEditComponent;
import ch.giantific.qwittig.presentation.tasks.addedit.di.TaskAddViewModelModule;
import ch.giantific.qwittig.presentation.tasks.addedit.di.TaskEditViewModelModule;

/**
 * Provides an interface for the user to add a new {@link Task}. Allows the selection of the time
 * frame, the deadline and the users involved. The title of the task is set in the {@link Toolbar}
 * of the hosting {@link Activity}.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class TaskAddEditFragment extends BaseRecyclerViewFragment<TaskAddEditViewModel, TaskAddEditFragment.ActivityListener>
        implements TaskAddEditViewModel.ViewListener {

    private static final String KEY_EDIT_TASK_ID = "EDIT_TASK_ID";
    private StringResSpinnerAdapter mTimeFrameAdapter;
    private FragmentTaskAddEditBinding mBinding;
    private ItemTouchHelper mItemTouchHelper;

    public TaskAddEditFragment() {
        // required empty constructor
    }

    /**
     * Returns a new instance of a {@link TaskAddEditFragment} in add mode without any params.
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

        final Bundle args = getArguments();
        if (args == null) {
            DaggerTaskAddComponent.builder()
                    .applicationComponent(Qwittig.getAppComponent(getActivity()))
                    .taskAddViewModelModule(new TaskAddViewModelModule(savedInstanceState, this))
                    .build()
                    .inject(this);
        } else {
            final String editTaskId = args.getString(KEY_EDIT_TASK_ID, "");
            DaggerTaskEditComponent.builder()
                    .applicationComponent(Qwittig.getAppComponent(getActivity()))
                    .taskEditViewModelModule(new TaskEditViewModelModule(savedInstanceState, this,
                            editTaskId))
                    .build()
                    .inject(this);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentTaskAddEditBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTimeFrameSpinner();
        setupIdentitiesSwipeHelper();
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.rvTaskUsersInvolved;
    }

    @Override
    protected RecyclerView.Adapter getRecyclerAdapter() {
        return new TaskAddEditUsersRecyclerAdapter(mViewModel);
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

    private void setupIdentitiesSwipeHelper() {
        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
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
    protected void setViewModelToActivity() {
        mActivity.setViewModel(mViewModel);
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.rvTaskUsersInvolved;
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
    public void finishScreen(@TaskAddEditViewModel.TaskResult int taskResult) {
        final Activity activity = getActivity();
        activity.setResult(taskResult);
        ActivityCompat.finishAfterTransition(activity);
    }

    @Override
    public void onStartUserDrag(@NonNull RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void setTimeFramePosition(int timeFrame) {
        int position = mTimeFrameAdapter.getPosition(timeFrame);
        mBinding.spTaskTimeFrame.setSelection(position);
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     * </p>
     * Extends {@link BaseFragment.ActivityListener}.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener {

        /**
         * Sets and binds the view model to the activity's layout.
         *
         * @param viewModel the view model to bind
         */
        void setViewModel(@NonNull TaskAddEditViewModel viewModel);
    }
}
