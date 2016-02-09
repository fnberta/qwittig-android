/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addusers;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import rx.Single;

/**
 * Hosts {@link SettingsAddUsersFragment} that allows the user to add users to his/her current
 * group.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class SettingsAddUsersActivity extends BaseActivity<SettingsAddUsersViewModel>
        implements SettingsAddUsersFragment.ActivityListener, AddUserWorkerListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_add_users);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsAddUsersFragment())
                    .commit();
        }
    }

    @Override
    public void setAddUserViewModel(@NonNull SettingsAddUsersViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public void setAddUserStream(@NonNull Single<String> single, @NonNull String workerTag) {
        mViewModel.setAddUserStream(single, workerTag);
    }
}
