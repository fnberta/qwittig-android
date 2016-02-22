/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParsePush;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.ApplicationModule;
import ch.giantific.qwittig.di.DaggerApplicationComponent;
import ch.giantific.qwittig.di.RestServiceModule;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Item;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.utils.parse.ParseConfigUtils;
import timber.log.Timber;

/**
 * Handles the initialisation of the Parse.com framework.
 * <p/>
 * Subclass of {@link android.app.Application}.
 */
public class Qwittig extends Application {

    private ApplicationComponent mAppComponent;

    public static ApplicationComponent getAppComponent(@NonNull Context context) {
        return ((Qwittig) context.getApplicationContext()).getAppComponent();
    }

    private ApplicationComponent getAppComponent() {
        return mAppComponent;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        if (BuildConfig.DEBUG) {
            MultiDex.install(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
//            LeakCanary.install(this);
        }

        buildAppComponent();
        initialiseFacebookSdk();
        initialiseParse();
    }

    private void buildAppComponent() {
        mAppComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .restServiceModule(new RestServiceModule())
                .build();
    }

    private void initialiseFacebookSdk() {
        FacebookSdk.sdkInitialize(getApplicationContext());
    }

    private void initialiseParse() {
        // register ParseObject subclasses
        ParseObject.registerSubclass(Group.class);
        ParseObject.registerSubclass(Identity.class);
        ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(Purchase.class);
        ParseObject.registerSubclass(Item.class);
        ParseObject.registerSubclass(Compensation.class);
        ParseObject.registerSubclass(Task.class);

        // enable local data store
        Parse.enableLocalDatastore(this);

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
    }
}