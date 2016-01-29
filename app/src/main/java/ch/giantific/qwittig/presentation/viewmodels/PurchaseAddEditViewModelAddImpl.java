/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.content.SharedPreferences;
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
import java.util.Map;

import ch.giantific.qwittig.BuildConfig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.MessageAction;
import ch.giantific.qwittig.domain.models.RowItem;
import ch.giantific.qwittig.domain.models.RowItemUser;
import ch.giantific.qwittig.domain.models.ocr.OcrItem;
import ch.giantific.qwittig.domain.models.ocr.OcrPurchase;
import ch.giantific.qwittig.domain.models.parse.Item;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.models.rates.CurrencyRates;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.viewmodels.PurchaseAddEditViewModel.ViewListener;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Func1;

/**
 * Created by fabio on 24.01.16.
 */
public class PurchaseAddEditViewModelAddImpl extends ListViewModelBaseImpl<RowItem, ViewListener>
        implements PurchaseAddEditViewModel {

    // add permission to manifest when enabling!
    static final boolean USE_CUSTOM_CAMERA = false;
    private static final String LOG_TAG = PurchaseAddEditViewModelAddImpl.class.getSimpleName();
    private static final String STATE_ROW_ITEMS = "STATE_ROW_ITEMS";
    private static final String STATE_SAVING = "STATE_SAVING";
    private static final String STATE_DATE = "STATE_DATE";
    private static final String STATE_STORE = "STATE_STORE";
    private static final String STATE_CURRENCY = "STATE_CURRENCY";
    private static final String STATE_NOTE = "STATE_NOTE";
    private static final String STATE_RECEIPT_IMAGE_PATH = "STATE_RECEIPT_IMAGE_PATH";
    private static final String STATE_RECEIPT_IMAGE_PATHS = "STATE_RECEIPT_IMAGE_PATHS";
    private static final String STATE_FETCHING_RATES = "STATE_FETCHING_RATES";
    private static final String EXCHANGE_RATE_LAST_FETCHED_TIME = "EXCHANGE_RATE_LAST_FETCHED_TIME";
    private static final long EXCHANGE_RATE_REFRESH_INTERVAL = 24 * 60 * 60 * 1000;
    PurchaseRepository mPurchaseRepo;
    String mCurrency;
    String mReceiptImagePath;
    String mNote;
    Date mDate;
    String mStore;
    BigDecimal mTotalPrice;
    float mExchangeRate;
    private SharedPreferences mSharedPreferences;
    private List<User> mUsersAvailable = new ArrayList<>();
    private boolean mSaving;
    private BigDecimal mMyShare;
    private boolean mFetchingExchangeRates;
    private ArrayList<String> mReceiptImagePaths;

    public PurchaseAddEditViewModelAddImpl(@Nullable Bundle savedState,
                                           @NonNull GroupRepository groupRepository,
                                           @NonNull UserRepository userRepository,
                                           @NonNull SharedPreferences sharedPreferences,
                                           @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, groupRepository, userRepository);

        mSharedPreferences = sharedPreferences;
        mPurchaseRepo = purchaseRepo;

        if (savedState != null) {
            mItems = savedState.getParcelableArrayList(STATE_ROW_ITEMS);
            mSaving = savedState.getBoolean(STATE_SAVING);
            mDate = DateUtils.parseLongToDate(savedState.getLong(STATE_DATE));
            mStore = savedState.getString(STATE_STORE);
            mCurrency = savedState.getString(STATE_CURRENCY);
            mNote = savedState.getString(STATE_NOTE);
            mReceiptImagePath = savedState.getString(STATE_RECEIPT_IMAGE_PATH);
            if (USE_CUSTOM_CAMERA) {
                mReceiptImagePaths = savedState.getStringArrayList(STATE_RECEIPT_IMAGE_PATHS);
            }
            mFetchingExchangeRates = savedState.getBoolean(STATE_FETCHING_RATES);
        } else {
            mDate = new Date();
            mCurrency = mCurrentGroup.getCurrency();
            if (USE_CUSTOM_CAMERA) {
                mReceiptImagePaths = new ArrayList<>();
            }
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelableArrayList(STATE_ROW_ITEMS, mItems);
        outState.putBoolean(STATE_SAVING, mSaving);
        outState.putLong(STATE_DATE, DateUtils.parseDateToLong(mDate));
        outState.putString(STATE_STORE, mStore);
        outState.putString(STATE_CURRENCY, mCurrency);
        outState.putString(STATE_NOTE, mNote);
        outState.putString(STATE_RECEIPT_IMAGE_PATH, mReceiptImagePath);
        if (USE_CUSTOM_CAMERA) {
            outState.putStringArrayList(STATE_RECEIPT_IMAGE_PATHS, mReceiptImagePaths);
        }
        outState.putBoolean(STATE_FETCHING_RATES, mFetchingExchangeRates);
    }

    @Override
    public boolean isSaving() {
        return mSaving;
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
    @Bindable
    public String getCurrency() {
        return mCurrency;
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
    }

    @Override
    public boolean isExchangeRateVisible() {
        return mExchangeRate == 1;
    }

    @Override
    public void onRowPriceChanged() {
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal myShare = BigDecimal.ZERO;
        final int maximumFractionDigits = MoneyUtils.getMaximumFractionDigits(mCurrency);

        for (RowItem rowItem : mItems) {
            final BigDecimal finalPrice = rowItem.parsePrice(mCurrency);

            // update total price
            totalPrice = totalPrice.add(finalPrice);

            // update my share
            final RowItemUser[] rowItemUsers = rowItem.getUsers();
            final List<User> usersInvolved = getUsersInvolved(rowItemUsers);
            if (usersInvolved.contains(mCurrentUser)) {
                final BigDecimal usersCount = new BigDecimal(usersInvolved.size());
                myShare = myShare.add(finalPrice.divide(usersCount, maximumFractionDigits,
                        BigDecimal.ROUND_HALF_UP));
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
        mCurrency = (String) parent.getItemAtPosition(position);

        // update item prices, total price and my share
        for (RowItem rowItem : mItems) {
            rowItem.updateCurrency(mCurrency);
        }
        notifyPropertyChanged(BR.totalPrice);
        notifyPropertyChanged(BR.myShare);
        notifyPropertyChanged(BR.currency);

        // update exchangeRate
        updateExchangeRate();
    }

    private void updateExchangeRate() {
        if (mCurrency.equals(mCurrentGroup.getCurrency())) {
            mExchangeRate = 1;
            notifyPropertyChanged(BR.exchangeRateVisible);
        } else {
            mExchangeRate = mSharedPreferences.getFloat(mCurrency, 1);
            if (mExchangeRate == 1) {
                mView.loadFetchExchangeRatesWorker(mCurrentGroup.getCurrency());
            } else {
                long lastFetched = mSharedPreferences.getLong(EXCHANGE_RATE_LAST_FETCHED_TIME, 0);
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastFetched > EXCHANGE_RATE_REFRESH_INTERVAL) {
                    final SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putLong(EXCHANGE_RATE_LAST_FETCHED_TIME, currentTime);
                    editor.apply();

                    mFetchingExchangeRates = true;
                    mView.loadFetchExchangeRatesWorker(mCurrentGroup.getCurrency());
                } else {
                    notifyPropertyChanged(BR.exchangeRate);
                    notifyPropertyChanged(BR.exchangeRateVisible);
                }
            }
        }
    }

    @Override
    public void setRatesFetchStream(@NonNull Observable<CurrencyRates> observable,
                                    @NonNull final String workerTag) {
        mSubscriptions.add(observable
                .map(new Func1<CurrencyRates, Map<String, Float>>() {
                    @Override
                    public Map<String, Float> call(CurrencyRates currencyRates) {
                        return currencyRates.getRates();
                    }
                })
                .toSingle()
                .subscribe(new SingleSubscriber<Map<String, Float>>() {
                    @Override
                    public void onSuccess(Map<String, Float> exchangeRates) {
                        mFetchingExchangeRates = false;
                        mView.removeWorker(workerTag);

                        final SharedPreferences.Editor editor = mSharedPreferences.edit();
                        for (Map.Entry<String, Float> exchangeRate : exchangeRates.entrySet()) {
                            final BigDecimal roundedExchangeRate = MoneyUtils.roundToFractionDigits(
                                    MoneyUtils.EXCHANGE_RATE_FRACTION_DIGITS, 1 / exchangeRate.getValue());
                            editor.putFloat(exchangeRate.getKey(), roundedExchangeRate.floatValue());
                        }
                        editor.apply();

                        setExchangeRate(exchangeRates.get(mCurrency));
                    }

                    @Override
                    public void onError(Throwable error) {
                        mFetchingExchangeRates = false;
                        mView.removeWorker(workerTag);
                        // TODO: handle error
                    }
                })
        );
    }

    @Override
    public void onExchangeRateSet(float exchangeRate) {
        final BigDecimal roundedExchangeRate = MoneyUtils.roundToFractionDigits(
                MoneyUtils.EXCHANGE_RATE_FRACTION_DIGITS, exchangeRate);
        setExchangeRate(roundedExchangeRate.floatValue());
    }

    @Override
    public void onExchangeRateClick(View view) {
        mView.showManualExchangeRateSelectorDialog(((TextView) view).getText().toString());
    }

    @Override
    public void attachView(@NonNull PurchaseAddEditViewModel.ViewListener view) {
        super.attachView(view);

        updateList();
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
        // only fill with first row on first start
        if (mItems.isEmpty()) {
            mItems.add(new RowItem(getRowItemUser(mUsersAvailable), mCurrency));
            mView.notifyItemInserted(ROWS_BEFORE_ITEMS);
        }
    }

    final RowItemUser[] getRowItemUser(@NonNull List<? extends ParseUser> users) {
        final int size = users.size();
        final RowItemUser[] rowItemUser = new RowItemUser[size];
        for (int i = 0; i < size; i++) {
            final User user = (User) users.get(i);
            rowItemUser[i] = new RowItemUser(user.getObjectId(), user.getNickname(), user.getAvatar());
        }

        return rowItemUser;
    }

    @Override
    public RowItem getItemAtPosition(int position) {
        return super.getItemAtPosition(getAdjustedPosition(position));
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }

        if (position == 1) {
            return TYPE_DATE;
        }

        if (position == 2) {
            return TYPE_STORE;
        }

        if (position == 3) {
            return TYPE_HEADER;
        }

        if (position == getLastPosition()) {
            return TYPE_ADD_ROW;
        }


        final RowItem rowItem = getItemAtPosition(position);
        if (rowItem == null) {
            return TYPE_USERS;
        }

        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        // TODO: or +2 if exchange rate visible
        return super.getItemCount() + ROWS_BEFORE_ITEMS + 1;
    }

    @Override
    public int getAdjustedPosition(int position) {
        return position - ROWS_BEFORE_ITEMS;
    }

    @Override
    public int adjustPosition(int position) {
        return position + ROWS_BEFORE_ITEMS;
    }

    @Override
    public void onItemDismissed(int position) {
        mItems.remove(getAdjustedPosition(position));
        mView.notifyItemRemoved(position);
        onRowPriceChanged();
    }

    @Override
    public void onTooFewUsersSelected() {
        mView.showMessage(R.string.toast_min_one_user);
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
        mView.showMessage(R.string.toast_receipt_added);
    }

    @Override
    public void onReceiptImageFailed() {
        mView.finishScreen(RESULT_PURCHASE_DISCARDED);
    }

    @Override
    public void onReceiptImagesTaken(@NonNull List<String> receiptImagePaths) {
        mReceiptImagePaths.clear();
        if (!receiptImagePaths.isEmpty()) {
            mReceiptImagePaths.addAll(receiptImagePaths);
        }
        mReceiptImagePath = mReceiptImagePaths.get(0);
        onReceiptImageTaken();
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
                for (int i = 0, size = ocrItems.size(); i < size; i++) {
                    final OcrItem ocrItem = ocrItems.get(i);
                    final String price = MoneyUtils.formatPrice(ocrItem.getPrice(), mCurrency);
                    mItems.add(new RowItem(ocrItem.getName(), price,
                            getRowItemUser(mUsersAvailable), mCurrency));
                    mView.notifyItemInserted(adjustPosition(i));
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
        final String tag = Purchase.PIN_LABEL + mCurrentGroup.getObjectId();
        if (!TextUtils.isEmpty(mReceiptImagePath)) {
            mSubscriptions.add(mView.getReceiptImage(mReceiptImagePath)
                    .flatMap(new Func1<byte[], Single<Purchase>>() {
                        @Override
                        public Single<Purchase> call(byte[] bytes) {
                            purchase.setReceiptData(bytes);
                            return mPurchaseRepo.savePurchaseAsDraftAsync(purchase, tag);
                        }
                    })
                    .subscribe(new SingleSubscriber<Purchase>() {
                        @Override
                        public void onSuccess(Purchase value) {
                            deleteTakenImages();
                            mView.finishScreen(RESULT_PURCHASE_DRAFT);
                        }

                        @Override
                        public void onError(Throwable error) {
                            // TODO: handle error
                        }
                    })
            );
        } else {
            mSubscriptions.add(mPurchaseRepo.savePurchaseAsDraftAsync(purchase, tag)
                    .subscribe(new SingleSubscriber<Purchase>() {
                        @Override
                        public void onSuccess(Purchase value) {
                            deleteTakenImages();
                            mView.finishScreen(RESULT_PURCHASE_DRAFT);
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
        if (mItems.size() < 1) {
            mView.showMessage(R.string.toast_min_one_item);
            return false;
        }

        boolean allValid = true;
        for (RowItem rowItem : mItems) {
            if (!rowItem.validateFields()) {
                allValid = false;
            }
        }

        return allValid;
    }

    private Purchase getPurchase() {
        final List<User> purchaseUsersInvolved = new ArrayList<>();
        final List<Item> purchaseItems = new ArrayList<>();

        for (RowItem rowItem : mItems) {
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
        final Purchase purchase = new Purchase(mCurrentUser, mCurrentGroup, mDate, mStore, purchaseItems,
                mTotalPrice.doubleValue(), purchaseUsersInvolved, mCurrency);
        if (!TextUtils.isEmpty(mNote)) {
            purchase.setNote(mNote);
        }
        return purchase;
    }

    @NonNull
    private List<User> getUsersInvolved(@NonNull RowItemUser[] rowItemUsers) {
        final List<User> usersInvolved = new ArrayList<>();
        for (int i = 0, size = mUsersAvailable.size(); i < size; i++) {
            final User user = mUsersAvailable.get(i);
            if (rowItemUsers[i].getObjectId().equals(user.getObjectId())) {
                usersInvolved.add(user);
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
        // TODO: possible?
        mView.showAddEditNoteDialog(mNote);
    }

    @Override
    public void onDeleteNoteClick() {
        mNote = "";
    }

    @Override
    public void onUpOrBackClick() {
        if (isSaving()) {
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
        mView.finishScreen(RESULT_PURCHASE_DISCARDED);
    }

    @Override
    public void onToggleUsersClick(int position) {
        final int insertPos = position + 1;
        final int insertPosAdj = getAdjustedPosition(insertPos);
        if (mItems.size() > insertPosAdj && mItems.get(insertPosAdj) == null) {
            mItems.remove(insertPosAdj);
            mView.notifyItemRemoved(insertPos);
        } else {
            mItems.add(insertPosAdj, null);
            mView.notifyItemInserted(insertPos);

            int pos = ROWS_BEFORE_ITEMS;
            for (Iterator<RowItem> iterator = mItems.iterator(); iterator.hasNext(); ) {
                final RowItem item = iterator.next();
                if (item == null && pos != insertPos) {
                    iterator.remove();
                    mView.notifyItemRemoved(pos);
                }

                pos++;
            }
        }
    }

    @Override
    public void onAddRowClick(int position) {
        mItems.add(new RowItem(getRowItemUser(mUsersAvailable), mCurrency));
        mView.notifyItemInserted(position);
        mView.scrollToPosition(getLastPosition());
    }

    @Override
    public void onProgressFinalAnimationComplete() {
        mView.finishScreen(RESULT_PURCHASE_SAVED);
    }
}
