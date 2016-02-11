/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details;

import android.annotation.SuppressLint;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.domain.models.parse.Item;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.SingleSubscriber;

/**
 * Created by fabio on 29.01.16.
 */
public class PurchaseDetailsViewModelImpl extends ListViewModelBaseImpl<PurchaseDetailsItem, PurchaseDetailsViewModel.ViewListener>
        implements PurchaseDetailsViewModel {

    private PurchaseRepository mPurchaseRepo;
    private String mPurchaseId;
    private Purchase mPurchase;

    public PurchaseDetailsViewModelImpl(@Nullable Bundle savedState,
                                        @NonNull PurchaseDetailsViewModel.ViewListener view,
                                        @NonNull IdentityRepository identityRepository,
                                        @NonNull UserRepository userRepository,
                                        @NonNull PurchaseRepository purchaseRepo,
                                        @NonNull String purchaseId) {
        super(savedState, view, identityRepository, userRepository);

        mPurchaseRepo = purchaseRepo;
        mPurchaseId = purchaseId;
        if (savedState != null) {
            mItems = new ArrayList<>();
        }
    }

    @Override
    @Bindable
    public String getPurchaseStore() {
        if (mPurchase != null) {
            return mPurchase.getStore();
        } else {
            return "";
        }
    }

    @Override
    @Bindable
    public String getPurchaseDate() {
        if (mPurchase != null) {
            return DateUtils.formatDateLong(mPurchase.getDate());
        } else {
            return "";
        }
    }

    @Override
    public Identity getPurchaseBuyer() {
        return mPurchase.getBuyer();
    }

    @Override
    public void loadData() {
//        if (!isUserInGroup()) {
//            mView.showMessage(R.string.toast_error_purchase_details_group_not);
//            return;
//        }

        mSubscriptions.add(mPurchaseRepo.getPurchaseLocalAsync(mPurchaseId, false)
                .subscribe(new SingleSubscriber<Purchase>() {
                    @Override
                    public void onSuccess(Purchase purchase) {
                        mPurchase = purchase;
                        updateItemList();
                        updateActionBarMenu();
                        updateReadBy();

                        notifyPropertyChanged(BR.purchaseStore);
                        notifyPropertyChanged(BR.purchaseDate);

                        setLoading(false);
                        mView.startPostponedEnterTransition();
                    }

                    @Override
                    public void onError(Throwable error) {
                        setLoading(false);
                        mView.showMessage(R.string.toast_error_purchase_details_load);
                    }
                })
        );
    }

    private void updateItemList() {
        mItems.clear();

        mItems.add(PurchaseDetailsItem.createHeaderInstance(R.string.header_users));
        mItems.add(PurchaseDetailsItem.createUsersInvolvedInstance(mPurchase.getIdentities()));

        mItems.add(PurchaseDetailsItem.createHeaderInstance(R.string.header_items));
        final String groupCurrency = mCurrentIdentity.getGroup().getCurrency();
        for (ParseObject item : mPurchase.getItems()) {
            mItems.add(PurchaseDetailsItem.createItemInstance((Item) item, mCurrentIdentity, groupCurrency));
        }
        final String total = MoneyUtils.formatMoney(mPurchase.getTotalPrice(), groupCurrency);
        final String purchaseCurrency = mPurchase.getCurrency();
        final String totalForeign = MoneyUtils.formatMoney(mPurchase.getTotalPriceForeign(), purchaseCurrency);
        mItems.add(PurchaseDetailsItem.createTotalInstance(total, totalForeign));

        final double share = mPurchase.calculateUserShare(mCurrentIdentity);
        final String myShare = MoneyUtils.formatMoney(share, groupCurrency);
        final String myShareForeign = MoneyUtils.formatMoney(share / mPurchase.getExchangeRate(), purchaseCurrency);
        mItems.add(PurchaseDetailsItem.createMyShareInstance(myShare, myShareForeign));

        final String note = mPurchase.getNote();
        if (note != null) {
            mItems.add(PurchaseDetailsItem.createHeaderInstance(R.string.header_note));
            mItems.add(PurchaseDetailsItem.createNoteInstance(note));
        }

        mView.notifyDataSetChanged();
    }

    /**
     * Checks if the current user is the buyer of the purchase, if yes shows the delete/edit options
     * in the ActionBar of the hosting activity. Checks also if the purchase has a receipt file,
     * if yes shows option to display it in the ActionBar of the hosting activity.
     */
    private void updateActionBarMenu() {
        final List<Identity> identities = mPurchase.getIdentities();
        boolean allUsersAreValid = true;

        for (Identity identity : identities) {
            if (!identity.isActive()) {
                allUsersAreValid = false;
                break;
            }
        }

        boolean userIsBuyer = false;
        if (allUsersAreValid) {
            final String buyerId = mPurchase.getBuyer().getObjectId();
            userIsBuyer = buyerId.equals(mCurrentIdentity.getObjectId());
        }
        boolean hasForeignCurrency = !mCurrentIdentity.getGroup().getCurrency().equals(mPurchase.getCurrency());

        mView.toggleMenuOptions(userIsBuyer, mPurchase.getReceipt() != null, hasForeignCurrency);
    }

    private void updateReadBy() {
        if (!mPurchase.userHasReadPurchase(mCurrentIdentity)) {
            mPurchase.addUserToReadBy(mCurrentIdentity);
            mPurchase.saveEventually();
        }
    }

    @Override
    public void onShowReceiptImageClick() {
        mView.showReceiptImage(mPurchaseId);
    }

    @Override
    public void onEditPurchaseClick() {
        mView.startPurchaseEditScreen(mPurchaseId);
    }

    @Override
    public void onDeletePurchaseClick() {
        mPurchaseRepo.deletePurchase(mPurchase);
        mView.finishScreen(PurchaseDetailsResult.PURCHASE_DELETED);
    }

    @Override
    public void onShowExchangeRateClick() {
        final double exchangeRate = mPurchase.getExchangeRate();
        mView.showMessage(R.string.toast_exchange_rate_value,
                MoneyUtils.formatMoneyNoSymbol(exchangeRate, MoneyUtils.EXCHANGE_RATE_FRACTION_DIGITS));
    }

    @Override
    public int getItemViewType(int position) {
        final PurchaseDetailsItem detailsItem = mItems.get(position);
        return detailsItem.getType();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onIdentitySelected() {
        mView.finishScreen(PurchaseDetailsResult.GROUP_CHANGED);
    }
}
