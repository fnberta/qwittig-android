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
import ch.giantific.qwittig.data.repositories.GroupRepository;
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
    protected final RemoteConfigHelper configHelper;
    protected final PurchaseRepository purchaseRepo;
    protected final ArrayList<PurchaseAddEditItemModel> items;
    private final GroupRepository groupRepo;
    private final NumberFormat exchangeRateFormatter;
    private final List<String> supportedCurrencies;
    private final DateFormat dateFormatter;
    protected ListInteraction listInteraction;
    protected List<Identity> identities;
    protected Identity currentIdentity;
    protected NumberFormat moneyFormatter;
    protected String currency;
    protected String receipt;
    protected String note;
    protected Date date;
    protected String store;
    protected double exchangeRate;
    private double totalValue = 0;
    private String total;
    private String myShare;
    private boolean fetchingExchangeRates;

    public PurchaseAddViewModelImpl(@Nullable Bundle savedState,
                                    @NonNull Navigator navigator,
                                    @NonNull RxBus<Object> eventBus,
                                    @NonNull UserRepository userRepo,
                                    @NonNull GroupRepository groupRepo,
                                    @NonNull PurchaseRepository purchaseRepo,
                                    @NonNull RemoteConfigHelper configHelper) {
        super(savedState, navigator, eventBus, userRepo);

        this.configHelper = configHelper;
        this.groupRepo = groupRepo;
        this.purchaseRepo = purchaseRepo;
        supportedCurrencies = Arrays.asList(this.configHelper.getSupportedCurrencyCodes());
        identities = new ArrayList<>();

        if (savedState != null) {
            items = savedState.getParcelableArrayList(STATE_ROW_ITEMS);
            date = new Date(savedState.getLong(STATE_DATE));
            store = savedState.getString(STATE_STORE);
            setCurrency(savedState.getString(STATE_CURRENCY));
            exchangeRate = savedState.getDouble(STATE_EXCHANGE_RATE);
            note = savedState.getString(STATE_NOTE);
            receipt = savedState.getString(STATE_RECEIPT_IMAGE_PATH);
            fetchingExchangeRates = savedState.getBoolean(STATE_FETCHING_RATES);
        } else {
            items = new ArrayList<>();
            date = new Date();
            exchangeRate = 1;
            initFixedRows();
        }

        dateFormatter = DateUtils.getDateFormatter(false);
        exchangeRateFormatter = MoneyUtils.getExchangeRateFormatter();
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelableArrayList(STATE_ROW_ITEMS, items);
        outState.putLong(STATE_DATE, date.getTime());
        outState.putString(STATE_STORE, store);
        outState.putString(STATE_CURRENCY, currency);
        outState.putDouble(STATE_EXCHANGE_RATE, exchangeRate);
        outState.putString(STATE_NOTE, note);
        outState.putString(STATE_RECEIPT_IMAGE_PATH, receipt);
        outState.putBoolean(STATE_FETCHING_RATES, fetchingExchangeRates);
    }

    @Override
    public void setListInteraction(@NonNull ListInteraction listInteraction) {
        this.listInteraction = listInteraction;
    }

    private void initFixedRows() {
        items.add(new PurchaseAddEditHeaderItem(R.string.header_purchase));
        items.add(PurchaseAddEditGenericItem.createNewDateInstance());
        items.add(PurchaseAddEditGenericItem.createNewStoreInstance());
        items.add(new PurchaseAddEditHeaderItem(R.string.header_items));
        items.add(PurchaseAddEditGenericItem.createNewAddRowInstance());
        items.add(PurchaseAddEditGenericItem.createNewTotalInstance());
    }

    @Override
    @Bindable
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public PurchaseAddEditItemModel getItemAtPosition(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    @Bindable
    public String getReceipt() {
        return receipt;
    }

    @Override
    public void setReceipt(@NonNull String receipt) {
        this.receipt = receipt;
        notifyPropertyChanged(BR.receipt);
        notifyPropertyChanged(BR.receiptAvailable);
    }

    @Override
    @Bindable
    public boolean isReceiptAvailable() {
        return !TextUtils.isEmpty(receipt);
    }

    @Override
    @Bindable
    public String getNote() {
        return note;
    }

    @Override
    public void setNote(@NonNull String note) {
        this.note = note;
        notifyPropertyChanged(BR.note);
    }

    @Override
    public boolean isNoteAvailable() {
        return !TextUtils.isEmpty(note);
    }

    @Override
    public List<String> getSupportedCurrencies() {
        return supportedCurrencies;
    }

    @Override
    @Bindable
    public String getDate() {
        return dateFormatter.format(date);
    }

    @Override
    public void setDate(@NonNull Date date) {
        this.date = date;
        notifyPropertyChanged(BR.date);
    }

    @Override
    public void onDateSet(@NonNull Date date) {
        setDate(date);
    }

    @Override
    public void onDateClick(View view) {
        this.view.showDatePickerDialog();
    }

    @Override
    @Bindable
    public String getStore() {
        return store;
    }

    @Override
    public void setStore(@NonNull String store) {
        this.store = store;
        notifyPropertyChanged(BR.store);
    }

    @Bindable
    public String getTotal() {
        return total;
    }

    public void setTotal(double total) {
        totalValue = total;
        this.total = moneyFormatter.format(total);
        notifyPropertyChanged(BR.total);
    }

    @Override
    @Bindable
    public String getMyShare() {
        return myShare;
    }

    @Override
    public void setMyShare(double myShare) {
        this.myShare = moneyFormatter.format(myShare);
        notifyPropertyChanged(BR.myShare);
    }

    @Override
    @Bindable
    public String getCurrency() {
        return currency;
    }

    @Override
    public void setCurrency(@NonNull String currency) {
        this.currency = currency;
        notifyPropertyChanged(BR.currencySelected);
        notifyPropertyChanged(BR.currency);
    }

    @Override
    @Bindable
    public int getCurrencySelected() {
        return supportedCurrencies.indexOf(currency);
    }

    @Override
    public void onCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        final String currency = (String) parent.getItemAtPosition(position);
        if (TextUtils.isEmpty(this.currency) || Objects.equals(currency, this.currency)) {
            return;
        }

        this.currency = currency;
        moneyFormatter = MoneyUtils.getMoneyFormatter(currency, false, true);

        // TODO: only needed once we support currencies with other than 2 decimal values
//        updatePriceFormatting();

        // update my share currency field
        notifyPropertyChanged(BR.currency);

        // get new exchange rate
        this.view.loadFetchExchangeRatesWorker(currentIdentity.getGroupCurrency(), this.currency);
    }

    private void updatePriceFormatting() {
        // update items price formatting
        for (int i = 0, size = items.size(); i < size; i++) {
            final PurchaseAddEditItemModel addEditItem = items.get(i);
            if (addEditItem.getType() == Type.ITEM) {
                final PurchaseAddEditItem purchaseAddEditItem = (PurchaseAddEditItem) addEditItem;
                purchaseAddEditItem.updatePriceFormat(moneyFormatter);
            }
        }

        // update total price and my share formatting
        notifyPropertyChanged(BR.total);
        notifyPropertyChanged(BR.myShare);
    }

    @Override
    @Bindable
    public String getExchangeRate() {
        return exchangeRateFormatter.format(exchangeRate);
    }

    @Override
    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
        notifyPropertyChanged(BR.exchangeRate);
        notifyPropertyChanged(BR.exchangeRateVisible);
    }

    @Override
    @Bindable
    public boolean isExchangeRateVisible() {
        return exchangeRate != 1;
    }

    @Override
    public void onExchangeRateClick(View view) {
        this.view.showManualExchangeRateSelectorDialog(((TextView) view).getText().toString());
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
                        view.removeWorker(workerTag);
                        fetchingExchangeRates = false;

                        setExchangeRate(exchangeRate);
                    }

                    @Override
                    public void onError(Throwable error) {
                        view.removeWorker(workerTag);
                        fetchingExchangeRates = false;

                        view.showMessageWithAction(R.string.toast_error_exchange_rate,
                                new MessageAction(R.string.action_retry) {
                                    @Override
                                    public void onClick(View v) {
                                        view.loadFetchExchangeRatesWorker(currentIdentity.getGroupCurrency(), currency);
                                    }
                                });
                    }
                })
        );
    }

    @Override
    public void onAddEditNoteMenuClick() {
        view.showAddEditNoteDialog(note);
    }

    @Override
    public void onDeleteNote() {
        setNote("");
        view.showPurchaseItems();
        view.reloadOptionsMenu();
        view.showMessage(R.string.toast_note_deleted);
    }

    @Override
    public void onNoteSet(@NonNull String note) {
        if (!Objects.equals(this.note, note)) {
            view.showMessage(TextUtils.isEmpty(this.note)
                    ? R.string.toast_note_added
                    : R.string.toast_note_edited);
            setNote(note);
            view.reloadOptionsMenu();
        }
    }

    @Override
    public void onRowPriceChanged() {
        updateTotalAndMyShare();
    }

    private void updateTotalAndMyShare() {
        double total = 0;
        double myShare = 0;
        for (PurchaseAddEditItemModel addEditItem : items) {
            if (addEditItem.getType() != Type.ITEM) {
                continue;
            }

            final PurchaseAddEditItem purchaseAddEditItem = (PurchaseAddEditItem) addEditItem;
            final double itemPrice = purchaseAddEditItem.parsePrice();

            // update total price
            total += itemPrice;

            // update my share
            final PurchaseAddEditItemIdentity[] itemUsersRows = purchaseAddEditItem.getIdentities();
            int selectedCount = 0;
            boolean currentIdentityInvolved = false;
            for (PurchaseAddEditItemIdentity addEditPurchaseItemUsersUser : itemUsersRows) {
                if (!addEditPurchaseItemUsersUser.isSelected()) {
                    continue;
                }

                selectedCount++;
                if (Objects.equals(addEditPurchaseItemUsersUser.getIdentityId(), currentIdentity.getId())) {
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
                        view.showMessage(R.string.toast_error_purchase_add_edit_load);
                    }
                })
        );
    }

    @NonNull
    protected final Single<List<Identity>> getInitialChain(@NonNull FirebaseUser currentUser) {
        return userRepo.getUser(currentUser.getUid())
                .flatMap(new Func1<User, Single<Identity>>() {
                    @Override
                    public Single<Identity> call(final User user) {
                        return userRepo.getIdentity(user.getCurrentIdentity());
                    }
                })
                .doOnSuccess(new Action1<Identity>() {
                    @Override
                    public void call(Identity identity) {
                        currentIdentity = identity;
                        if (TextUtils.isEmpty(currency)) {
                            setCurrency(identity.getGroupCurrency());
                        }
                        moneyFormatter = MoneyUtils.getMoneyFormatter(currency, false, true);
                    }
                })
                .flatMapObservable(new Func1<Identity, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(Identity identity) {
                        return groupRepo.getGroupIdentities(identity.getGroup(), true);
                    }
                })
                .toSortedList()
                .toSingle()
                .doOnSuccess(new Action1<List<Identity>>() {
                    @Override
                    public void call(List<Identity> identities) {
                        PurchaseAddViewModelImpl.this.identities = identities;
                    }
                });
    }

    protected final void updateRows() {
        boolean hasItems = false;
        for (int i = 0, size = items.size(); i < size; i++) {
            final PurchaseAddEditItemModel addEditItem = items.get(i);

            if (addEditItem.getType() == Type.ITEM) {
                hasItems = true;

                final PurchaseAddEditItem purchaseAddEditItem = (PurchaseAddEditItem) addEditItem;
                purchaseAddEditItem.setMoneyFormatter(moneyFormatter);
                purchaseAddEditItem.setPriceChangedListener(this);
                continue;
            }

            // fill with one row on first start
            if (addEditItem.getType() == Type.ADD_ROW && !hasItems) {
                final PurchaseAddEditItem purchaseAddEditItem = new PurchaseAddEditItem(getItemUsers());
                purchaseAddEditItem.setMoneyFormatter(moneyFormatter);
                purchaseAddEditItem.setPriceChangedListener(this);
                items.add(i, purchaseAddEditItem);
                listInteraction.notifyItemInserted(i);
                return;
            }
        }

        // hack: run another update total and my share run because after configuration change,
        // recycler view is not yet able to receive the update calls before
        updateTotalAndMyShare();
    }

    protected final PurchaseAddEditItemIdentity[] getItemUsers() {
        final Set<String> identitiesIds = new HashSet<>(identities.size());
        for (Identity identity : identities) {
            identitiesIds.add(identity.getId());
        }

        return getItemUsers(identitiesIds);
    }

    protected final PurchaseAddEditItemIdentity[] getItemUsers(@NonNull Set<String> identities) {
        final int size = this.identities.size();
        final PurchaseAddEditItemIdentity[] itemUsersRow = new PurchaseAddEditItemIdentity[size];
        for (int i = 0; i < size; i++) {
            final Identity identity = this.identities.get(i);
            final String id = identity.getId();
            itemUsersRow[i] = new PurchaseAddEditItemIdentity(id, identity.getNickname(),
                    identity.getAvatar(), identities.contains(id));
        }

        return itemUsersRow;
    }


    @Override
    public void onItemDismiss(int position) {
        items.remove(position);
        if (getItemViewType(position) != Type.USERS) {
            listInteraction.notifyItemRemoved(position);
        } else {
            items.remove(position);
            listInteraction.notifyItemRangeRemoved(position, 2);
        }
        onRowPriceChanged();
    }

    @Override
    public void onItemRowUserClick() {
        for (int i = 0, mItemsSize = items.size(); i < mItemsSize; i++) {
            final PurchaseAddEditItemModel itemModel = items.get(i);
            if (itemModel.getType() != Type.USERS) {
                continue;
            }

            final PurchaseAddEditItem addEditItem = (PurchaseAddEditItem) getItemAtPosition(i - 1);
            addEditItem.notifyPropertyChanged(BR.identities);
        }

        updateTotalAndMyShare();
    }

    @Override
    public void onItemRowUserLongClick(@NonNull PurchaseAddEditItemIdentity userClicked) {
        for (PurchaseAddEditItemModel itemModel : items) {
            if (itemModel.getType() != Type.ITEM) {
                continue;
            }

            final PurchaseAddEditItem addEditItem = (PurchaseAddEditItem) itemModel;
            addEditItem.toggleUser(userClicked);
            addEditItem.notifyPropertyChanged(BR.identities);
        }

        updateTotalAndMyShare();
    }

    @Override
    public void onToggleUsersClick(@NonNull PurchaseAddEditItem itemModel) {
        final int insertPos = items.indexOf(itemModel) + 1;
        if (items.size() < insertPos) {
            expandItemRow(insertPos, itemModel);
        } else if (getItemViewType(insertPos) == Type.USERS) {
            collapseItemRow(insertPos);
        } else {
            expandItemRow(insertPos, itemModel);
        }
    }

    private void collapseItemRow(int pos) {
        items.remove(pos);
        listInteraction.notifyItemRemoved(pos);
    }

    private void expandItemRow(int pos, PurchaseAddEditItemModel parent) {
        final PurchaseAddEditItem purchaseAddEditItem = (PurchaseAddEditItem) parent;
        items.add(pos, new PurchaseAddEditItemIdentities(purchaseAddEditItem.getIdentities()));
        listInteraction.notifyItemInserted(pos);

        collapseOtherItemRows(pos);
    }

    private void collapseOtherItemRows(int insertPos) {
        int pos = 0;
        for (Iterator<PurchaseAddEditItemModel> iterator = items.iterator(); iterator.hasNext(); ) {
            final int type = iterator.next().getType();
            if (type == Type.USERS && pos != insertPos) {
                iterator.remove();
                listInteraction.notifyItemRemoved(pos);
            }

            pos++;
        }
    }

    @Override
    public void onAddRowClick(@NonNull PurchaseAddEditItemModel itemModel) {
        final int position = items.indexOf(itemModel);

        final PurchaseAddEditItem purchaseAddEditItem = new PurchaseAddEditItem(getItemUsers());
        purchaseAddEditItem.setMoneyFormatter(moneyFormatter);
        purchaseAddEditItem.setPriceChangedListener(this);
        items.add(position, purchaseAddEditItem);
        listInteraction.notifyItemInserted(position);
        listInteraction.scrollToPosition(position + 1);
    }

    @Override
    public void onAddEditReceiptImageMenuClick() {
        view.captureImage();
    }

    @Override
    public void onReceiptImageTaken(@NonNull String receiptImagePath) {
        view.showMessage(TextUtils.isEmpty(receipt) ? R.string.toast_receipt_added : R.string.toast_receipt_changed);
        setReceipt(receiptImagePath);
        view.reloadOptionsMenu();
    }

    @Override
    public void onReceiptImageTakeFailed() {
        view.showMessage(R.string.toast_create_image_file_failed);
    }

    @Override
    public void onDeleteReceiptMenuClick() {
        deleteReceiptImage();
        setReceipt("");

        view.showMessage(R.string.toast_receipt_deleted);
        view.reloadOptionsMenu();
    }

    private void deleteReceiptImage() {
        final File receipt = new File(this.receipt);
        if (!receipt.delete()) {
            Timber.w("failed to delete receipt file");
        }
    }

    @Override
    public void onFabSavePurchaseClick(View view) {
        if (fetchingExchangeRates) {
            this.view.showMessage(R.string.toast_exchange_rate_fetching);
            return;
        }

        if (!Objects.equals(currency, currentIdentity.getGroupCurrency()) && exchangeRate == 1) {
            this.view.showMessageWithAction(R.string.toast_exchange_no_data,
                    new MessageAction(R.string.action_purchase_save_draft) {
                        @Override
                        public void onClick(View v) {
                            onSaveAsDraftMenuClick();
                        }
                    });
            return;
        }

        if (TextUtils.isEmpty(store)) {
            this.view.showMessage(R.string.toast_store_empty);
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
        for (PurchaseAddEditItemModel addEditItem : items) {
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
            view.showMessage(R.string.toast_min_one_item);
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
            purchaseRepo.saveDraft(purchase, null);
        } else {
            purchaseRepo.savePurchase(purchase, null, false);
        }
    }

    protected void onPurchaseSaved(boolean asDraft) {
        navigator.finish(asDraft ? PurchaseResult.PURCHASE_DRAFT : PurchaseResult.PURCHASE_SAVED);
    }

    @NonNull
    private Purchase getPurchase(final boolean isDraft) {
        final List<String> purchaseIdentities = new ArrayList<>();
        final List<Item> purchaseItems = new ArrayList<>();
        final int fractionDigits = MoneyUtils.getFractionDigits(currency);

        for (PurchaseAddEditItemModel addEditItem : items) {
            if (addEditItem.getType() != Type.ITEM) {
                continue;
            }

            final PurchaseAddEditItem itemModel = (PurchaseAddEditItem) addEditItem;
            final String itemName = itemModel.getName();
            final String name = itemName != null ? itemName : "";
            final String price = itemModel.getPrice();

            final List<String> identities = new ArrayList<>();
            for (PurchaseAddEditItemIdentity row : itemModel.getIdentities()) {
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
            if (exchangeRate == 1) {
                return price;
            }

            return new BigDecimal(price * exchangeRate)
                    .setScale(MoneyUtils.CONVERTED_PRICE_FRACTION_DIGITS, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
        } catch (NumberFormatException e) {
            try {
                final double parsed = moneyFormatter.parse(priceText).doubleValue();
                if (exchangeRate == 1) {
                    return new BigDecimal(parsed)
                            .setScale(fractionDigits, BigDecimal.ROUND_HALF_UP)
                            .doubleValue();
                }

                return new BigDecimal(parsed * exchangeRate)
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
        return new Purchase(currentIdentity.getGroup(), currentIdentity.getId(), date, store,
                total, currency, exchangeRate, receipt, note, isDraft, null,
                purchaseIdentities, purchaseItems);
    }

    final double convertRoundTotal(int fractionDigits) {
        if (exchangeRate == 1) {
            return new BigDecimal(totalValue).setScale(fractionDigits,
                    BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        final double totalConverted = totalValue * exchangeRate;
        return new BigDecimal(totalConverted).setScale(MoneyUtils.CONVERTED_PRICE_FRACTION_DIGITS,
                BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    @Override
    public void onSaveAsDraftMenuClick() {
        if (fetchingExchangeRates) {
            view.showMessage(R.string.toast_exchange_rate_fetching);
            return;
        }

        startPurchaseSave(true);
    }

    @Override
    public void onExitClick() {
        view.showPurchaseDiscardDialog();
    }

    @Override
    public void onDiscardChangesSelected() {
        navigator.finish(PurchaseResult.PURCHASE_DISCARDED);
    }
}