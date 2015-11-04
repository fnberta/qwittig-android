/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.data.repositories.ParseGroupRepository;
import ch.giantific.qwittig.data.repositories.ParsePurchaseRepository;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.ui.activities.HomeActivity;
import ch.giantific.qwittig.ui.activities.PurchaseEditActivity;
import ch.giantific.qwittig.ui.adapters.DraftsAdapter;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseUtils;

/**
 * Displays the currently open drafts of the current user in an {@link ListView} list.
 * <p/>
 * Long-click on a draft will start selection mode, allowing the user to select more drafts and
 * deleting them via the contextual {@link ActionBar}.
 * <p/>
 * TODO: switch to {@link RecyclerView} and implement selection mode
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class HomeDraftsFragment extends BaseFragment implements
        PurchaseRepository.GetPurchasesLocalListener {

    public static final String INTENT_PURCHASE_EDIT_DRAFT = "INTENT_PURCHASE_EDIT_DRAFT";
    private PurchaseRepository mPurchaseRepo;
    private GroupRepository mGroupRepo;
    private TextView mTextViewEmpty;
    private ListView mListView;
    private DraftsAdapter mDraftsAdapter;
    @NonNull
    private List<ParseObject> mDrafts = new ArrayList<>();

    public HomeDraftsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateCurrentUserAndGroup();
        mPurchaseRepo = new ParsePurchaseRepository();
        mGroupRepo = new ParseGroupRepository();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_drafts, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextViewEmpty = (TextView) view.findViewById(R.id.tv_empty_view);

        mListView = (ListView) view.findViewById(R.id.lv_drafts);
        mDraftsAdapter = new DraftsAdapter(mDrafts);
        mListView.setAdapter(mDraftsAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(@NonNull ActionMode mode, int position, long id,
                                                  boolean checked) {
                mode.setTitle(getString(R.string.cab_title_selected,
                        mListView.getCheckedItemCount()));
                mDraftsAdapter.notifyDataSetChanged();
            }

            @Override
            public boolean onCreateActionMode(@NonNull ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_cab_drafts, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(@NonNull ActionMode mode, @NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_draft_delete:
                        deleteSelectedDrafts();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editPurchaseDraft(position);
            }
        });
    }

    private void editPurchaseDraft(int position) {
        Purchase draft = (Purchase) mDrafts.get(position);

        Intent intent = new Intent(getActivity(), PurchaseEditActivity.class);
        intent.putExtra(HomePurchasesFragment.INTENT_PURCHASE_ID, draft.getDraftId());
        intent.putExtra(INTENT_PURCHASE_EDIT_DRAFT, true);
        ActivityOptionsCompat activityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
        startActivityForResult(intent, HomeActivity.INTENT_REQUEST_PURCHASE_MODIFY,
                activityOptionsCompat.toBundle());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == HomeActivity.INTENT_REQUEST_PURCHASE_MODIFY) {
            switch (resultCode) {
                case PurchaseEditFragment.RESULT_PURCHASE_SAVED:
                    MessageUtils.showBasicSnackbar(mListView, getString(R.string.toast_purchase_added));
                    break;
                case PurchaseEditFragment.RESULT_PURCHASE_DISCARDED:
                    MessageUtils.showBasicSnackbar(mListView, getString(R.string.toast_changes_discarded));
                    break;
                case PurchaseEditFragment.RESULT_PURCHASE_DRAFT:
                    MessageUtils.showBasicSnackbar(mListView, getString(R.string.toast_changes_saved_as_draft));
                    break;
                case PurchaseEditFragment.RESULT_PURCHASE_DRAFT_DELETED:
                    MessageUtils.showBasicSnackbar(mListView, getString(R.string.toast_draft_deleted));
                    break;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        updateAdapter();
    }

    /**
     * Re-queries the data and loads it in the adapter.
     */
    public void updateAdapter() {
        mPurchaseRepo.getPurchasesLocalAsync(mCurrentUser, true, this);
    }

    @Override
    public void onPurchasesLocalLoaded(@NonNull List<ParseObject> purchases) {
        mDrafts.clear();

        if (!purchases.isEmpty()) {
            mDrafts.addAll(purchases);
        }

        checkCurrentGroup();
    }

    private void checkCurrentGroup() {
        if (mCurrentGroup != null) {
            if (mCurrentGroup.isDataAvailable()) {
                updateView();
            } else {
                mGroupRepo.fetchGroupDataAsync(mCurrentGroup, new GroupRepository.GetGroupLocalListener() {
                    @Override
                    public void onGroupLocalLoaded(@NonNull Group group) {
                        updateView();
                    }
                });
            }
        } else {
            updateView();
        }
    }

    private void updateView() {
        mDraftsAdapter.setCurrentGroupCurrency(ParseUtils.getGroupCurrencyWithFallback(mCurrentGroup));
        mDraftsAdapter.notifyDataSetChanged();
        toggleEmptyViewVisibility();
    }

    private void toggleEmptyViewVisibility() {
        if (mDrafts.isEmpty()) {
            mTextViewEmpty.setVisibility(View.VISIBLE);
        } else {
            mTextViewEmpty.setVisibility(View.GONE);
        }
    }

    private void deleteSelectedDrafts() {
        SparseBooleanArray checkedItemPositions = mListView.getCheckedItemPositions();
        int draftsSize = mDrafts.size();
        for (int i = draftsSize - 1; i >= 0; i--) {
            if (checkedItemPositions.get(i)) {
                Purchase purchase = (Purchase) mDrafts.get(i);
                purchase.unpinInBackground();
                mDrafts.remove(i);
            }
        }

        toggleEmptyViewVisibility();
        mDraftsAdapter.notifyDataSetChanged();
    }

    /**
     * Updates the current user and current group fields and tells the {@link RecyclerView} adapter
     * to reload its data.
     */
    public void updateFragment() {
        updateCurrentUserAndGroup();
        updateAdapter();
    }
}
