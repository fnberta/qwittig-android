package ch.giantific.qwittig.presentation.common.di;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 17.06.16.
 */
@Module
public class NavigatorModule {

    private final FragmentActivity mActivity;

    public NavigatorModule(@NonNull FragmentActivity activity) {
        mActivity = activity;
    }

    @PerActivity
    @Provides
    Navigator providesNavigator() {
        return new Navigator(mActivity);
    }
}
