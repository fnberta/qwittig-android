package ch.giantific.qwittig.di;

import android.app.Application;

import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;

import javax.inject.Singleton;

import ch.giantific.qwittig.R;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 02.07.16.
 */
@Module
public class FirebaseModule {

    @Provides
    @Singleton
    FirebaseAuth providesFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Provides
    @Singleton
    FirebaseDatabase providesFirebaseDatabase() {
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.setPersistenceEnabled(true);
        return firebaseDatabase;
    }

    @Provides
    @Singleton
    FirebaseStorage providesFirebaseStorage() {
        return FirebaseStorage.getInstance();
    }

    @Provides
    @Singleton
    FirebaseMessaging providesFirebaseMessaging() {
        return FirebaseMessaging.getInstance();
    }

    @Provides
    @Singleton
    FirebaseRemoteConfig providesFirebaseRemoteConfig() {
        final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setDefaults(R.xml.remote_config_defaults);
        return remoteConfig;
    }

    @Provides
    @Singleton
    Driver providesJobDispatcherDriver(Application application) {
        return new GooglePlayDriver(application);
    }

    @Provides
    @Singleton
    FirebaseJobDispatcher providesJobDispatcher(Driver driver) {
        return new FirebaseJobDispatcher(driver);
    }
}
