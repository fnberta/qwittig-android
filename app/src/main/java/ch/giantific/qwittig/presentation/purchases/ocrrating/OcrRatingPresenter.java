package ch.giantific.qwittig.presentation.purchases.ocrrating;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RatingBar;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditContract.PurchaseResult;

/**
 * Created by fabio on 22.06.16.
 */
public class OcrRatingPresenter extends BasePresenterImpl<OcrRatingContract.ViewListener>
        implements OcrRatingContract.Presenter {

    private static final String STATE_SATISFACTION = "STATE_SATISFACTION";
    private static final String STATE_RATING_NAMES = "STATE_RATING_NAMES";
    private static final String STATE_RATING_PRICES = "STATE_RATING_PRICES";
    private static final String STATE_RATING_MISSING = "STATE_RATING_MISSING";
    private static final String STATE_RATING_SPEED = "STATE_RATING_SPEED";

    private final PurchaseRepository purchaseRepo;
    private final String ocrDataId;
    private int satisfaction;
    private int ratingNames;
    private int ratingPrices;
    private int ratingMissing;
    private int ratingSpeed;

    public OcrRatingPresenter(@Nullable Bundle savedState,
                              @NonNull Navigator navigator,
                              @NonNull UserRepository userRepo,
                              @NonNull PurchaseRepository purchaseRepo,
                              @NonNull String ocrDataId) {
        super(savedState, navigator, userRepo);

        this.purchaseRepo = purchaseRepo;
        this.ocrDataId = ocrDataId;

        if (savedState != null) {
            satisfaction = savedState.getInt(STATE_SATISFACTION);
            ratingNames = savedState.getInt(STATE_RATING_NAMES);
            ratingPrices = savedState.getInt(STATE_RATING_PRICES);
            ratingMissing = savedState.getInt(STATE_RATING_MISSING);
            ratingSpeed = savedState.getInt(STATE_RATING_SPEED);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putInt(STATE_SATISFACTION, satisfaction);
        outState.putInt(STATE_RATING_NAMES, ratingNames);
        outState.putInt(STATE_RATING_PRICES, ratingPrices);
        outState.putInt(STATE_RATING_MISSING, ratingMissing);
        outState.putInt(STATE_RATING_SPEED, ratingSpeed);
    }

    @Override
    public void onDoneClick(View view) {
        if (satisfaction == 0) {
            this.view.showMessage(R.string.toast_ocr_rating_satisfaction);
            return;
        }

        this.view.showRatingDetails();
    }

    @Override
    public void onDetailsDoneClick(View view) {
        purchaseRepo.saveOcrRating(satisfaction, ratingNames, ratingPrices, ratingMissing,
                ratingSpeed, ocrDataId);
        navigator.finish(PurchaseResult.PURCHASE_SAVED);
    }

    @Override
    public void onSatisfactionChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        satisfaction = (int) rating;
    }

    @Override
    public void onRatingNamesChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        ratingNames = (int) rating;
    }

    @Override
    public void onRatingPricesChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        ratingPrices = (int) rating;
    }

    @Override
    public void onRatingMissingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        ratingMissing = (int) rating;
    }

    @Override
    public void onRatingSpeedChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        ratingSpeed = (int) rating;
    }
}
