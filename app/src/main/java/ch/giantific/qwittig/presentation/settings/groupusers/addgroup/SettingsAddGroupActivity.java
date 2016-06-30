/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.addgroup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.settings.groupusers.di.DaggerSettingsGroupUsersComponent;
import ch.giantific.qwittig.presentation.settings.groupusers.di.SettingsAddGroupViewModelModule;
import ch.giantific.qwittig.presentation.settings.groupusers.di.SettingsGroupUsersComponent;
import ch.giantific.qwittig.presentation.settings.groupusers.di.SettingsUsersViewModelModule;
import ch.giantific.qwittig.presentation.settings.groupusers.users.AddUserWorkerListener;
import ch.giantific.qwittig.presentation.settings.groupusers.users.SettingsUsersViewModel;
import ch.giantific.qwittig.utils.AvatarUtils;
import rx.Single;

/**
 * Hosts {@link SettingsAddGroupFragment} that allows to user to create a new group.
 * <p/>
 * Mostly handles transition animations and communication between dialogs and fragments. Handles
 * the circle loading animation of the {@link FloatingActionButton}.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class SettingsAddGroupActivity extends BaseActivity<SettingsGroupUsersComponent> implements
        SettingsAddGroupFragment.ActivityListener,
        AddGroupWorkerListener,
        AddUserWorkerListener {

    public static final String ADD_USERS_FRAGMENT = "ADD_USERS_FRAGMENT";
    public static final String ADD_GROUP_FRAGMENT = "ADD_GROUP_FRAGMENT";
    @Inject
    SettingsAddGroupViewModel mAddGroupViewModel;
    @Inject
    SettingsUsersViewModel mAddUsersViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_add_group);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsAddGroupFragment(), ADD_GROUP_FRAGMENT)
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
        return Arrays.asList(new ViewModel[]{mAddGroupViewModel, mAddUsersViewModel});
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Navigator.INTENT_REQUEST_IMAGE_PICK:
                if (resultCode == Activity.RESULT_OK) {
                    final Uri imageUri = data.getData();
                    AvatarUtils.saveImageLocal(this, imageUri, new AvatarUtils.AvatarLocalSaveListener() {
                        @Override
                        public void onAvatarSaved(@NonNull String path) {
                            mAddUsersViewModel.onNewAvatarTaken(path);
                        }
                    });
                }
        }
    }

    @Override
    public void setUpIconAsDone() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_done_white_24dp);
        }
    }

    @Override
    public void setCreateGroupStream(@NonNull Single<Identity> single, @NonNull String workerTag) {
        mAddGroupViewModel.setCreateGroupStream(single, workerTag);
    }

    @Override
    public void setAddUserStream(@NonNull Single<Identity> single, @NonNull String workerTag) {
        mAddUsersViewModel.setAddUserStream(single, workerTag);
    }
}
