package ch.giantific.qwittig.presentation.about;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.presentation.about.itemmodels.AboutHeader;
import ch.giantific.qwittig.presentation.about.itemmodels.AboutItem;
import ch.giantific.qwittig.presentation.about.itemmodels.AboutItemModel;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;

/**
 * Created by fabio on 26.06.16.
 */
public class AboutViewModelImpl extends ViewModelBaseImpl<AboutViewModel.ViewListener>
        implements AboutViewModel {

    private static final AboutItemModel[] ABOUT_ITEMS = new AboutItemModel[]{
            new AboutHeader(R.string.header_social),
            new AboutItem(R.string.about_facebook, R.drawable.ic_facebook_box_black_24dp),
            new AboutItem(R.string.about_twitter, R.drawable.ic_twitter_box_black_24dp),
            new AboutItem(R.string.about_website, R.drawable.ic_public_black_24dp),
            new AboutHeader(R.string.header_legal),
            new AboutItem(R.string.about_usage),
            new AboutItem(R.string.about_privacy),
    };
    private static final String FACEBOOK_URL = "http://facebook.com/qwittig";
    private static final String TWITTER_URL = "http://twitter.com/qwittig";
    private static final String WEBSITE_URL = "http://www.qwittig.ch";

    public AboutViewModelImpl(@Nullable Bundle savedState,
                              @NonNull Navigator navigator,
                              @NonNull RxBus<Object> eventBus,
                              @NonNull UserRepository userRepository) {
        super(savedState, navigator, eventBus, userRepository);
    }

    @Override
    public int getItemCount() {
        return ABOUT_ITEMS.length;
    }

    @Override
    public AboutItemModel getItemAtPosition(int position) {
        return ABOUT_ITEMS[position];
    }

    @Override
    public void onAboutItemClick(@NonNull AboutItem itemModel) {
        final int titleId = itemModel.getTitle();
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
