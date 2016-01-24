/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.CheckBox;

import com.parse.ParseConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.utils.parse.ParseConfigUtils;
import ch.giantific.qwittig.presentation.ui.adapters.StoresRecyclerAdapter;
import ch.giantific.qwittig.utils.parse.ParseUtils;

/**
 * Displays the settings screen where the user sees all stores added for his/her account and can
 * add/delete new ones or un-star existing ones.
 * <p/>
 * Long-click on a user-added store will start selection mode, allowing the user to select
 * more stores and deleting them via the contextual {@link ActionBar}.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class SettingsStoresFragment extends BaseFragment implements
        StoresRecyclerAdapter.AdapterInteractionListener,
        ActionMode.Callback {

    private static final String STATE_DRAFTS_SELECTED = "STATE_DRAFTS_SELECTED";
    private static final String STATE_ACTION_MODE = "STATE_ACTION_MODE";
    private static final String LOG_TAG = SettingsStoresFragment.class.getSimpleName();
    private ActionMode mActionMode;
    private FragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private StoresRecyclerAdapter mRecyclerAdapter;
    @NonNull
    private List<String> mStoresAdded = new ArrayList<>();
    @NonNull
    private List<String> mStoresFavorites = new ArrayList<>();
    private ArrayList<String> mStoresSelected;
    private boolean mInActionMode;
    private boolean mDeleteSelectedStores;

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

        if (savedInstanceState != null) {
            mStoresSelected = savedInstanceState.getStringArrayList(STATE_DRAFTS_SELECTED);
            mInActionMode = savedInstanceState.getBoolean(STATE_ACTION_MODE, false);
        } else {
            mStoresSelected = new ArrayList<>();
        }

        updateCurrentUserAndGroup();
        setupStoreLists();
    }

    private void setupStoreLists() {
        List<String> storeFavorites = mCurrentUser.getStoresFavorites();
        if (!storeFavorites.isEmpty()) {
            mStoresFavorites.addAll(storeFavorites);
        }

        List<String> storesAdded = mCurrentUser.getStoresAdded();
        if (!storesAdded.isEmpty()) {
            mStoresAdded.addAll(storesAdded);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putStringArrayList(STATE_DRAFTS_SELECTED, mStoresSelected);
        outState.putBoolean(STATE_ACTION_MODE, mActionMode != null);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_stores, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_stores);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerAdapter = new StoresRecyclerAdapter(getActivity(), mStoresAdded, mStoresFavorites,
                mStoresSelected, this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mInActionMode) {
            startActionMode();
            if (!mStoresSelected.isEmpty()) {
                // scroll to the first selected item
                String firstSelected = mStoresSelected.get(0);
                final int position = mStoresAdded.indexOf(firstSelected);
                // let RecyclerView layout its items first
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.scrollToPosition(
                                mRecyclerAdapter.getAdjustedStoreAddedPosition(position));
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
        mActionMode.setTitle(getString(R.string.cab_title_selected, mStoresSelected.size()));
    }

    @Override
    public void onStoreRowStarClick(int position, CheckBox checkBox) {
        boolean isChecked = checkBox.isChecked();

        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            checkBox.setChecked(!isChecked);
            return;
        }

        String store = mRecyclerAdapter.getItem(position);
        if (isChecked) {
            if (!mStoresFavorites.contains(store)) {
                mStoresFavorites.add(store);
            }
        } else if (mStoresFavorites.contains(store)) {
            mStoresFavorites.remove(store);
        }

        // update store lists in Parse database
        onStoresSet();
    }

    @Override
    public void onStoreRowClick(int position) {
        if (mActionMode != null) {
            mRecyclerAdapter.toggleSelection(position);
            setActionModeTitle();
        }
    }

    @Override
    public void onStoreRowLongClick(int position) {
        if (mActionMode == null) {
            mRecyclerAdapter.toggleSelection(position);
            startActionMode();
        }
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mListener.toggleFabVisibility();
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_cab_stores, menu);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_store_delete:
                if (ParseUtils.isTestUser(mCurrentUser)) {
                    mListener.showAccountCreateDialog();
                    return true;
                }

                mDeleteSelectedStores = true;
                mode.finish(); // Action picked, so close the CAB
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
        mRecyclerAdapter.clearSelection();
        mListener.toggleFabVisibility();
    }

    @Override
    public void onStoresSet() {
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
        List<String> defaultStores = config.getList(ParseConfigUtils.DEFAULT_STORES);
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
            mRecyclerAdapter.notifyDataSetChanged();

            // update store lists in Parse database
            onStoresSet();
        } else {
            showSnackbar(getString(R.string.toast_store_already_in_list));
        }
    }

    private void showSnackbar(@NonNull String message) {
        Snackbar.make(mRecyclerView, message, Snackbar.LENGTH_LONG).show();
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

        ActionMode startActionMode();
    }
}
