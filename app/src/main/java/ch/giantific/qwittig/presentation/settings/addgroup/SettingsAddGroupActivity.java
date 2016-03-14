/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addgroup;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.settings.users.AddUserWorkerListener;
import ch.giantific.qwittig.presentation.settings.users.SettingsUsersFragment;
import ch.giantific.qwittig.presentation.settings.users.SettingsUsersViewModel;
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
        SettingsUsersFragment.ActivityListener,
        AddUserWorkerListener {

    private SettingsUsersViewModel mAddUsersViewModel;

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
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                ActivityCompat.finishAfterTransition(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setUpIconDone() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_done_white_24dp);
        }
    }

    @Override
    public void setGroupNewViewModel(@NonNull SettingsAddGroupViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public void setCreateGroupStream(@NonNull Single<Identity> single, @NonNull String workerTag) {
        mViewModel.setCreateGroupStream(single, workerTag);
    }

    @Override
    public void setAddUserViewModel(@NonNull SettingsUsersViewModel viewModel) {
        mAddUsersViewModel = viewModel;
    }

    @Override
    public void setAddUserStream(@NonNull Single<Identity> single, @NonNull String workerTag) {
        mAddUsersViewModel.setAddUserStream(single, workerTag);
    }
}
