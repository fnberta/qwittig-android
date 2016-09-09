/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.list;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.ActivityAssignmentsBinding;
import ch.giantific.qwittig.presentation.assignments.addedit.AssignmentAddEditViewModel.AssignmentResult;
import ch.giantific.qwittig.presentation.assignments.details.AssignmentDetailsViewModel.AssignmentDetailsResult;
import ch.giantific.qwittig.presentation.assignments.list.di.AssignmentsSubcomponent;
import ch.giantific.qwittig.presentation.assignments.list.di.AssignmentsViewModelModule;
import ch.giantific.qwittig.presentation.assignments.list.models.AssignmentDeadline;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.navdrawer.BaseNavDrawerActivity;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerComponent;

/**
 * Hosts {@link AssignmentsFragment} that displays a list of recent assignments. Only loads the
 * fragment if the user is logged in.
 * <p/>
 * Allows the user to change the tasks display by changing the value of the spinner in the
 * {@link Toolbar}.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class AssignmentsActivity extends BaseNavDrawerActivity<AssignmentsSubcomponent> {

    @Inject
    AssignmentsViewModel assignmentsViewModel;
    private ActivityAssignmentsBinding binding;
    private AssignmentDeadline[] deadlines;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_assignments);
        binding.setViewModel(assignmentsViewModel);

        checkNavDrawerItem(R.id.nav_assignments);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        showFab();
        setupDeadlineSpinner();

        if (userLoggedIn && savedInstanceState == null) {
            addAssignmentsFragment();
        }
    }

    @Override
    protected void injectDependencies(@NonNull NavDrawerComponent navComp,
                                      @Nullable Bundle savedInstanceState) {
        deadlines = new AssignmentDeadline[]{
                AssignmentDeadline.newAllInstance(getString(R.string.deadline_all)),
                AssignmentDeadline.newTodayInstance(getString(R.string.deadline_today)),
                AssignmentDeadline.newWeekInstance(getString(R.string.deadline_week)),
                AssignmentDeadline.newMonthInstance(getString(R.string.deadline_month)),
                AssignmentDeadline.newYearInstance(getString(R.string.deadline_year))
        };

        component = navComp.plus(new AssignmentsViewModelModule(savedInstanceState, deadlines[0]));
        component.inject(this);
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{assignmentsViewModel});
    }

    private void showFab() {
        if (ViewCompat.isLaidOut(binding.fabAssignmentAdd)) {
            binding.fabAssignmentAdd.show();
        } else {
            binding.fabAssignmentAdd.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(@NonNull View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    binding.fabAssignmentAdd.show();
                }
            });
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setupDeadlineSpinner() {
        final ActionBar actionBar = getSupportActionBar();
        final Context themedContext = actionBar.getThemedContext();
        final ArrayAdapter<AssignmentDeadline> adapter =
                new ArrayAdapter<>(themedContext, R.layout.spinner_item_toolbar, deadlines);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spTasksDeadline.setAdapter(adapter);
    }

    private void addAssignmentsFragment() {
        final AssignmentsFragment assignmentsFragment = new AssignmentsFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, assignmentsFragment)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Navigator.INTENT_REQUEST_ASSIGNMENT_DETAILS:
                switch (resultCode) {
                    case AssignmentDetailsResult.DELETED:
                        assignmentsViewModel.onAssignmentDeleted(data.getStringExtra(Navigator.INTENT_OBJECT_ID));
                        break;
                }
                break;
            case Navigator.INTENT_REQUEST_ASSIGNMENT_NEW:
                switch (resultCode) {
                    case AssignmentResult.SAVED:
                        showMessage(R.string.toast_assignment_added_new);
                        break;
                    case AssignmentResult.DISCARDED:
                        showMessage(R.string.toast_assignment_discarded);
                        break;
                }
                break;
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_assignments;
    }

    @Override
    public void setupScreenAfterLogin() {
        super.setupScreenAfterLogin();

        addAssignmentsFragment();
    }
}
