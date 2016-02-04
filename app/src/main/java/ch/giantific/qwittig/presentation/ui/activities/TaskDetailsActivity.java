/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.activities;

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

import ch.giantific.qwittig.LocalBroadcast;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.ActivityTaskDetailsBinding;
import ch.giantific.qwittig.di.components.NavDrawerComponent;
import ch.giantific.qwittig.presentation.ui.fragments.TaskDetailsFragment;
import ch.giantific.qwittig.presentation.ui.fragments.TasksFragment;
import ch.giantific.qwittig.presentation.viewmodels.TaskDetailsViewModel;
import ch.giantific.qwittig.receivers.PushBroadcastReceiver;

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
    void handleLocalBroadcast(Intent intent, int dataType) {
        super.handleLocalBroadcast(intent, dataType);

        if (dataType == LocalBroadcast.DataType.TASKS_UPDATED) {
            mViewModel.updateList();
        }
    }

    @Override
    protected void injectNavDrawerDependencies(@NonNull NavDrawerComponent navComp) {
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
        getFragmentManager().beginTransaction()
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
    public void setViewModel(@NonNull TaskDetailsViewModel viewModel) {
        mViewModel = viewModel;
        mBinding.setViewModel(viewModel);
    }

    @Override
    void onLoginSuccessful() {
        super.onLoginSuccessful();

        addDetailsFragment();
    }
}
