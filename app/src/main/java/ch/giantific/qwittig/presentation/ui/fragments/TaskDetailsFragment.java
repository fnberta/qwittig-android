/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseUser;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentTaskDetailsBinding;
import ch.giantific.qwittig.di.components.DaggerTaskDetailsComponent;
import ch.giantific.qwittig.di.modules.TaskDetailsViewModelModule;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.presentation.ui.activities.BaseActivity;
import ch.giantific.qwittig.presentation.ui.activities.TaskEditActivity;
import ch.giantific.qwittig.presentation.ui.adapters.TaskHistoryRecyclerAdapter;
import ch.giantific.qwittig.presentation.viewmodels.TaskDetailsViewModel;

/**
 * Shows the details of a {@link Task}. Most of the information gets displayed in the
 * {@link Toolbar} of the hosting {@link Activity}. The fragment itself shows a list of users that
 * have previously finished the task.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class TaskDetailsFragment extends BaseRecyclerViewFragment<TaskDetailsViewModel, TaskDetailsFragment.ActivityListener>
        implements TaskDetailsViewModel.ViewListener {

    private static final String KEY_TASK_ID = "TASK_ID";
    private FragmentTaskDetailsBinding mBinding;
    private boolean mShowEditOptions;

    public TaskDetailsFragment() {
    }

    /**
     * Returns a new instance of {@link TaskDetailsFragment}.
     *
     * @param taskId the object id of the task for which the details should be displayed
     * @return a new instance of {@link TaskDetailsFragment}
     */
    @NonNull
    public static TaskDetailsFragment newInstance(@NonNull String taskId) {
        TaskDetailsFragment fragment = new TaskDetailsFragment();

        Bundle args = new Bundle();
        args.putString(KEY_TASK_ID, taskId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        final String taskId = getArguments().getString(KEY_TASK_ID, "");
        DaggerTaskDetailsComponent.builder()
                .taskDetailsViewModelModule(new TaskDetailsViewModelModule(savedInstanceState, this, taskId))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentTaskDetailsBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.rvTaskDetailsHistory;
    }

    @Override
    protected RecyclerView.Adapter getRecyclerAdapter() {
        return new TaskHistoryRecyclerAdapter(mViewModel);
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setViewModel(mViewModel);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_task_details, menu);
        if (mShowEditOptions) {
            menu.findItem(R.id.action_task_edit).setVisible(true);
            menu.findItem(R.id.action_task_delete).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_task_delete:
                mViewModel.deleteTask();
                return true;
            case R.id.action_task_edit:
                mViewModel.editTask();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case BaseActivity.INTENT_REQUEST_TASK_MODIFY:
                switch (resultCode) {
                    case TaskAddEditFragment.RESULT_TASK_DISCARDED:
                        Snackbar.make(mRecyclerView, R.string.toast_changes_discarded,
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case TaskAddEditFragment.RESULT_TASK_SAVED:
                        Snackbar.make(mRecyclerView, R.string.toast_changes_saved,
                                Snackbar.LENGTH_LONG).show();
                        break;
                }
                break;
        }
    }

    @Override
    public void startPostponedEnterTransition() {
        ActivityCompat.startPostponedEnterTransition(getActivity());
    }

    @Override
    public void toggleEditOptions(boolean showOptions) {
        mShowEditOptions = showOptions;
        getActivity().invalidateOptionsMenu();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startEditTaskActivity(@NonNull String taskId) {
        Intent intent = new Intent(getActivity(), TaskEditActivity.class);
        intent.putExtra(TasksFragment.INTENT_TASK_ID, taskId);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
        startActivityForResult(intent, BaseActivity.INTENT_REQUEST_TASK_MODIFY, options.toBundle());
    }

    @NonNull
    @Override
    public SpannableStringBuilder buildUsersInvolvedString(@NonNull List<ParseUser> usersInvolved,
                                                           @NonNull User userResponsible,
                                                           @NonNull User currentUser) {
        final SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        final int usersInvolvedSize = usersInvolved.size();
        final Activity activity = getActivity();
        stringBuilder.append(userResponsible.getNicknameOrMe(activity, currentUser));

        if (usersInvolvedSize > 1) {
            final int spanEnd = stringBuilder.length();
            stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, spanEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            for (int i = 1; i < usersInvolvedSize; i++) {
                final User user = (User) usersInvolved.get(i);
                stringBuilder.append(" - ").append(user.getNicknameOrMe(activity, currentUser));
            }
        }

        return stringBuilder;
    }

    @Override
    public void finishScreen(int result) {
        Activity activity = getActivity();
        activity.setResult(result);
        activity.finish();
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     * <p/>
     * Extends {@link BaseFragment.ActivityListener}.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener {
        void setViewModel(@NonNull TaskDetailsViewModel viewModel);
    }
}
