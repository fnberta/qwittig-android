/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.edit;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Article;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddViewModelImpl;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditArticleItem;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemModel;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemModel.Type;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link PurchaseAddEditViewModel} for the edit purchase screen.
 * <p>
 * Subclass of {@link PurchaseAddViewModelImpl}.
 */
public class PurchaseEditViewModelImpl extends PurchaseAddViewModelImpl {

    private static final String STATE_ITEMS_SET = "STATE_ITEMS_SET";

    final String editPurchaseId;
    Purchase editPurchase;
    private boolean deleteOldReceipt;
    private boolean oldValuesSet;

    @SuppressWarnings("SimplifiableIfStatement")
    public PurchaseEditViewModelImpl(@Nullable Bundle savedState,
                                     @NonNull Navigator navigator,
                                     @NonNull RxBus<Object> eventBus,
                                     @NonNull RemoteConfigHelper configHelper,
                                     @NonNull UserRepository userRepository,
                                     @NonNull GroupRepository groupRepository,
                                     @NonNull PurchaseRepository purchaseRepository,
                                     @NonNull String editPurchaseId) {
        super(savedState, navigator, eventBus, userRepository, groupRepository,
                purchaseRepository, configHelper);

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
        getSubscriptions().add(getInitialChain(currentUser)
                .flatMap(new Func1<List<Identity>, Single<Purchase>>() {
                    @Override
                    public Single<Purchase> call(List<Identity> identities) {
                        return getPurchase();
                    }
                })
                .doOnSuccess(new Action1<Purchase>() {
                    @Override
                    public void call(Purchase purchase) {
                        editPurchase = purchase;
                    }
                })
                .subscribe(new SingleSubscriber<Purchase>() {
                    @Override
                    public void onSuccess(Purchase purchase) {
                        if (oldValuesSet) {
                            updateRows();
                        } else {
                            setOldPurchase(purchase);
                            setOldArticles();
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
        setNote(purchase.getNote());
        view.reloadOptionsMenu();

        setStore(purchase.getStore());
        setDate(purchase.getDateDate());
        setCurrency(purchase.getCurrency());
        setExchangeRate(purchase.getExchangeRate());
        setReceipt(purchase.getReceipt());
    }

    private void setOldArticles() {
        final List<Article> oldArticles = editPurchase.getArticles();
        for (Article article : oldArticles) {
            final Set<String> identities = article.getIdentitiesIds();
            final String price = moneyFormatter.format(article.getPriceForeign(exchangeRate));
            final PurchaseAddEditArticleItem articleItem =
                    new PurchaseAddEditArticleItem(article.getName(), price, getArticleIdentities(identities));
            articleItem.setMoneyFormatter(moneyFormatter);
            articleItem.setPriceChangedListener(this);
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
            purchaseRepo.savePurchase(purchase, editPurchaseId, currentUserId, isDraft());
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
        if (editPurchase.getDateDate().compareTo(date) != 0
                || !Objects.equals(editPurchase.getStore(), store)
                || !Objects.equals(editPurchase.getCurrency(), currency)
                || !Objects.equals(editPurchase.getNote(), note)) {
            return true;
        }

        final List<Article> oldArticles = editPurchase.getArticles();
        for (int i = 0, size = items.size(), skipCount = 0; i < size; i++) {
            final PurchaseAddEditItemModel addEditItem = items.get(i);
            if (addEditItem.getType() != Type.ARTICLE) {
                skipCount++;
                continue;
            }

            final Article articleOld;
            try {
                articleOld = oldArticles.get(i - skipCount);
            } catch (IndexOutOfBoundsException e) {
                return true;
            }
            final PurchaseAddEditArticleItem articleItem = (PurchaseAddEditArticleItem) addEditItem;
            if (!Objects.equals(articleOld.getName(), articleItem.getName())) {
                return true;
            }

            final double oldPrice = articleOld.getPriceForeign(editPurchase.getExchangeRate());
            final double newPrice = articleItem.parsePrice();
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

        if (deleteOldReceipt || !Objects.equals(receipt, editPurchase.getReceipt())) {
            return true;
        }

        return false;
    }
}
