package ch.giantific.qwittig.presentation.purchases.ocrrating;

import android.support.annotation.NonNull;
import android.view.View;

import javax.inject.Inject;

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

    private final OcrRatingViewModel viewModel;
    private final PurchaseRepository purchaseRepo;
    private final String ocrDataId;

    @Inject
    public OcrRatingPresenter(@NonNull Navigator navigator,
                              @NonNull OcrRatingViewModel viewModel,
                              @NonNull UserRepository userRepo,
                              @NonNull PurchaseRepository purchaseRepo,
                              @NonNull String ocrDataId) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
        this.purchaseRepo = purchaseRepo;
        this.ocrDataId = ocrDataId;
    }

    @Override
    public void onDoneClick(View view) {
        if (viewModel.getSatisfaction() == 0f) {
            this.view.showMessage(R.string.toast_ocr_rating_satisfaction);
            return;
        }

        this.view.showRatingDetails();
    }

    @Override
    public void onDetailsDoneClick(View view) {
        purchaseRepo.saveOcrRating(viewModel.getSatisfaction(), viewModel.getRatingNames(),
                viewModel.getRatingPrices(), viewModel.getRatingMissing(),
                viewModel.getRatingSpeed(), ocrDataId);
        navigator.finish(PurchaseResult.PURCHASE_SAVED);
    }
}
