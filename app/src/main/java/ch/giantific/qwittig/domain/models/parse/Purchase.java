/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.models.parse;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

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
    public static final String USERS_INVOLVED = "usersInvolved";
    public static final String CURRENCY = "currency";
    public static final String EXCHANGE_RATE = "exchangeRate";
    public static final String READ_BY = "readBy";
    public static final String RECEIPT = "receipt";
    public static final String RECEIPT_BYTE = "receiptByte";
    public static final String NOTE = "note";
    public static final String DRAFT_ID = "draftId";
    public static final String PIN_LABEL = "purchasesPinLabel";

    public Purchase() {
        // A default constructor is required.
    }

    public Purchase(@NonNull ParseUser currentUser, @NonNull Group group, @NonNull Date date,
                    @NonNull String store, @NonNull List<Item> items, double totalPrice,
                    @NonNull List<User> usersInvolved, @NonNull String currency) {
        this(currentUser, group, date, store, items, totalPrice, usersInvolved, currency, 1);
    }

    public Purchase(@NonNull ParseUser currentUser, @NonNull Group group, @NonNull Date date,
                    @NonNull String store, @NonNull List<Item> items, double totalPrice,
                    @NonNull List<User> usersInvolved, @NonNull String currency,
                    float exchangeRate) {
        setBuyer(currentUser);
        setGroup(group);
        setDate(date);
        setStore(store);
        setItems(items);
        setTotalPrice(totalPrice);
        setUsersInvolved(usersInvolved);
        setCurrency(currency);
        setExchangeRate(exchangeRate);
        addUserToReadBy(currentUser);
        setAccessRights(group);
    }

    private void setAccessRights(ParseObject group) {
        ParseACL acl = ParseUtils.getDefaultAcl(group);
        setACL(acl);
    }

    public Group getGroup() {
        return (Group) getParseObject(GROUP);
    }

    public void setGroup(@NonNull ParseObject group) {
        put(GROUP, group);
    }

    public User getBuyer() {
        return (User) getParseUser(BUYER);
    }

    public void setBuyer(@NonNull ParseUser buyer) {
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

    public List<ParseObject> getItems() {
        return getList(ITEMS);
    }

    public void setItems(@NonNull List<Item> items) {
        put(ITEMS, items);
    }

    public List<ParseUser> getUsersInvolved() {
        return getList(USERS_INVOLVED);
    }

    public void setUsersInvolved(@NonNull List<User> usersInvolved) {
        put(USERS_INVOLVED, usersInvolved);
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

    public float getExchangeRate() {
        return (float) getDouble(EXCHANGE_RATE);
    }

    public void setExchangeRate(float exchangeRate) {
        put(EXCHANGE_RATE, exchangeRate);
    }

    public ParseFile getReceiptParseFile() {
        return getParseFile(RECEIPT);
    }

    public void setReceiptParseFile(@NonNull ParseFile file) {
        put(RECEIPT, file);
    }

    public byte[] getReceiptData() {
        return getBytes(RECEIPT_BYTE);
    }

    public void setReceiptData(@NonNull byte[] bytes) {
        put(RECEIPT_BYTE, bytes);
    }

    public String getNote() {
        return getString(NOTE);
    }

    public void setNote(@NonNull String note) {
        put(NOTE, note);
    }

    public String getDraftId() {
        return getString(DRAFT_ID);
    }

    public void setDraftId(@NonNull String draftId) {
        put(DRAFT_ID, draftId);
    }

    @NonNull
    public List<ParseUser> getReadBy() {
        List<ParseUser> readBy = getList(READ_BY);
        if (readBy == null) {
            return Collections.emptyList();
        }

        return readBy;
    }

    public void setReadBy(List<ParseUser> users) {
        put(READ_BY, users);
    }

    public void addItems(List<ParseObject> items) {
        addAll(ITEMS, items);
    }

    public void removeItems(List<ParseObject> items) {
        removeAll(ITEMS, items);
    }

    public void replaceItems(List<Item> items) {
        put(ITEMS, items);
    }

    public void addItem(ParseObject item) {
        add(ITEMS, item);
    }

    /**
     * Returns whether the user has already read the purchase or not.
     *
     * @param user the user to check the read status for
     * @return whether the user has read the purchase or not
     */
    public boolean userHasReadPurchase(@NonNull ParseUser user) {
        List<String> readByIds = getReadByIds();
        return readByIds.contains(user.getObjectId());
    }

    /**
     * Returns the object ids of the users that have already read the purchase.
     *
     * @return the object ids of the users that read the purchase
     */
    @NonNull
    public List<String> getReadByIds() {
        List<String> readByIds = new ArrayList<>();
        List<ParseUser> readBy = getReadBy();
        if (!readBy.isEmpty()) {
            for (ParseUser user : readBy) {
                readByIds.add(user.getObjectId());
            }
        }

        return readByIds;
    }

    public void addUserToReadBy(@NonNull ParseUser user) {
        addReadBy(user);
    }

    public void addReadBy(@NonNull ParseUser user) {
        addUnique(READ_BY, user);
    }

    /**
     * Resets the read by field to only the user.
     */
    public void resetReadBy(@NonNull ParseUser user) {
        List<ParseUser> readBy = new ArrayList<>();
        readBy.add(user);
        put(READ_BY, readBy);
    }

    public void removeReceiptParseFile() {
        remove(RECEIPT);
    }

    /**
     * Removes the receipt parse file and instead sets the provided byte array.
     *
     * @param bytes the byte array representation of the receipt
     */
    public void swapReceiptParseFileToData(@NonNull byte[] bytes) {
        put(RECEIPT_BYTE, bytes);
        removeReceiptParseFile();
    }

    public void removeReceiptData() {
        remove(RECEIPT_BYTE);
    }

    public void removeNote() {
        remove(NOTE);
    }

    /**
     * Returns and sets a random id for purchase, identifying it as draft.
     * <p/>
     * This is needed because {@link ParseObject} and its subclasses only receive an object id when
     * they are saved online in the database. Because we need to be able to reference drafts, which
     * are only stored in the local datastore, we assign random draft id.
     *
     * @return a random id
     */
    public String setRandomDraftId() {
        String draftId = getDraftId();

        if (TextUtils.isEmpty(draftId)) {
            draftId = UUID.randomUUID().toString();
            put(DRAFT_ID, draftId);
        }

        return draftId;
    }

    public void removeDraftId() {
        remove(DRAFT_ID);
    }

    /**
     * Converts the purchase's total price either to the group's currency or the foreign currency
     * using the exchange rate of the purchase.
     *
     * @param toGroupCurrency whether to convert to the group's currency or to a foreign one
     */
    public void convertTotalPrice(boolean toGroupCurrency) {
        double totalPrice = getTotalPrice();
        double exchangeRate = getExchangeRate();
        double totalPriceConverted = toGroupCurrency ? totalPrice * exchangeRate : totalPrice / exchangeRate;
        setTotalPrice(MoneyUtils.roundToFractionDigits(4, totalPriceConverted));
    }

    /**
     * Returns the total price converted to foreign currency using the provided exchange rate.
     *
     * @return the total price in foreign currency
     */
    public double getTotalPriceForeign() {
        double totalPrice = getTotalPrice();
        double exchangeRate = getExchangeRate();
        if (exchangeRate == 1) {
            return totalPrice;
        }

        return totalPrice / getExchangeRate();
    }

    /**
     * Returns a user's share from the purchase.
     *
     * @param user the user to calculate the share for
     * @return the share of the user
     */
    public double calculateUserShare(@NonNull ParseUser user) {
        double userShare = 0;
        double exchangeRate = getExchangeRate();
        List<ParseObject> items = getItems();
        for (ParseObject parseObject : items) {
            Item item = (Item) parseObject;
            List<ParseUser> usersInvolved = item.getUsersInvolved();
            if (usersInvolved.contains(user)) {
                userShare += (item.getPrice() * exchangeRate / usersInvolved.size());
            }
        }

        return userShare;
    }
}

