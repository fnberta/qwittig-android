/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.details;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
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

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentTaskDetailsBinding;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;
import ch.giantific.qwittig.presentation.tasks.details.di.TaskDetailsSubcomponent;

/**
 * Shows the details of a {@link Task}. Most of the information gets displayed in the
 * {@link Toolbar} of the hosting {@link Activity}. The fragment itself shows a list of users that
 * have previously finished the task.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class TaskDetailsFragment extends BaseRecyclerViewFragment<TaskDetailsSubcomponent, TaskDetailsViewModel, BaseFragment.ActivityListener<TaskDetailsSubcomponent>>
        implements TaskDetailsViewModel.ViewListener {

    private FragmentTaskDetailsBinding mBinding;
    private boolean mShowEditOptions;

    public TaskDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentTaskDetailsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.attachView(this);
        mViewModel.setListInteraction(mRecyclerAdapter);
        mBinding.setViewModel(mViewModel);
    }

    @Override
    protected void injectDependencies(@NonNull TaskDetailsSubcomponent component) {
        component.inject(this);
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.rvTaskDetailsHistory;
    }

    @Override
    protected BaseRecyclerAdapter getRecyclerAdapter() {
        return new TaskHistoryRecyclerAdapter(mViewModel);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_task_details, menu);
        if (mShowEditOptions) {
            menu.findItem(R.id.action_task_edit).setVisible(true);
            menu.findItem(R.id.action_task_delete).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_task_delete:
                mViewModel.deleteTask();
                return true;
            case R.id.action_task_edit:
                mViewModel.editTask();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void startPostponedEnterTransition() {
        ActivityCompat.startPostponedEnterTransition(getActivity());
    }

    @Override
    public void toggleEditOptions(boolean showOptions) {
        mShowEditOptions = showOptions;
        getActivity().invalidateOptionsMenu();
    }

    @NonNull
    @Override
    public SpannableStringBuilder buildTaskIdentitiesString(@NonNull List<Identity> identities,
                                                            @NonNull Identity identityResponsible) {
        final SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        final int usersInvolvedSize = identities.size();
        stringBuilder.append(identityResponsible.getNickname());

        if (usersInvolvedSize > 1) {
            final int spanEnd = stringBuilder.length();
            stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, spanEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            for (int i = 1; i < usersInvolvedSize; i++) {
                final Identity identity = identities.get(i);
                stringBuilder.append(" - ").append(identity.getNickname());
            }
        }

        return stringBuilder;
    }
}
