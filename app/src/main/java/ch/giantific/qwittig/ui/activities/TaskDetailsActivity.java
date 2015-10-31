/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.receivers.PushBroadcastReceiver;
import ch.giantific.qwittig.ui.fragments.TaskDetailsFragment;
import ch.giantific.qwittig.ui.fragments.TasksFragment;
import ch.giantific.qwittig.utils.MessageUtils;

/**
 * Hosts {@link TaskDetailsFragment} that shows the details of a task.
 * <p/>
 * Displays the title, the time frame and the users involved in the {@link Toolbar}.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class TaskDetailsActivity extends BaseNavDrawerActivity implements
        TaskDetailsFragment.FragmentInteractionListener {

    public static final int RESULT_TASK_DELETED = 2;
    public static final int RESULT_GROUP_CHANGED = 3;
    private static final String STATE_TASK_DETAILS_FRAGMENT = "STATE_TASK_DETAILS_FRAGMENT";
    private TaskDetailsFragment mTaskDetailsFragment;
    private TextView mTextViewTitle;
    private TextView mTextViewTimeFrame;
    private TextView mTextViewUsersInvolved;
    private FloatingActionButton mFab;
    private boolean mShowEditOptions;
    private String mTaskId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        // disable default actionBar title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        replaceDrawerIndicatorWithUp();
        uncheckNavDrawerItems();
        supportPostponeEnterTransition();
        getTaskObjectId();

        final TaskDetailsActivity activity = this;
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(activity);
            }
        });

        mTextViewTitle = (TextView) findViewById(R.id.tv_task_details_title);
        mTextViewTimeFrame = (TextView) findViewById(R.id.tv_task_details_subtitle);
        mTextViewUsersInvolved = (TextView) findViewById(R.id.tv_task_details_users_involved);
        mFab = (FloatingActionButton) findViewById(R.id.fab_task_details_done);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTaskDetailsFragment.setTaskDone();
            }
        });

        if (savedInstanceState == null) {
            mTaskDetailsFragment = TaskDetailsFragment.newInstance(mTaskId);
            getFragmentManager().beginTransaction()
                    .add(R.id.container, mTaskDetailsFragment)
                    .commit();
        } else {
            mTaskDetailsFragment = (TaskDetailsFragment) getFragmentManager()
                    .getFragment(savedInstanceState, STATE_TASK_DETAILS_FRAGMENT);
        }

        fetchCurrentUserGroups();
    }

    private void getTaskObjectId() {
        final Intent intent = getIntent();
        mTaskId = intent.getStringExtra(TasksFragment.INTENT_TASK_ID); // started from TaskFragment

        if (mTaskId == null) { // started via Push Notification
            try {
                JSONObject jsonExtras = PushBroadcastReceiver.getData(intent);
                mTaskId = jsonExtras.optString(PushBroadcastReceiver.PUSH_PARAM_TASK_ID);
            } catch (JSONException e) {
                MessageUtils.showBasicSnackbar(mToolbar, getString(R.string.toast_error_task_details_load));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getFragmentManager().putFragment(outState, STATE_TASK_DETAILS_FRAGMENT, mTaskDetailsFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_details, menu);
        if (mShowEditOptions) {
            menu.findItem(R.id.action_task_edit).setVisible(true);
            menu.findItem(R.id.action_task_delete).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_task_delete:
                mTaskDetailsFragment.deleteTask();
                return true;
            case R.id.action_task_edit:
                mTaskDetailsFragment.editTask();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setToolbarHeader(@NonNull String title, @NonNull String timeFrame,
                                 @NonNull SpannableStringBuilder usersInvolved,
                                 boolean currentUserIsResponsible) {
        mTextViewTitle.setText(title);
        mTextViewTimeFrame.setText(timeFrame);
        mTextViewUsersInvolved.setText(usersInvolved);
        if (currentUserIsResponsible) {
            mFab.show();
        } else {
            mFab.hide();
        }
    }

    @Override
    public void showEditOptions(boolean show) {
        mShowEditOptions = show;
        invalidateOptionsMenu();
    }

    @Override
    public void onTasksUpdated() {
        super.onTasksUpdated();

        mTaskDetailsFragment.queryData();
    }

    @Override
    protected void onNewGroupSet() {
        // NavDrawer group setting needs to be updated
        setResult(RESULT_GROUP_CHANGED);
        finish();
    }
}
