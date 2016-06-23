/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details;

import android.annotation.SuppressLint;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Item;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.purchases.details.items.PurchaseDetailsBaseItem;
import ch.giantific.qwittig.presentation.purchases.details.items.PurchaseDetailsHeaderItem;
import ch.giantific.qwittig.presentation.purchases.details.items.PurchaseDetailsIdentitiesItem;
import ch.giantific.qwittig.presentation.purchases.details.items.PurchaseDetailsItem;
import ch.giantific.qwittig.presentation.purchases.details.items.PurchaseDetailsMyShareItem;
import ch.giantific.qwittig.presentation.purchases.details.items.PurchaseDetailsNoteItem;
import ch.giantific.qwittig.presentation.purchases.details.items.PurchaseDetailsTotalItem;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.SingleSubscriber;

/**
 * Provides an implementation of the {@link PurchaseDetailsViewModel}.
 */
public class PurchaseDetailsViewModelImpl extends ListViewModelBaseImpl<PurchaseDetailsBaseItem, PurchaseDetailsViewModel.ViewListener>
        implements PurchaseDetailsViewModel {

    private static final String STATE_RECEIPT_SHOWN = "STATE_RECEIPT_SHOWN";
    private final Navigator mNavigator;
    private final PurchaseRepository mPurchaseRepo;
    private final String mPurchaseId;
    private final NumberFormat mMoneyFormatter;
    private final DateFormat mDateFormatter;
    private Purchase mPurchase;
    private boolean mReceiptShown;

    public PurchaseDetailsViewModelImpl(@Nullable Bundle savedState,
                                        @NonNull Navigator navigator,
                                        @NonNull RxBus<Object> eventBus,
                                        @NonNull UserRepository userRepository,
                                        @NonNull PurchaseRepository purchaseRepo,
                                        @NonNull String purchaseId) {
        super(savedState, eventBus, userRepository);

        mNavigator = navigator;
        mPurchaseRepo = purchaseRepo;
        mPurchaseId = purchaseId;
        final String groupCurrency = mCurrentIdentity.getGroup().getCurrency();
        mMoneyFormatter = MoneyUtils.getMoneyFormatter(groupCurrency, true, true);
        mDateFormatter = DateUtils.getDateFormatter(false);

        if (savedState != null) {
            mItems = new ArrayList<>();
            mReceiptShown = savedState.getBoolean(STATE_RECEIPT_SHOWN);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_RECEIPT_SHOWN, mReceiptShown);
    }

    @Override
    public void setReceiptShown(boolean receiptShown) {
        mReceiptShown = receiptShown;
    }

    @Override
    @Bindable
    public String getPurchaseStore() {
        return mPurchase != null ? mPurchase.getStore() : "";
    }

    @Override
    @Bindable
    public String getPurchaseDate() {
        return mPurchase != null ? mDateFormatter.format(mPurchase.getDate()) : "";
    }

    @Override
    public Identity getPurchaseBuyer() {
        return mPurchase.getBuyer();
    }

    @Override
    @Bindable
    public String getReceiptImage() {
        return mPurchase != null ? mPurchase.getReceiptUrl() : "";
    }

    @Override
    public void loadData() {
        getSubscriptions().add(mPurchaseRepo.getPurchase(mPurchaseId)
                .subscribe(new SingleSubscriber<Purchase>() {
                    @Override
                    public void onSuccess(Purchase purchase) {
                        mPurchase = purchase;
                        if (mReceiptShown) {
                            updateActionBarMenu();
                            notifyPropertyChanged(BR.receiptImage);
                        } else {
                            updateActionBarMenu();
                            updateItemList();
                            updateReadBy();
                            setLoading(false);
                        }

                        notifyPropertyChanged(BR.purchaseStore);
                        notifyPropertyChanged(BR.purchaseDate);

                        mView.startEnterTransition();
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

        mItems.add(new PurchaseDetailsHeaderItem(R.string.header_users));
        mItems.add(new PurchaseDetailsIdentitiesItem(mPurchase.getIdentities()));

        mItems.add(new PurchaseDetailsHeaderItem(R.string.header_items));
        for (Item item : mPurchase.getItems()) {
            mItems.add(new PurchaseDetailsItem(item, mCurrentIdentity, mMoneyFormatter));
        }

        final NumberFormat foreignFormatter = MoneyUtils.getMoneyFormatter(mPurchase.getCurrency(), true, true);
        final String total = mMoneyFormatter.format(mPurchase.getTotalPrice());
        final String totalForeign = foreignFormatter.format(mPurchase.getTotalPriceForeign());
        mItems.add(new PurchaseDetailsTotalItem(total, totalForeign));

        final double share = mPurchase.calculateUserShare(mCurrentIdentity);
        final String myShare = mMoneyFormatter.format(share);
        final String myShareForeign = foreignFormatter.format(share / mPurchase.getExchangeRate());
        mItems.add(new PurchaseDetailsMyShareItem(myShare, myShareForeign));

        final String note = mPurchase.getNoteOrEmpty();
        if (!TextUtils.isEmpty(note)) {
            mItems.add(new PurchaseDetailsHeaderItem(R.string.header_note));
            mItems.add(new PurchaseDetailsNoteItem(note));
        }

        mListInteraction.notifyDataSetChanged();
    }

    /**
     * Checks if the current user is the buyer of the purchase, if yes shows the delete/edit options
     * in the ActionBar of the hosting activity. Checks also if the purchase has a receipt file,
     * if yes shows option to display it in the ActionBar of the hosting activity.
     */
    private void updateActionBarMenu() {
        final List<Identity> identities = mPurchase.getIdentities();
        boolean valid = !TextUtils.isEmpty(mPurchase.getObjectId());
        for (Identity identity : identities) {
            if (!identity.isActive()) {
                valid = false;
                break;
            }
        }

        boolean showEdit = false;
        if (valid) {
            final String buyerId = mPurchase.getBuyer().getObjectId();
            showEdit = Objects.equals(buyerId, mCurrentIdentity.getObjectId());
        }
        final boolean foreignCurrency = !Objects.equals(mCurrentIdentity.getGroup().getCurrency(), mPurchase.getCurrency());
        final boolean receiptImage = !TextUtils.isEmpty(mPurchase.getReceiptUrl());
        mView.toggleMenuOptions(showEdit, receiptImage, foreignCurrency);
    }

    private void updateReadBy() {
        if (!mPurchase.isRead(mCurrentIdentity)) {
            mPurchase.addUserToReadBy(mCurrentIdentity);
            mPurchase.saveEventually();
        }
    }

    @Override
    public void onShowReceiptImageClick() {
        setLoading(true);
        mView.showPurchaseDetailsReceipt();
    }

    @Override
    public void onEditPurchaseClick() {
        mNavigator.startPurchaseEdit(mPurchaseId);
    }

    @Override
    public void onDeletePurchaseClick() {
        mPurchaseRepo.deletePurchase(mPurchase);
        mNavigator.finish(PurchaseDetailsResult.PURCHASE_DELETED);
    }

    @Override
    public void onShowExchangeRateClick() {
        final double exchangeRate = mPurchase.getExchangeRate();
        final NumberFormat formatter = MoneyUtils.getExchangeRateFormatter();
        mView.showMessage(R.string.toast_exchange_rate_value, formatter.format(exchangeRate));
    }

    @Override
    public int getItemViewType(int position) {
        final PurchaseDetailsBaseItem detailsItem = mItems.get(position);
        return detailsItem.getType();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onIdentitySelected(@NonNull Identity identitySelected) {
        mNavigator.finish(PurchaseDetailsResult.GROUP_CHANGED);
    }
}
