package ch.giantific.qwittig;

import android.app.Application;
import android.content.Context;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.Item;
import ch.giantific.qwittig.data.parse.models.Group;

/**
 * A subclass of {@link android.app.Application} to register Parse.com database.
 */
public class Qwittig extends Application {

    public static RefWatcher getRefWatcher(Context context) {
        Qwittig application = (Qwittig) context.getApplicationContext();
        return application.mRefWatcher;
    }

    private RefWatcher mRefWatcher;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            // check memory leaks
            mRefWatcher = LeakCanary.install(this);
        }

        // register ParseObject subclasses
        ParseObject.registerSubclass(Group.class);
        ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(Purchase.class);
        ParseObject.registerSubclass(Item.class);
        ParseObject.registerSubclass(Compensation.class);

        // enable local datastore
        Parse.enableLocalDatastore(this);

        // Enable Crash Reporting
        ParseCrashReporting.enable(this);

        // initialise Parse
        Parse.initialize(this, "yLuL6xJB2dUD2hjfh4W2EcZizcPsJZKDgDzbrPji",
                "XByv1XfsM9lwxAFw7KnAIGDoz2XxfES7cfd43q5t");

        // set default ACL with read/write access only for the user that creates an object
        ParseACL acl = new ParseACL();
        ParseACL.setDefaultACL(acl, true);

        // set up default channel for push notifications
        ParsePush.subscribeInBackground("");
    }
}