/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.ParsePurchaseRepository;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.ui.activities.HomeActivity;
import ch.giantific.qwittig.ui.activities.PurchaseEditActivity;
import ch.giantific.qwittig.ui.adapters.DraftsRecyclerAdapter;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseUtils;

/**
 * Displays the currently open drafts of the current user in an {@link RecyclerView list.
 * <p/>
 * Long-click on a draft will start selection mode, allowing the user to select more drafts and
 * deleting them via the contextual {@link ActionBar}.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
 */
public class HomeDraftsFragment extends BaseRecyclerViewFragment implements
        ActionMode.Callback,
        PurchaseRepository.GetPurchasesLocalListener,
        DraftsRecyclerAdapter.AdapterInteractionListener {

    public static final String INTENT_PURCHASE_EDIT_DRAFT = "INTENT_PURCHASE_EDIT_DRAFT";
    private static final String STATE_DRAFTS_SELECTED = "STATE_DRAFTS_SELECTED";
    private static final String STATE_ACTION_MODE = "STATE_ACTION_MODE";
    private PurchaseRepository mDraftsRepo;
    @NonNull
    private List<ParseObject> mDrafts = new ArrayList<>();
    private DraftsRecyclerAdapter mRecyclerAdapter;
    private boolean mDeleteSelectedItems;
    private ActionMode mActionMode;
    private FragmentInteractionListener mListener;
    private ArrayList<String> mDraftsSelected;
    private boolean mInActionMode;

    public HomeDraftsFragment() {
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mDraftsSelected = savedInstanceState.getStringArrayList(STATE_DRAFTS_SELECTED);
            mInActionMode = savedInstanceState.getBoolean(STATE_ACTION_MODE, false);
        } else {
            mDraftsSelected = new ArrayList<>();
        }

        mDraftsRepo = new ParsePurchaseRepository();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putStringArrayList(STATE_DRAFTS_SELECTED, mDraftsSelected);
        outState.putBoolean(STATE_ACTION_MODE, mActionMode != null);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_drafts, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new DraftsRecyclerAdapter(mDrafts, mDraftsSelected, this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mInActionMode) {
            startActionMode();
            if (!mDraftsSelected.isEmpty()) {
                // scroll to the first selected item
                String firstSelected = mDraftsSelected.get(0);
                int posFirst = 0;
                for (int i = 0, draftsSize = mDrafts.size(); i < draftsSize; i++) {
                    final Purchase draft = (Purchase) mDrafts.get(i);
                    if (firstSelected.equals(draft.getDraftId())) {
                        posFirst = i;
                        break;
                    }
                }

                // let RecyclerView layout its items first
                final int position = posFirst;
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.scrollToPosition(position);
                    }
                });
            }
        }
    }

    private void startActionMode() {
        mActionMode = mListener.startActionMode();
        setActionModeTitle();
    }

    private void setActionModeTitle() {
        mActionMode.setTitle(getString(R.string.cab_title_selected, mDraftsSelected.size()));
    }

    /**
     * Re-queries the data and loads it in the adapter.
     */
    public void updateAdapter() {
        mDraftsRepo.getPurchasesLocalAsync(mCurrentUser, true, this);
    }

    @Override
    public void onPurchasesLocalLoaded(@NonNull List<ParseObject> purchases) {
        mDrafts.clear();

        if (!purchases.isEmpty()) {
            mDrafts.addAll(purchases);
        }

        checkCurrentGroup();
    }

    @Override
    protected void updateView() {
        mRecyclerAdapter.setCurrentGroupCurrency(
                ParseUtils.getGroupCurrencyWithFallback(mCurrentGroup));
        mRecyclerAdapter.notifyDataSetChanged();
        showMainView();
    }

    @Override
    protected void toggleEmptyViewVisibility() {
        if (mDrafts.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDraftRowClick(int position) {
        if (mActionMode == null) {
            Purchase draft = (Purchase) mDrafts.get(position);

            Intent intent = new Intent(getActivity(), PurchaseEditActivity.class);
            intent.putExtra(HomePurchasesFragment.INTENT_PURCHASE_ID, draft.getDraftId());
            intent.putExtra(INTENT_PURCHASE_EDIT_DRAFT, true);
            ActivityOptionsCompat activityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
            startActivityForResult(intent, HomeActivity.INTENT_REQUEST_PURCHASE_MODIFY,
                    activityOptionsCompat.toBundle());
        } else {
            mRecyclerAdapter.toggleSelection(position);
            setActionModeTitle();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == HomeActivity.INTENT_REQUEST_PURCHASE_MODIFY) {
            switch (resultCode) {
                case PurchaseEditFragment.RESULT_PURCHASE_SAVED:
                    MessageUtils.showBasicSnackbar(mRecyclerView, getString(R.string.toast_purchase_added));
                    break;
                case PurchaseEditFragment.RESULT_PURCHASE_DISCARDED:
                    MessageUtils.showBasicSnackbar(mRecyclerView, getString(R.string.toast_changes_discarded));
                    break;
                case PurchaseEditFragment.RESULT_PURCHASE_DRAFT:
                    MessageUtils.showBasicSnackbar(mRecyclerView, getString(R.string.toast_changes_saved_as_draft));
                    break;
                case PurchaseEditFragment.RESULT_PURCHASE_DRAFT_DELETED:
                    MessageUtils.showBasicSnackbar(mRecyclerView, getString(R.string.toast_draft_deleted));
                    break;
            }
        }
    }

    @Override
    public void onDraftRowLongClick(int position) {
        if (mActionMode == null) {
            mRecyclerAdapter.toggleSelection(position);
            startActionMode();
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
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
                mDeleteSelectedItems = true;
                mode.finish(); // Action picked, so close the CAB
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
        mRecyclerAdapter.clearSelection(mDeleteSelectedItems);
        mDeleteSelectedItems = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentInteractionListener extends BaseFragmentInteractionListener {
        ActionMode startActionMode();
    }
}
