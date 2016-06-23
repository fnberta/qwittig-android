package ch.giantific.qwittig.presentation.purchases.ocrrating;

import android.view.View;
import android.widget.RatingBar;

import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Created by fabio on 22.06.16.
 */
public interface OcrRatingViewModel extends ViewModel<OcrRatingViewModel.ViewListener> {

    void onFabDoneClick(View view);

    void onFabDetailsDoneClick(View view);

    void onSatisfactionChanged(RatingBar ratingBar, float rating, boolean fromUser);

    void onRatingNamesChanged(RatingBar ratingBar, float rating, boolean fromUser);

    void onRatingPricesChanged(RatingBar ratingBar, float rating, boolean fromUser);

    void onRatingMissingChanged(RatingBar ratingBar, float rating, boolean fromUser);

    void onRatingSpeedChanged(RatingBar ratingBar, float rating, boolean fromUser);

    interface ViewListener extends ViewModel.ViewListener {

        void showRatingDetails();
    }
}
