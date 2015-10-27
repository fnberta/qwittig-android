/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import ch.giantific.qwittig.ui.adapters.StoresAdapter;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseUtils;

/**
 * Displays the settings screen where the user sees all stores added for his/her account and can
 * add/delete new ones or un-star existing ones.
 * <p/>
 * Long-click on a user-added drafstoret will start selection mode, allowing the user to select
 * more stores and deleting them via the contextual {@link ActionBar}.
 * <p/>
 * TODO: switch to {@link RecyclerView} and implement selection mode.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class SettingsStoresFragment extends BaseFragment {

    private FragmentInteractionListener mListener;
    private StoresAdapter mStoresAdapter;
    private ListView mListView;
    @NonNull
    private List<String> mStoresAdded = new ArrayList<>();
    @NonNull
    private List<String> mStoresFavorites = new ArrayList<>();
    private User mCurrentUser;

    public SettingsStoresFragment() {
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_stores, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView = (ListView) view.findViewById(R.id.lv_stores);
        mStoresAdapter = new StoresAdapter(getActivity(), mStoresAdded, mStoresFavorites);
        mListView.setAdapter(mStoresAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(@NonNull ActionMode mode, int position, long id,
                                                  boolean checked) {
                mode.setTitle(getString(R.string.cab_title_selected,
                        mListView.getCheckedItemCount()));
                mStoresAdapter.notifyDataSetChanged();
            }

            @Override
            public boolean onCreateActionMode(@NonNull ActionMode mode, Menu menu) {
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
            public boolean onActionItemClicked(@NonNull ActionMode mode, @NonNull MenuItem item) {
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
            public void onItemClick(AdapterView<?> parent, @NonNull View view, int position, long id) {
                long viewId = view.getId();

                if (viewId == R.id.cb_store_favorite) {
                    onStoreChecked(position, ((CheckBox) view));
                }
            }
        });
    }

    private void deleteSelectedStores() {
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
     * Adds entered store to the list if is not empty and not already in the list and saves it to
     * the online Parse.com database.
     *
     * @param store the store to add
     */
    public void onNewStoreSet(@NonNull String store) {
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

    private void onStoreChecked(int position, @NonNull CheckBox checkBox) {
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

    private void showSnackbar(@NonNull String message) {
        MessageUtils.showBasicSnackbar(mListView, message);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     * <p/>
     * Extends {@link BaseFragmentInteractionListener}.
     */
    public interface FragmentInteractionListener extends BaseFragmentInteractionListener {
        /**
         * Handles the visibility change of the {@link FloatingActionButton}.
         */
        void toggleFabVisibility();
    }
}
