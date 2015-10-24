package ch.giantific.qwittig.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
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
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.activities.HomeActivity;
import ch.giantific.qwittig.ui.activities.PurchaseEditActivity;
import ch.giantific.qwittig.ui.adapters.DraftsAdapter;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class HomeDraftsFragment extends BaseFragment implements
        LocalQuery.PurchaseLocalQueryListener,
        LocalQuery.ObjectLocalFetchListener {

    public static final String INTENT_PURCHASE_EDIT_DRAFT = "INTENT_PURCHASE_EDIT_DRAFT";
    private User mCurrentUser;
    private Group mCurrentGroup;
    private TextView mTextViewEmpty;
    private ListView mListView;
    private DraftsAdapter mDraftsAdapter;
    private List<ParseObject> mDrafts = new ArrayList<>();

    public HomeDraftsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_drafts, container, false);

        mTextViewEmpty = (TextView) rootView.findViewById(R.id.tv_empty_view);
        mListView = (ListView) rootView.findViewById(R.id.lv_drafts);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDraftsAdapter = new DraftsAdapter(R.layout.row_drafts, mDrafts);
        mListView.setAdapter(mDraftsAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                                                  boolean checked) {
                mode.setTitle(getString(R.string.cab_title_selected,
                        mListView.getCheckedItemCount()));
                mDraftsAdapter.notifyDataSetChanged();
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

    public void updateAdapter() {
        updateCurrentUserGroup();
        if (mCurrentUser != null) {
            LocalQuery.queryDrafts(this);
        }
    }

    private void updateCurrentUserGroup() {
        mCurrentUser = (User) ParseUser.getCurrentUser();
        if (mCurrentUser != null) {
            mCurrentGroup = mCurrentUser.getCurrentGroup();
        }
    }

    @Override
    public void onPurchasesLocalQueried(List<ParseObject> purchases) {
        updateDrafts(purchases);
    }

    private void updateDrafts(List<ParseObject> drafts) {
        mDrafts.clear();

        if (!drafts.isEmpty()) {
            mDrafts.addAll(drafts);
        }

        checkCurrentGroup();
    }

    private void checkCurrentGroup() {
        if (mCurrentGroup != null) {
            if (mCurrentGroup.isDataAvailable()) {
                updateView();
            } else {
                LocalQuery.fetchObjectData(this, mCurrentGroup);
            }
        } else {
            updateView();
        }
    }

    @Override
    public void onObjectFetched(ParseObject object) {
        updateView();
    }

    private void updateView() {
        mDraftsAdapter.setCurrentGroupCurrency(ParseUtils.getGroupCurrency());
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
}
