package ch.giantific.qwittig.presentation.purchases.ocrrating;

import android.view.View;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;

/**
 * Created by fabio on 22.06.16.
 */
public interface OcrRatingContract {

    interface Presenter extends BasePresenter<ViewListener> {
        void onDoneClick(View view);

        void onDetailsDoneClick(View view);
    }

    interface ViewListener extends BaseView {

        void showRatingDetails();
    }
}
