/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import com.parse.ParseObject;

import java.util.List;

import javax.inject.Inject;

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
        if (isUserLoggedIn()) {
            loadUserGroups();
        }
    }

    @Override
    public void detachView() {
        mView = null;
    }

    private void loadUserGroups() {
        mUserGroups = mCurrentUser.getGroups();
        mGroupRepo.fetchGroupsDataAsync(mUserGroups)
                .toSingle()
                .subscribe(new SingleSubscriber<Group>() {
                    @Override
                    public void onSuccess(Group value) {
                        mView.bindHeaderView();
                        mView.setupHeaderGroupSelection(mUserGroups);
                        updateGroupSelectionList();
                    }

                    @Override
                    public void onError(Throwable error) {
                        // TODO: handle error
                    }
                });
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
        notifyPropertyChanged(BR.selectedGroup);
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
}
