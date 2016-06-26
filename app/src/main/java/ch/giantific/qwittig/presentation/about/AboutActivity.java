package ch.giantific.qwittig.presentation.about;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.about.di.AboutComponent;
import ch.giantific.qwittig.presentation.about.di.AboutViewModelModule;
import ch.giantific.qwittig.presentation.about.di.DaggerAboutComponent;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

public class AboutActivity extends BaseActivity<AboutComponent> {

    @Inject
    AboutViewModel mAboutViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new AboutFragment())
                    .commit();
        }
    }

    @Override
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        mComponent = DaggerAboutComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .aboutViewModelModule(new AboutViewModelModule(savedInstanceState))
                .build();
        mComponent.inject(this);
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{mAboutViewModel});
    }
}
