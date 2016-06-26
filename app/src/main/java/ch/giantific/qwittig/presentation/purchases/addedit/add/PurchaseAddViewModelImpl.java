/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.add;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Item;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel.ViewListener;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditGenericItem;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditHeaderItem;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItem;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemModel;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemModel.Type;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemUsers;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemUsersUser;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MessageAction;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Single;
import rx.SingleSubscriber;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link PurchaseAddEditViewModel} for the add purchase screen.
 */
public class PurchaseAddViewModelImpl extends ListViewModelBaseImpl<PurchaseAddEditItemModel, ViewListener>
        implements PurchaseAddEditViewModel {

    private static final String STATE_ROW_ITEMS = "STATE_ROW_ITEMS";
    private static final String STATE_DATE = "STATE_DATE";
    private static final String STATE_STORE = "STATE_STORE";
    private static final String STATE_CURRENCY = "STATE_CURRENCY";
    private static final String STATE_EXCHANGE_RATE = "STATE_EXCHANGE_RATE";
    private static final String STATE_NOTE = "STATE_NOTE";
    private static final String STATE_RECEIPT_IMAGE_PATH = "STATE_RECEIPT_IMAGE_PATH";
    private static final String STATE_FETCHING_RATES = "STATE_FETCHING_RATES";
    private static final String STATE_RECEIPT_OR_NOTE_SHOWN = "STATE_RECEIPT_OR_NOTE_SHOWN";
    protected final PurchaseRepository mPurchaseRepo;
    protected final Group mCurrentGroup;
    protected final Navigator mNavigator;
    private final NumberFormat mExchangeRateFormatter;
    private final List<String> mSupportedCurrencies = Arrays.asList(MoneyUtils.SUPPORTED_CURRENCIES);
    private final DateFormat mDateFormatter;
    protected String mCurrency;
    protected String mReceiptImagePath;
    protected String mNote;
    protected Date mDate;
    protected String mStore;
    protected double mTotalPrice = 0;
    protected double mExchangeRate;
    protected NumberFormat mMoneyFormatter;
    protected List<Identity> mIdentities = new ArrayList<>();
    protected boolean mNoteShown;
    private double mMyShare = 0;
    private boolean mFetchingExchangeRates;

    public PurchaseAddViewModelImpl(@Nullable Bundle savedState,
                                    @NonNull Navigator navigator,
                                    @NonNull RxBus<Object> eventBus,
                                    @NonNull UserRepository userRepository,
                                    @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, eventBus, userRepository);

        mNavigator = navigator;
        mPurchaseRepo = purchaseRepo;
        mCurrentGroup = mCurrentIdentity.getGroup();

        if (savedState != null) {
            mItems = savedState.getParcelableArrayList(STATE_ROW_ITEMS);
            mDate = new Date(savedState.getLong(STATE_DATE));
            mStore = savedState.getString(STATE_STORE);
            setCurrency(savedState.getString(STATE_CURRENCY, mCurrentGroup.getCurrency()));
            mExchangeRate = savedState.getDouble(STATE_EXCHANGE_RATE);
            mNote = savedState.getString(STATE_NOTE);
            mReceiptImagePath = savedState.getString(STATE_RECEIPT_IMAGE_PATH);
            mFetchingExchangeRates = savedState.getBoolean(STATE_FETCHING_RATES);
            mNoteShown = savedState.getBoolean(STATE_RECEIPT_OR_NOTE_SHOWN);
        } else {
            initFixedRows();
            mLoading = false;
            mDate = new Date();
            mCurrency = mCurrentGroup.getCurrency();
            mExchangeRate = 1;
        }

        mMoneyFormatter = MoneyUtils.getMoneyFormatter(mCurrency, false, true);
        mDateFormatter = DateUtils.getDateFormatter(false);
        mExchangeRateFormatter = MoneyUtils.getExchangeRateFormatter();
    }

    private void initFixedRows() {
        mItems.add(new PurchaseAddEditHeaderItem(R.string.header_purchase));
        mItems.add(PurchaseAddEditGenericItem.createNewDateInstance());
        mItems.add(PurchaseAddEditGenericItem.createNewStoreInstance());
        mItems.add(new PurchaseAddEditHeaderItem(R.string.header_items));
        mItems.add(PurchaseAddEditGenericItem.createNewAddRowInstance());
        mItems.add(PurchaseAddEditGenericItem.createNewTotalInstance());
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelableArrayList(STATE_ROW_ITEMS, mItems);
        outState.putLong(STATE_DATE, mDate.getTime());
        outState.putString(STATE_STORE, mStore);
        outState.putString(STATE_CURRENCY, mCurrency);
        outState.putDouble(STATE_EXCHANGE_RATE, mExchangeRate);
        outState.putString(STATE_NOTE, mNote);
        outState.putString(STATE_RECEIPT_IMAGE_PATH, mReceiptImagePath);
        outState.putBoolean(STATE_FETCHING_RATES, mFetchingExchangeRates);
        outState.putBoolean(STATE_RECEIPT_OR_NOTE_SHOWN, mNoteShown);
    }

    public void setNoteShown(boolean noteShown) {
        mNoteShown = noteShown;
    }

    @Override
    @Bindable
    public String getReceiptImage() {
        return mReceiptImagePath;
    }

    @Override
    public void setReceiptImage(@NonNull String receiptImagePath) {
        mReceiptImagePath = receiptImagePath;
        notifyPropertyChanged(BR.receiptImage);
        notifyPropertyChanged(BR.receiptImageAvailable);
    }

    @Override
    @Bindable
    public boolean isReceiptImageAvailable() {
        return !TextUtils.isEmpty(mReceiptImagePath);
    }

    @Override
    @Bindable
    public String getNote() {
        return mNote;
    }

    @Override
    public void setNote(@NonNull String note) {
        mNote = note;
        notifyPropertyChanged(BR.note);
    }

    @Override
    public boolean isNoteAvailable() {
        return !TextUtils.isEmpty(mNote);
    }

    @Override
    public NumberFormat getMoneyFormatter() {
        return mMoneyFormatter;
    }

    @Override
    public List<String> getSupportedCurrencies() {
        return mSupportedCurrencies;
    }

    @Override
    @Bindable
    public String getDate() {
        return mDateFormatter.format(mDate);
    }

    @Override
    public void setDate(@NonNull Date date) {
        mDate = date;
        notifyPropertyChanged(BR.date);
    }

    @Override
    public void onDateSet(@NonNull Date date) {
        setDate(date);
    }

    @Override
    public void onDateClick(View view) {
        mView.showDatePickerDialog();
    }

    @Override
    @Bindable
    public String getStore() {
        return mStore;
    }

    @Override
    public void setStore(@NonNull String store) {
        mStore = store;
        notifyPropertyChanged(BR.store);
    }

    @Override
    @Bindable
    public String getTotalPrice() {
        return mMoneyFormatter.format(mTotalPrice);
    }

    @Override
    public void setTotalPrice(double totalPrice) {
        mTotalPrice = totalPrice;
        notifyPropertyChanged(BR.totalPrice);
    }

    @Override
    @Bindable
    public String getMyShare() {
        return mMoneyFormatter.format(mMyShare);
    }

    @Override
    public void setMyShare(double myShare) {
        mMyShare = myShare;
        notifyPropertyChanged(BR.myShare);
    }

    @Override
    @Bindable
    public String getCurrency() {
        return mCurrency;
    }

    @Override
    public void setCurrency(@NonNull String currency) {
        mCurrency = currency;
        notifyPropertyChanged(BR.currencySelected);
        notifyPropertyChanged(BR.currency);
    }

    @Override
    @Bindable
    public int getCurrencySelected() {
        return mSupportedCurrencies.indexOf(mCurrency);
    }

    @Override
    public void onCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final String currency = (String) parent.getItemAtPosition(position);
        if (Objects.equals(currency, mCurrency)) {
            return;
        }

        mCurrency = currency;
        mMoneyFormatter = MoneyUtils.getMoneyFormatter(currency, false, true);

        // TODO: only needed once we support currencies with other than 2 decimal values
//        updateItemsPriceFormatting();
        // update total price and my share formatting
//        notifyPropertyChanged(BR.totalPrice);
//        notifyPropertyChanged(BR.myShare);

        // update my share currency field
        notifyPropertyChanged(BR.currency);

        // get new exchange rate
        mView.loadFetchExchangeRatesWorker(mCurrentGroup.getCurrency(), mCurrency);
    }

    private void updateItemsPriceFormatting() {
        for (int i = 0, size = mItems.size(); i < size; i++) {
            final PurchaseAddEditItemModel addEditItem = mItems.get(i);
            if (addEditItem.getType() == Type.ITEM) {
                final PurchaseAddEditItem purchaseAddEditItem = (PurchaseAddEditItem) addEditItem;
                purchaseAddEditItem.updatePriceFormat(mMoneyFormatter);
            }
        }
    }

    @Override
    @Bindable
    public String getExchangeRate() {
        return mExchangeRateFormatter.format(mExchangeRate);
    }

    @Override
    public void setExchangeRate(double exchangeRate) {
        mExchangeRate = exchangeRate;
        notifyPropertyChanged(BR.exchangeRate);
        notifyPropertyChanged(BR.exchangeRateVisible);
    }

    @Override
    @Bindable
    public boolean isExchangeRateVisible() {
        return mExchangeRate != 1;
    }

    @Override
    public void onExchangeRateClick(View view) {
        mView.showManualExchangeRateSelectorDialog(((TextView) view).getText().toString());
    }

    @Override
    public void onExchangeRateManuallySet(double exchangeRate) {
        final BigDecimal roundedExchangeRate = MoneyUtils.roundExchangeRate(exchangeRate);
        setExchangeRate(roundedExchangeRate.doubleValue());
    }

    @Override
    public void setRateFetchStream(@NonNull Single<Float> single,
                                   @NonNull final String workerTag) {
        getSubscriptions().add(single
                .subscribe(new SingleSubscriber<Float>() {
                    @Override
                    public void onSuccess(Float exchangeRate) {
                        mView.removeWorker(workerTag);
                        mFetchingExchangeRates = false;

                        setExchangeRate(exchangeRate);
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        mFetchingExchangeRates = false;

                        mView.showMessageWithAction(R.string.toast_error_exchange_rate,
                                new MessageAction(R.string.action_retry) {
                                    @Override
                                    public void onClick(View v) {
                                        mView.loadFetchExchangeRatesWorker(mCurrentGroup.getCurrency(), mCurrency);
                                    }
                                });
                    }
                })
        );
    }

    @Override
    public void onAddEditNoteMenuClick() {
        mView.showAddEditNoteDialog(mNote);
    }

    @Override
    public void onShowNoteMenuClick() {
        mView.showNote(mNote);
    }

    @Override
    public void onDeleteNoteMenuClick() {
        onNoteSet("");
        mView.showPurchaseItems();
    }

    @Override
    public void onNoteSet(@NonNull String note) {
        setNote(note);
        mView.showMessage(TextUtils.isEmpty(note) ? R.string.toast_note_deleted : R.string.toast_note_added);
        mView.reloadOptionsMenu();
    }

    @Override
    public void onRowPriceChanged() {
        updateTotalAndMyShare();
    }

    private void updateTotalAndMyShare() {
        double totalPrice = 0;
        double myShare = 0;
        final String currentIdentityId = mCurrentIdentity.getObjectId();
        for (PurchaseAddEditItemModel addEditItem : mItems) {
            if (addEditItem.getType() != Type.ITEM) {
                continue;
            }

            final PurchaseAddEditItem purchaseAddEditItem = (PurchaseAddEditItem) addEditItem;
            final double itemPrice = purchaseAddEditItem.parsePrice();

            // update total price
            totalPrice += itemPrice;

            // update my share
            final PurchaseAddEditItemUsersUser[] itemUsersRows = purchaseAddEditItem.getUsers();
            int selectedCount = 0;
            boolean currentIdentityInvolved = false;
            for (PurchaseAddEditItemUsersUser addEditPurchaseItemUsersUser : itemUsersRows) {
                if (!addEditPurchaseItemUsersUser.isSelected()) {
                    continue;
                }

                selectedCount++;
                if (Objects.equals(addEditPurchaseItemUsersUser.getObjectId(), currentIdentityId)) {
                    currentIdentityInvolved = true;
                }
            }
            if (currentIdentityInvolved) {
                myShare += (itemPrice / selectedCount);
            }
        }

        setTotalPrice(totalPrice);
        setMyShare(myShare);
    }

    @Override
    public void loadData() {
        getSubscriptions().add(mUserRepo.getIdentities(mCurrentGroup, true)
                .toSortedList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<Identity>>() {
                    @Override
                    public void onSuccess(List<Identity> identities) {
                        identities.remove(mCurrentIdentity);
                        identities.add(0, mCurrentIdentity);
                        mIdentities = identities;

                        if (!mNoteShown) {
                            updateRows();
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_purchase_add_edit_load);
                    }
                })
        );
    }

    protected final void updateRows() {
        boolean hasItems = false;
        for (int i = 0, size = mItems.size(); i < size; i++) {
            final PurchaseAddEditItemModel addEditItem = mItems.get(i);

            if (addEditItem.getType() == Type.ITEM) {
                hasItems = true;

                final PurchaseAddEditItem purchaseAddEditItem = (PurchaseAddEditItem) addEditItem;
                purchaseAddEditItem.setMoneyFormatter(mMoneyFormatter);
                purchaseAddEditItem.setPriceChangedListener(this);
                continue;
            }

            // fill with one row on first start
            if (addEditItem.getType() == Type.ADD_ROW && !hasItems) {
                final PurchaseAddEditItem purchaseAddEditItem = new PurchaseAddEditItem(getItemUsers(mIdentities));
                purchaseAddEditItem.setMoneyFormatter(mMoneyFormatter);
                purchaseAddEditItem.setPriceChangedListener(this);
                mItems.add(i, purchaseAddEditItem);
                mListInteraction.notifyDataSetChanged();
                return;
            }
        }

        // hack: run another update total and my share run because after configuration change,
        // recycler is not yet able to receive the update calls before
        updateTotalAndMyShare();
    }

    protected final PurchaseAddEditItemUsersUser[] getItemUsers(@NonNull List<Identity> identities) {
        final int size = mIdentities.size();
        final PurchaseAddEditItemUsersUser[] itemUsersRow = new PurchaseAddEditItemUsersUser[size];
        for (int i = 0; i < size; i++) {
            final Identity identity = mIdentities.get(i);
            itemUsersRow[i] = new PurchaseAddEditItemUsersUser(identity.getObjectId(),
                    identity.getNickname(), identity.getAvatarUrl(), identities.contains(identity));
        }

        return itemUsersRow;
    }


    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    @Override
    public void onItemDismiss(int position) {
        mItems.remove(position);
        if (getItemViewType(position) != Type.USERS) {
            mListInteraction.notifyItemRemoved(position);
        } else {
            mItems.remove(position);
            mListInteraction.notifyItemRangeRemoved(position, 2);
        }
        onRowPriceChanged();
    }

    @Override
    public void onTooFewUsersSelected() {
        mView.showMessage(R.string.toast_min_one_user);
    }

    @Override
    public void onItemRowUserClick(int position) {
        for (int i = 0, mItemsSize = mItems.size(); i < mItemsSize; i++) {
            final PurchaseAddEditItemModel item = mItems.get(i);
            if (item.getType() != Type.USERS) {
                continue;
            }

            final PurchaseAddEditItem addEditItem = (PurchaseAddEditItem) getItemAtPosition(i - 1);
            addEditItem.notifyPropertyChanged(BR.users);
        }

        updateTotalAndMyShare();
    }

    @Override
    public void onToggleUsersClick(@NonNull PurchaseAddEditItem itemModel) {
        final int insertPos = mItems.indexOf(itemModel) + 1;
        if (mItems.size() < insertPos) {
            expandItemRow(insertPos, itemModel);
        } else if (getItemViewType(insertPos) == Type.USERS) {
            collapseItemRow(insertPos);
        } else {
            expandItemRow(insertPos, itemModel);
        }
    }

    private void collapseItemRow(int pos) {
        mItems.remove(pos);
        mListInteraction.notifyItemRemoved(pos);
    }

    private void expandItemRow(int pos, PurchaseAddEditItemModel parent) {
        final PurchaseAddEditItem purchaseAddEditItem = (PurchaseAddEditItem) parent;
        mItems.add(pos, new PurchaseAddEditItemUsers(purchaseAddEditItem.getUsers()));
        mListInteraction.notifyItemInserted(pos);

        collapseOtherItemRows(pos);
    }

    private void collapseOtherItemRows(int insertPos) {
        int pos = 0;
        for (Iterator<PurchaseAddEditItemModel> iterator = mItems.iterator(); iterator.hasNext(); ) {
            final int type = iterator.next().getType();
            if (type == Type.USERS && pos != insertPos) {
                iterator.remove();
                mListInteraction.notifyItemRemoved(pos);
            }

            pos++;
        }
    }

    @Override
    public void onAddRowClick(@NonNull PurchaseAddEditItemModel itemModel) {
        final int position = mItems.indexOf(itemModel);

        final PurchaseAddEditItem purchaseAddEditItem = new PurchaseAddEditItem(getItemUsers(mIdentities));
        purchaseAddEditItem.setMoneyFormatter(mMoneyFormatter);
        purchaseAddEditItem.setPriceChangedListener(this);
        mItems.add(position, purchaseAddEditItem);
        mListInteraction.notifyItemInserted(position);
        mListInteraction.scrollToPosition(position + 1);
    }

    @Override
    public void onAddEditReceiptImageMenuClick() {
        mView.captureImage();
    }

    @Override
    public void onReceiptImageTaken(@NonNull String receiptImagePath) {
        setReceiptImage(receiptImagePath);
        mView.reloadOptionsMenu();
        mView.showMessage(TextUtils.isEmpty(mReceiptImagePath) ? R.string.toast_receipt_added : R.string.toast_receipt_changed);
    }

    @Override
    public void onReceiptImageTakeFailed() {
        mView.showMessage(R.string.toast_create_image_file_failed);
    }

    @Override
    public void onDeleteReceiptMenuClick() {
        deleteReceiptImage();
        setReceiptImage("");

        mView.showMessage(R.string.toast_receipt_deleted);
        mView.reloadOptionsMenu();
    }

    private void deleteReceiptImage() {
        final File receipt = new File(mReceiptImagePath);
        if (!receipt.delete()) {
            Timber.w("failed to delete file");
        }
    }

    @Override
    public void onFabSavePurchaseClick(View view) {
        if (mFetchingExchangeRates) {
            mView.showMessage(R.string.toast_exchange_rate_fetching);
            return;
        }

        if (!Objects.equals(mCurrency, mCurrentGroup.getCurrency()) && mExchangeRate == 1) {
            mView.showMessageWithAction(R.string.toast_exchange_no_data,
                    new MessageAction(R.string.action_purchase_save_draft) {
                        @Override
                        public void onClick(View v) {
                            onSaveAsDraftMenuClick();
                        }
                    });
            return;
        }

        if (TextUtils.isEmpty(mStore)) {
            mView.showMessage(R.string.toast_store_empty);
            return;
        }

        if (!validateItems()) {
            return;
        }

        savePurchase(false);
    }

    private void savePurchase(final boolean asDraft) {
        final Purchase purchase = getPurchase();
        if (asDraft) {
            purchase.setDraft(true);
        }

        getSubscriptions().add(getSavePurchaseAction(purchase)
                .subscribe(new SingleSubscriber<Purchase>() {
                    @Override
                    public void onSuccess(Purchase purchase) {
                        onPurchaseSaved(asDraft);
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(asDraft
                                ? R.string.toast_error_purchase_save_draft
                                : R.string.toast_error_purchase_save);
                    }
                })
        );
    }

    protected Single<Purchase> getSavePurchaseAction(@NonNull Purchase purchase) {
        return mPurchaseRepo.savePurchase(purchase);
    }

    protected void onPurchaseSaved(boolean asDraft) {
        mNavigator.finish(asDraft ? PurchaseResult.PURCHASE_DRAFT : PurchaseResult.PURCHASE_SAVED);
    }

    @Override
    public void onSaveAsDraftMenuClick() {
        if (mFetchingExchangeRates) {
            mView.showMessage(R.string.toast_exchange_rate_fetching);
            return;
        }

        savePurchase(true);
    }

    private boolean validateItems() {
        boolean hasItem = false;
        boolean allValid = true;
        for (PurchaseAddEditItemModel addEditItem : mItems) {
            if (addEditItem.getType() != Type.ITEM) {
                continue;
            }

            hasItem = true;
            final PurchaseAddEditItem itemRow = (PurchaseAddEditItem) addEditItem;
            if (!itemRow.validateFields()) {
                allValid = false;
            }
        }

        if (!hasItem) {
            mView.showMessage(R.string.toast_min_one_item);
            return false;
        }

        return allValid;
    }

    private Purchase getPurchase() {
        final List<Identity> purchaseIdentities = new ArrayList<>();
        final List<Item> purchaseItems = new ArrayList<>();
        final int fractionDigits = MoneyUtils.getFractionDigits(mCurrency);

        for (PurchaseAddEditItemModel addEditItem : mItems) {
            if (addEditItem.getType() != Type.ITEM) {
                continue;
            }

            final PurchaseAddEditItem itemRow = (PurchaseAddEditItem) addEditItem;
            final String rowItemName = itemRow.getName();
            final String name = rowItemName != null ? rowItemName : "";
            final String price = itemRow.getPrice();

            final List<Identity> identities = getIdentities(itemRow.getUsers());
            for (Identity identity : identities) {
                if (!purchaseIdentities.contains(identity)) {
                    purchaseIdentities.add(identity);
                }
            }

            final Item item = new Item(name, roundPrice(price, fractionDigits), identities, mCurrentGroup);
            purchaseItems.add(item);
        }

        return createPurchase(purchaseIdentities, purchaseItems, fractionDigits);
    }

    private BigDecimal roundPrice(@NonNull String price, int fractionDigits) {
        try {
            return new BigDecimal(price).setScale(fractionDigits, BigDecimal.ROUND_HALF_UP);
        } catch (NumberFormatException e) {
            try {
                final double parsed = mMoneyFormatter.parse(price).doubleValue();
                return new BigDecimal(parsed).setScale(fractionDigits, BigDecimal.ROUND_HALF_UP);
            } catch (ParseException e1) {
                return BigDecimal.ZERO;
            }
        }
    }

    @NonNull
    protected Purchase createPurchase(@NonNull List<Identity> purchaseIdentities,
                                      @NonNull List<Item> purchaseItems, int fractionDigits) {
        final BigDecimal totalPriceRounded =
                new BigDecimal(mTotalPrice).setScale(fractionDigits, BigDecimal.ROUND_HALF_UP);
        final Purchase purchase = new Purchase(mCurrentIdentity, mCurrentGroup, mDate,
                !TextUtils.isEmpty(mStore) ? mStore : "", purchaseItems, totalPriceRounded,
                purchaseIdentities, mCurrency, mExchangeRate);
        if (!TextUtils.isEmpty(mNote)) {
            purchase.setNote(mNote);
        }
        if (!TextUtils.isEmpty(mReceiptImagePath)) {
            purchase.setReceiptLocal(mReceiptImagePath);
        }

        return purchase;
    }

    @NonNull
    private List<Identity> getIdentities(@NonNull PurchaseAddEditItemUsersUser[] itemUsersRows) {
        final List<Identity> identities = new ArrayList<>();
        for (int i = 0, length = itemUsersRows.length; i < length; i++) {
            if (itemUsersRows[i].isSelected()) {
                identities.add(mIdentities.get(i));
            }
        }

        return identities;
    }

    @Override
    public void onExitClick() {
        mView.showPurchaseDiscardDialog();
    }

    @Override
    public void onDiscardChangesSelected() {
        mNavigator.finish(PurchaseResult.PURCHASE_DISCARDED);
    }
}
