/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.details;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentAssignmentDetailsBinding;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.assignments.details.di.AssignmentDetailsSubcomponent;
import ch.giantific.qwittig.presentation.assignments.details.viewmodels.AssignmentDetailsViewModel;
import ch.giantific.qwittig.presentation.assignments.details.viewmodels.items.AssignmentDetailsHeaderItemViewModel;
import ch.giantific.qwittig.presentation.assignments.details.viewmodels.items.AssignmentDetailsHistoryItemViewModel;
import ch.giantific.qwittig.presentation.common.BaseFragment;

/**
 * Shows the details of a task. Most of the information gets displayed in the
 * {@link Toolbar} of the hosting {@link Activity}. The fragment itself shows a list of users that
 * have previously finished the task.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class AssignmentDetailsFragment extends BaseFragment<AssignmentDetailsSubcomponent,
        AssignmentDetailsContract.Presenter,
        BaseFragment.ActivityListener<AssignmentDetailsSubcomponent>>
        implements AssignmentDetailsContract.ViewListener {

    @Inject
    AssignmentDetailsViewModel viewModel;
    private FragmentAssignmentDetailsBinding binding;
    private AssignmentHistoryRecyclerAdapter recyclerAdapter;

    public AssignmentDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAssignmentDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupRecyclerView();
        presenter.attachView(this);
        binding.setViewModel(viewModel);
    }

    @Override
    protected void injectDependencies(@NonNull AssignmentDetailsSubcomponent component) {
        component.inject(this);
    }

    private void setupRecyclerView() {
        binding.rvAssignmentDetailsHistory.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvAssignmentDetailsHistory.setHasFixedSize(true);
        recyclerAdapter = new AssignmentHistoryRecyclerAdapter();
        binding.rvAssignmentDetailsHistory.setAdapter(recyclerAdapter);
    }

    @Override
    protected View getSnackbarView() {
        return binding.rvAssignmentDetailsHistory;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_assignment_details, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_assignment_delete:
                presenter.onDeleteAssignmentMenuClick();
                return true;
            case R.id.action_assignment_edit:
                presenter.onEditAssignmentMenuClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void startPostponedEnterTransition() {
        ActivityCompat.startPostponedEnterTransition(getActivity());
    }

    @NonNull
    @Override
    public SpannableStringBuilder buildIdentitiesString(@NonNull List<Identity> identities) {
        final SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        final int size = identities.size();
        final Identity responsible = identities.get(0);
        stringBuilder.append(responsible.getNickname());

        if (size > 1) {
            final int spanEnd = stringBuilder.length();
            stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, spanEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            for (int i = 1; i < size; i++) {
                final Identity identity = identities.get(i);
                stringBuilder.append(" - ").append(identity.getNickname());
            }
        }

        return stringBuilder;
    }

    @Override
    public void addItem(@NonNull AssignmentDetailsHeaderItemViewModel itemViewModel) {
        recyclerAdapter.addItem(itemViewModel);
    }

    @Override
    public void addItems(@NonNull List<AssignmentDetailsHistoryItemViewModel> itemViewModels) {
        recyclerAdapter.addItems(itemViewModels);
    }

    @Override
    public void clearItems() {
        recyclerAdapter.clearItems();
    }

    @Override
    public void notifyItemsChanged() {
        recyclerAdapter.notifyDataSetChanged();
    }
}
