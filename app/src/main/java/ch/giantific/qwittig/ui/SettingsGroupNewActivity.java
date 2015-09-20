package ch.giantific.qwittig.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.transition.Explode;
import android.transition.Transition;
import android.view.View;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.parse.ParseException;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.constants.AppConstants;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.helpers.CreateGroupHelper;
import ch.giantific.qwittig.ui.listeners.TransitionListenerAdapter;
import ch.giantific.qwittig.utils.Utils;

public class SettingsGroupNewActivity extends BaseActivity implements
        SettingsGroupNewFragment.FragmentInteractionListener,
        FABProgressListener,
        CreateGroupHelper.HelperInteractionListener {

    private static final String GROUP_NEW_FRAGMENT = "group_new_fragment";
    public static final String RESULT_DATA_GROUP = "result_data_group";
    private static final String LOG_TAG = SettingsGroupNewActivity.class.getSimpleName();
    private SettingsGroupNewFragment mSettingsGroupNewFragment;
    private FloatingActionButton mFab;
    private FABProgressCircle mFabProgressCircle;
    private String mNewGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_group_new);

        mFab = (FloatingActionButton) findViewById(R.id.fab_group_new);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingsGroupNewFragment.addNewGroup();
            }
        });
        mFabProgressCircle = (FABProgressCircle) findViewById(R.id.fab_group_new_circle);
        mFabProgressCircle.attachListener(this);

        if (savedInstanceState == null) {
            if (Utils.isRunningLollipopAndHigher()) {
                addActivityTransitionListener();
            } else {
                mFab.show();
            }

            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsGroupNewFragment(), GROUP_NEW_FRAGMENT)
                    .commit();
        } else {
            mFab.show();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void addActivityTransitionListener() {
        Transition enter = getWindow().getEnterTransition();
        enter.addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                super.onTransitionEnd(transition);
                transition.removeListener(this);

                mFab.show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mSettingsGroupNewFragment = (SettingsGroupNewFragment)
                getFragmentManager().findFragmentByTag(GROUP_NEW_FRAGMENT);
    }

    @Override
    public void progressCircleShow() {
        mFabProgressCircle.show();
    }

    @Override
    public void progressCircleHide() {
        mFabProgressCircle.hide();
    }

    @Override
    public void onFABProgressAnimationEnd() {
        mSettingsGroupNewFragment.setIsCreatingNew(false);

        Intent intentNewGroupName = new Intent();
        intentNewGroupName.putExtra(RESULT_DATA_GROUP, mNewGroupName);
        setResult(RESULT_OK, intentNewGroupName);
        ActivityCompat.finishAfterTransition(this);
    }

    @Override
    public void onNewGroupCreated(Group newGroup, boolean invitingUser) {
        mSettingsGroupNewFragment.onNewGroupCreated(newGroup, invitingUser);
    }

    @Override
    public void onCreateNewGroupFailed(ParseException e) {
        mSettingsGroupNewFragment.onCreateNewGroupFailed(e);
    }

    @Override
    public void onUsersInvited() {
        mSettingsGroupNewFragment.onUsersInvited();
    }

    @Override
    public void onInviteUsersFailed(ParseException e) {
        mSettingsGroupNewFragment.onInviteUsersFailed(e);
    }

    @Override
    public void finishGroupCreation(String newGroupName) {
        mNewGroupName = newGroupName;
        mFabProgressCircle.beginFinalAnimation();
    }
}
