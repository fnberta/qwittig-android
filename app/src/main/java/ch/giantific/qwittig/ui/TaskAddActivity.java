package ch.giantific.qwittig.ui;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.transition.Transition;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import java.util.Date;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.ui.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.ui.listeners.TransitionListenerAdapter;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.Utils;

public class TaskAddActivity extends BaseActivity implements
        TaskAddFragment.FragmentInteractionListener,
        DatePickerDialog.OnDateSetListener,
        DiscardChangesDialogFragment.DialogInteractionListener {

    private static final String TASK_ADD_FRAGMENT = "task_add_fragment";
    TaskAddFragment mTaskAddFragment;
    TextInputLayout mTextInputLayoutTitle;
    private FloatingActionButton mFab;

    @Override
    public String getTaskTitle() {
        return mTextInputLayoutTitle.getEditText().getText().toString();
    }

    @Override
    public void setTaskTitle(String title) {
        mTextInputLayoutTitle.getEditText().setText(title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_add);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
        }

        mTextInputLayoutTitle = (TextInputLayout) findViewById(R.id.til_task_add_title);
        mFab = (FloatingActionButton) findViewById(R.id.fab_task_save);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTaskAddFragment.saveTask(getTaskTitle());
            }
        });

        if (savedInstanceState == null) {
            if (Utils.isRunningLollipopAndHigher()) {
                addActivityTransitionListener();
            } else {
                mFab.show();
            }

            addTaskFragment();
        } else {
            mFab.show();
        }
    }

    void addTaskFragment() {
        getFragmentManager().beginTransaction()
                .add(R.id.container, new TaskAddFragment(), TASK_ADD_FRAGMENT)
                .commit();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void addActivityTransitionListener() {
        Transition enter = getWindow().getEnterTransition();
        enter.addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                super.onTransitionEnd(transition);
                transition.removeListener(this);

                mFab.show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        findTaskFragment();
    }

    void findTaskFragment() {
        mTaskAddFragment = (TaskAddFragment) getFragmentManager()
                .findFragmentByTag(TASK_ADD_FRAGMENT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                mTaskAddFragment.checkForChangesAndExit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void discardChanges() {
        mTaskAddFragment.finish(TaskAddFragment.TASK_DISCARDED);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Date deadline = DateUtils.parseDateFromPicker(year, monthOfYear, dayOfMonth);
        mTaskAddFragment.setDeadline(deadline);
    }

    @Override
    public void onBackPressed() {
        mTaskAddFragment.checkForChangesAndExit();
    }
}
