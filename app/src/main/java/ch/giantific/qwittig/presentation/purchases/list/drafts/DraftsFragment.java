/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.drafts;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentHomeDraftsBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.purchases.list.di.HomeSubcomponent;

/**
 * Displays the currently open drafts of the current user in an {@link RecyclerView list.
 * <p/>
 * Long-click on a draft will start selection mode, allowing the user to select more drafts and
 * deleting them via the contextual {@link ActionBar}.
 */
public class DraftsFragment extends BaseFragment<HomeSubcomponent, DraftsContract.Presenter, DraftsFragment.ActivityListener>
        implements ActionMode.Callback, DraftsContract.ViewListener {

    private FragmentHomeDraftsBinding binding;
    private ActionMode actionMode;

    public DraftsFragment() {
        // required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeDraftsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final DraftsRecyclerAdapter adapter = setupRecyclerView();
        presenter.attachView(this);
        presenter.setListInteraction(adapter);
        binding.setPresenter(presenter);
        binding.setViewModel(presenter.getViewModel());
    }

    @Override
    protected void injectDependencies(@NonNull HomeSubcomponent component) {
        component.inject(this);
    }

    private DraftsRecyclerAdapter setupRecyclerView() {
        binding.rvDrafts.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvDrafts.setHasFixedSize(true);
        final DraftsRecyclerAdapter adapter = new DraftsRecyclerAdapter(presenter);
        binding.rvDrafts.setAdapter(adapter);

        return adapter;
    }

    @Override
    protected View getSnackbarView() {
        return binding.rvDrafts;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        final MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_cab_drafts, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_draft_delete:
                presenter.onDeleteSelectedDraftsClick();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        presenter.onSelectionModeEnded();
    }

    @Override
    public void startSelectionMode() {
        actionMode = activity.startActionMode();
    }

    @Override
    public void stopSelectionMode() {
        actionMode.finish();
    }

    @Override
    public void setSelectionModeTitle(@StringRes int title, int draftsSelected) {
        actionMode.setTitle(getString(title, draftsSelected));
    }

    public interface ActivityListener extends BaseFragment.ActivityListener<HomeSubcomponent> {
        ActionMode startActionMode();
    }
}
