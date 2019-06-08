/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.edit;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Article;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddPresenter;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.BasePurchaseAddEditItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.BasePurchaseAddEditItemViewModel.ViewType;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditArticleItemViewModel;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Single;
import rx.SingleSubscriber;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link PurchaseAddEditViewModel} for the edit purchase screen.
 * <p>
 * Subclass of {@link PurchaseAddEditViewModel}.
 */
public class PurchaseEditPresenter extends PurchaseAddPresenter {

    final String editPurchaseId;
    Purchase editPurchase;
    private boolean deleteOldReceipt;

    @Inject
    public PurchaseEditPresenter(@NonNull Navigator navigator,
                                 @NonNull PurchaseAddEditViewModel viewModel,
                                 @NonNull UserRepository userRepo,
                                 @NonNull GroupRepository groupRepo,
                                 @NonNull PurchaseRepository purchaseRepo,
                                 @NonNull RemoteConfigHelper configHelper,
                                 @NonNull String editPurchaseId) {
        super(navigator, viewModel, userRepo, groupRepo, purchaseRepo, configHelper);

        this.editPurchaseId = editPurchaseId;
    }

    @Override
    protected void loadPurchase(@NonNull FirebaseUser currentUser) {
        subscriptions.add(getInitialChain(currentUser)
                .flatMap(identities -> getPurchase())
                .doOnSuccess(purchase -> editPurchase = purchase)
                .subscribe(new SingleSubscriber<Purchase>() {
                    @Override
                    public void onSuccess(Purchase purchase) {
                        if (!viewModel.isDataSet()) {
                            setOldPurchase(purchase);
                            setOldArticles(purchase.getArticles());
                            viewModel.setDataSet(true);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to load edit purchase with error:");
                        view.showMessage(R.string.toast_error_purchase_edit_load);
                    }
                })
        );
    }

    protected Single<Purchase> getPurchase() {
        return purchaseRepo.getPurchase(editPurchaseId);
    }

    private void setOldPurchase(@NonNull Purchase purchase) {
        viewModel.setNote(purchase.getNote());
        view.reloadOptionsMenu();

        viewModel.setStore(purchase.getStore());
        final Date date = purchase.getDateDate();
        viewModel.setDate(date, dateFormatter.format(date));
        final String currency = purchase.getCurrency();
        viewModel.setCurrency(currency, true);
        final double exchangeRate = purchase.getExchangeRate();
        viewModel.setExchangeRate(exchangeRate, exchangeRateFormatter.format(exchangeRate));
        viewModel.setReceipt(purchase.getReceipt());
    }

    private void setOldArticles(@NonNull List<Article> oldArticles) {
        for (Article article : oldArticles) {
            final Set<String> identities = article.getIdentitiesIds();
            final double price = article.getPriceForeign(viewModel.getExchangeRate());
            final String priceFormatted = moneyFormatter.format(price);
            final PurchaseAddEditArticleItemViewModel articleItem =
                    new PurchaseAddEditArticleItemViewModel(article.getName(), priceFormatted,
                            price, getArticleIdentities(identities));
            final int pos = viewModel.getItemCount() - 2;
            viewModel.addItemAtPosition(pos, articleItem);
            view.notifyItemAdded(pos);
        }
    }

    @Override
    protected void savePurchase(@NonNull Purchase purchase, boolean asDraft) {
        if (asDraft) {
            purchaseRepo.saveDraft(purchase, editPurchaseId);
        } else {
            purchaseRepo.savePurchase(purchase, editPurchaseId, currentIdentity.getUser(), isDraft());
        }
    }

    boolean isDraft() {
        return false;
    }

    @Override
    public void onDeleteReceiptMenuClick() {
        super.onDeleteReceiptMenuClick();

        deleteOldReceipt = true;
    }

    @Override
    public void onExitClick() {
        if (changesWereMade()) {
            view.showDiscardEditChangesDialog();
        } else {
            navigator.finish(Activity.RESULT_CANCELED);
        }
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean changesWereMade() {
        if (editPurchase.getDateDate().compareTo(viewModel.getDate()) != 0
                || !Objects.equals(editPurchase.getStore(), viewModel.getStore())
                || !Objects.equals(editPurchase.getCurrency(), viewModel.getCurrency())
                || !Objects.equals(editPurchase.getNote(), viewModel.getNote())) {
            return true;
        }

        if (viewModel.isItemsChanged(editPurchase.getArticles(), editPurchase.getExchangeRate())) {
            return true;
        }

        if (deleteOldReceipt || !Objects.equals(viewModel.getReceipt(), editPurchase.getReceipt())) {
            return true;
        }

        return false;
    }
}
