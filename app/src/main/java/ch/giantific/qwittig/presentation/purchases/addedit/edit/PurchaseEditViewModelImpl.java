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
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Item;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddViewModelImpl;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItem;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemModel;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemModel.Type;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link PurchaseAddEditViewModel} for the edit purchase screen.
 * <p/>
 * Subclass of {@link PurchaseAddViewModelImpl}.
 */
public class PurchaseEditViewModelImpl extends PurchaseAddViewModelImpl {

    private static final String STATE_ITEMS_SET = "STATE_ITEMS_SET";
    final String mEditPurchaseId;
    Purchase mEditPurchase;
    private boolean mDeleteOldReceipt;
    private boolean mOldValuesSet;

    @SuppressWarnings("SimplifiableIfStatement")
    public PurchaseEditViewModelImpl(@Nullable Bundle savedState,
                                     @NonNull Navigator navigator,
                                     @NonNull RxBus<Object> eventBus,
                                     @NonNull RemoteConfigHelper configHelper,
                                     @NonNull UserRepository userRepository,
                                     @NonNull PurchaseRepository purchaseRepository,
                                     @NonNull String editPurchaseId) {
        super(savedState, navigator, eventBus, userRepository, purchaseRepository, configHelper);

        mEditPurchaseId = editPurchaseId;

        if (savedState != null) {
            mOldValuesSet = savedState.getBoolean(STATE_ITEMS_SET, false);
        } else {
            mOldValuesSet = false;
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_ITEMS_SET, mOldValuesSet);
    }

    @Override
    protected void loadPurchase(@NonNull FirebaseUser currentUser) {
        getSubscriptions().add(getInitialChain(currentUser)
                .flatMap(new Func1<List<Identity>, Single<Purchase>>() {
                    @Override
                    public Single<Purchase> call(List<Identity> identities) {
                        return mPurchaseRepo.getPurchase(mEditPurchaseId, isDraft());
                    }
                })
                .doOnSuccess(new Action1<Purchase>() {
                    @Override
                    public void call(Purchase purchase) {
                        mEditPurchase = purchase;
                    }
                })
                .subscribe(new SingleSubscriber<Purchase>() {
                    @Override
                    public void onSuccess(Purchase purchase) {
                        if (mOldValuesSet) {
                            updateRows();
                        } else {
                            setOldPurchaseValues(purchase);
                            setOldItemValues();
                            mOldValuesSet = true;
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_purchase_edit_load);
                    }
                })
        );
    }

    protected boolean isDraft() {
        return false;
    }

    private void setOldPurchaseValues(@NonNull Purchase purchase) {
        setNote(purchase.getNote());
        mView.reloadOptionsMenu();

        setStore(purchase.getStore());
        setDate(purchase.getDateDate());
        setCurrency(purchase.getCurrency());
        setExchangeRate(purchase.getExchangeRate());
        setReceipt(purchase.getReceipt());
    }

    private void setOldItemValues() {
        final List<Item> oldItems = mEditPurchase.getItems();
        for (Item item : oldItems) {
            final Set<String> identities = item.getIdentitiesIds();
            final String price = mMoneyFormatter.format(item.getPriceForeign(mExchangeRate));
            final PurchaseAddEditItem purchaseAddEditItem =
                    new PurchaseAddEditItem(item.getName(), price, getItemUsers(identities));
            purchaseAddEditItem.setMoneyFormatter(mMoneyFormatter);
            purchaseAddEditItem.setPriceChangedListener(this);
            final int pos = getItemCount() - 2;
            mItems.add(pos, purchaseAddEditItem);
            mListInteraction.notifyItemInserted(pos);
        }
    }

    @Override
    protected void savePurchase(Purchase purchase, boolean asDraft) {
        if (asDraft) {
            mPurchaseRepo.saveDraft(purchase, mEditPurchaseId);
        } else {
            mPurchaseRepo.savePurchase(purchase, mEditPurchaseId, isDraft());
        }
    }

    @Override
    public void onDeleteReceiptMenuClick() {
        super.onDeleteReceiptMenuClick();

        mDeleteOldReceipt = true;
    }

    @Override
    public void onExitClick() {
        if (changesWereMade()) {
            mView.showDiscardEditChangesDialog();
        } else {
            mNavigator.finish(Activity.RESULT_CANCELED);
        }
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean changesWereMade() {
        if (mEditPurchase.getDateDate().compareTo(mDate) != 0
                || !Objects.equals(mEditPurchase.getStore(), mStore)
                || !Objects.equals(mEditPurchase.getCurrency(), mCurrency)
                || !Objects.equals(mEditPurchase.getNote(), mNote)) {
            return true;
        }

        final List<Item> oldItems = mEditPurchase.getItems();
        for (int i = 0, size = mItems.size(), skipCount = 0; i < size; i++) {
            final PurchaseAddEditItemModel addEditItem = mItems.get(i);
            if (addEditItem.getType() != Type.ITEM) {
                skipCount++;
                continue;
            }

            final Item itemOld;
            try {
                itemOld = oldItems.get(i - skipCount);
            } catch (IndexOutOfBoundsException e) {
                return true;
            }
            final PurchaseAddEditItem purchaseAddEditItem = (PurchaseAddEditItem) addEditItem;
            if (!Objects.equals(itemOld.getName(), purchaseAddEditItem.getName())) {
                return true;
            }

            final double oldPrice = itemOld.getPriceForeign(mEditPurchase.getExchangeRate());
            final double newPrice = purchaseAddEditItem.parsePrice();
            if (Math.abs(oldPrice - newPrice) >= MoneyUtils.MIN_DIFF) {
                return true;
            }

            final Set<String> identitiesOld = itemOld.getIdentitiesIds();
            final List<String> identitiesNew = purchaseAddEditItem.getSelectedIdentitiesIds();
            if (!identitiesNew.containsAll(identitiesOld) ||
                    !identitiesOld.containsAll(identitiesNew)) {
                return true;
            }
        }

        if (mDeleteOldReceipt || !Objects.equals(mReceipt, mEditPurchase.getReceipt())) {
            return true;
        }

        return false;
    }
}
