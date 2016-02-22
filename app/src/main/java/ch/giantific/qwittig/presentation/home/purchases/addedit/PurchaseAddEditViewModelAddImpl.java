/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.databinding.library.baseAdapters.BR;

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ch.giantific.qwittig.BuildConfig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.rest.OcrItem;
import ch.giantific.qwittig.data.rest.OcrPurchase;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Item;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseAddEditViewModel.ViewListener;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.PurchaseAddEditBaseItem;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.PurchaseAddEditBaseItem.Type;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.PurchaseAddEditGenericItem;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.PurchaseAddEditHeaderItem;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.PurchaseAddEditItem;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.PurchaseAddEditItemUsers;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.PurchaseAddEditItemUsersUser;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MessageAction;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.parse.ParseUtils;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link PurchaseAddEditViewModel} for the add purchase screen.
 */
public class PurchaseAddEditViewModelAddImpl extends ListViewModelBaseImpl<PurchaseAddEditBaseItem, ViewListener>
        implements PurchaseAddEditViewModel {

    // add permission to manifest when enabling!
    static final boolean USE_CUSTOM_CAMERA = false;
    private static final String STATE_ROW_ITEMS = "STATE_ROW_ITEMS";
    private static final String STATE_SAVING = "STATE_SAVING";
    private static final String STATE_DATE = "STATE_DATE";
    private static final String STATE_STORE = "STATE_STORE";
    private static final String STATE_CURRENCY = "STATE_CURRENCY";
    private static final String STATE_EXCHANGE_RATE = "STATE_EXCHANGE_RATE";
    private static final String STATE_NOTE = "STATE_NOTE";
    private static final String STATE_RECEIPT_IMAGE_PATH = "STATE_RECEIPT_IMAGE_PATH";
    private static final String STATE_RECEIPT_IMAGE_PATHS = "STATE_RECEIPT_IMAGE_PATHS";
    private static final String STATE_FETCHING_RATES = "STATE_FETCHING_RATES";
    final PurchaseRepository mPurchaseRepo;
    private final NumberFormat mExchangeRateFormatter;
    private final List<String> mSupportedCurrencies = ParseUtils.getSupportedCurrencyCodes();
    private final Group mCurrentGroup;
    private final DateFormat mDateFormatter;
    String mCurrency;
    String mReceiptImagePath;
    String mNote;
    Date mDate;
    String mStore;
    double mTotalPrice = 0;
    double mExchangeRate;
    NumberFormat mMoneyFormatter;
    private boolean mSaving;
    private List<Identity> mIdentities = new ArrayList<>();
    private double mMyShare = 0;
    private boolean mFetchingExchangeRates;
    private ArrayList<String> mReceiptImagePaths;

    public PurchaseAddEditViewModelAddImpl(@Nullable Bundle savedState,
                                           @NonNull PurchaseAddEditViewModel.ViewListener view,
                                           @NonNull IdentityRepository identityRepository,
                                           @NonNull UserRepository userRepository,
                                           @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, view, identityRepository, userRepository);

        mPurchaseRepo = purchaseRepo;
        mCurrentGroup = mCurrentIdentity.getGroup();

        if (savedState != null) {
            mItems = savedState.getParcelableArrayList(STATE_ROW_ITEMS);
            mSaving = savedState.getBoolean(STATE_SAVING);
            mDate = new Date(savedState.getLong(STATE_DATE));
            mStore = savedState.getString(STATE_STORE);
            setCurrency(savedState.getString(STATE_CURRENCY, mCurrentGroup.getCurrency()));
            mExchangeRate = savedState.getDouble(STATE_EXCHANGE_RATE);
            mNote = savedState.getString(STATE_NOTE);
            mReceiptImagePath = savedState.getString(STATE_RECEIPT_IMAGE_PATH);
            if (USE_CUSTOM_CAMERA) {
                mReceiptImagePaths = savedState.getStringArrayList(STATE_RECEIPT_IMAGE_PATHS);
            }
            mFetchingExchangeRates = savedState.getBoolean(STATE_FETCHING_RATES);
        } else {
            initFixedRows();
            mLoading = false;
            mDate = new Date();
            mCurrency = mCurrentGroup.getCurrency();
            mExchangeRate = 1;
            if (USE_CUSTOM_CAMERA) {
                mReceiptImagePaths = new ArrayList<>();
            }
        }

        mDateFormatter = DateUtils.getDateFormatter(false);
        mMoneyFormatter = MoneyUtils.getMoneyFormatter(mCurrency, false, true);
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
        outState.putBoolean(STATE_SAVING, mSaving);
        outState.putLong(STATE_DATE, mDate.getTime());
        outState.putString(STATE_STORE, mStore);
        outState.putString(STATE_CURRENCY, mCurrency);
        outState.putDouble(STATE_EXCHANGE_RATE, mExchangeRate);
        outState.putString(STATE_NOTE, mNote);
        outState.putString(STATE_RECEIPT_IMAGE_PATH, mReceiptImagePath);
        if (USE_CUSTOM_CAMERA) {
            outState.putStringArrayList(STATE_RECEIPT_IMAGE_PATHS, mReceiptImagePaths);
        }
        outState.putBoolean(STATE_FETCHING_RATES, mFetchingExchangeRates);
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
    @Bindable
    public String getStore() {
        return mStore;
    }

    public void setStore(String store) {
        mStore = store;
        notifyPropertyChanged(BR.store);
    }

    @Override
    public void onStoreChanged(CharSequence s, int start, int before, int count) {
        mStore = s.toString();
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
    public NumberFormat getMoneyFormatter() {
        return mMoneyFormatter;
    }

    @Override
    public List<String> getSupportedCurrencies() {
        return mSupportedCurrencies;
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
    public void onRowPriceChanged() {
        updateTotalAndMyShare();
    }

    private void updateTotalAndMyShare() {
        double totalPrice = 0;
        double myShare = 0;
        final String currentIdentityId = mCurrentIdentity.getObjectId();
        for (PurchaseAddEditBaseItem addEditItem : mItems) {
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
            for (PurchaseAddEditItemUsersUser purchaseAddEditItemUsersUser : itemUsersRows) {
                if (!purchaseAddEditItemUsersUser.isSelected()) {
                    continue;
                }

                selectedCount++;
                if (purchaseAddEditItemUsersUser.getObjectId().equals(currentIdentityId)) {
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
    public void onDateSet(@NonNull Date date) {
        setDate(date);
    }

    @Override
    public void onDateClick(View view) {
        mView.showDatePickerDialog();
    }

    @Override
    public void onNoteSet(@NonNull String note) {
        mNote = note;
    }

    @Override
    public void onCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final String currency = (String) parent.getItemAtPosition(position);
        if (currency.equals(mCurrency)) {
            return;
        }

        mCurrency = currency;
        mMoneyFormatter = MoneyUtils.getMoneyFormatter(currency, false, true);

        // TODO: only needed once we support currencies with other than 2 decimal values
//        updateItemsPriceFormatting();

        // update total price and my share formatting
        notifyPropertyChanged(BR.totalPrice);
        notifyPropertyChanged(BR.myShare);
        // update my share currency field
        notifyPropertyChanged(BR.currency);

        // get new exchange rate
        mView.loadFetchExchangeRatesWorker(mCurrentGroup.getCurrency(), mCurrency);
    }

    private void updateItemsPriceFormatting() {
        for (int i = 0, size = mItems.size(); i < size; i++) {
            final PurchaseAddEditBaseItem addEditItem = mItems.get(i);
            if (addEditItem.getType() == Type.ITEM) {
                final PurchaseAddEditItem purchaseAddEditItem = (PurchaseAddEditItem) addEditItem;
                purchaseAddEditItem.updatePriceFormat(mMoneyFormatter);
            }
        }
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
    public void onExchangeRateClick(View view) {
        mView.showManualExchangeRateSelectorDialog(((TextView) view).getText().toString());
    }

    @Override
    public void onExchangeRateManuallySet(double exchangeRate) {
        final BigDecimal roundedExchangeRate = MoneyUtils.roundExchangeRate(exchangeRate);
        setExchangeRate(roundedExchangeRate.doubleValue());
    }

    @Override
    public void loadData() {
        getSubscriptions().add(mIdentityRepo.getIdentitiesLocalAsync(mCurrentGroup, true)
                .toSortedList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<Identity>>() {
                    @Override
                    public void onSuccess(List<Identity> identities) {
                        identities.remove(mCurrentIdentity);
                        identities.add(0, mCurrentIdentity);
                        mIdentities = identities;

                        onIdentitiesReady();
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_purchase_add_edit_load);
                    }
                })
        );
    }

    void onIdentitiesReady() {
        boolean hasItems = false;
        for (int i = 0, size = mItems.size(); i < size; i++) {
            final PurchaseAddEditBaseItem addEditItem = mItems.get(i);

            if (addEditItem.getType() == Type.ITEM) {
                hasItems = true;

                final PurchaseAddEditItem purchaseAddEditItem = (PurchaseAddEditItem) addEditItem;
                purchaseAddEditItem.setMoneyFormatter(mMoneyFormatter);
                purchaseAddEditItem.setPriceChangedListener(this);
                continue;
            }

            // fill with one row on first start
            if (addEditItem.getType() == Type.ADD_ROW && !hasItems) {
                final PurchaseAddEditItem purchaseAddEditItem = new PurchaseAddEditItem(getItemUsersItemUsers(mIdentities));
                purchaseAddEditItem.setMoneyFormatter(mMoneyFormatter);
                purchaseAddEditItem.setPriceChangedListener(this);
                mItems.add(i, purchaseAddEditItem);
                mView.notifyDataSetChanged();
                return;
            }
        }
    }

    final PurchaseAddEditItemUsersUser[] getItemUsersItemUsers(@NonNull List<Identity> identities) {
        final int size = mIdentities.size();
        final PurchaseAddEditItemUsersUser[] itemUsersRow = new PurchaseAddEditItemUsersUser[size];
        for (int i = 0; i < size; i++) {
            final Identity identity = mIdentities.get(i);
            if (identities.contains(identity)) {
                itemUsersRow[i] = new PurchaseAddEditItemUsersUser(identity.getObjectId(), identity.getNickname(),
                        identity.getAvatarUrl(), true);
            } else {
                itemUsersRow[i] = new PurchaseAddEditItemUsersUser(identity.getObjectId(), identity.getNickname(),
                        identity.getAvatarUrl(), false);
            }
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
            mView.notifyItemRemoved(position);
        } else {
            mItems.remove(position);
            mView.notifyItemRangeRemoved(position, 2);
        }
        onRowPriceChanged();
    }

    @Override
    public void onTooFewUsersSelected() {
        mView.showMessage(R.string.toast_min_one_user);
    }

    @Override
    public void onItemRowUserClick(int position) {
        updateTotalAndMyShare();
    }

    @Override
    public void onAddReceiptImageClick() {
        mView.captureImage(USE_CUSTOM_CAMERA);
    }

    @Override
    public void onReceiptImagePathSet(@NonNull String receiptImagePath) {
        mReceiptImagePath = receiptImagePath;
    }

    @Override
    public void onReceiptImageTaken() {
        mView.toggleReceiptMenuOption(true);
        mView.showMessage(R.string.toast_receipt_added);
    }

    @Override
    public void onReceiptImageFailed() {
        mView.finishScreen(PurchaseResult.PURCHASE_DISCARDED);
    }

    @Override
    public void onReceiptImagesTaken(@NonNull List<String> receiptImagePaths) {
        mReceiptImagePaths.clear();
        if (!receiptImagePaths.isEmpty()) {
            mReceiptImagePaths.addAll(receiptImagePaths);
        }
        mReceiptImagePath = mReceiptImagePaths.get(0);
    }

    @Override
    public void setOcrStream(@NonNull final Single<OcrPurchase> single,
                             @NonNull final String workerTag) {
        final PurchaseAddEditItem.PriceChangedListener priceListener = this;

        getSubscriptions().add(single.subscribe(new SingleSubscriber<OcrPurchase>() {
            @Override
            public void onSuccess(OcrPurchase ocrPurchase) {
                mView.removeWorker(workerTag);

//                setDate(ocrPurchase.getDate());
                setStore(ocrPurchase.getStore());

                final List<OcrItem> ocrItems = ocrPurchase.getItems();
                for (OcrItem ocrItem : ocrItems) {
                    final String price = mMoneyFormatter.format(ocrItem.getPrice());
                    final PurchaseAddEditItem purchaseAddEditItem = new PurchaseAddEditItem(ocrItem.getName(), price,
                            getItemUsersItemUsers(mIdentities));
                    purchaseAddEditItem.setMoneyFormatter(mMoneyFormatter);
                    purchaseAddEditItem.setPriceChangedListener(priceListener);
                    mItems.add(purchaseAddEditItem);
                    mView.notifyItemInserted(mItems.indexOf(purchaseAddEditItem));
                }

                setLoading(false);
            }

            @Override
            public void onError(Throwable error) {
                mView.removeWorker(workerTag);

                mView.showMessage(mPurchaseRepo.getErrorMessage(error));
                setLoading(false);
            }
        }));
    }

    @Override
    public void onFabSavePurchaseClick(View view) {
        if (mSaving) {
            return;
        }

        if (!mView.isNetworkAvailable()) {
            mView.showMessage(R.string.toast_no_connection);
            return;
        }

        if (mFetchingExchangeRates) {
            mView.showMessage(R.string.toast_exchange_rate_fetching);
            return;
        }

        if (!mCurrency.equals(mCurrentGroup.getCurrency()) && mExchangeRate == 1) {
            mView.showMessageWithAction(R.string.toast_exchange_no_data,
                    new MessageAction(R.string.action_purchase_save_draft) {
                        @Override
                        public void onClick(View v) {
                            onSavePurchaseAsDraftClick();
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

        mSaving = true;
        mView.startSaveAnim();
        final Purchase purchase = getPurchase();
        if (!TextUtils.isEmpty(mReceiptImagePath)) {
            getSubscriptions().add(mView.encodeReceiptImage(mReceiptImagePath)
                    .subscribe(new SingleSubscriber<byte[]>() {
                        @Override
                        public void onSuccess(byte[] receiptImage) {
                            loadSavePurchaseWorker(purchase, receiptImage);
                        }

                        @Override
                        public void onError(Throwable error) {
                            mSaving = false;
                            mView.stopSaveAnim();
                            onPurchaseSaveError();
                        }
                    })
            );
        } else {
            loadSavePurchaseWorker(purchase, null);
        }
    }

    /**
     * Loads the save purchase worker. EditImpl will send additional parameters.
     *
     * @param purchase     the purchase to save
     * @param receiptImage the receipt image
     */
    void loadSavePurchaseWorker(@NonNull Purchase purchase, @Nullable byte[] receiptImage) {
        mView.loadSavePurchaseWorker(purchase, receiptImage);
    }

    @Override
    public void onSavePurchaseAsDraftClick() {
        if (mSaving) {
            return;
        }

        if (mFetchingExchangeRates) {
            mView.showMessage(R.string.toast_exchange_rate_fetching);
            return;
        }

        final Purchase purchase = getPurchase();
        purchase.setRandomDraftId();
        if (!TextUtils.isEmpty(mReceiptImagePath)) {
            getSubscriptions().add(mView.encodeReceiptImage(mReceiptImagePath)
                    .flatMap(new Func1<byte[], Single<Purchase>>() {
                        @Override
                        public Single<Purchase> call(byte[] bytes) {
                            purchase.setReceiptData(bytes);
                            return mPurchaseRepo.savePurchaseAsDraftAsync(purchase, Purchase.PIN_LABEL_DRAFT);
                        }
                    })
                    .subscribe(new SingleSubscriber<Purchase>() {
                        @Override
                        public void onSuccess(Purchase value) {
                            onDraftSaved();
                        }

                        @Override
                        public void onError(Throwable error) {
                            mView.showMessage(R.string.toast_error_purchase_save_draft);
                        }
                    })
            );
        } else {
            getSubscriptions().add(mPurchaseRepo.savePurchaseAsDraftAsync(purchase, Purchase.PIN_LABEL_DRAFT)
                    .subscribe(new SingleSubscriber<Purchase>() {
                        @Override
                        public void onSuccess(Purchase value) {
                            onDraftSaved();
                        }

                        @Override
                        public void onError(Throwable error) {
                            mView.showMessage(R.string.toast_error_purchase_save_draft);
                        }
                    })
            );
        }
    }

    private void onDraftSaved() {
        deleteTakenImages();
        mView.finishScreen(getDraftFinishedResult());
    }

    int getDraftFinishedResult() {
        return PurchaseResult.PURCHASE_DRAFT;
    }

    private boolean validateItems() {
        boolean hasItem = false;
        boolean allValid = true;
        for (PurchaseAddEditBaseItem addEditItem : mItems) {
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

        for (PurchaseAddEditBaseItem addEditItem : mItems) {
            if (addEditItem.getType() != Type.ITEM) {
                continue;
            }

            final PurchaseAddEditItem itemRow = (PurchaseAddEditItem) addEditItem;
            final String rowItemName = itemRow.getName();
            final String name = rowItemName != null ? rowItemName : "";
            final String price = itemRow.getPrice();
            final BigDecimal priceRounded = roundPrice(price, fractionDigits);
            final List<Identity> identities = getIdentities(itemRow.getUsers());
            for (Identity identity : identities) {
                if (!purchaseIdentities.contains(identity)) {
                    purchaseIdentities.add(identity);
                }
            }
            final Item item = new Item(name, priceRounded, identities, mCurrentGroup);
            purchaseItems.add(item);
        }

        return createPurchase(purchaseIdentities, purchaseItems, fractionDigits);
    }

    private BigDecimal roundPrice(@NonNull String price, int fractionDigits) {
        try {
            return new BigDecimal(price).setScale(fractionDigits, BigDecimal.ROUND_UP);
        } catch (NumberFormatException e) {
            try {
                final double parsed = mMoneyFormatter.parse(price).doubleValue();
                return new BigDecimal(parsed).setScale(fractionDigits, BigDecimal.ROUND_UP);
            } catch (ParseException e1) {
                return BigDecimal.ZERO;
            }
        }
    }

    @NonNull
    Purchase createPurchase(@NonNull List<Identity> purchaseIdentities,
                            @NonNull List<Item> purchaseItems, int fractionDigits) {
        final BigDecimal totalPriceRounded =
                new BigDecimal(mTotalPrice).setScale(fractionDigits, BigDecimal.ROUND_UP);
        final Purchase purchase = new Purchase(mCurrentIdentity, mCurrentGroup, mDate, mStore,
                purchaseItems, totalPriceRounded, purchaseIdentities, mCurrency, mExchangeRate);
        if (!TextUtils.isEmpty(mNote)) {
            purchase.setNote(mNote);
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
    public void setPurchaseSaveStream(@NonNull Single<Purchase> single,
                                      @NonNull final String workerTag) {
        getSubscriptions().add(single.subscribe(new SingleSubscriber<Purchase>() {
            @Override
            public void onSuccess(Purchase value) {
                mView.removeWorker(workerTag);
                onPurchaseSaved();
            }

            @Override
            public void onError(Throwable error) {
                mView.removeWorker(workerTag);
                onPurchaseSaveError();
                // TODO: maybe give user action to save as draft
//                    ParseFile receipt = mPurchase.getReceipt();
//                    if (receipt != null) {
//                        receipt.getDataInBackground(new GetDataCallback() {
//                            @Override
//                            public void done(@NonNull byte[] data, ParseException e) {
//                                mPurchase.swapReceiptParseFileToData(data);
//                                pinPurchaseAsDraft();
//                            }
//                        });
//                    } else {
//                        pinPurchaseAsDraft();
//                    }
            }
        }));
    }

    @CallSuper
    void onPurchaseSaved() {
        mSaving = false;
        deleteTakenImages();
        mView.showSaveFinishedAnim();
    }

    @CallSuper
    void onPurchaseSaveError() {
        mSaving = false;
        mView.stopSaveAnim();
        mView.showMessage(R.string.toast_error_purchase_save);
    }

    private void deleteTakenImages() {
        if (USE_CUSTOM_CAMERA) {
            if (!mReceiptImagePaths.isEmpty()) {
                for (String path : mReceiptImagePaths) {
                    final boolean fileDeleted = new File(path).delete();
                    if (!fileDeleted && BuildConfig.DEBUG) {
                        Timber.e("failed to delete file");
                    }
                }
                mReceiptImagePaths.clear();
            }
        } else if (!TextUtils.isEmpty(mReceiptImagePath)) {
            boolean fileDeleted = new File(mReceiptImagePath).delete();
            if (!fileDeleted && BuildConfig.DEBUG) {
                Timber.e("failed to delete file");
            }
            mReceiptImagePath = "";
        }
    }

    @Override
    public void onShowReceiptImageClick() {
        mView.showReceiptImage(mReceiptImagePath);
    }

    @Override
    public void onDeleteReceiptImageClick() {
        deleteTakenImages();
    }

    @Override
    public void onAddNoteClick() {
        mView.showAddEditNoteDialog(mNote);
    }

    @Override
    public void onShowNoteClick() {
        mView.showNote(mNote);
    }

    @Override
    public void onEditNoteClick() {
        // TODO: doesn't work because note fragment is shown, move to note fragment view model
        mView.showAddEditNoteDialog(mNote);
    }

    @Override
    public void onDeleteNoteClick() {
        mNote = "";
    }

    @Override
    public void onUpOrBackClick() {
        if (mSaving) {
            mView.showMessage(R.string.toast_saving_purchase);
        } else {
            askToDiscard();
        }
    }

    /**
     * Asks the user if he really wants to discard the purchase or if he wants to save it as a
     * draft instead. EditImpl will show different dialog.
     */
    void askToDiscard() {
        mView.showPurchaseDiscardDialog();
    }

    @Override
    public void onDiscardChangesSelected() {
        mView.finishScreen(PurchaseResult.PURCHASE_DISCARDED);
    }

    @Override
    public void onToggleUsersClick(int position) {
        final PurchaseAddEditBaseItem addEditItem = mItems.get(position);
        final int insertPos = position + 1;
        if (mItems.size() < insertPos) {
            expandItemRow(insertPos, addEditItem);
        } else if (getItemViewType(insertPos) == Type.USERS) {
            collapseItemRow(insertPos);
        } else {
            expandItemRow(insertPos, addEditItem);
        }
    }

    private void collapseItemRow(int pos) {
        mItems.remove(pos);
        mView.notifyItemRemoved(pos);
    }

    private void expandItemRow(int pos, PurchaseAddEditBaseItem parent) {
        final PurchaseAddEditItem purchaseAddEditItem = (PurchaseAddEditItem) parent;
        mItems.add(pos, new PurchaseAddEditItemUsers(purchaseAddEditItem.getUsers()));
        mView.notifyItemInserted(pos);

        collapseOtherItemRows(pos);
    }

    private void collapseOtherItemRows(int insertPos) {
        int pos = 0;
        for (Iterator<PurchaseAddEditBaseItem> iterator = mItems.iterator(); iterator.hasNext(); ) {
            final int type = iterator.next().getType();
            if (type == Type.USERS && pos != insertPos) {
                iterator.remove();
                mView.notifyItemRemoved(pos);
            }

            pos++;
        }
    }

    @Override
    public void onAddRowClick(int position) {
        final PurchaseAddEditItem purchaseAddEditItem = new PurchaseAddEditItem(getItemUsersItemUsers(mIdentities));
        purchaseAddEditItem.setMoneyFormatter(mMoneyFormatter);
        purchaseAddEditItem.setPriceChangedListener(this);
        mItems.add(position, purchaseAddEditItem);
        mView.notifyItemInserted(position);
        mView.scrollToPosition(position + 1);
    }

    @Override
    public void onProgressFinalAnimationComplete() {
        mView.finishScreen(PurchaseResult.PURCHASE_SAVED);
    }
}
