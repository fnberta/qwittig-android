/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.repositories.UserRepository;

/**
 * Created by fabio on 22.01.16.
 */
public class HomeViewModelImpl extends ViewModelBaseImpl<HomeViewModel.ViewListener>
        implements HomeViewModel {

    public HomeViewModelImpl(@Nullable Bundle savedState, @NonNull UserRepository userRepository) {
        super(savedState, userRepository);

        if (mCurrentUser != null) {
//            checkForInvitations();
        }
    }

    @Override
    public void onFabAddPurchaseManualClick(View view) {
        if (isUserInGroup()) {
            mView.startPurchaseAddActivity(false);
        } else {
            mView.showCreateGroupDialog(R.string.dialog_group_create_purchases);
        }
    }

    @Override
    public void onFabAddPurchaseAutoClick(View view) {
        if (isUserInGroup()) {
            mView.startPurchaseAddActivity(true);
        } else {
            mView.showCreateGroupDialog(R.string.dialog_group_create_purchases);
        }
    }

    @Override
    public boolean isDraftsEmpty() {
        return false;
    }
}
