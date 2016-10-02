/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.users;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.settings.groupusers.di.DaggerSettingsGroupUsersComponent;
import ch.giantific.qwittig.presentation.settings.groupusers.di.SettingsAddGroupPresenterModule;
import ch.giantific.qwittig.presentation.settings.groupusers.di.SettingsGroupUsersComponent;
import ch.giantific.qwittig.presentation.settings.groupusers.di.SettingsUsersPresenterModule;
import ch.giantific.qwittig.utils.AvatarUtils;

/**
 * Hosts {@link SettingsUsersFragment} that allows the user to add users to his/her current
 * group.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class SettingsUsersActivity extends BaseActivity<SettingsGroupUsersComponent>
        implements NicknamePromptDialogFragment.DialogInteractionListener {

    @Inject
    SettingsUsersContract.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_users);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_done_white_24dp);
        }

        supportPostponeEnterTransition();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsUsersFragment())
                    .commit();
        }
    }

    @Override
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        component = DaggerSettingsGroupUsersComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .settingsAddGroupPresenterModule(new SettingsAddGroupPresenterModule(savedInstanceState))
                .settingsUsersPresenterModule(new SettingsUsersPresenterModule(savedInstanceState))
                .build();
        component.inject(this);
    }

    @Override
    protected List<BasePresenter> getPresenters() {
        return Arrays.asList(new BasePresenter[]{presenter});
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
            case Navigator.RC_IMAGE_PICK:
                if (resultCode == Activity.RESULT_OK) {
                    final Uri imageUri = data.getData();
                    AvatarUtils.saveImageLocal(this, imageUri, path -> presenter.onNewAvatarTaken(path));
                }
        }
    }

    @Override
    public void onValidNicknameEntered(@NonNull String nickname, int position) {
        presenter.onValidNicknameEntered(nickname, position);
    }
}
