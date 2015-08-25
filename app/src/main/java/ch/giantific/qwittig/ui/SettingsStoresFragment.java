package ch.giantific.qwittig.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.CheckBox;
import android.widget.ListView;

import com.parse.ParseConfig;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.models.Config;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.adapter.StoresAdapter;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsStoresFragment extends Fragment {

    private FragmentInteractionListener mListener;
    private StoresAdapter mStoresAdapter;
    private ListView mListView;
    private List<String> mStoresAdded = new ArrayList<>();
    private List<String> mStoresFavorites = new ArrayList<>();
    private User mCurrentUser;

    public SettingsStoresFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurrentUser = (User) ParseUser.getCurrentUser();
        setupStoreList();
    }

    private void setupStoreList() {
        List<String> storeFavorites = mCurrentUser.getStoresFavorites();
        if (!storeFavorites.isEmpty()) {
            for (String store : storeFavorites) {
                mStoresFavorites.add(store);
            }
        }

        List<String> storesAdded = mCurrentUser.getStoresAdded();
        if (!storesAdded.isEmpty()) {
            for (String store : storesAdded) {
                mStoresAdded.add(store);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_stores, container, false);

        mListView = (ListView) rootView.findViewById(R.id.lv_stores);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStoresAdapter = new StoresAdapter(getActivity(), R.layout.row_stores, mStoresAdded,
                mStoresFavorites);
        mListView.setAdapter(mStoresAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                                                  boolean checked) {
                mode.setTitle(getString(R.string.cab_title_selected,
                        mListView.getCheckedItemCount()));
                mStoresAdapter.notifyDataSetChanged();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mListener.toggleFabVisibility();

                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_cab_stores, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_store_delete:
                        deleteSelectedStores();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mListener.toggleFabVisibility();
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long viewId = view.getId();

                if (viewId == R.id.cb_store_favorite) {
                    onStoreChecked(position, ((CheckBox) view));
                }
            }
        });
    }

    public void deleteSelectedStores() {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        // remove selected stores from list
        SparseBooleanArray checkedItemPositions = mListView.getCheckedItemPositions();
        int storesSize = mStoresAdded.size();
        for (int i = storesSize - 1; i >= 0; i--) {
            int adjustedPosition = mStoresAdapter.getAdjustedStoreAddedPosition(i);
            if (checkedItemPositions.get(adjustedPosition)) {
                String store = (String) mListView.getItemAtPosition(adjustedPosition);
                mStoresAdded.remove(store);
                if (mStoresFavorites.contains(store)) {
                    mStoresFavorites.remove(store);
                }
            }
        }
        mStoresAdapter.notifyDataSetChanged();

        // update store lists in Parse database
        saveStoreListInParse();
    }

    private void saveStoreListInParse() {
        mCurrentUser.setStoresAdded(mStoresAdded);
        mCurrentUser.setStoresFavorites(mStoresFavorites);
        mCurrentUser.saveEventually();
    }

    /**
     * Called from activity method which in turn is called from add store dialog. Adds entered
     * store to list if is not empty and not already in the list.
     *
     * @param store
     */
    public void addStoreToList(String store) {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        ParseConfig config = ParseConfig.getCurrentConfig();
        List<String> defaultStores = config.getList(Config.DEFAULT_STORES);
        boolean storeIsNew = true;

        for (String defaultStore : defaultStores) {
            if (store.equalsIgnoreCase(defaultStore)) {
                storeIsNew = false;
            }
        }

        for (String addedStore : mStoresAdded) {
            if (store.equalsIgnoreCase(addedStore)) {
                storeIsNew = false;
            }
        }

        if (storeIsNew) {
            mStoresAdded.add(store);
            Collections.sort(mStoresAdded, String.CASE_INSENSITIVE_ORDER);
            mStoresFavorites.add(store);
            mStoresAdapter.notifyDataSetChanged();

            // update store lists in Parse database
            saveStoreListInParse();
        } else {
            showSnackbar(getString(R.string.toast_store_already_in_list));
        }
    }

    private void onStoreChecked(int position, CheckBox checkBox) {
        boolean isChecked = checkBox.isChecked();

        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            checkBox.setChecked(!isChecked);
            return;
        }

        String store = (String) mListView.getItemAtPosition(position);
        if (isChecked) {
            if (!mStoresFavorites.contains(store)) {
                mStoresFavorites.add(store);
            }
        } else if (mStoresFavorites.contains(store)) {
            mStoresFavorites.remove(store);
        }

        // update store lists in Parse database
        saveStoreListInParse();
    }

    private void showSnackbar(String message) {
        MessageUtils.showBasicSnackbar(getView(), message);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentInteractionListener {
        void toggleFabVisibility();

        void showAccountCreateDialog();
    }
}
