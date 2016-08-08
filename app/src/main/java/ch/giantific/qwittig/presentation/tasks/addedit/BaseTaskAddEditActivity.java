package ch.giantific.qwittig.presentation.tasks.addedit;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.transition.Transition;
import android.view.MenuItem;
import android.widget.DatePicker;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.ActivityTaskAddEditBinding;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.TransitionListenerAdapter;
import ch.giantific.qwittig.presentation.common.fragments.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.utils.Utils;

/**
 * Created by fabio on 17.06.16.
 */
public abstract class BaseTaskAddEditActivity<T> extends BaseActivity<T>
        implements DatePickerDialog.OnDateSetListener,
        DiscardChangesDialogFragment.DialogInteractionListener {

    @Inject
    TaskAddEditViewModel mAddEditViewModel;
    private ActivityTaskAddEditBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_task_add_edit);
        mBinding.setViewModel(mAddEditViewModel);

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

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{mAddEditViewModel});
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

    protected abstract BaseTaskAddEditFragment getTaskFragment();

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                mAddEditViewModel.onUpOrBackClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDiscardChangesSelected() {
        mAddEditViewModel.onDiscardChangesSelected();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mAddEditViewModel.onDateSet(view, year, monthOfYear, dayOfMonth);
    }

    @Override
    public void onBackPressed() {
        mAddEditViewModel.onUpOrBackClick();
    }
}
