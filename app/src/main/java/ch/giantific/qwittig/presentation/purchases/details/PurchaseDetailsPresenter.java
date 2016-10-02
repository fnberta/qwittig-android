/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Article;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.listadapters.interactions.ListInteraction;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.purchases.details.PurchaseDetailsContract.PurchaseDetailsResult;
import ch.giantific.qwittig.presentation.purchases.details.viewmodels.PurchaseDetailsViewModel;
import ch.giantific.qwittig.presentation.purchases.details.viewmodels.items.PurchaseDetailsArticleItemViewModel;
import ch.giantific.qwittig.presentation.purchases.details.viewmodels.items.PurchaseDetailsIdentityItemViewModel;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;

/**
 * Provides an implementation of the {@link PurchaseDetailsContract}.
 */
public class PurchaseDetailsPresenter extends BasePresenterImpl<PurchaseDetailsContract.ViewListener>
        implements PurchaseDetailsContract.Presenter {

    private static final String STATE_VIEW_MODEL = PurchaseDetailsViewModel.class.getCanonicalName();
    private final PurchaseDetailsViewModel viewModel;
    private final PurchaseRepository purchaseRepo;
    private final List<PurchaseDetailsArticleItemViewModel> articleItems;
    private final List<PurchaseDetailsIdentityItemViewModel> identityItems;
    private final String purchaseId;
    private final String purchaseGroupId;
    private final DateFormat dateFormatter;
    private ListInteraction itemsListInteraction;
    private ListInteraction identitiesListInteraction;
    private String currentIdentityId;
    private NumberFormat moneyFormatter;
    private NumberFormat foreignMoneyFormatter;
    private boolean identitiesActive = true;

    public PurchaseDetailsPresenter(@Nullable Bundle savedState,
                                    @NonNull Navigator navigator,
                                    @NonNull UserRepository userRepo,
                                    @NonNull PurchaseRepository purchaseRepo,
                                    @NonNull String purchaseId,
                                    @Nullable String purchaseGroupId) {
        super(savedState, navigator, userRepo);

        this.purchaseRepo = purchaseRepo;
        this.purchaseId = purchaseId;
        this.purchaseGroupId = purchaseGroupId;

        articleItems = new ArrayList<>();
        identityItems = new ArrayList<>();
        dateFormatter = DateUtils.getDateFormatter(false);

        if (savedState != null) {
            viewModel = savedState.getParcelable(STATE_VIEW_MODEL);
        } else {
            viewModel = new PurchaseDetailsViewModel(true);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelable(STATE_VIEW_MODEL, viewModel);
    }

    @Override
    public PurchaseDetailsViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void setListInteraction(@NonNull ListInteraction listInteraction) {
        itemsListInteraction = listInteraction;
    }

    @Override
    public void setIdentitiesListInteraction(@NonNull ListInteraction listInteraction) {
        identitiesListInteraction = listInteraction;
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        subscriptions.add(userRepo.observeCurrentIdentityId(currentUser.getUid())
                .doOnNext(currentIdentityId -> {
                    if (!TextUtils.isEmpty(this.currentIdentityId) && !Objects.equals(this.currentIdentityId, currentIdentityId)) {
                        navigator.finish();
                    }
                    this.currentIdentityId = currentIdentityId;
                })
                .flatMap(currentIdentityId -> userRepo.getIdentity(currentIdentityId).toObservable())
                .flatMap(identity -> {
                    if (TextUtils.isEmpty(purchaseGroupId) || Objects.equals(purchaseGroupId, identity.getGroup())) {
                        return Observable.just(identity);
                    }
                    // change identity and don't pass anything further down the chain
                    // chain will restart because we are observing the current identity id
                    return userRepo.switchGroup(identity.getUser(), purchaseGroupId)
                            .flatMap(newIdentity -> Observable.never());
                })
                .map(Identity::getGroupCurrency)
                .doOnNext(groupCurrency -> moneyFormatter = MoneyUtils.getMoneyFormatter(groupCurrency, true, true))
                .flatMap(groupCurrency -> purchaseRepo.observePurchase(purchaseId), Pair::create)
                .doOnNext(pair -> {
                    foreignMoneyFormatter = MoneyUtils.getMoneyFormatter(pair.second.getCurrency(), true, true);
                    updateReadBy(pair.second, currentIdentityId);
                    updateActionBarMenu(pair.second, pair.first, currentIdentityId);
                    setPurchaseDetails(pair.second, currentIdentityId);
                })
                .flatMap(pair -> getIdentityItemViewModels(pair.second))
                .subscribe(new IndefiniteSubscriber<List<PurchaseDetailsIdentityItemViewModel>>() {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        view.showMessage(R.string.toast_error_purchase_details_load);
                    }

                    @Override
                    public void onNext(List<PurchaseDetailsIdentityItemViewModel> itemViewModels) {
                        identityItems.clear();
                        identityItems.addAll(itemViewModels);
                        identitiesListInteraction.notifyDataSetChanged();
                        checkIdentitiesActive(itemViewModels);

                        viewModel.setEmpty(getItemCount() == 0);
                        viewModel.setLoading(false);
                        view.startEnterTransition();
                    }
                })
        );
    }

    private Observable<List<PurchaseDetailsIdentityItemViewModel>> getIdentityItemViewModels(@NonNull Purchase purchase) {
        return Observable.from(purchase.getIdentitiesIds())
                .flatMap(identityId -> userRepo.getIdentity(identityId).toObservable())
                .map(identity -> {
                    final boolean isBuyer = Objects.equals(purchase.getBuyer(), identity.getId());
                    return new PurchaseDetailsIdentityItemViewModel(identity, isBuyer);
                })
                .toSortedList();
    }

    private void setPurchaseDetails(@NonNull Purchase purchase, @NonNull String identityId) {
        viewModel.setStore(purchase.getStore());
        viewModel.setDate(dateFormatter.format(purchase.getDateDate()));
        viewModel.setTotal(moneyFormatter.format(purchase.getTotal()));
        viewModel.setTotalForeign(foreignMoneyFormatter.format(purchase.getTotalForeign()));
        final double myShare = purchase.calculateUserShare(identityId);
        viewModel.setMyShare(moneyFormatter.format(myShare));
        final double exchangeRate = purchase.getExchangeRate();
        viewModel.setExchangeRate(exchangeRate);
        viewModel.setMyShareForeign(foreignMoneyFormatter.format(myShare / exchangeRate));
        viewModel.setNote(purchase.getNote());
        viewModel.setReceipt(purchase.getReceipt());
        setArticles(purchase.getArticles(), identityId);
    }

    private void setArticles(@NonNull List<Article> articles, @NonNull String identityId) {
        articleItems.clear();
        for (Article article : articles) {
            final PurchaseDetailsArticleItemViewModel viewModel =
                    new PurchaseDetailsArticleItemViewModel(article, identityId, moneyFormatter);
            articleItems.add(viewModel);
        }
        itemsListInteraction.notifyDataSetChanged();
    }

    private void checkIdentitiesActive(@NonNull List<PurchaseDetailsIdentityItemViewModel> itemViewModels) {
        for (PurchaseDetailsIdentityItemViewModel itemViewModel : itemViewModels) {
            if (!itemViewModel.isActive()) {
                identitiesActive = false;
                break;
            }
        }
    }

    private void updateActionBarMenu(@NonNull Purchase purchase,
                                     @NonNull String groupCurrency,
                                     @NonNull String currentIdentity) {
        final boolean showEdit = Objects.equals(purchase.getBuyer(), currentIdentity);
        final boolean showExchangeRate = !Objects.equals(groupCurrency, purchase.getCurrency());
        view.toggleMenuOptions(showEdit, showExchangeRate);
    }

    private void updateReadBy(@NonNull Purchase purchase, @NonNull String currentIdentity) {
        if (!purchase.isRead(currentIdentity)) {
            purchaseRepo.updateReadBy(purchaseId, currentIdentity);
        }
    }

    @Override
    public PurchaseDetailsArticleItemViewModel getItemAtPosition(int position) {
        return articleItems.get(position);
    }


    @Override
    public int getItemCount() {
        return articleItems.size();
    }

    @Override
    public PurchaseDetailsIdentityItemViewModel getIdentityAtPosition(int position) {
        return identityItems.get(position);
    }

    @Override
    public int getIdentityCount() {
        return identityItems.size();
    }

    @Override
    public void onEditPurchaseClick() {
        if (identitiesActive) {
            navigator.startPurchaseEdit(purchaseId, false);
        } else {
            view.showMessage(R.string.toast_purchase_edit_identities_inactive);
        }
    }

    @Override
    public void onDeletePurchaseClick() {
        if (identitiesActive) {
            purchaseRepo.deletePurchase(purchaseId);
            navigator.finish(PurchaseDetailsResult.PURCHASE_DELETED, purchaseId);
        } else {
            view.showMessage(R.string.toast_purchase_edit_identities_inactive);
        }
    }

    @Override
    public void onShowExchangeRateClick() {
        final NumberFormat formatter = MoneyUtils.getExchangeRateFormatter();
        view.showMessage(R.string.toast_exchange_rate_value, formatter.format(viewModel.getExchangeRate()));
    }
}