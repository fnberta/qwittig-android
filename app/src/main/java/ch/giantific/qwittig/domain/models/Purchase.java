/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.parse.ParseUtils;


/**
 * Represents a purchase a user makes in a store.
 * <p/>
 * Subclass of {@link ParseObject}.
 */
@ParseClassName("Purchase")
public class Purchase extends ParseObject {

    public static final String CLASS = "Purchase";
    public static final String BUYER = "buyer";
    public static final String GROUP = "group";
    public static final String DATE = "date";
    public static final String STORE = "store";
    public static final String ITEMS = "items";
    public static final String TOTAL_PRICE = "totalPrice";
    public static final String IDENTITIES = "identities";
    public static final String CURRENCY = "currency";
    public static final String EXCHANGE_RATE = "exchangeRate";
    public static final String READ_BY = "readBy";
    public static final String RECEIPT = "receipt";
    public static final String RECEIPT_BYTES = "receiptBytes";
    public static final String NOTE = "note";
    public static final String TEMP_ID = "tempId";
    public static final String DRAFT = "draft";
    public static final String PIN_LABEL = "purchasesPinLabel";
    public static final String PIN_LABEL_TEMP = "purchasesTempPinLabel";

    public Purchase() {
        // A default constructor is required.
    }

    public Purchase(@NonNull Identity currentIdentity, @NonNull Group group, @NonNull Date date,
                    @NonNull String store, @NonNull List<Item> items, @NonNull BigDecimal totalPrice,
                    @NonNull List<Identity> identities, @NonNull String currency,
                    double exchangeRate) {
        setBuyer(currentIdentity);
        setGroup(group);
        setDate(date);
        setStore(store);
        setItems(items);
        setTotalPrice(totalPrice);
        setIdentities(identities);
        setCurrency(currency);
        setExchangeRate(exchangeRate);
        addUserToReadBy(currentIdentity);
        setAccessRights(group);
    }

    private void setAccessRights(@NonNull Group group) {
        final ParseACL acl = ParseUtils.getDefaultAcl(group, true);
        setACL(acl);
    }

    public Group getGroup() {
        return (Group) getParseObject(GROUP);
    }

    public void setGroup(@NonNull Group group) {
        put(GROUP, group);
    }

    public Identity getBuyer() {
        return (Identity) getParseObject(BUYER);
    }

    public void setBuyer(@NonNull Identity buyer) {
        put(BUYER, buyer);
    }

    public Date getDate() {
        return getDate(DATE);
    }

    public void setDate(@NonNull Date date) {
        put(DATE, date);
    }

    public String getStore() {
        return getString(STORE);
    }

    public void setStore(@NonNull String store) {
        put(STORE, store);
    }

    public List<Item> getItems() {
        return getList(ITEMS);
    }

    public void setItems(@NonNull List<Item> items) {
        put(ITEMS, items);
    }

    public List<Identity> getIdentities() {
        return getList(IDENTITIES);
    }

    public void setIdentities(@NonNull List<Identity> identities) {
        put(IDENTITIES, identities);
    }

    public double getTotalPrice() {
        return getDouble(TOTAL_PRICE);
    }

    public void setTotalPrice(@NonNull Number totalPrice) {
        put(TOTAL_PRICE, totalPrice);
    }

    public String getCurrency() {
        return getString(CURRENCY);
    }

    public void setCurrency(@NonNull String currency) {
        put(CURRENCY, currency);
    }

    public double getExchangeRate() {
        return getDouble(EXCHANGE_RATE);
    }

    public void setExchangeRate(double exchangeRate) {
        put(EXCHANGE_RATE, exchangeRate);
    }

    public String getReceiptUrl() {
        final ParseFile receipt = getReceipt();
        return receipt != null ? receipt.getUrl() : "";
    }

    public ParseFile getReceipt() {
        return getParseFile(RECEIPT);
    }

    public void setReceipt(@NonNull ParseFile file) {
        put(RECEIPT, file);
    }

    public byte[] getReceiptData() {
        return getBytes(RECEIPT_BYTES);
    }

    public void setReceiptData(@NonNull byte[] bytes) {
        put(RECEIPT_BYTES, bytes);
    }

    public String getNote() {
        return getString(NOTE);
    }

    public void setNote(@NonNull String note) {
        put(NOTE, note);
    }

    public String getTempId() {
        return getString(TEMP_ID);
    }

    public void setTempId(@NonNull String tempId) {
        put(TEMP_ID, tempId);
    }

    public boolean isDraft() {
        return getBoolean(DRAFT);
    }

    public void setDraft(boolean draft) {
        put(DRAFT, draft);
    }

    public void removeDraft() {
        remove(DRAFT);
    }

    @NonNull
    public List<Identity> getReadBy() {
        final List<Identity> readBy = getList(READ_BY);
        if (readBy == null) {
            return Collections.emptyList();
        }

        return readBy;
    }

    public void setReadBy(@NonNull List<Identity> identities) {
        put(READ_BY, identities);
    }

    public void replaceItems(@NonNull List<Item> items) {
        // TODO: delete old items
        put(ITEMS, items);
    }

    /**
     * Returns whether the identity has already read the purchase or not.
     *
     * @param identity the identity to check the read status for
     * @return whether the identity has read the purchase or not
     */
    public boolean isRead(@NonNull Identity identity) {
        final List<Identity> readBy = getReadBy();
        final String identityId = identity.getObjectId();
        for (Identity readByIdentity : readBy) {
            if (readByIdentity.getObjectId().equals(identityId)) {
                return true;
            }
        }

        return false;
    }

    public void addUserToReadBy(@NonNull Identity identity) {
        addUnique(READ_BY, identity);
    }

    /**
     * Resets the read by field to only the identity.
     */
    public void resetReadBy(@NonNull Identity identity) {
        final List<Identity> readBy = new ArrayList<>();
        readBy.add(identity);
        put(READ_BY, readBy);
    }

    public void removeReceipt() {
        remove(RECEIPT);
    }

    public void removeReceiptData() {
        remove(RECEIPT_BYTES);
    }

    public void removeNote() {
        remove(NOTE);
    }

    /**
     * Returns and sets a random id for purchase, identifying it as draft or a not yet online
     * saved purchase.
     * <p/>
     * This is needed because {@link ParseObject} and its subclasses only receive an object id when
     * they are saved online in the database. Because we need to be able to reference drafts, which
     * are only stored in the local datastore, we assign random draft id.
     *
     * @return a random id
     */
    public String setRandomTempId() {
        String tempId = getTempId();

        if (TextUtils.isEmpty(tempId)) {
            tempId = UUID.randomUUID().toString();
            put(TEMP_ID, tempId);
        }

        return tempId;
    }

    public void removeTempId() {
        remove(TEMP_ID);
    }

    /**
     * Converts the purchase's total price either to the group's currency or the foreign currency
     * using the exchange rate of the purchase.
     *
     * @param toGroupCurrency whether to convert to the group's currency or to a foreign one
     */
    public void convertTotalPrice(boolean toGroupCurrency) {
        final double totalPrice = getTotalPrice();
        final double exchangeRate = getExchangeRate();
        final double totalPriceConverted = toGroupCurrency ? totalPrice * exchangeRate : totalPrice / exchangeRate;
        setTotalPrice(MoneyUtils.roundConvertedPrice(totalPriceConverted));
    }

    /**
     * Returns the total price converted to foreign currency using the provided exchange rate.
     *
     * @return the total price in foreign currency
     */
    public double getTotalPriceForeign() {
        final double totalPrice = getTotalPrice();
        final double exchangeRate = getExchangeRate();
        if (exchangeRate == 1) {
            return totalPrice;
        }

        return totalPrice / getExchangeRate();
    }

    /**
     * Returns a identity's share from the purchase.
     *
     * @param identity the identity to calculate the share for
     * @return the share of the identity
     */
    public double calculateUserShare(@NonNull Identity identity) {
        double userShare = 0;
        final double exchangeRate = getExchangeRate();
        List<Item> items = getItems();
        for (Item item : items) {
            List<Identity> identities = item.getIdentities();
            if (identities.contains(identity)) {
                userShare += (item.getPrice() * exchangeRate / identities.size());
            }
        }

        return userShare;
    }
}

