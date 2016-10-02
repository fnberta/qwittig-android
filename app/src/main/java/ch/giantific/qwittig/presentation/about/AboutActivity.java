package ch.giantific.qwittig.presentation.about;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.about.di.AboutComponent;
import ch.giantific.qwittig.presentation.about.di.AboutPresenterModule;
import ch.giantific.qwittig.presentation.about.di.DaggerAboutComponent;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;

public class AboutActivity extends BaseActivity<AboutComponent> {

    @Inject
    AboutContract.Presenter presenter;

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
        component = DaggerAboutComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .aboutPresenterModule(new AboutPresenterModule(savedInstanceState))
                .build();
        component.inject(this);
    }

    @Override
    protected List<BasePresenter> getPresenters() {
        return Arrays.asList(new BasePresenter[]{presenter});
    }
}
