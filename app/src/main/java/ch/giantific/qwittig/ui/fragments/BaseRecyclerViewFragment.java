/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.ParseGroupRepository;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.repositories.GroupRepository;

/**
 * Provides a an abstract base class for screens with a {@link RecyclerView} and shows a progress
 * bar when loading and an empty view if no items are available.
 * <p/>
 * Subclass of {@link BaseFragment}.
 *
 * @see RecyclerView
 * @see ProgressBar
 */
public abstract class BaseRecyclerViewFragment extends BaseFragment {

    RecyclerView mRecyclerView;
    View mEmptyView;
    private ContentLoadingProgressBar mProgressBarLoading;
    private GroupRepository mGroupRepository;

    public BaseRecyclerViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateCurrentUserAndGroup();
        mGroupRepository = new ParseGroupRepository();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressBarLoading = (ContentLoadingProgressBar) view.findViewById(R.id.pb_base);
        mEmptyView = view.findViewById(R.id.empty_view);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_base);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        updateAdapter();
    }

    protected abstract void updateAdapter();

    final void checkCurrentGroup() {
        if (mCurrentGroup != null) {
            if (mCurrentGroup.isDataAvailable()) {
                updateView();
            } else {
                mGroupRepository.fetchGroupDataAsync(mCurrentGroup, new GroupRepository.GetGroupLocalListener() {
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

    protected abstract void updateView();

    /**
     * Hides the loading {@link ProgressBar} and displays the {@link RecyclerView}.
     */
    @CallSuper
    void showMainView() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mProgressBarLoading.hide();
        toggleEmptyViewVisibility();
    }

    protected abstract void toggleEmptyViewVisibility();

    /**
     * Updates the current user and current group fields and tells the {@link RecyclerView} adapter
     * to reload its data.
     */
    public void updateFragment() {
        updateCurrentUserAndGroup();
        updateAdapter();
    }
}
