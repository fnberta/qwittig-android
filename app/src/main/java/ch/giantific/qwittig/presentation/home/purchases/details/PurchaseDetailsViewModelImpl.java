/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details;

import android.annotation.SuppressLint;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Item;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.home.purchases.details.items.PurchaseDetailsBaseItem;
import ch.giantific.qwittig.presentation.home.purchases.details.items.PurchaseDetailsHeaderItem;
import ch.giantific.qwittig.presentation.home.purchases.details.items.PurchaseDetailsIdentitiesItem;
import ch.giantific.qwittig.presentation.home.purchases.details.items.PurchaseDetailsItem;
import ch.giantific.qwittig.presentation.home.purchases.details.items.PurchaseDetailsMyShareItem;
import ch.giantific.qwittig.presentation.home.purchases.details.items.PurchaseDetailsNoteItem;
import ch.giantific.qwittig.presentation.home.purchases.details.items.PurchaseDetailsTotalItem;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.SingleSubscriber;

/**
 * Provides an implementation of the {@link PurchaseDetailsViewModel}.
 */
public class PurchaseDetailsViewModelImpl extends ListViewModelBaseImpl<PurchaseDetailsBaseItem, PurchaseDetailsViewModel.ViewListener>
        implements PurchaseDetailsViewModel {

    private final PurchaseRepository mPurchaseRepo;
    private final String mPurchaseId;
    private final NumberFormat mMoneyFormatter;
    private final DateFormat mDateFormatter;
    private Purchase mPurchase;

    public PurchaseDetailsViewModelImpl(@Nullable Bundle savedState,
                                        @NonNull PurchaseDetailsViewModel.ViewListener view,
                                        @NonNull UserRepository userRepository,
                                        @NonNull PurchaseRepository purchaseRepo,
                                        @NonNull String purchaseId) {
        super(savedState, view, userRepository);

        mPurchaseRepo = purchaseRepo;
        mPurchaseId = purchaseId;
        final String groupCurrency = mCurrentIdentity.getGroup().getCurrency();
        mMoneyFormatter = MoneyUtils.getMoneyFormatter(groupCurrency, true, true);
        mDateFormatter = DateUtils.getDateFormatter(false);

        if (savedState != null) {
            mItems = new ArrayList<>();
        }
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
    public void loadData() {
        getSubscriptions().add(mPurchaseRepo.getPurchase(mPurchaseId, false)
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

        final String note = mPurchase.getNote();
        if (note != null) {
            mItems.add(new PurchaseDetailsHeaderItem(R.string.header_note));
            mItems.add(new PurchaseDetailsNoteItem(note));
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

        boolean showEdit = false;
        if (allUsersAreValid) {
            final String buyerId = mPurchase.getBuyer().getObjectId();
            showEdit = buyerId.equals(mCurrentIdentity.getObjectId());
        }
        final boolean foreignCurrency = !mCurrentIdentity.getGroup().getCurrency().equals(mPurchase.getCurrency());
        final boolean receiptImage = mPurchase.getReceipt() != null;
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
    public void onIdentitySelected() {
        mView.finishScreen(PurchaseDetailsResult.GROUP_CHANGED);
    }
}
