package ch.giantific.qwittig.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import ch.giantific.qwittig.PushBroadcastReceiver;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.utils.MessageUtils;

public class TaskDetailsActivity extends BaseNavDrawerActivity implements
        TaskDetailsFragment.FragmentInteractionListener {

    public static final int RESULT_TASK_DELETED = 2;
    public static final int RESULT_GROUP_CHANGED = 3;
    private static final String TASK_DETAILS_FRAGMENT = "tasks_details_fragment";
    private TaskDetailsFragment mTaskDetailsFragment;
    private TextView mTextViewTitle;
    private TextView mTextViewTimeFrame;
    private TextView mTextViewUsersInvolved;
    private FloatingActionButton mFab;
    private boolean mShowEditOption;
    private String mTaskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        mTextViewTitle = (TextView) findViewById(R.id.tv_details_title);
        mTextViewTimeFrame = (TextView) findViewById(R.id.tv_details_subtitle);
        mTextViewUsersInvolved = (TextView) findViewById(R.id.tv_task_details_users_involved);
        mFab = (FloatingActionButton) findViewById(R.id.fab_task_details_done);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTaskDetailsFragment.setTaskDone();
            }
        });

        if (savedInstanceState == null && mUserIsLoggedIn) {
            addDetailsFragment();
        }
    }

    /**
     * Gets data passed on in intent from HomeActivity or Push Notification
     */
    private void getTaskObjectId() {
        final Intent intent = getIntent();
        mTaskId = intent.getStringExtra(TasksFragment.INTENT_TASK_ID); // started from TaskFragment

        if (mTaskId == null) { // started via Push Notification
            try {
                JSONObject jsonExtras = PushBroadcastReceiver.getData(intent);
                mTaskId = jsonExtras.optString(PushBroadcastReceiver.PUSH_PARAM_TASK);
            } catch (JSONException e) {
                MessageUtils.showBasicSnackbar(mToolbar, getString(R.string.toast_error_task_details_load));
            }
        }
    }

    private void addDetailsFragment() {
        getFragmentManager().beginTransaction()
                .add(R.id.container, TaskDetailsFragment.newInstance(mTaskId),
                        TASK_DETAILS_FRAGMENT)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mUserIsLoggedIn) {
            findDetailsFragment();
        }
    }

    private void findDetailsFragment() {
        mTaskDetailsFragment = (TaskDetailsFragment) getFragmentManager()
                .findFragmentByTag(TASK_DETAILS_FRAGMENT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_details, menu);
        if (mShowEditOption) {
            menu.findItem(R.id.action_task_edit).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
    void afterLoginSetup() {
        super.afterLoginSetup();

        addDetailsFragment();
        getFragmentManager().executePendingTransactions();
        findDetailsFragment();
    }

    @Override
    public void setToolbarHeader(String title, String timeFrame, String usersInvolved,
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
    public void showEditOption(boolean show) {
        mShowEditOption = show;
        invalidateOptionsMenu();
    }

    @Override
    public void onTasksPinned() {
        super.onTasksPinned();

        mTaskDetailsFragment.queryData();
    }

    @Override
    protected void onNewGroupSet() {
        // NavDrawer group setting needs to be updated
        setResult(RESULT_GROUP_CHANGED);
        finish();
    }
}
