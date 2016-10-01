package ch.giantific.qwittig.presentation.purchases.ocrrating;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.transition.Slide;
import android.view.Gravity;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.purchases.ocrrating.di.DaggerOcrRatingComponent;
import ch.giantific.qwittig.presentation.purchases.ocrrating.di.OcrRatingComponent;
import ch.giantific.qwittig.presentation.purchases.ocrrating.di.OcrRatingPresenterModule;
import ch.giantific.qwittig.utils.Utils;

public class OcrRatingActivity extends BaseActivity<OcrRatingComponent>
        implements OcrRatingContract.ViewListener {

    private static final String FRAGMENT_RATING = "FRAGMENT_RATING";

    @Inject
    OcrRatingContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_rating);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new OcrRatingFragment(), FRAGMENT_RATING)
                    .commit();
        }
    }

    @Override
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        final String ocrDataId = getIntent().getStringExtra(Navigator.EXTRA_OCR_DATA_ID);
        component = DaggerOcrRatingComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .ocrRatingPresenterModule(new OcrRatingPresenterModule(savedInstanceState, ocrDataId))
                .build();
        component.inject(this);
        presenter.attachView(this);
    }

    @Override
    protected List<BasePresenter> getPresenters() {
        return Arrays.asList(new BasePresenter[]{presenter});
    }

    @Override
    public void showRatingDetails() {
        final FragmentManager fm = getSupportFragmentManager();
        final Fragment fragment = new OcrRatingDetailsFragment();
        if (Utils.isRunningLollipopAndHigher()) {
            final Fragment currentFrag = fm.findFragmentByTag(FRAGMENT_RATING);
            currentFrag.setExitTransition(new Slide(Gravity.BOTTOM));
            fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, FRAGMENT_RATING)
                .addToBackStack(null)
                .commit();
    }
}
