/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import rx.Single;

/**
 * Hosts {@link SettingsUsersFragment} that allows the user to add users to his/her current
 * group.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class SettingsUsersActivity extends BaseActivity<SettingsUsersViewModel>
        implements SettingsUsersFragment.ActivityListener, AddUserWorkerListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_users);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_done_white_24dp);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsUsersFragment())
                    .commit();
        }
    }

    @Override
    public void setAddUserViewModel(@NonNull SettingsUsersViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public void setAddUserStream(@NonNull Single<String> single, @NonNull String workerTag) {
        mViewModel.setAddUserStream(single, workerTag);
    }
}
