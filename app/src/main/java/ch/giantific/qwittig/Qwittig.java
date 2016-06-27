/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

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
import ch.giantific.qwittig.domain.models.OcrData;
import ch.giantific.qwittig.domain.models.OcrRating;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.models.TaskHistoryEvent;
import ch.giantific.qwittig.domain.models.User;
import io.branch.referral.Branch;
import timber.log.Timber;

/**
 * Handles application wide initialisations.
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
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
//            LeakCanary.install(this);
        }

        buildAppComponent();
        initialiseParse();
        Branch.getAutoInstance(this);
    }

    private void buildAppComponent() {
        mAppComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .restServiceModule(new RestServiceModule())
                .build();
    }

    private void initialiseParse() {
        // register ParseObject subclasses
        ParseObject.registerSubclass(Group.class);
        ParseObject.registerSubclass(Identity.class);
        ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(Purchase.class);
        ParseObject.registerSubclass(OcrData.class);
        ParseObject.registerSubclass(OcrRating.class);
        ParseObject.registerSubclass(Item.class);
        ParseObject.registerSubclass(Compensation.class);
        ParseObject.registerSubclass(Task.class);
        ParseObject.registerSubclass(TaskHistoryEvent.class);

        // initialise Parse
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("yLuL6xJB2dUD2hjfh4W2EcZizcPsJZKDgDzbrPji")
                .server("https://qwittig.com/api/data/")
                .clientKey(null)
                .enableLocalDataStore()
                .build()
        );

        // initialize Parse Facebook Utils
        ParseFacebookUtils.initialize(this);

        // set default ACL with read/write access only for the user that creates an object
        final ParseACL acl = new ParseACL();
        ParseACL.setDefaultACL(acl, true);

        // set up default channel for push notifications
        ParsePush.subscribeInBackground("");
    }
}