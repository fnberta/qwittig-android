/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.push.PushBroadcastReceiver;
import ch.giantific.qwittig.databinding.ActivityTaskDetailsBinding;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.navdrawer.BaseNavDrawerActivity;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerComponent;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddEditViewModel;
import ch.giantific.qwittig.presentation.tasks.details.di.TaskDetailsSubcomponent;
import ch.giantific.qwittig.presentation.tasks.details.di.TaskDetailsViewModelModule;

/**
 * Hosts {@link TaskDetailsFragment} that shows the details of a task.
 * <p/>
 * Displays the title, the time frame and the users involved in the {@link Toolbar}.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class TaskDetailsActivity extends BaseNavDrawerActivity<TaskDetailsSubcomponent> {

    private static final String FRAGMENT_TASK_DETAILS = "FRAGMENT_TASK_DETAILS";

    @Inject
    TaskDetailsViewModel detailsViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityTaskDetailsBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_task_details);
        binding.setViewModel(detailsViewModel);

        // disable default actionBar title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        replaceDrawerIndicatorWithUp();
        setUpNavigation();
        unCheckNavDrawerItems();
        supportPostponeEnterTransition();

        if (userLoggedIn && savedInstanceState == null) {
            addDetailsFragment();
        }
    }

    @Override
    protected void injectDependencies(@NonNull NavDrawerComponent navComp, Bundle savedInstanceState) {
        component = navComp.plus(new TaskDetailsViewModelModule(savedInstanceState, getTaskObjectId()));
        component.inject(this);
    }

    private String getTaskObjectId() {
        final Intent intent = getIntent();
        String taskId = intent.getStringExtra(Navigator.INTENT_TASK_ID); // started from TaskFragment

        if (taskId == null) { // started via Push Notification
            try {
                final JSONObject jsonExtras = PushBroadcastReceiver.getData(intent);
                taskId = jsonExtras.optString(PushBroadcastReceiver.PUSH_PARAM_TASK_ID);
            } catch (JSONException e) {
                showMessage(R.string.toast_error_task_details_load);
            }
        }

        return taskId;
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{detailsViewModel});
    }

    private void setUpNavigation() {
        final TaskDetailsActivity activity = this;
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(activity);
            }
        });
    }

    private void addDetailsFragment() {
        final TaskDetailsFragment fragment = new TaskDetailsFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment, FRAGMENT_TASK_DETAILS)
                .commit();
    }

    @Override
    public void setupScreenAfterLogin() {
        super.setupScreenAfterLogin();

        addDetailsFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Navigator.INTENT_REQUEST_TASK_MODIFY:
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
}
