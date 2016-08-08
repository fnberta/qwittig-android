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

import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Item;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.ListInteraction;
import ch.giantific.qwittig.presentation.common.MessageAction;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel.ViewListener;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditGenericItem;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditHeaderItem;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItem;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemIdentities;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemIdentity;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemModel;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemModel.Type;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link PurchaseAddEditViewModel} for the add purchase screen.
 */
public class PurchaseAddViewModelImpl extends ViewModelBaseImpl<ViewListener>
        implements PurchaseAddEditViewModel {

    private static final String STATE_ROW_ITEMS = "STATE_ROW_ITEMS";
    private static final String STATE_DATE = "STATE_DATE";
    private static final String STATE_STORE = "STATE_STORE";
    private static final String STATE_CURRENCY = "STATE_CURRENCY";
    private static final String STATE_EXCHANGE_RATE = "STATE_EXCHANGE_RATE";
    private static final String STATE_NOTE = "STATE_NOTE";
    private static final String STATE_RECEIPT_IMAGE_PATH = "STATE_RECEIPT_IMAGE_PATH";
    private static final String STATE_FETCHING_RATES = "STATE_FETCHING_RATES";
    protected final RemoteConfigHelper mConfigHelper;
    protected final PurchaseRepository mPurchaseRepo;
    protected final ArrayList<PurchaseAddEditItemModel> mItems;
    private final NumberFormat mExchangeRateFormatter;
    private final List<String> mSupportedCurrencies;
    private final DateFormat mDateFormatter;
    protected ListInteraction mListInteraction;
    protected List<Identity> mIdentities;
    protected Identity mCurrentIdentity;
    protected NumberFormat mMoneyFormatter;
    protected String mCurrency;
    protected String mReceipt;
    protected String mNote;
    protected Date mDate;
    protected String mStore;
    protected double mTotalValue = 0;
    protected double mExchangeRate;
    private String mTotal;
    private String mMyShare;
    private boolean mFetchingExchangeRates;

    public PurchaseAddViewModelImpl(@Nullable Bundle savedState,
                                    @NonNull Navigator navigator,
                                    @NonNull RxBus<Object> eventBus,
                                    @NonNull UserRepository userRepository,
                                    @NonNull PurchaseRepository purchaseRepo,
                                    @NonNull RemoteConfigHelper configHelper) {
        super(savedState, navigator, eventBus, userRepository);

        mConfigHelper = configHelper;
        mPurchaseRepo = purchaseRepo;
        mSupportedCurrencies = Arrays.asList(mConfigHelper.getSupportedCurrencyCodes());
        mIdentities = new ArrayList<>();

        if (savedState != null) {
            mItems = savedState.getParcelableArrayList(STATE_ROW_ITEMS);
            mDate = new Date(savedState.getLong(STATE_DATE));
            mStore = savedState.getString(STATE_STORE);
            setCurrency(savedState.getString(STATE_CURRENCY));
            mExchangeRate = savedState.getDouble(STATE_EXCHANGE_RATE);
            mNote = savedState.getString(STATE_NOTE);
            mReceipt = savedState.getString(STATE_RECEIPT_IMAGE_PATH);
            mFetchingExchangeRates = savedState.getBoolean(STATE_FETCHING_RATES);
        } else {
            mItems = new ArrayList<>();
            mDate = new Date();
            mExchangeRate = 1;
            initFixedRows();
        }

        mDateFormatter = DateUtils.getDateFormatter(false);
        mExchangeRateFormatter = MoneyUtils.getExchangeRateFormatter();
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
        outState.putString(STATE_RECEIPT_IMAGE_PATH, mReceipt);
        outState.putBoolean(STATE_FETCHING_RATES, mFetchingExchangeRates);
    }

    @Override
    public void setListInteraction(@NonNull ListInteraction listInteraction) {
        mListInteraction = listInteraction;
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
    @Bindable
    public boolean isEmpty() {
        return mItems.isEmpty();
    }

    @Override
    public PurchaseAddEditItemModel getItemAtPosition(int position) {
        return mItems.get(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    @Override
    @Bindable
    public String getReceipt() {
        return mReceipt;
    }

    @Override
    public void setReceipt(@NonNull String receipt) {
        mReceipt = receipt;
        notifyPropertyChanged(BR.receipt);
        notifyPropertyChanged(BR.receiptAvailable);
    }

    @Override
    @Bindable
    public boolean isReceiptAvailable() {
        return !TextUtils.isEmpty(mReceipt);
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

    @Bindable
    public String getTotal() {
        return mTotal;
    }

    public void setTotal(double total) {
        mTotalValue = total;
        mTotal = mMoneyFormatter.format(total);
        notifyPropertyChanged(BR.total);
    }

    @Override
    @Bindable
    public String getMyShare() {
        return mMyShare;
    }

    @Override
    public void setMyShare(double myShare) {
        mMyShare = mMoneyFormatter.format(myShare);
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
        if (TextUtils.isEmpty(mCurrency) || Objects.equals(currency, mCurrency)) {
            return;
        }

        mCurrency = currency;
        mMoneyFormatter = MoneyUtils.getMoneyFormatter(currency, false, true);

        // TODO: only needed once we support currencies with other than 2 decimal values
//        updatePriceFormatting();

        // update my share currency field
        notifyPropertyChanged(BR.currency);

        // get new exchange rate
        mView.loadFetchExchangeRatesWorker(mCurrentIdentity.getGroupCurrency(), mCurrency);
    }

    private void updatePriceFormatting() {
        // update items price formatting
        for (int i = 0, size = mItems.size(); i < size; i++) {
            final PurchaseAddEditItemModel addEditItem = mItems.get(i);
            if (addEditItem.getType() == Type.ITEM) {
                final PurchaseAddEditItem purchaseAddEditItem = (PurchaseAddEditItem) addEditItem;
                purchaseAddEditItem.updatePriceFormat(mMoneyFormatter);
            }
        }

        // update total price and my share formatting
        notifyPropertyChanged(BR.total);
        notifyPropertyChanged(BR.myShare);
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
                                        mView.loadFetchExchangeRatesWorker(mCurrentIdentity.getGroupCurrency(), mCurrency);
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
    public void onDeleteNote() {
        setNote("");
        mView.showPurchaseItems();
        mView.reloadOptionsMenu();
        mView.showMessage(R.string.toast_note_deleted);
    }

    @Override
    public void onNoteSet(@NonNull String note) {
        if (!Objects.equals(mNote, note)) {
            mView.showMessage(TextUtils.isEmpty(mNote)
                    ? R.string.toast_note_added
                    : R.string.toast_note_edited);
            setNote(note);
            mView.reloadOptionsMenu();
        }
    }

    @Override
    public void onRowPriceChanged() {
        updateTotalAndMyShare();
    }

    private void updateTotalAndMyShare() {
        double total = 0;
        double myShare = 0;
        for (PurchaseAddEditItemModel addEditItem : mItems) {
            if (addEditItem.getType() != Type.ITEM) {
                continue;
            }

            final PurchaseAddEditItem purchaseAddEditItem = (PurchaseAddEditItem) addEditItem;
            final double itemPrice = purchaseAddEditItem.parsePrice();

            // update total price
            total += itemPrice;

            // update my share
            final PurchaseAddEditItemIdentity[] itemUsersRows = purchaseAddEditItem.getUsers();
            int selectedCount = 0;
            boolean currentIdentityInvolved = false;
            for (PurchaseAddEditItemIdentity addEditPurchaseItemUsersUser : itemUsersRows) {
                if (!addEditPurchaseItemUsersUser.isSelected()) {
                    continue;
                }

                selectedCount++;
                if (Objects.equals(addEditPurchaseItemUsersUser.getIdentityId(), mCurrentIdentity.getId())) {
                    currentIdentityInvolved = true;
                }
            }
            if (currentIdentityInvolved) {
                myShare += (itemPrice / selectedCount);
            }
        }

        setTotal(total);
        setMyShare(myShare);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        loadPurchase(currentUser);
    }

    protected void loadPurchase(@NonNull FirebaseUser currentUser) {
        getSubscriptions().add(getInitialChain(currentUser)
                .subscribe(new SingleSubscriber<List<Identity>>() {
                    @Override
                    public void onSuccess(List<Identity> value) {
                        updateRows();
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_purchase_add_edit_load);
                    }
                })
        );
    }

    @NonNull
    protected final Single<List<Identity>> getInitialChain(@NonNull FirebaseUser currentUser) {
        return mUserRepo.getUser(currentUser.getUid())
                .flatMap(new Func1<User, Single<Identity>>() {
                    @Override
                    public Single<Identity> call(final User user) {
                        return mUserRepo.getIdentity(user.getCurrentIdentity());
                    }
                })
                .doOnSuccess(new Action1<Identity>() {
                    @Override
                    public void call(Identity identity) {
                        mCurrentIdentity = identity;
                        if (TextUtils.isEmpty(mCurrency)) {
                            setCurrency(identity.getGroupCurrency());
                        }
                        mMoneyFormatter = MoneyUtils.getMoneyFormatter(mCurrency, false, true);
                    }
                })
                .flatMapObservable(new Func1<Identity, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(Identity identity) {
                        return mUserRepo.getGroupIdentities(identity.getGroup(), true);
                    }
                })
                .toSortedList()
                .toSingle()
                .doOnSuccess(new Action1<List<Identity>>() {
                    @Override
                    public void call(List<Identity> identities) {
                        mIdentities = identities;
                    }
                });
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
                final PurchaseAddEditItem purchaseAddEditItem = new PurchaseAddEditItem(getItemUsers());
                purchaseAddEditItem.setMoneyFormatter(mMoneyFormatter);
                purchaseAddEditItem.setPriceChangedListener(this);
                mItems.add(i, purchaseAddEditItem);
                mListInteraction.notifyItemInserted(i);
                return;
            }
        }

        // hack: run another update total and my share run because after configuration change,
        // recycler view is not yet able to receive the update calls before
        updateTotalAndMyShare();
    }

    protected final PurchaseAddEditItemIdentity[] getItemUsers() {
        final Set<String> identitiesIds = new HashSet<>(mIdentities.size());
        for (Identity identity : mIdentities) {
            identitiesIds.add(identity.getId());
        }

        return getItemUsers(identitiesIds);
    }

    protected final PurchaseAddEditItemIdentity[] getItemUsers(@NonNull Set<String> identities) {
        final int size = mIdentities.size();
        final PurchaseAddEditItemIdentity[] itemUsersRow = new PurchaseAddEditItemIdentity[size];
        for (int i = 0; i < size; i++) {
            final Identity identity = mIdentities.get(i);
            final String id = identity.getId();
            itemUsersRow[i] = new PurchaseAddEditItemIdentity(id, identity.getNickname(),
                    identity.getAvatar(), identities.contains(id));
        }

        return itemUsersRow;
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
    public void onItemRowUserClick() {
        for (int i = 0, mItemsSize = mItems.size(); i < mItemsSize; i++) {
            final PurchaseAddEditItemModel itemModel = mItems.get(i);
            if (itemModel.getType() != Type.USERS) {
                continue;
            }

            final PurchaseAddEditItem addEditItem = (PurchaseAddEditItem) getItemAtPosition(i - 1);
            addEditItem.notifyPropertyChanged(BR.users);
        }

        updateTotalAndMyShare();
    }

    @Override
    public void onItemRowUserLongClick(@NonNull PurchaseAddEditItemIdentity userClicked) {
        for (PurchaseAddEditItemModel itemModel : mItems) {
            if (itemModel.getType() != Type.ITEM) {
                continue;
            }

            final PurchaseAddEditItem addEditItem = (PurchaseAddEditItem) itemModel;
            addEditItem.toggleUser(userClicked);
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
        mItems.add(pos, new PurchaseAddEditItemIdentities(purchaseAddEditItem.getUsers()));
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

        final PurchaseAddEditItem purchaseAddEditItem = new PurchaseAddEditItem(getItemUsers());
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
        mView.showMessage(TextUtils.isEmpty(mReceipt) ? R.string.toast_receipt_added : R.string.toast_receipt_changed);
        setReceipt(receiptImagePath);
        mView.reloadOptionsMenu();
    }

    @Override
    public void onReceiptImageTakeFailed() {
        mView.showMessage(R.string.toast_create_image_file_failed);
    }

    @Override
    public void onDeleteReceiptMenuClick() {
        deleteReceiptImage();
        setReceipt("");

        mView.showMessage(R.string.toast_receipt_deleted);
        mView.reloadOptionsMenu();
    }

    private void deleteReceiptImage() {
        final File receipt = new File(mReceipt);
        if (!receipt.delete()) {
            Timber.w("failed to delete receipt file");
        }
    }

    @Override
    public void onFabSavePurchaseClick(View view) {
        if (mFetchingExchangeRates) {
            mView.showMessage(R.string.toast_exchange_rate_fetching);
            return;
        }

        if (!Objects.equals(mCurrency, mCurrentIdentity.getGroupCurrency()) && mExchangeRate == 1) {
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

        startPurchaseSave(false);
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

    private void startPurchaseSave(final boolean asDraft) {
        final Purchase purchase = getPurchase(asDraft);
        savePurchase(purchase, asDraft);
        onPurchaseSaved(asDraft);
    }

    protected void savePurchase(Purchase purchase, boolean asDraft) {
        if (asDraft) {
            mPurchaseRepo.saveDraft(purchase, null);
        } else {
            mPurchaseRepo.savePurchase(purchase, null, false);
        }
    }

    protected void onPurchaseSaved(boolean asDraft) {
        mNavigator.finish(asDraft ? PurchaseResult.PURCHASE_DRAFT : PurchaseResult.PURCHASE_SAVED);
    }

    @NonNull
    private Purchase getPurchase(final boolean isDraft) {
        final List<String> purchaseIdentities = new ArrayList<>();
        final List<Item> purchaseItems = new ArrayList<>();
        final int fractionDigits = MoneyUtils.getFractionDigits(mCurrency);

        for (PurchaseAddEditItemModel addEditItem : mItems) {
            if (addEditItem.getType() != Type.ITEM) {
                continue;
            }

            final PurchaseAddEditItem itemModel = (PurchaseAddEditItem) addEditItem;
            final String itemName = itemModel.getName();
            final String name = itemName != null ? itemName : "";
            final String price = itemModel.getPrice();

            final List<String> identities = new ArrayList<>();
            for (PurchaseAddEditItemIdentity row : itemModel.getUsers()) {
                if (row.isSelected()) {
                    final String identityId = row.getIdentityId();
                    identities.add(identityId);
                    if (!purchaseIdentities.contains(identityId)) {
                        purchaseIdentities.add(identityId);
                    }
                }
            }

            final double priceRounded = convertRoundItemPrice(price, fractionDigits);
            final Item item = new Item(name, priceRounded, identities);
            purchaseItems.add(item);
        }

        return createPurchase(purchaseIdentities, purchaseItems, fractionDigits, isDraft);
    }

    private double convertRoundItemPrice(@NonNull String priceText, int fractionDigits) {
        try {
            final double price = new BigDecimal(priceText)
                    .setScale(fractionDigits, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
            if (mExchangeRate == 1) {
                return price;
            }

            return new BigDecimal(price * mExchangeRate)
                    .setScale(MoneyUtils.CONVERTED_PRICE_FRACTION_DIGITS, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
        } catch (NumberFormatException e) {
            try {
                final double parsed = mMoneyFormatter.parse(priceText).doubleValue();
                if (mExchangeRate == 1) {
                    return new BigDecimal(parsed)
                            .setScale(fractionDigits, BigDecimal.ROUND_HALF_UP)
                            .doubleValue();
                }

                return new BigDecimal(parsed * mExchangeRate)
                        .setScale(MoneyUtils.CONVERTED_PRICE_FRACTION_DIGITS, BigDecimal.ROUND_HALF_UP)
                        .doubleValue();
            } catch (ParseException e1) {
                return 0;
            }
        }
    }

    @NonNull
    protected Purchase createPurchase(@NonNull List<String> purchaseIdentities,
                                      @NonNull List<Item> purchaseItems,
                                      int fractionDigits, boolean isDraft) {
        final double total = convertRoundTotal(fractionDigits);
        return new Purchase(mCurrentIdentity.getGroup(), mCurrentIdentity.getId(), mDate, mStore,
                total, mCurrency, mExchangeRate, mReceipt, mNote, isDraft, null,
                purchaseIdentities, purchaseItems);
    }

    protected final double convertRoundTotal(int fractionDigits) {
        if (mExchangeRate == 1) {
            return new BigDecimal(mTotalValue).setScale(fractionDigits,
                    BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        final double totalConverted = mTotalValue * mExchangeRate;
        return new BigDecimal(totalConverted).setScale(MoneyUtils.CONVERTED_PRICE_FRACTION_DIGITS,
                BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    @Override
    public void onSaveAsDraftMenuClick() {
        if (mFetchingExchangeRates) {
            mView.showMessage(R.string.toast_exchange_rate_fetching);
            return;
        }

        startPurchaseSave(true);
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