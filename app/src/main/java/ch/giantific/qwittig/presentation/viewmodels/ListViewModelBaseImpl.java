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
import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import rx.functions.Action1;

/**
 * Created by fabio on 10.01.16.
 */
public abstract class ListViewModelBaseImpl<T, S extends ListViewModel.ViewListener> extends
        ViewModelBaseImpl<S> implements
        ListViewModel<T, S> {

    private static final String STATE_LOADING = "STATE_LOADING";

    List<T> mItems;
    private GroupRepository mGroupRepo;
    private boolean mLoading;

    public ListViewModelBaseImpl(@Nullable Bundle savedState,
                                 @NonNull GroupRepository groupRepo,
                                 @NonNull UserRepository userRepository) {
        super(savedState, userRepository);

        if (savedState != null) {
            setLoading(savedState.getBoolean(STATE_LOADING, false));
        }
        mGroupRepo = groupRepo;
        mItems = new ArrayList<>();
    }

    @VisibleForTesting
    public void setItems(List<T> items) {
        mItems = items;
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_LOADING, mLoading);
    }

    @Override
    public void onNewGroupSet() {
        super.onNewGroupSet();

        updateList();
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
    @Bindable
    public boolean isEmpty() {
        return mItems.isEmpty();
    }

    @Override
    public T getItemAtPosition(int position) {
        return mItems.get(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    final void checkCurrentGroupData() {
        if (mCurrentGroup != null) {
            if (mCurrentGroup.isDataAvailable()) {
                showView();
            } else {
                mGroupRepo.fetchGroupDataAsync(mCurrentGroup).subscribe(new Action1<Group>() {
                    @Override
                    public void call(Group group) {
                        showView();
                    }
                });
            }
        } else {
            showView();
        }
    }

    private void showView() {
        setLoading(false);
        mView.notifyDataSetChanged();
    }
}
