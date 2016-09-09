/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.details;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.push.FcmMessagingService;
import ch.giantific.qwittig.databinding.ActivityAssignmentDetailsBinding;
import ch.giantific.qwittig.presentation.assignments.addedit.AssignmentAddEditViewModel.AssignmentResult;
import ch.giantific.qwittig.presentation.assignments.details.di.AssignmentDetailsSubcomponent;
import ch.giantific.qwittig.presentation.assignments.details.di.AssignmentDetailsViewModelModule;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.navdrawer.BaseNavDrawerActivity;
import ch.giantific.qwittig.presentation.navdrawer.di.NavDrawerComponent;

/**
 * Hosts {@link AssignmentDetailsFragment} that shows the details of a task.
 * <p/>
 * Displays the title, the time frame and the users involved in the {@link Toolbar}.
 * <p/>
 * Subclass of {@link BaseNavDrawerActivity}.
 */
public class AssignmentDetailsActivity extends BaseNavDrawerActivity<AssignmentDetailsSubcomponent> {

    private static final String FRAGMENT_ASSIGNMENT_DETAILS = "FRAGMENT_ASSIGNMENT_DETAILS";

    @Inject
    AssignmentDetailsViewModel detailsViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityAssignmentDetailsBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_assignment_details);
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
        component = navComp.plus(new AssignmentDetailsViewModelModule(savedInstanceState,
                getAssignmentId()));
        component.inject(this);
    }

    private String getAssignmentId() {
        final Intent intent = getIntent();
        // started from AssignmentActivity
        String assignmentId = intent.getStringExtra(Navigator.INTENT_ASSIGNMENT_ID);
        if (TextUtils.isEmpty(assignmentId)) {
            // started via push notification
            assignmentId = intent.getStringExtra(FcmMessagingService.PUSH_ASSIGNMENT_ID);
        }

        return assignmentId;
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{detailsViewModel});
    }

    private void setUpNavigation() {
        final AssignmentDetailsActivity activity = this;
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(activity);
            }
        });
    }

    private void addDetailsFragment() {
        final AssignmentDetailsFragment fragment = new AssignmentDetailsFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment, FRAGMENT_ASSIGNMENT_DETAILS)
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
            case Navigator.INTENT_REQUEST_ASSIGNMENT_MODIFY:
                switch (resultCode) {
                    case AssignmentResult.DISCARDED:
                        showMessage(R.string.toast_changes_discarded);
                        break;
                    case AssignmentResult.SAVED:
                        showMessage(R.string.toast_changes_saved);
                        break;
                }
                break;
        }
    }
}
