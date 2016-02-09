/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addgroup;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.settings.addusers.AddUserWorkerListener;
import ch.giantific.qwittig.presentation.settings.addusers.SettingsAddUsersFragment;
import ch.giantific.qwittig.presentation.settings.addusers.SettingsAddUsersViewModel;
import rx.Single;

/**
 * Hosts {@link SettingsAddGroupFragment} that allows to user to create a new group.
 * <p/>
 * Mostly handles transition animations and communication between dialogs and fragments. Handles
 * the circle loading animation of the {@link FloatingActionButton}.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class SettingsAddGroupActivity extends BaseActivity<SettingsAddGroupViewModel> implements
        SettingsAddGroupFragment.ActivityListener,
        AddGroupWorkerListener,
        SettingsAddUsersFragment.ActivityListener,
        AddUserWorkerListener {

    public static final String RESULT_DATA_GROUP = "RESULT_DATA_GROUP";
    private SettingsAddUsersViewModel mAddUsersViewModel;
    private String mNewGroupName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_add_group);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsAddGroupFragment())
                    .commit();
        }
    }

    @Override
    public void setGroupNewViewModel(@NonNull SettingsAddGroupViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public void setCreateGroupStream(@NonNull Single<User> single, @NonNull String workerTag) {
        mViewModel.setCreateGroupStream(single, workerTag);
    }

    @Override
    public void setAddUserViewModel(@NonNull SettingsAddUsersViewModel viewModel) {
        mAddUsersViewModel = viewModel;
    }

    @Override
    public void setAddUserStream(@NonNull Single<String> single, @NonNull String workerTag) {
        mAddUsersViewModel.setAddUserStream(single, workerTag);
    }

    //    private void finishGroupCreation(@NonNull String newGroupName) {
//        mNewGroupName = newGroupName;
//        final Intent intentNewGroupName = new Intent();
//        intentNewGroupName.putExtra(RESULT_DATA_GROUP, mNewGroupName);
//        setResult(RESULT_OK, intentNewGroupName);
//        ActivityCompat.finishAfterTransition(activity);
//    }
}
