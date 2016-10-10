/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.domain.models.Article;
import ch.giantific.qwittig.presentation.common.viewmodels.PurchaseReceiptViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.BasePurchaseAddEditItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.BasePurchaseAddEditItemViewModel.ViewType;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditArticleIdentityItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditArticleItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditDateItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditStoreItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditTotalItemViewModel;
import ch.giantific.qwittig.utils.MoneyUtils;

public class PurchaseAddEditViewModel extends BaseObservable
        implements Parcelable,
        PurchaseReceiptViewModel,
        PurchaseAddEditDateItemViewModel,
        PurchaseAddEditStoreItemViewModel,
        PurchaseAddEditTotalItemViewModel {

    public static final String TAG = PurchaseAddEditViewModel.class.getCanonicalName();
    public static final Creator<PurchaseAddEditViewModel> CREATOR = new Creator<PurchaseAddEditViewModel>() {
        @Override
        public PurchaseAddEditViewModel createFromParcel(Parcel in) {
            return new PurchaseAddEditViewModel(in);
        }

        @Override
        public PurchaseAddEditViewModel[] newArray(int size) {
            return new PurchaseAddEditViewModel[size];
        }
    };
    private final ArrayList<BasePurchaseAddEditItemViewModel> items;
    private List<String> supportedCurrencies;
    private boolean loading;
    private String receipt;
    private String currency;
    private String note;
    private Date date;
    private String dateFormatted;
    private String store;
    private double exchangeRate;
    private String exchangeRateFormatted;
    private double total;
    private String totalFormatted;
    private String myShare;
    private boolean fetchingExchangeRates;
    private boolean dataSet;

    public PurchaseAddEditViewModel() {
        items = new ArrayList<>();
        this.exchangeRate = 1;
    }

    private PurchaseAddEditViewModel(Parcel in) {
        items = new ArrayList<>();
        in.readList(items, BasePurchaseAddEditItemViewModel.class.getClassLoader());
        supportedCurrencies = in.createStringArrayList();
        loading = in.readByte() != 0;
        receipt = in.readString();
        currency = in.readString();
        note = in.readString();
        dateFormatted = in.readString();
        store = in.readString();
        exchangeRate = in.readDouble();
        exchangeRateFormatted = in.readString();
        total = in.readDouble();
        totalFormatted = in.readString();
        myShare = in.readString();
        fetchingExchangeRates = in.readByte() != 0;
        dataSet = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(items);
        dest.writeStringList(supportedCurrencies);
        dest.writeByte((byte) (loading ? 1 : 0));
        dest.writeString(receipt);
        dest.writeString(currency);
        dest.writeString(note);
        dest.writeString(dateFormatted);
        dest.writeString(store);
        dest.writeDouble(exchangeRate);
        dest.writeString(exchangeRateFormatted);
        dest.writeDouble(total);
        dest.writeString(totalFormatted);
        dest.writeString(myShare);
        dest.writeByte((byte) (fetchingExchangeRates ? 1 : 0));
        dest.writeByte((byte) (dataSet ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    @Bindable
    public boolean isLoading() {
        return loading;
    }

    @Override
    public void setLoading(boolean loading) {
        this.loading = loading;
        notifyPropertyChanged(BR.loading);
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

    @Bindable
    public String getNote() {
        return note;
    }

    public void setNote(@NonNull String note) {
        this.note = note;
        notifyPropertyChanged(BR.note);
    }

    public boolean isNoteAvailable() {
        return !TextUtils.isEmpty(note);
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    @Bindable
    public String getDateFormatted() {
        return dateFormatted;
    }

    @Override
    public void setDate(@NonNull Date date, @NonNull String dateFormatted) {
        this.date = date;
        this.dateFormatted = dateFormatted;
        notifyPropertyChanged(BR.dateFormatted);
    }

    @Bindable
    public String getStore() {
        return store;
    }

    @Override
    public void setStore(@NonNull String store) {
        this.store = store;
        notifyPropertyChanged(BR.store);
    }

    @Override
    public double getTotal() {
        return total;
    }

    @Bindable
    public String getTotalFormatted() {
        return totalFormatted;
    }

    @Override
    public void setTotal(double total, @NonNull String totalFormatted) {
        this.total = total;
        this.totalFormatted = totalFormatted;
        notifyPropertyChanged(BR.totalFormatted);
    }

    @Override
    @Bindable
    public String getMyShare() {
        return myShare;
    }

    @Override
    public void setMyShare(@NonNull String myShareFormatted) {
        this.myShare = myShareFormatted;
        notifyPropertyChanged(BR.myShare);
    }

    @Override
    @Bindable
    public String getCurrency() {
        return currency;
    }

    @Override
    public void setCurrency(@NonNull String currency, boolean notify) {
        this.currency = currency;
        notifyPropertyChanged(BR.currency);
        if (notify) {
            notifyPropertyChanged(BR.currencySelected);
        }
    }

    public List<String> getSupportedCurrencies() {
        return supportedCurrencies;
    }

    public void setSupportedCurrencies(@NonNull List<String> supportedCurrencies) {
        this.supportedCurrencies = supportedCurrencies;
    }

    @Override
    @Bindable
    public int getCurrencySelected() {
        return supportedCurrencies.indexOf(currency);
    }

    @Override
    @Bindable
    public double getExchangeRate() {
        return exchangeRate;
    }

    @Override
    @Bindable
    public String getExchangeRateFormatted() {
        return exchangeRateFormatted;
    }

    @Override
    public void setExchangeRate(double exchangeRate, @NonNull String exchangeRateFormatted) {
        this.exchangeRate = exchangeRate;
        this.exchangeRateFormatted = exchangeRateFormatted;
        notifyPropertyChanged(BR.exchangeRateFormatted);
        notifyPropertyChanged(BR.exchangeRateVisible);
    }

    @Override
    @Bindable
    public boolean isExchangeRateVisible() {
        return exchangeRate != 1;
    }

    public boolean isFetchingExchangeRates() {
        return fetchingExchangeRates;
    }

    public void setFetchingExchangeRates(boolean fetchingExchangeRates) {
        this.fetchingExchangeRates = fetchingExchangeRates;
    }

    public boolean isDataSet() {
        return dataSet;
    }

    public void setDataSet(boolean dataSet) {
        this.dataSet = dataSet;
    }

    public Iterator<BasePurchaseAddEditItemViewModel> getItemsIterator() {
        return items.iterator();
    }

    public int getItemCount() {
        return items.size();
    }

    public BasePurchaseAddEditItemViewModel getItemAtPosition(int position) {
        return items.get(position);
    }

    public int getPositionForItem(@NonNull BasePurchaseAddEditItemViewModel item) {
        return items.indexOf(item);
    }

    public void addItem(@NonNull BasePurchaseAddEditItemViewModel item) {
        items.add(item);
    }

    public void addItemAtPosition(int position, @NonNull BasePurchaseAddEditItemViewModel item) {
        items.add(position, item);
    }

    public void removeItemAtPosition(int position) {
        items.remove(position);
    }

    public void updateTotalAndMyShare(@NonNull String currentIdentityId,
                                      @NonNull NumberFormat moneyFormatter) {
        double total = 0;
        double myShare = 0;
        for (BasePurchaseAddEditItemViewModel itemViewModel : items) {
            if (itemViewModel.getViewType() != ViewType.ARTICLE) {
                continue;
            }

            final PurchaseAddEditArticleItemViewModel articleItem =
                    (PurchaseAddEditArticleItemViewModel) itemViewModel;
            final double itemPrice = articleItem.getPriceParsed();

            // update total price
            total += itemPrice;

            // update my share
            final PurchaseAddEditArticleIdentityItemViewModel[] articleIdentities =
                    articleItem.getIdentities();
            int selectedCount = 0;
            boolean currentIdentityInvolved = false;
            for (PurchaseAddEditArticleIdentityItemViewModel articleIdentity : articleIdentities) {
                if (!articleIdentity.isSelected()) {
                    continue;
                }

                selectedCount++;
                if (Objects.equals(articleIdentity.getIdentityId(), currentIdentityId)) {
                    currentIdentityInvolved = true;
                }
            }
            if (currentIdentityInvolved) {
                myShare += (itemPrice / selectedCount);
            }
        }

        setTotal(total, moneyFormatter.format(total));
        setMyShare(moneyFormatter.format(myShare));
    }

    public int getOpenIdentityRowPosition() {
        for (int i = 0, size = getItemCount(); i < size; i++) {
            final BasePurchaseAddEditItemViewModel itemViewModel = getItemAtPosition(i);
            if (itemViewModel.getViewType() == ViewType.IDENTITIES) {
                return i;
            }
        }

        throw new RuntimeException("no identity row open!");
    }

    public boolean isItemsValid() {
        boolean allValid = true;
        for (BasePurchaseAddEditItemViewModel itemViewModel : items) {
            if (itemViewModel.getViewType() != ViewType.ARTICLE) {
                continue;
            }

            final PurchaseAddEditArticleItemViewModel articleItem =
                    (PurchaseAddEditArticleItemViewModel) itemViewModel;
            if (!articleItem.isInputValid()) {
                allValid = false;
            }
        }

        return allValid;
    }

    public boolean isItemsChanged(@NonNull List<Article> oldArticles, double exchangeRate) {
        for (int i = 0, size = getItemCount(), skipCount = 0; i < size; i++) {
            final BasePurchaseAddEditItemViewModel addEditItem = getItemAtPosition(i);
            if (addEditItem.getViewType() != ViewType.ARTICLE) {
                skipCount++;
                continue;
            }

            final Article articleOld;
            try {
                articleOld = oldArticles.get(i - skipCount);
            } catch (IndexOutOfBoundsException e) {
                return true;
            }
            final PurchaseAddEditArticleItemViewModel articleItem = (PurchaseAddEditArticleItemViewModel) addEditItem;
            if (!Objects.equals(articleOld.getName(), articleItem.name.get())) {
                return true;
            }

            final double oldPrice = articleOld.getPriceForeign(exchangeRate);
            final double newPrice = articleItem.getPriceParsed();
            if (Math.abs(oldPrice - newPrice) >= MoneyUtils.MIN_DIFF) {
                return true;
            }

            final Set<String> identitiesOld = articleOld.getIdentitiesIds();
            final List<String> identitiesNew = articleItem.getSelectedIdentitiesIds();
            if (!identitiesNew.containsAll(identitiesOld) ||
                    !identitiesOld.containsAll(identitiesNew)) {
                return true;
            }
        }

        return false;
    }

    public List<Article> getArticlesFromItems(@NonNull NumberFormat moneyFormatter) {
        final List<Article> articles = new ArrayList<>();
        final int fractionDigits = MoneyUtils.getFractionDigits(currency);

        for (BasePurchaseAddEditItemViewModel itemViewModel : items) {
            if (itemViewModel.getViewType() != ViewType.ARTICLE) {
                continue;
            }

            final PurchaseAddEditArticleItemViewModel articleItem =
                    (PurchaseAddEditArticleItemViewModel) itemViewModel;
            final String itemName = articleItem.name.get();
            final String name = itemName != null ? itemName : "";
            final String price = articleItem.getPrice();

            final List<String> identities = new ArrayList<>();
            for (PurchaseAddEditArticleIdentityItemViewModel row : articleItem.getIdentities()) {
                if (row.isSelected()) {
                    final String identityId = row.getIdentityId();
                    identities.add(identityId);
                }
            }

            final double priceRounded = convertRoundItemPrice(price, moneyFormatter, fractionDigits);
            final Article article = new Article(name, priceRounded, identities);
            articles.add(article);
        }

        return articles;
    }

    private double convertRoundItemPrice(@NonNull String priceText,
                                         @NonNull NumberFormat moneyFormatter,
                                         int fractionDigits) {
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
}