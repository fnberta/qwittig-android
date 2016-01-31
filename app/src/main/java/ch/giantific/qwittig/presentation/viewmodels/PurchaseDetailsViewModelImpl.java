/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.annotation.SuppressLint;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.PurchaseDetailsItem;
import ch.giantific.qwittig.domain.models.parse.Item;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
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
                                        @NonNull GroupRepository groupRepo,
                                        @NonNull UserRepository userRepository,
                                        @NonNull PurchaseRepository purchaseRepo,
                                        @NonNull String purchaseId) {
        super(savedState, groupRepo, userRepository);

        mPurchaseRepo = purchaseRepo;
        mPurchaseId = purchaseId;
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
    public User getPurchaseBuyer() {
        return mPurchase.getBuyer();
    }

    @Override
    public void updateList() {
        if (!isUserInGroup()) {
            mView.showMessage(R.string.toast_error_purchase_details_group_not);
            return;
        }

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
        mItems.add(PurchaseDetailsItem.createUsersInvolvedInstance(mPurchase.getUsersInvolved()));

        mItems.add(PurchaseDetailsItem.createHeaderInstance(R.string.header_items));
        for (ParseObject item : mPurchase.getItems()) {
            mItems.add(PurchaseDetailsItem.createItemInstance((Item) item, mCurrentUser, mCurrentGroup.getCurrency()));
        }
        final String currency = mCurrentGroup.getCurrency();
        final String total = MoneyUtils.formatMoney(mPurchase.getTotalPrice(), currency);
        final String totalForeign = MoneyUtils.formatMoney(mPurchase.getTotalPriceForeign(), currency);
        mItems.add(PurchaseDetailsItem.createTotalInstance(total, totalForeign));

        final double share = mPurchase.calculateUserShare(mCurrentUser);
        final String myShare = MoneyUtils.formatMoney(share, currency);
        final String myShareForeign = MoneyUtils.formatMoney(share / mPurchase.getExchangeRate(), currency);
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
        final List<ParseUser> usersInvolved = mPurchase.getUsersInvolved();
        boolean allUsersAreValid = true;

        for (ParseUser parseUser : usersInvolved) {
            User user = (User) parseUser;
            if (!user.getGroupIds().contains(mCurrentGroup.getObjectId())) {
                allUsersAreValid = false;
                break;
            }
        }

        boolean userIsBuyer = false;
        if (allUsersAreValid) {
            String buyerId = mPurchase.getBuyer().getObjectId();
            userIsBuyer = buyerId.equals(mCurrentUser.getObjectId());
        }
        boolean hasForeignCurrency = !mCurrentGroup.getCurrency().equals(mPurchase.getCurrency());

        mView.toggleMenuOptions(userIsBuyer, mPurchase.getReceiptParseFile() != null, hasForeignCurrency);
    }

    private void updateReadBy() {
        if (!mPurchase.userHasReadPurchase(mCurrentUser)) {
            mPurchase.addUserToReadBy(mCurrentUser);
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
        mView.finishScreen(RESULT_PURCHASE_DELETED);
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
    public void onNewGroupSet() {
        mView.finishScreen(RESULT_GROUP_CHANGED);
    }
}
