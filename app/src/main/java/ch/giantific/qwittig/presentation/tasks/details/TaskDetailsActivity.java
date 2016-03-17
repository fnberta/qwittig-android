/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import ch.giantific.qwittig.data.bus.LocalBroadcast;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.ActivityTaskDetailsBinding;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerComponent;
import ch.giantific.qwittig.presentation.navdrawer.BaseNavDrawerActivity;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddEditViewModel;
import ch.giantific.qwittig.presentation.tasks.list.TasksFragment;
import ch.giantific.qwittig.data.push.PushBroadcastReceiver;

/**
 * Hosts {@link TaskDetailsFragment} that shows the details of a task.
 * <p/>
 * Displays the title, the time frame and the users involved in the {@link Toolbar}.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class TaskDetailsActivity extends BaseNavDrawerActivity<TaskDetailsViewModel>
        implements TaskDetailsFragment.ActivityListener {

    private ActivityTaskDetailsBinding mBinding;

    @Override
    protected void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);

        if (dataType == LocalBroadcast.DataType.TASKS_UPDATED) {
            mViewModel.loadData();
        }
    }

    @Override
    protected void injectDependencies(@NonNull NavDrawerComponent navComp, Bundle savedInstanceState) {
        navComp.inject(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_task_details);

        // disable default actionBar title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        replaceDrawerIndicatorWithUp();
        setUpNavigation();
        unCheckNavDrawerItems();
        supportPostponeEnterTransition();

        if (mUserLoggedIn && savedInstanceState == null) {
            addDetailsFragment();
        }
    }

    private void setUpNavigation() {
        final TaskDetailsActivity activity = this;
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(activity);
            }
        });
    }

    private void addDetailsFragment() {
        final TaskDetailsFragment fragment = TaskDetailsFragment.newInstance(getTaskObjectId());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment)
                .commit();
    }

    private String getTaskObjectId() {
        final Intent intent = getIntent();
        String taskId = intent.getStringExtra(TasksFragment.INTENT_TASK_ID); // started from TaskFragment

        if (taskId == null) { // started via Push Notification
            try {
                JSONObject jsonExtras = PushBroadcastReceiver.getData(intent);
                taskId = jsonExtras.optString(PushBroadcastReceiver.PUSH_PARAM_TASK_ID);
            } catch (JSONException e) {
                Snackbar.make(mToolbar, R.string.toast_error_task_details_load,
                        Snackbar.LENGTH_LONG).show();
            }
        }

        return taskId;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_TASK_MODIFY:
                switch (resultCode) {
                    case TaskAddEditViewModel.TaskResult.TASK_DISCARDED:
                        showMessage(R.string.toast_changes_discarded);
                        break;
                    case TaskAddEditViewModel.TaskResult.TASK_SAVED:
                        showMessage(R.string.toast_changes_saved);
                        break;
                }
                break;
        }
    }

    @Override
    public void setViewModel(@NonNull TaskDetailsViewModel viewModel) {
        mViewModel = viewModel;
        mBinding.setViewModel(viewModel);
    }

    @Override
    protected void onLoginSuccessful() {
        super.onLoginSuccessful();

        addDetailsFragment();
    }
}
