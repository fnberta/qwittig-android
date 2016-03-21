/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.MenuItem;
import android.widget.DatePicker;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.ActivityTaskAddEditBinding;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.TransitionListenerAdapter;
import ch.giantific.qwittig.presentation.common.fragments.DiscardChangesDialogFragment;
import ch.giantific.qwittig.utils.Utils;

/**
 * Hosts {@link TaskAddEditFragment} that allows the user to create a new task.
 * <p/>
 * Handles transition animations and displays the task's title in the {@link Toolbar}.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class TaskAddActivity extends BaseActivity<TaskAddEditViewModel>
        implements TaskAddEditFragment.ActivityListener,
        DatePickerDialog.OnDateSetListener,
        DiscardChangesDialogFragment.DialogInteractionListener {

    private ActivityTaskAddEditBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_task_add_edit);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        if (savedInstanceState == null) {
            if (Utils.isRunningLollipopAndHigher()) {
                addActivityTransitionListener();
            } else {
                mBinding.fabTaskSave.show();
            }

            addFragment();
        } else {
            mBinding.fabTaskSave.show();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void addActivityTransitionListener() {
        Transition enter = getWindow().getEnterTransition();
        enter.addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                super.onTransitionEnd(transition);
                transition.removeListener(this);

                mBinding.fabTaskSave.show();
            }
        });
    }

    private void addFragment() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, getTaskFragment())
                .commit();
    }

    TaskAddEditFragment getTaskFragment() {
        return TaskAddEditFragment.newAddInstance();
    }

    @Override
    public void setViewModel(@NonNull TaskAddEditViewModel viewModel) {
        mViewModel = viewModel;
        mBinding.setViewModel(viewModel);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                mViewModel.onUpOrBackClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDiscardChangesSelected() {
        mViewModel.onDiscardChangesSelected();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mViewModel.onDateSet(view, year, monthOfYear, dayOfMonth);
    }

    @Override
    public void onBackPressed() {
        mViewModel.onUpOrBackClick();
    }
}
