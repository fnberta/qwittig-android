/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.list;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentHomeDraftsBinding;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;
import ch.giantific.qwittig.presentation.home.HomeActivity;
import ch.giantific.qwittig.presentation.home.purchases.addedit.EditPurchaseActivity;
import ch.giantific.qwittig.presentation.home.purchases.list.di.DaggerDraftsListComponent;
import ch.giantific.qwittig.presentation.home.purchases.list.di.DraftsListViewModelModule;

/**
 * Displays the currently open drafts of the current user in an {@link RecyclerView list.
 * <p/>
 * Long-click on a draft will start selection mode, allowing the user to select more drafts and
 * deleting them via the contextual {@link ActionBar}.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
 */
public class DraftsFragment extends BaseRecyclerViewFragment<DraftsViewModel, DraftsFragment.ActivityListener>
        implements ActionMode.Callback, DraftsViewModel.ViewListener {

    public static final String INTENT_PURCHASE_EDIT_DRAFT = "INTENT_PURCHASE_EDIT_DRAFT";
    private ActionMode mActionMode;
    private FragmentHomeDraftsBinding mBinding;

    public DraftsFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerDraftsListComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(getActivity()))
                .draftsListViewModelModule(new DraftsListViewModelModule(savedInstanceState, this))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentHomeDraftsBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.onReadyForSelectionMode();
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
                mViewModel.onDeleteSelectedDraftsClick();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mViewModel.onSelectionModeEnded();
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.rvDrafts;
    }

    @Override
    protected RecyclerView.Adapter getRecyclerAdapter() {
        return new DraftsRecyclerAdapter(mViewModel);
    }

    @Override
    protected void setViewModelToActivity() {
        // do nothing
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startPurchaseEditActivity(@NonNull Purchase draft) {
        final Activity activity = getActivity();
        final Intent intent = new Intent(activity, EditPurchaseActivity.class);
        intent.putExtra(PurchasesFragment.INTENT_PURCHASE_ID, draft.getTempId());
        intent.putExtra(INTENT_PURCHASE_EDIT_DRAFT, true);
        final ActivityOptionsCompat activityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        activity.startActivityForResult(intent, HomeActivity.INTENT_REQUEST_PURCHASE_MODIFY,
                activityOptionsCompat.toBundle());
    }

    @Override
    public void startSelectionMode() {
        mActionMode = mActivity.startActionMode();
    }

    @Override
    public void stopSelectionMode() {
        mActionMode.finish();
    }

    @Override
    public void setSelectionModeTitle(@StringRes int title, int draftsSelected) {
        mActionMode.setTitle(getString(title, draftsSelected));
    }

    @Override
    public void scrollToPosition(final int position) {
        // override to let RecyclerView layout its items first
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.scrollToPosition(position);
            }
        });
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener {
        ActionMode startActionMode();
    }
}
