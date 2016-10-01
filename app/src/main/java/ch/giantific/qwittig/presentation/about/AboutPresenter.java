package ch.giantific.qwittig.presentation.about;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.presentation.about.viewmodels.items.AboutHeaderViewModel;
import ch.giantific.qwittig.presentation.about.viewmodels.items.AboutItemViewModel;
import ch.giantific.qwittig.presentation.about.viewmodels.items.BaseAboutItemViewModel;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;

/**
 * Created by fabio on 26.06.16.
 */
public class AboutPresenter extends BasePresenterImpl<AboutContract.ViewListener>
        implements AboutContract.Presenter {

    private static final BaseAboutItemViewModel[] ABOUT_ITEMS = new BaseAboutItemViewModel[]{
            new AboutHeaderViewModel(R.string.header_social),
            new AboutItemViewModel(R.string.about_facebook, R.drawable.ic_facebook_box_black_24dp),
            new AboutItemViewModel(R.string.about_twitter, R.drawable.ic_twitter_box_black_24dp),
            new AboutItemViewModel(R.string.about_website, R.drawable.ic_public_black_24dp),
            new AboutHeaderViewModel(R.string.header_legal),
            new AboutItemViewModel(R.string.about_usage),
            new AboutItemViewModel(R.string.about_privacy),
    };
    private static final String FACEBOOK_URL = "http://facebook.com/qwittig";
    private static final String TWITTER_URL = "http://twitter.com/qwittig";
    private static final String WEBSITE_URL = "http://www.qwittig.ch";

    public AboutPresenter(@Nullable Bundle savedState,
                          @NonNull Navigator navigator,
                          @NonNull UserRepository userRepo) {
        super(savedState, navigator, userRepo);
    }

    @Override
    public BaseAboutItemViewModel getItemAtPosition(int position) {
        return ABOUT_ITEMS[position];
    }

    @Override
    public int getItemCount() {
        return ABOUT_ITEMS.length;
    }

    @Override
    public void onAboutItemClick(@NonNull AboutItemViewModel itemViewModel) {
        final int titleId = itemViewModel.getTitle();
        switch (titleId) {
            case R.string.about_facebook:
                navigator.openWebsite(FACEBOOK_URL);
                break;
            case R.string.about_twitter:
                navigator.openWebsite(TWITTER_URL);
                break;
            case R.string.about_website:
                navigator.openWebsite(WEBSITE_URL);
                break;
            case R.string.about_usage:
                // TODO: show usage info
                break;
            case R.string.about_privacy:
                // TODO: show privacy info
                break;
        }
    }
}
