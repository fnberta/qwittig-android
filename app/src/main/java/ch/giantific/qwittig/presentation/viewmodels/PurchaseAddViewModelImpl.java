/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.content.SharedPreferences;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;

import com.android.databinding.library.baseAdapters.BR;
import com.parse.ParseUser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.domain.models.RowItem;
import ch.giantific.qwittig.domain.models.RowItemUser;
import ch.giantific.qwittig.domain.models.parse.Item;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.models.rates.CurrencyRates;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by fabio on 24.01.16.
 */
public class PurchaseAddViewModelImpl extends ViewModelBaseImpl<PurchaseAddViewModel.ViewListener>
        implements PurchaseAddViewModel {

    private static final String STATE_ROW_ITEMS = "STATE_ROW_ITEMS";
    private static final String STATE_DATE = "STATE_DATE";
    private static final String STATE_STORE = "STATE_STORE";
    private static final String STATE_CURRENCY = "STATE_CURRENCY";
    private static final String STATE_NOTE = "STATE_NOTE";
    private static final String STATE_RECEIPT_IMAGE_PATH = "STATE_RECEIPT_IMAGE_PATH";
    private SharedPreferences mSharedPreferences;
    private List<User> mUsersAvailable = new ArrayList<>();
    private List<Item> mItems = new ArrayList<>();
    private ArrayList<RowItem> mRowItems;
    private Date mDate;
    private String mStore;
    private double mTotalPrice;
    private String mCurrency;
    private float mExchangeRate;
    private String mReceiptImagePath;
    private String mNote;

    public PurchaseAddViewModelImpl(@Nullable Bundle savedState,
                                    @NonNull UserRepository userRepository,
                                    @NonNull SharedPreferences sharedPreferences) {
        super(savedState, userRepository);
        mSharedPreferences = sharedPreferences;

        if (savedState != null) {
            mRowItems = savedState.getParcelableArrayList(STATE_ROW_ITEMS);
            mDate = DateUtils.parseLongToDate(savedState.getLong(STATE_DATE));
            mStore = savedState.getString(STATE_STORE);
            mCurrency = savedState.getString(STATE_CURRENCY);
            mNote = savedState.getString(STATE_NOTE);
            mReceiptImagePath = savedState.getString(STATE_RECEIPT_IMAGE_PATH);
        } else {
            mRowItems = new ArrayList<>();
            mDate = new Date();
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelableArrayList(STATE_ROW_ITEMS, mRowItems);
        outState.putLong(STATE_DATE, DateUtils.parseDateToLong(mDate));
        outState.putString(STATE_STORE, mStore);
        outState.putString(STATE_CURRENCY, mCurrency);
        outState.putString(STATE_NOTE, mNote);
        outState.putString(STATE_RECEIPT_IMAGE_PATH, mReceiptImagePath);
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

    @Override
    public void onStoreChanged(CharSequence s, int start, int before, int count) {
        mStore = s.toString();
    }

    @Override
    public void onCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
        mCurrency = (String) parent.getItemAtPosition(position);
        // TODO: update rest with currency
    }

    @Override
    public void attachView(@NonNull PurchaseAddViewModel.ViewListener view) {
        super.attachView(view);

        loadUsersAvailable();
    }

    private void loadUsersAvailable() {
        mSubscriptions.add(mUserRepo.getUsersLocalAsync(mCurrentGroup)
                .subscribe(new Subscriber<User>() {
                    @Override
                    public void onCompleted() {
                        if (mRowItems.isEmpty()) {
                            mRowItems.add(new RowItem(getRowItemUser()));
                            mView.notifyItemInserted(ROWS_BEFORE_ITEMS);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        // TODO: handle error
                    }

                    @Override
                    public void onNext(User user) {
                        mUsersAvailable.add(user);
                    }
                })
        );
    }

    private RowItemUser[] getRowItemUser() {
        final int size = mUsersAvailable.size();
        final RowItemUser[] rowItemUser = new RowItemUser[size];
        for (int i = 0; i < size; i++) {
            final User user = mUsersAvailable.get(i);
            rowItemUser[i] = new RowItemUser(user.getObjectId(), user.getNickname(), user.getAvatar());
        }

        return rowItemUser;
    }

    @Override
    public RowItem getRowItemAtPosition(int position) {
        return mRowItems.get(getAdjustedPosition(position));
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


        final RowItem rowItem = getRowItemAtPosition(position);
        if (rowItem == null) {
            return TYPE_USERS;
        }

        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mRowItems.size() + ROWS_BEFORE_ITEMS + 1;
    }

    @Override
    public int getLastPosition() {
        return getItemCount() - 1;
    }

    @Override
    public int getAdjustedPosition(int position) {
        return position - ROWS_BEFORE_ITEMS;
    }

    @Override
    public void onItemDismissed(int position) {
        mRowItems.remove(getAdjustedPosition(position));
        mView.notifyItemRemoved(position);
    }

    @Override
    public void onFabSavePurchaseClick(View view) {
        if (!validateItems()) {
            return;
        }

        savePurchase();
    }

    @Override
    public void onSavePurchaseAsDraftClick() {
        savePurchase();
    }

    private boolean validateItems() {
        boolean allValid = true;
        for (RowItem rowItem : mRowItems) {
            if (!rowItem.validateFields()) {
                allValid = false;
            }
        }

        return allValid;
    }

    private void savePurchase() {
        for (RowItem rowItem : mRowItems) {
            final BigDecimal price = rowItem.parsePrice(mCurrency);
            mTotalPrice += price.doubleValue();

            final List<ParseUser> usersInvolved = getUsersInvolved(rowItem.getUsers());
            final Item item = new Item(rowItem.getName(), price, usersInvolved, mCurrentGroup);
            mItems.add(item);
        }

        final Purchase purchase = new Purchase(mCurrentUser, mCurrentGroup, mDate, mStore, mItems,
                mTotalPrice, mCurrency);
        mView.loadSavePurchaseWorker(purchase, mReceiptImagePath);
    }

    @NonNull
    private List<ParseUser> getUsersInvolved(@NonNull RowItemUser[] rowItemUsers) {
        final List<ParseUser> usersInvolved = new ArrayList<>();
        for (int i = 0, size = mUsersAvailable.size(); i < size; i++) {
            final User user = mUsersAvailable.get(i);
            if (rowItemUsers[i].getObjectId().equals(user.getObjectId())) {
                usersInvolved.add(user);
            }
        }
        return usersInvolved;
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
                        mView.removeWorker(workerTag);

                        final SharedPreferences.Editor editor = mSharedPreferences.edit();
                        for (Map.Entry<String, Float> exchangeRate : exchangeRates.entrySet()) {
                            final BigDecimal roundedExchangeRate = MoneyUtils.roundToFractionDigits(
                                    MoneyUtils.EXCHANGE_RATE_FRACTION_DIGITS, 1 / exchangeRate.getValue());
                            editor.putFloat(exchangeRate.getKey(), roundedExchangeRate.floatValue());
                        }
                        editor.apply();

                        mExchangeRate = exchangeRates.get(mCurrency);
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                    }
                })
        );
    }

    @Override
    public void onToggleUsersClick(int position) {
        final int insertPos = position + 1;
        final int insertPosAdj = getAdjustedPosition(insertPos);
        if (mRowItems.size() > insertPosAdj && mRowItems.get(insertPosAdj) == null) {
            mRowItems.remove(insertPosAdj);
            mView.notifyItemRemoved(insertPos);
        } else {
            mRowItems.add(insertPosAdj, null);
            mView.notifyItemInserted(insertPos);

            int pos = ROWS_BEFORE_ITEMS;
            for (Iterator<RowItem> iterator = mRowItems.iterator(); iterator.hasNext(); ) {
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
        mRowItems.add(new RowItem(getRowItemUser()));
        mView.notifyItemInserted(position);
        mView.scrollToPosition(getLastPosition());
    }
}
