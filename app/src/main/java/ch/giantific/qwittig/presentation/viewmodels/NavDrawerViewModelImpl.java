/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import com.parse.ParseObject;

import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import rx.SingleSubscriber;

/**
 * Created by fabio on 12.01.16.
 */
public class NavDrawerViewModelImpl extends BaseObservable implements NavDrawerViewModel {

    private ViewListener mView;
    private User mCurrentUser;
    private GroupRepository mGroupRepo;
    private UserRepository mUserRepo;
    private List<ParseObject> mUserGroups;

    public NavDrawerViewModelImpl(@NonNull GroupRepository groupRepository,
                                  @NonNull UserRepository userRepository) {
        mGroupRepo = groupRepository;
        mUserRepo = userRepository;
        mCurrentUser = userRepository.getCurrentUser();
    }

    @Override
    public void attachView(@NonNull NavDrawerViewModel.ViewListener view) {
        mView = view;
    }

    @Override
    public void onNavDrawerReady() {
        if (isUserLoggedIn()) {
            loadUserGroups();
        }
    }

    private void loadUserGroups() {
        mUserGroups = mCurrentUser.getGroups();
        mGroupRepo.fetchGroupsDataAsync(mUserGroups)
                .toList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<Group>>() {
                    @Override
                    public void onSuccess(List<Group> groups) {
                        mView.bindHeaderView();
                        mView.setupHeaderGroupSelection(groups);
                    }

                    @Override
                    public void onError(Throwable error) {

                    }
                });
    }

    @Override
    @Bindable
    public String getUserNickname() {
        return mCurrentUser.getNickname();
    }

    @Override
    @Bindable
    public byte[] getUserAvatar() {
        return mCurrentUser.getAvatar();
    }

    @Override
    @Bindable
    public int getSelectedGroup() {
        return mUserGroups.indexOf(mCurrentUser.getCurrentGroup());
    }

    @Override
    public void notifySelectedGroupChanged() {
        notifyPropertyChanged(BR.selectedGroup);
    }

    @Override
    public boolean isUserLoggedIn() {
        return mCurrentUser != null;
    }

    @Override
    public void onLoginSuccessful() {
        mCurrentUser = mUserRepo.getCurrentUser();
    }

    @Override
    public void onLogout() {
        mView.startHomeActivityAndFinish();
    }

    @Override
    public void onGroupChanged() {
        updateGroupSelectionList();
        notifySelectedGroupChanged();
    }

    private void updateGroupSelectionList() {
        mUserGroups.clear();
        List<ParseObject> groups = mCurrentUser.getGroups();
        if (!groups.isEmpty()) {
            mUserGroups.addAll(groups);
        }

        mView.notifyHeaderGroupListChanged();
    }

    @Override
    public void onProfileUpdated() {
        notifyPropertyChanged(BR.userNickname);
        notifyPropertyChanged(BR.userAvatar);
        mView.showMessage(R.string.toast_profile_update);
    }

    @Override
    public void onGroupSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final Group groupSelected = (Group) parent.getItemAtPosition(position);
        final Group oldGroup = mCurrentUser.getCurrentGroup();
        if (oldGroup.getObjectId().equals(groupSelected.getObjectId())) {
            return;
        }

        mCurrentUser.setCurrentGroup(groupSelected);
        mCurrentUser.saveEventually();
        mView.onNewGroupSet();
    }

    @Override
    public void onAvatarClick(View view) {
        mView.startProfileSettingsActivity();
    }

    @Override
    public void unsubscribe() {
        // clean up any long running tasks
    }

    @Override
    public void detachView() {
        mView = null;
    }
}
