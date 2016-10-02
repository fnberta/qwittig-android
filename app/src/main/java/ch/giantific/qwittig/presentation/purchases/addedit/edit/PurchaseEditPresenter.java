/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.edit;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    private static final String STATE_ITEMS_SET = "STATE_ITEMS_SET";

    final String editPurchaseId;
    Purchase editPurchase;
    private boolean deleteOldReceipt;
    private boolean oldValuesSet;

    @SuppressWarnings("SimplifiableIfStatement")
    public PurchaseEditPresenter(@Nullable Bundle savedState,
                                 @NonNull Navigator navigator,
                                 @NonNull UserRepository userRepo,
                                 @NonNull GroupRepository groupRepo,
                                 @NonNull PurchaseRepository purchaseRepo,
                                 @NonNull RemoteConfigHelper configHelper,
                                 @NonNull String editPurchaseId) {
        super(savedState, navigator, userRepo, groupRepo, purchaseRepo, configHelper);

        this.editPurchaseId = editPurchaseId;

        if (savedState != null) {
            oldValuesSet = savedState.getBoolean(STATE_ITEMS_SET, false);
        } else {
            oldValuesSet = false;
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_ITEMS_SET, oldValuesSet);
    }

    @Override
    protected void loadPurchase(@NonNull FirebaseUser currentUser) {
        subscriptions.add(getInitialChain(currentUser)
                .flatMap(identities -> getPurchase())
                .doOnSuccess(purchase -> editPurchase = purchase)
                .subscribe(new SingleSubscriber<Purchase>() {
                    @Override
                    public void onSuccess(Purchase purchase) {
                        if (!oldValuesSet) {
                            setOldPurchase(purchase);
                            setOldArticles(purchase.getArticles());
                            oldValuesSet = true;
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
            final String price = moneyFormatter.format(article.getPriceForeign(viewModel.getExchangeRate()));
            final PurchaseAddEditArticleItemViewModel articleItem =
                    new PurchaseAddEditArticleItemViewModel(article.getName(), price, getArticleIdentities(identities));
            final int pos = getItemCount() - 2;
            items.add(pos, articleItem);
            listInteraction.notifyItemInserted(pos);
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

        final List<Article> oldArticles = editPurchase.getArticles();
        for (int i = 0, size = items.size(), skipCount = 0; i < size; i++) {
            final BasePurchaseAddEditItemViewModel addEditItem = items.get(i);
            if (addEditItem.getViewType() != ViewType.ARTICLE) {
                skipCount++;
                continue;
            }

            final Article articleOld;
            try {
                articleOld = oldArticles.get(i - skipCount);
            } catch (IndexOutOfBoundsException e) {
                return true;
            }
            final PurchaseAddEditArticleItemViewModel articleItem = (PurchaseAddEditArticleItemViewModel) addEditItem;
            if (!Objects.equals(articleOld.getName(), articleItem.getName())) {
                return true;
            }

            final double oldPrice = articleOld.getPriceForeign(editPurchase.getExchangeRate());
            final double newPrice = articleItem.getPriceParsed();
            if (Math.abs(oldPrice - newPrice) >= MoneyUtils.MIN_DIFF) {
                return true;
            }

            final Set<String> identitiesOld = articleOld.getIdentitiesIds();
            final List<String> identitiesNew = articleItem.getSelectedIdentitiesIds();
            if (!identitiesNew.containsAll(identitiesOld) ||
                    !identitiesOld.containsAll(identitiesNew)) {
                return true;
            }
        }

        if (deleteOldReceipt || !Objects.equals(viewModel.getReceipt(), editPurchase.getReceipt())) {
            return true;
        }

        return false;
    }
}
