package ch.giantific.qwittig.presentation.assignments.addedit;

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
import ch.giantific.qwittig.databinding.ActivityAssignmentAddEditBinding;
import ch.giantific.qwittig.presentation.assignments.addedit.viewmodels.AssignmentAddEditViewModel;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.TransitionListenerAdapter;
import ch.giantific.qwittig.presentation.common.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.utils.Utils;

/**
 * Created by fabio on 17.06.16.
 */
public abstract class BaseAssignmentAddEditActivity<T> extends BaseActivity<T>
        implements DatePickerDialog.OnDateSetListener,
        DiscardChangesDialogFragment.DialogInteractionListener {

    @Inject
    AssignmentAddEditContract.Presenter presenter;
    @Inject
    AssignmentAddEditViewModel viewModel;
    private ActivityAssignmentAddEditBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_assignment_add_edit);
        binding.setPresenter(presenter);
        binding.setViewModel(viewModel);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        if (savedInstanceState == null) {
            if (Utils.isRunningLollipopAndHigher()) {
                addActivityTransitionListener();
            } else {
                binding.fabAssignmentSave.show();
            }

            addFragment();
        } else {
            binding.fabAssignmentSave.show();
        }
    }

    @Override
    protected List<BasePresenter> getPresenters() {
        return Arrays.asList(new BasePresenter[]{presenter});
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(AssignmentAddEditViewModel.TAG, viewModel);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void addActivityTransitionListener() {
        Transition enter = getWindow().getEnterTransition();
        enter.addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                super.onTransitionEnd(transition);
                transition.removeListener(this);

                binding.fabAssignmentSave.show();
            }
        });
    }

    private void addFragment() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, getAssignmentFragment())
                .commit();
    }

    protected abstract BaseAssignmentAddEditFragment getAssignmentFragment();

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                presenter.onUpOrBackClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDiscardChangesSelected() {
        presenter.onDiscardChangesSelected();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        presenter.onDateSet(view, year, monthOfYear, dayOfMonth);
    }

    @Override
    public void onBackPressed() {
        presenter.onUpOrBackClick();
    }
}
