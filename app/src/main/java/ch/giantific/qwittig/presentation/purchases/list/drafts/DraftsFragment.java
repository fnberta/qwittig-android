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

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentHomeDraftsBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.common.BaseSortedListFragment;
import ch.giantific.qwittig.presentation.common.listadapters.BaseSortedListRecyclerAdapter;
import ch.giantific.qwittig.presentation.purchases.list.di.HomeSubcomponent;
import ch.giantific.qwittig.presentation.purchases.list.drafts.viewmodels.DraftsViewModel;
import ch.giantific.qwittig.presentation.purchases.list.drafts.viewmodels.items.DraftItemViewModel;

/**
 * Displays the currently open drafts of the current user in an {@link RecyclerView list.
 * <p/>
 * Long-click on a draft will start selection mode, allowing the user to select more drafts and
 * deleting them via the contextual {@link ActionBar}.
 */
public class DraftsFragment extends BaseSortedListFragment<HomeSubcomponent,
        DraftsContract.Presenter,
        DraftsFragment.ActivityListener,
        DraftItemViewModel>
        implements ActionMode.Callback, DraftsContract.ViewListener {

    @Inject
    DraftsViewModel viewModel;
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

        presenter.attachView(this);
        binding.setPresenter(presenter);
        binding.setViewModel(viewModel);
    }

    @Override
    protected void injectDependencies(@NonNull HomeSubcomponent component) {
        component.inject(this);
    }

    @Override
    protected BaseSortedListRecyclerAdapter<DraftItemViewModel, DraftsContract.Presenter,
            ? extends RecyclerView.ViewHolder> getRecyclerAdapter() {
        return new DraftsRecyclerAdapter(presenter);
    }

    @Override
    protected void setupRecyclerView() {
        binding.rvDrafts.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvDrafts.setHasFixedSize(true);
        binding.rvDrafts.setAdapter(recyclerAdapter);
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

    @Override
    public int getItemCount() {
        return recyclerAdapter.getItemCount();
    }

    @Override
    public void notifyItemChanged(int position) {
        recyclerAdapter.notifyItemChanged(position);
    }

    @Override
    public void scrollToItemPosition(int position) {
        binding.rvDrafts.post(() -> binding.rvDrafts.scrollToPosition(position));
    }

    public interface ActivityListener extends BaseFragment.ActivityListener<HomeSubcomponent> {
        ActionMode startActionMode();
    }
}
