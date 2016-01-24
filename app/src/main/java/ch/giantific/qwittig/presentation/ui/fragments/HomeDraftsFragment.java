/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

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

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentHomeDraftsBinding;
import ch.giantific.qwittig.di.components.DaggerHomeComponent;
import ch.giantific.qwittig.di.modules.HomeViewModelModule;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.presentation.ui.activities.HomeActivity;
import ch.giantific.qwittig.presentation.ui.activities.PurchaseEditActivity;
import ch.giantific.qwittig.presentation.ui.adapters.DraftsRecyclerAdapter;
import ch.giantific.qwittig.presentation.viewmodels.HomeDraftsViewModel;

/**
 * Displays the currently open drafts of the current user in an {@link RecyclerView list.
 * <p/>
 * Long-click on a draft will start selection mode, allowing the user to select more drafts and
 * deleting them via the contextual {@link ActionBar}.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
 */
public class HomeDraftsFragment extends BaseRecyclerViewFragment<HomeDraftsViewModel, HomeDraftsFragment.ActivityListener>
        implements ActionMode.Callback, HomeDraftsViewModel.ViewListener {

    public static final String INTENT_PURCHASE_EDIT_DRAFT = "INTENT_PURCHASE_EDIT_DRAFT";
    private ActionMode mActionMode;
    private FragmentHomeDraftsBinding mBinding;

    public HomeDraftsFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerHomeComponent.builder()
                .homeViewModelModule(new HomeViewModelModule(savedInstanceState))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentHomeDraftsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.onReadyForSelectionMode();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == HomeActivity.INTENT_REQUEST_PURCHASE_MODIFY) {
            switch (resultCode) {
                case PurchaseEditFragment.RESULT_PURCHASE_SAVED:
                    showMessage(R.string.toast_purchase_added);
                    break;
                case PurchaseEditFragment.RESULT_PURCHASE_DISCARDED:
                    showMessage(R.string.toast_changes_discarded);
                    break;
                case PurchaseEditFragment.RESULT_PURCHASE_DRAFT:
                    showMessage(R.string.toast_changes_saved_as_draft);
                    break;
                case PurchaseEditFragment.RESULT_PURCHASE_DRAFT_DELETED:
                    showMessage(R.string.toast_draft_deleted);
                    break;
            }
        }
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
        return mBinding.rvBase;
    }

    @Override
    protected RecyclerView.Adapter getRecyclerAdapter() {
        return new DraftsRecyclerAdapter(mViewModel);
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setDraftsViewModel(mViewModel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startPurchaseEditActivity(@NonNull Purchase draft) {
        final Activity activity = getActivity();
        final Intent intent = new Intent(activity, PurchaseEditActivity.class);
        intent.putExtra(HomePurchasesFragment.INTENT_PURCHASE_ID, draft.getDraftId());
        intent.putExtra(INTENT_PURCHASE_EDIT_DRAFT, true);
        final ActivityOptionsCompat activityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        startActivityForResult(intent, HomeActivity.INTENT_REQUEST_PURCHASE_MODIFY,
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

    public interface ActivityListener extends BaseFragment.ActivityListener {
        void setDraftsViewModel(@NonNull HomeDraftsViewModel viewModel);

        ActionMode startActionMode();
    }
}
