/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.util.ArrayList;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;

/**
 * Created by fabio on 10.01.16.
 */
public abstract class ListViewModelBaseImpl<T, S extends ListViewModel.ViewListener>
        extends ViewModelBaseImpl<S>
        implements ListViewModel<T> {

    private static final String STATE_LOADING = "STATE_LOADING";

    ArrayList<T> mItems;
    GroupRepository mGroupRepo;
    boolean mLoading;

    public ListViewModelBaseImpl(@Nullable Bundle savedState,
                                 @NonNull S view,
                                 @NonNull GroupRepository groupRepo,
                                 @NonNull UserRepository userRepository) {
        super(savedState, view, userRepository);

        mGroupRepo = groupRepo;

        if (savedState != null) {
            mLoading = (savedState.getBoolean(STATE_LOADING, false));
        } else {
            mLoading = true;
            mItems = new ArrayList<>();
        }
    }

    @VisibleForTesting
    public void setItems(ArrayList<T> items) {
        mItems = items;
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_LOADING, mLoading);
    }

    @Override
    public void onStart() {
        super.onStart();

        loadData();
    }

    @Override
    @Bindable
    public boolean isEmpty() {
        return mItems.isEmpty();
    }

    @Override
    @Bindable
    public boolean isLoading() {
        return mLoading;
    }

    @Override
    public void setLoading(boolean loading) {
        mLoading = loading;
        notifyPropertyChanged(BR.loading);
    }

    @Override
    public User getCurrentUser() {
        return mCurrentUser;
    }

    @Override
    public T getItemAtPosition(int position) {
        return mItems.get(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getLastPosition() {
        return getItemCount() - 1;
    }

    @Override
    public void onNewGroupSet() {
        super.onNewGroupSet();

        loadData();
    }
}
