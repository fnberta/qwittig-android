/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.users;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.settings.groupusers.di.DaggerSettingsGroupUsersComponent;
import ch.giantific.qwittig.presentation.settings.groupusers.di.SettingsAddGroupViewModelModule;
import ch.giantific.qwittig.presentation.settings.groupusers.di.SettingsGroupUsersComponent;
import ch.giantific.qwittig.presentation.settings.groupusers.di.SettingsUsersViewModelModule;
import rx.Single;

/**
 * Hosts {@link SettingsUsersFragment} that allows the user to add users to his/her current
 * group.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class SettingsUsersActivity extends BaseActivity<SettingsGroupUsersComponent>
        implements AddUserWorkerListener,
        NicknamePromptDialogFragment.DialogInteractionListener {

    @Inject
    SettingsUsersViewModel mUsersViewModel;

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
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        mComponent = DaggerSettingsGroupUsersComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .settingsAddGroupViewModelModule(new SettingsAddGroupViewModelModule(savedInstanceState))
                .settingsUsersViewModelModule(new SettingsUsersViewModelModule(savedInstanceState))
                .build();
        mComponent.inject(this);
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{mUsersViewModel});
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
    public void setAddUserStream(@NonNull Single<Identity> single, @NonNull String workerTag) {
        mUsersViewModel.setAddUserStream(single, workerTag);
    }

    @Override
    public void onValidNicknameEntered(@NonNull String nickname, int position) {
        mUsersViewModel.onValidNicknameEntered(nickname, position);
    }
}
