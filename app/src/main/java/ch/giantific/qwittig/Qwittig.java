/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParsePush;

import ch.giantific.qwittig.di.components.ApplicationComponent;
import ch.giantific.qwittig.di.components.DaggerApplicationComponent;
import ch.giantific.qwittig.di.modules.ApplicationModule;
import ch.giantific.qwittig.di.modules.RestServiceModule;
import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Item;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.utils.parse.ParseConfigUtils;

/**
 * Handles the initialisation of the Parse.com framework.
 * <p/>
 * Subclass of {@link android.app.Application}.
 */
public class Qwittig extends Application {

    private ApplicationComponent mAppComponent;

    public static ApplicationComponent getAppComponent(@NonNull Activity activity) {
        return ((Qwittig) activity.getApplication()).getAppComponent();
    }

    public ApplicationComponent getAppComponent() {
        return mAppComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // initialise Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());

        // register ParseObject subclasses
        ParseObject.registerSubclass(Group.class);
        ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(Purchase.class);
        ParseObject.registerSubclass(Item.class);
        ParseObject.registerSubclass(Compensation.class);
        ParseObject.registerSubclass(Task.class);

        // enable local data store
        Parse.enableLocalDatastore(this);

        // Enable Crash Reporting
        ParseCrashReporting.enable(this);

        // initialise Parse
        Parse.initialize(this, "yLuL6xJB2dUD2hjfh4W2EcZizcPsJZKDgDzbrPji",
                "XByv1XfsM9lwxAFw7KnAIGDoz2XxfES7cfd43q5t");

        // initialize Parse Facebook Utils
        ParseFacebookUtils.initialize(this);

        // set default ACL with read/write access only for the user that creates an object
        ParseACL acl = new ParseACL();
        ParseACL.setDefaultACL(acl, true);

        // set up default channel for push notifications
        ParsePush.subscribeInBackground("");

        // refresh ParseConfig
        ParseConfigUtils.refreshConfig();

        mAppComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .restServiceModule(new RestServiceModule())
                .build();
    }
}