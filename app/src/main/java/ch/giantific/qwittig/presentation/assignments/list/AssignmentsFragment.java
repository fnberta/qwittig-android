/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentAssignmentsBinding;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.assignments.list.di.AssignmentsSubcomponent;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;

/**
 * Displays a {@link RecyclerView} list of all the ongoing Assignments in a group in card base interface.
 * <p/>
 * Subclass {@link BaseRecyclerViewFragment}.
 */
public class AssignmentsFragment extends BaseRecyclerViewFragment<AssignmentsSubcomponent, AssignmentsViewModel, BaseRecyclerViewFragment.ActivityListener<AssignmentsSubcomponent>>
        implements AssignmentsViewModel.ViewListener {

    private FragmentAssignmentsBinding binding;

    public AssignmentsFragment() {
        // required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAssignmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewModel.attachView(this);
        viewModel.setListInteraction(recyclerAdapter);
        binding.setViewModel(viewModel);
    }

    @Override
    protected void injectDependencies(@NonNull AssignmentsSubcomponent component) {
        component.inject(this);
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return binding.srlRv.rvBase;
    }

    @Override
    protected BaseRecyclerAdapter getRecyclerAdapter() {
        return new AssignmentsRecyclerAdapter(viewModel);
    }

    @Override
    public String buildUpNextString(@NonNull List<Identity> identities) {
        final Identity identityResponsible = identities.get(0);
        String identitiesFormatted = "";
        if (identities.size() > 1) {
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getString(R.string.assignment_identities_next)).append(" ");
            for (Identity identity : identities) {
                if (!Objects.equals(identity, identityResponsible)) {
                    stringBuilder.append(identity.getNickname()).append(" - ");
                }
            }
            // delete last -
            final int length = stringBuilder.length();
            stringBuilder.delete(length - 3, length - 1);
            identitiesFormatted = stringBuilder.toString();
        }

        return identitiesFormatted;
    }

    @Override
    public String buildDeadlineString(@StringRes int res, Object... args) {
        return getString(res, args);
    }
}
