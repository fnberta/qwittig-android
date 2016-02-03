/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.databinding.library.baseAdapters.BR;
import com.parse.ParseUser;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ch.giantific.qwittig.BuildConfig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.MessageAction;
import ch.giantific.qwittig.domain.models.PurchaseAddEditItem;
import ch.giantific.qwittig.domain.models.PurchaseAddEditItem.Type;
import ch.giantific.qwittig.domain.models.RowItem;
import ch.giantific.qwittig.domain.models.RowItemUser;
import ch.giantific.qwittig.domain.models.ocr.OcrItem;
import ch.giantific.qwittig.domain.models.ocr.OcrPurchase;
import ch.giantific.qwittig.domain.models.parse.Item;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.viewmodels.PurchaseAddEditViewModel.ViewListener;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.parse.ParseUtils;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Func1;

/**
 * Created by fabio on 24.01.16.
 */
public class PurchaseAddEditViewModelAddImpl extends ListViewModelBaseImpl<PurchaseAddEditItem, ViewListener>
        implements PurchaseAddEditViewModel {

    // add permission to manifest when enabling!
    static final boolean USE_CUSTOM_CAMERA = false;
    private static final String LOG_TAG = PurchaseAddEditViewModelAddImpl.class.getSimpleName();
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
    PurchaseRepository mPurchaseRepo;
    String mCurrency;
    String mReceiptImagePath;
    String mNote;
    Date mDate;
    String mStore;
    BigDecimal mTotalPrice = BigDecimal.ZERO;
    float mExchangeRate;
    boolean mSaving;
    private List<String> mSupportedCurrencies = ParseUtils.getSupportedCurrencyCodes();
    private List<User> mUsersAvailable = new ArrayList<>();
    private BigDecimal mMyShare = BigDecimal.ZERO;
    private boolean mFetchingExchangeRates;
    private ArrayList<String> mReceiptImagePaths;

    public PurchaseAddEditViewModelAddImpl(@Nullable Bundle savedState,
                                           @NonNull GroupRepository groupRepository,
                                           @NonNull UserRepository userRepository,
                                           @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, groupRepository, userRepository);

        mPurchaseRepo = purchaseRepo;

        if (savedState != null) {
            mItems = savedState.getParcelableArrayList(STATE_ROW_ITEMS);
            mSaving = savedState.getBoolean(STATE_SAVING);
            mDate = DateUtils.parseLongToDate(savedState.getLong(STATE_DATE));
            mStore = savedState.getString(STATE_STORE);
            setCurrency(savedState.getString(STATE_CURRENCY, mCurrentGroup.getCurrency()));
            mExchangeRate = savedState.getFloat(STATE_EXCHANGE_RATE);
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
    }

    private void initFixedRows() {
        mItems.add(PurchaseAddEditItem.createNewHeaderInstance(R.string.header_purchase));
        mItems.add(PurchaseAddEditItem.createNewDateInstance());
        mItems.add(PurchaseAddEditItem.createNewStoreInstance());
        mItems.add(PurchaseAddEditItem.createNewHeaderInstance(R.string.header_items));
        mItems.add(PurchaseAddEditItem.createNewAddRowInstance());
        mItems.add(PurchaseAddEditItem.createNewTotalInstance());
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelableArrayList(STATE_ROW_ITEMS, mItems);
        outState.putBoolean(STATE_SAVING, mSaving);
        outState.putLong(STATE_DATE, DateUtils.parseDateToLong(mDate));
        outState.putString(STATE_STORE, mStore);
        outState.putString(STATE_CURRENCY, mCurrency);
        outState.putFloat(STATE_EXCHANGE_RATE, mExchangeRate);
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
        return DateUtils.formatDateLong(mDate);
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
        return MoneyUtils.formatMoneyNoSymbol(mTotalPrice, mCurrency);
    }

    @Override
    public void setTotalPrice(@NonNull BigDecimal totalPrice) {
        mTotalPrice = totalPrice;
        notifyPropertyChanged(BR.totalPrice);
    }

    @Override
    @Bindable
    public String getMyShare() {
        return MoneyUtils.formatMoneyNoSymbol(mMyShare, mCurrency);
    }

    @Override
    public void setMyShare(@NonNull BigDecimal myShare) {
        mMyShare = myShare;
        notifyPropertyChanged(BR.myShare);
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
        return MoneyUtils.formatMoneyNoSymbol(mExchangeRate, MoneyUtils.EXCHANGE_RATE_FRACTION_DIGITS);
    }

    @Override
    public void setExchangeRate(float exchangeRate) {
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
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal myShare = BigDecimal.ZERO;
        final int maximumFractionDigits = MoneyUtils.getMaximumFractionDigits(mCurrency);

        for (PurchaseAddEditItem addEditItem : mItems) {
            if (addEditItem.getType() != Type.ITEM) {
                continue;
            }

            final RowItem rowItem = addEditItem.getRowItem();
            final BigDecimal finalPrice = rowItem.parsePrice(mCurrency);

            // update total price
            totalPrice = totalPrice.add(finalPrice);

            // update my share
            final RowItemUser[] rowItemUsers = rowItem.getUsers();
            int selectedCount = 0;
            boolean currentUserInvolved = false;
            for (RowItemUser rowItemUser : rowItemUsers) {
                if (!rowItemUser.isSelected()) {
                    continue;
                }

                selectedCount++;
                if (rowItemUser.getObjectId().equals(mCurrentUser.getObjectId())) {
                    currentUserInvolved = true;
                }
            }
            if (currentUserInvolved) {
                myShare = myShare.add(finalPrice.divide(new BigDecimal(selectedCount),
                        maximumFractionDigits, BigDecimal.ROUND_HALF_UP));
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

        // update item price formatting
        for (int i = 0, size = mItems.size(); i < size; i++) {
            final PurchaseAddEditItem addEditItem = mItems.get(i);
            if (addEditItem.getType() == Type.ITEM) {
                addEditItem.getRowItem().updateCurrency(mCurrency);
            }
        }

        // update total price and my share formatting
        notifyPropertyChanged(BR.totalPrice);
        notifyPropertyChanged(BR.myShare);
        // update my share currency field
        notifyPropertyChanged(BR.currency);

        // get new exchange rate
        mView.loadFetchExchangeRatesWorker(mCurrentGroup.getCurrency(), mCurrency);
    }

    @Override
    public void setRateFetchStream(@NonNull Single<Float> single,
                                   @NonNull final String workerTag) {
        mSubscriptions.add(single
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
    public void onExchangeRateManuallySet(float exchangeRate) {
        final BigDecimal roundedExchangeRate = MoneyUtils.roundToFractionDigits(
                MoneyUtils.EXCHANGE_RATE_FRACTION_DIGITS, exchangeRate);
        setExchangeRate(roundedExchangeRate.floatValue());
    }

    @Override
    public void updateList() {
        mSubscriptions.add(mUserRepo.getUsersLocalAsync(mCurrentGroup)
                .toSortedList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<User>>() {
                    @Override
                    public void onSuccess(List<User> users) {
                        users.remove(mCurrentUser);
                        users.add(0, mCurrentUser);
                        mUsersAvailable = users;

                        onUsersReady();
                    }

                    @Override
                    public void onError(Throwable error) {
                        // TODO: handle error
                    }
                })
        );
    }

    void onUsersReady() {
        // fill with one row on first start
        for (int i = 0, size = mItems.size(); i < size; i++) {
            final PurchaseAddEditItem addEditItem = mItems.get(i);
            if (addEditItem.getType() == Type.ITEM) {
                return;
            }

            if (addEditItem.getType() == Type.ADD_ROW) {
                final RowItem rowItem = new RowItem(getRowItemUser(mUsersAvailable), mCurrency);
                mItems.add(i, PurchaseAddEditItem.createNewRowItemInstance(rowItem));
                mView.notifyDataSetChanged();
                return;
            }
        }
    }

    final RowItemUser[] getRowItemUser(@NonNull List<? extends ParseUser> users) {
        final int size = mUsersAvailable.size();
        final RowItemUser[] rowItemUser = new RowItemUser[size];
        for (int i = 0; i < size; i++) {
            final User user = mUsersAvailable.get(i);
            if (users.contains(user)) {
                rowItemUser[i] = new RowItemUser(user.getObjectId(), user.getNickname(),
                        user.getAvatar(), true);
            } else {
                rowItemUser[i] = new RowItemUser(user.getObjectId(), user.getNickname(),
                        user.getAvatar(), false);
            }
        }

        return rowItemUser;
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    @Override
    public void onItemDismissed(int position) {
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
    public void onRowItemUserClick(int position) {
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
        mSubscriptions.add(single.subscribe(new SingleSubscriber<OcrPurchase>() {
            @Override
            public void onSuccess(OcrPurchase ocrPurchase) {
                mView.removeWorker(workerTag);

//                setDate(ocrPurchase.getDate());
                setStore(ocrPurchase.getStore());

                final List<OcrItem> ocrItems = ocrPurchase.getItems();
                for (OcrItem ocrItem : ocrItems) {
                    final String price = MoneyUtils.formatPrice(ocrItem.getPrice(), mCurrency);
                    final RowItem rowItem = new RowItem(ocrItem.getName(), price,
                            getRowItemUser(mUsersAvailable), mCurrency);
                    final PurchaseAddEditItem addEditItem = PurchaseAddEditItem.createNewRowItemInstance(rowItem);
                    mItems.add(addEditItem);
                    mView.notifyItemInserted(mItems.indexOf(addEditItem));
                }

                //        setEditTextPriceImeOptions();
//
//        if (!itemRowsNew.isEmpty()) {
//            ItemRow firstItemRow = itemRowsNew.get(0);
//            firstItemRow.requestFocusForName();
//        }

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
            mSubscriptions.add(mView.getReceiptImage(mReceiptImagePath)
                    .subscribe(new SingleSubscriber<byte[]>() {
                        @Override
                        public void onSuccess(byte[] receiptImage) {
                            loadSavePurchaseWorker(purchase, receiptImage);
                        }

                        @Override
                        public void onError(Throwable error) {
                            mSaving = false;
                            mView.stopSaveAnim();
                            // TODO: handle error
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
            mSubscriptions.add(mView.getReceiptImage(mReceiptImagePath)
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
                            deleteTakenImages();
                            mView.finishScreen(PurchaseResult.PURCHASE_DRAFT);
                        }

                        @Override
                        public void onError(Throwable error) {
                            // TODO: handle error
                        }
                    })
            );
        } else {
            mSubscriptions.add(mPurchaseRepo.savePurchaseAsDraftAsync(purchase, Purchase.PIN_LABEL_DRAFT)
                    .subscribe(new SingleSubscriber<Purchase>() {
                        @Override
                        public void onSuccess(Purchase value) {
                            deleteTakenImages();
                            mView.finishScreen(PurchaseResult.PURCHASE_DRAFT);
                        }

                        @Override
                        public void onError(Throwable error) {
                            // TODO: handle error
                        }
                    })
            );
        }
    }

    private boolean validateItems() {
        boolean hasItem = false;
        boolean allValid = true;
        for (PurchaseAddEditItem addEditItem : mItems) {
            if (addEditItem.getType() != Type.ITEM) {
                continue;
            }

            hasItem = true;
            final RowItem rowItem = addEditItem.getRowItem();
            if (!rowItem.validateFields()) {
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
        final List<User> purchaseUsersInvolved = new ArrayList<>();
        final List<Item> purchaseItems = new ArrayList<>();

        for (PurchaseAddEditItem addEditItem : mItems) {
            if (addEditItem.getType() != Type.ITEM) {
                continue;
            }

            final RowItem rowItem = addEditItem.getRowItem();
            final String name = rowItem.getName();
            final BigDecimal price = rowItem.parsePrice(mCurrency);
            final List<User> usersInvolved = getUsersInvolved(rowItem.getUsers());
            for (User user : usersInvolved) {
                // TODO: check if need to use object ids here
                if (!purchaseUsersInvolved.contains(user)) {
                    purchaseUsersInvolved.add(user);
                }
            }
            final Item item = new Item(name, price, usersInvolved, mCurrentGroup);
            purchaseItems.add(item);
        }

        return createPurchase(purchaseUsersInvolved, purchaseItems);
    }

    @NonNull
    Purchase createPurchase(@NonNull List<User> purchaseUsersInvolved,
                            @NonNull List<Item> purchaseItems) {
        final Purchase purchase = new Purchase(mCurrentUser, mCurrentGroup, mDate, mStore,
                purchaseItems, mTotalPrice.doubleValue(), purchaseUsersInvolved, mCurrency, mExchangeRate);
        if (!TextUtils.isEmpty(mNote)) {
            purchase.setNote(mNote);
        }
        return purchase;
    }

    @NonNull
    private List<User> getUsersInvolved(@NonNull RowItemUser[] rowItemUsers) {
        final List<User> usersInvolved = new ArrayList<>();
        for (int i = 0, length = rowItemUsers.length; i < length; i++) {
            if (rowItemUsers[i].isSelected()) {
                usersInvolved.add(mUsersAvailable.get(i));
            }
        }

        return usersInvolved;
    }

    @Override
    public void setPurchaseSaveStream(@NonNull Single<Purchase> single,
                                      @NonNull final String workerTag) {
        mSubscriptions.add(single.subscribe(new SingleSubscriber<Purchase>() {
            @Override
            public void onSuccess(Purchase value) {
                mView.removeWorker(workerTag);
                onPurchaseSaved();
            }

            @Override
            public void onError(Throwable error) {
                mView.removeWorker(workerTag);
                onPurchaseSaveError(error);
                // TODO: maybe give user action to save as draft
//                    ParseFile receipt = mPurchase.getReceiptParseFile();
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
    void onPurchaseSaveError(Throwable error) {
        mSaving = false;
        mView.stopSaveAnim();
        mView.showMessage(mPurchaseRepo.getErrorMessage(error));
    }

    private void deleteTakenImages() {
        if (USE_CUSTOM_CAMERA) {
            if (!mReceiptImagePaths.isEmpty()) {
                for (String path : mReceiptImagePaths) {
                    final boolean fileDeleted = new File(path).delete();
                    if (!fileDeleted && BuildConfig.DEBUG) {
                        Log.e(LOG_TAG, "failed to delete file");
                    }
                }
                mReceiptImagePaths.clear();
            }
        } else if (!TextUtils.isEmpty(mReceiptImagePath)) {
            boolean fileDeleted = new File(mReceiptImagePath).delete();
            if (!fileDeleted && BuildConfig.DEBUG) {
                Log.e(LOG_TAG, "failed to delete file");
            }
            mReceiptImagePath = "";
        }
    }

    @Override
    public void onShowReceiptImageClick() {
        mView.showReceiptImage();
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
        // TODO: possible, no!
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
        final PurchaseAddEditItem addEditItem = mItems.get(position);
        final int insertPos = position + 1;
        if (mItems.size() < insertPos) {
            expandRowItem(insertPos, addEditItem);
        } else if (getItemViewType(insertPos) == Type.USERS) {
            collapseRowItem(insertPos);
        } else {
            expandRowItem(insertPos, addEditItem);
        }
    }

    private void collapseRowItem(int pos) {
        mItems.remove(pos);
        mView.notifyItemRemoved(pos);
    }

    private void expandRowItem(int pos, PurchaseAddEditItem parent) {
        final RowItem rowItem = parent.getRowItem();
        mItems.add(pos, PurchaseAddEditItem.createNewUsersInstance(rowItem.getUsers()));
        mView.notifyItemInserted(pos);

        collapseAllOtherRowItems(pos);
    }

    private void collapseAllOtherRowItems(int insertPos) {
        int pos = 0;
        for (Iterator<PurchaseAddEditItem> iterator = mItems.iterator(); iterator.hasNext(); ) {
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
        final RowItem rowItem = new RowItem(getRowItemUser(mUsersAvailable), mCurrency);
        mItems.add(position, PurchaseAddEditItem.createNewRowItemInstance(rowItem));
        mView.notifyItemInserted(position);
        mView.scrollToPosition(position + 1);
    }

    @Override
    public void onProgressFinalAnimationComplete() {
        mView.finishScreen(PurchaseResult.PURCHASE_SAVED);
    }
}
