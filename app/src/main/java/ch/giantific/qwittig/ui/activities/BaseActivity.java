package ch.giantific.qwittig.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.models.Config;
import ch.giantific.qwittig.ui.fragments.dialogs.AccountCreateDialogFragment;

/**
 * BaseActivity that extends the Android ActionBarActivity Class.
 */
public abstract class BaseActivity extends AppCompatActivity implements
        AccountCreateDialogFragment.DialogInteractionListener {

    public static final int INTENT_REQUEST_LOGIN = 1;
    public static final int INTENT_REQUEST_SETTINGS = 2;
    public static final int INTENT_REQUEST_PURCHASE_MODIFY = 3;
    public static final int INTENT_REQUEST_PURCHASE_DETAILS = 4;
    public static final int INTENT_REQUEST_SETTINGS_PROFILE = 5;
    public static final int INTENT_REQUEST_SETTINGS_GROUP_NEW = 6;
    public static final int INTENT_REQUEST_TASK_NEW = 7;
    public static final int INTENT_REQUEST_TASK_MODIFY = 8;
    public static final int INTENT_REQUEST_TASK_DETAILS = 9;

    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // refresh ParseConfig
        Config.refreshConfig();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        setupToolbar();
    }

    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
            }
        }
    }

    public void showAccountCreateDialog() {
        AccountCreateDialogFragment accountCreateDialogFragment =
                new AccountCreateDialogFragment();
        accountCreateDialogFragment.show(getFragmentManager(), "account_create");
    }

    @Override
    public void createNewAccount() {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                startHomeScreen();
            }
        });
    }

    private void startHomeScreen() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(LoginActivity.INTENT_EXTRA_SIGN_UP, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
