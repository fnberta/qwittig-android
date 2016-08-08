package ch.giantific.qwittig.presentation.purchases.ocrrating;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RatingBar;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel.PurchaseResult;

/**
 * Created by fabio on 22.06.16.
 */
public class OcrRatingViewModelImpl extends ViewModelBaseImpl<OcrRatingViewModel.ViewListener>
        implements OcrRatingViewModel {

    private static final String STATE_SATISFACTION = "STATE_SATISFACTION";
    private static final String STATE_RATING_NAMES = "STATE_RATING_NAMES";
    private static final String STATE_RATING_PRICES = "STATE_RATING_PRICES";
    private static final String STATE_RATING_MISSING = "STATE_RATING_MISSING";
    private static final String STATE_RATING_SPEED = "STATE_RATING_SPEED";
    private final PurchaseRepository mPurchaseRepo;
    private final String mOcrDataId;
    private int mSatisfaction;
    private int mRatingNames;
    private int mRatingPrices;
    private int mRatingMissing;
    private int mRatingSpeed;

    public OcrRatingViewModelImpl(@Nullable Bundle savedState,
                                  @NonNull Navigator navigator,
                                  @NonNull RxBus<Object> eventBus,
                                  @NonNull UserRepository userRepository,
                                  @NonNull PurchaseRepository purchaseRepository,
                                  @NonNull String ocrDataId) {
        super(savedState, navigator, eventBus, userRepository);

        mPurchaseRepo = purchaseRepository;
        mOcrDataId = ocrDataId;

        if (savedState != null) {
            mSatisfaction = savedState.getInt(STATE_SATISFACTION);
            mRatingNames = savedState.getInt(STATE_RATING_NAMES);
            mRatingPrices = savedState.getInt(STATE_RATING_PRICES);
            mRatingMissing = savedState.getInt(STATE_RATING_MISSING);
            mRatingSpeed = savedState.getInt(STATE_RATING_SPEED);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putInt(STATE_SATISFACTION, mSatisfaction);
        outState.putInt(STATE_RATING_NAMES, mRatingNames);
        outState.putInt(STATE_RATING_PRICES, mRatingPrices);
        outState.putInt(STATE_RATING_MISSING, mRatingMissing);
        outState.putInt(STATE_RATING_SPEED, mRatingSpeed);
    }

    @Override
    public void onFabDoneClick(View view) {
        if (mSatisfaction == 0) {
            mView.showMessage(R.string.toast_ocr_rating_satisfaction);
            return;
        }

        mView.showRatingDetails();
    }

    @Override
    public void onFabDetailsDoneClick(View view) {
        mPurchaseRepo.saveOcrRating(mSatisfaction, mRatingNames, mRatingPrices, mRatingMissing,
                mRatingSpeed, mOcrDataId);
        mNavigator.finish(PurchaseResult.PURCHASE_SAVED);
    }

    @Override
    public void onSatisfactionChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        mSatisfaction = (int) rating;
    }

    @Override
    public void onRatingNamesChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        mRatingNames = (int) rating;
    }

    @Override
    public void onRatingPricesChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        mRatingPrices = (int) rating;
    }

    @Override
    public void onRatingMissingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        mRatingMissing = (int) rating;
    }

    @Override
    public void onRatingSpeedChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        mRatingSpeed = (int) rating;
    }
}
