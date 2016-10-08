/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.addgroup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.transition.Slide;
import android.view.Gravity;
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
import ch.giantific.qwittig.presentation.settings.groupusers.users.SettingsUsersContract;
import ch.giantific.qwittig.presentation.settings.groupusers.users.SettingsUsersFragment;
import ch.giantific.qwittig.utils.AvatarUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Hosts {@link SettingsAddGroupFragment} that allows to user to create a new group.
 * <p/>
 * Mostly handles transition animations and communication between dialogs and fragments. Handles
 * the circle loading animation of the {@link FloatingActionButton}.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class SettingsAddGroupActivity extends BaseActivity<SettingsGroupUsersComponent> implements
        SettingsAddGroupFragment.ActivityListener {

    public static final String ADD_USERS_FRAGMENT = "ADD_USERS_FRAGMENT";
    public static final String ADD_GROUP_FRAGMENT = "ADD_GROUP_FRAGMENT";

    @Inject
    SettingsAddGroupContract.Presenter groupPresenter;
    @Inject
    SettingsUsersContract.Presenter usersPresenter;

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
        return Arrays.asList(new BasePresenter[]{groupPresenter, usersPresenter});
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
                    AvatarUtils.saveImageLocal(this, imageUri, path -> usersPresenter.onNewAvatarTaken(path));
                }
        }
    }

    @Override
    public void showAddUsersFragment() {
        final FragmentManager fm = getSupportFragmentManager();
        final SettingsAddGroupFragment groupFragment =
                (SettingsAddGroupFragment) fm.findFragmentByTag(ADD_GROUP_FRAGMENT);
        final SettingsUsersFragment usersFragment = new SettingsUsersFragment();
        if (Utils.isRunningLollipopAndHigher()) {
            groupFragment.setExitTransition(new Slide(Gravity.START));
            groupFragment.setAllowReturnTransitionOverlap(false);
            usersFragment.setEnterTransition(new Slide(Gravity.END));
            usersFragment.setAllowEnterTransitionOverlap(false);
        }

        fm.beginTransaction()
                .replace(R.id.container, usersFragment, SettingsAddGroupActivity.ADD_USERS_FRAGMENT)
                .commit();

        setUpIconAsDone();
    }

    private void setUpIconAsDone() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_done_white_24dp);
        }
    }
}
