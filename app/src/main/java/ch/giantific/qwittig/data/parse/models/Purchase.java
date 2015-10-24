package ch.giantific.qwittig.data.parse.models;

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
import ch.giantific.qwittig.utils.ParseUtils;


/**
 * Created by fabio on 12.10.14.
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
    public static final String DRAFT_ID = "draftId";
    public static final String PIN_LABEL = "purchasesPinLabel";

    public Group getGroup() {
        return (Group) getParseObject(GROUP);
    }

    public void setGroup(ParseObject group) {
        put(GROUP, group);
    }

    public User getBuyer() {
        return (User) getParseUser(BUYER);
    }

    public void setBuyer(ParseUser buyer) {
        put(BUYER, buyer);
    }

    public Date getDate() {
        return getDate(DATE);
    }

    public void setDate(Date date) {
        put(DATE, date);
    }

    public String getStore() {
        return getString(STORE);
    }

    public void setStore(String store) {
        put(STORE, store);
    }

    public List<ParseObject> getItems() {
        return getList(ITEMS);
    }

    public void setItems(List<ParseObject> items) {
        put(ITEMS, items);
    }

    public List<ParseUser> getUsersInvolved() {
        return getList(USERS_INVOLVED);
    }

    public void setUsersInvolved(List<ParseUser> usersInvolved) {
        put(USERS_INVOLVED, usersInvolved);
    }

    public double getTotalPrice() {
        return getDouble(TOTAL_PRICE);
    }

    public void setTotalPrice(Number totalPrice) {
        put(TOTAL_PRICE, totalPrice);
    }

    public String getCurrency() {
        return getString(CURRENCY);
    }

    public void setCurrency(String currency) {
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

    public void setReceiptParseFile(ParseFile file) {
        put(RECEIPT, file);
    }

    public byte[] getReceiptData() {
        return getBytes(RECEIPT_BYTE);
    }

    public void setReceiptData(byte[] bytes) {
        put(RECEIPT_BYTE, bytes);
    }

    public String getDraftId() {
        return getString(DRAFT_ID);
    }

    public void setDraftId(String draftId) {
        put(DRAFT_ID, draftId);
    }

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

    public Purchase() {
        // A default constructor is required.
    }

    public Purchase(ParseObject wg, Date date, String store, List<ParseObject> items,
                    double totalPrice, List<ParseUser> usersInvolved, String currency,
                    float exchangeRate, ParseFile receipt) {
        this(wg, date, store, items, totalPrice, usersInvolved, currency, exchangeRate);
        setReceiptParseFile(receipt);
    }

    public Purchase(ParseObject wg, Date date, String store, List<ParseObject> items,
                    double totalPrice, List<ParseUser> usersInvolved, String currency,
                    ParseFile receipt) {
        this(wg, date, store, items, totalPrice, usersInvolved, currency, 1);
        setReceiptParseFile(receipt);
    }

    public Purchase(ParseObject wg, Date date, String store, List<ParseObject> items,
                    double totalPrice, List<ParseUser> usersInvolved, String currency) {
        this(wg, date, store, items, totalPrice, usersInvolved, currency, 1);
    }

    public Purchase(ParseObject wg, Date date, String store, List<ParseObject> items,
                    double totalPrice, List<ParseUser> usersInvolved, String currency,
                    float exchangeRate) {
        setBuyer(ParseUser.getCurrentUser());
        setGroup(wg);
        setDate(date);
        setStore(store);
        setItems(items);
        setTotalPrice(totalPrice);
        setUsersInvolved(usersInvolved);
        setCurrency(currency);
        setExchangeRate(exchangeRate);
        addCurrentUserToReadBy();
        setAccessRights(getCurrentGroup());
    }

    private ParseObject getCurrentGroup() {
        User currentUser = (User) ParseUser.getCurrentUser();
        return currentUser.getCurrentGroup();
    }

    private void setAccessRights(ParseObject group) {
        ParseACL acl = ParseUtils.getDefaultAcl(group);
        setACL(acl);
    }

    public void addItems(List<ParseObject> items) {
        addAll(ITEMS, items);
    }

    public void removeItems(List<ParseObject> items) {
        removeAll(ITEMS, items);
    }

    public void replaceItems(List<ParseObject> items) {
        put(ITEMS, items);
    }

    public void addItem(ParseObject item) {
        add(ITEMS, item);
    }

    public List<String> getUsersInvolvedIds() {
        List<String> listIds = new ArrayList<>();
        List<ParseUser> list = getUsersInvolved();
        for (ParseUser user : list) {
            listIds.add(user.getObjectId());
        }
        return listIds;
    }


    public void addUsersInvolved(List<ParseUser> usersInvolved) {
        addAll(USERS_INVOLVED, usersInvolved);
    }

    public void removeUsersInvolved(List<ParseUser> usersInvolved) {
        removeAll(USERS_INVOLVED, usersInvolved);
    }

    public boolean currentUserHasReadPurchase() {
        List<String> readByIds = getReadByIds();
        User currentUser = (User) ParseUser.getCurrentUser();

        return currentUser != null && readByIds.contains(currentUser.getObjectId());
    }

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

    public void addCurrentUserToReadBy() {
        addReadBy(ParseUser.getCurrentUser());
    }

    public void addReadBy(ParseUser user) {
        addUnique(READ_BY, user);
    }

    public void resetReadBy() {
        List<ParseUser> readBy = new ArrayList<>();
        readBy.add(ParseUser.getCurrentUser());
        put(READ_BY, readBy);
    }

    public void removeReceiptParseFile() {
        remove(RECEIPT);
    }

    public void swapReceiptParseFileToData(byte[] bytes) {
        put(RECEIPT_BYTE, bytes);
        removeReceiptParseFile();
    }

    public void removeReceiptData() {
        remove(RECEIPT_BYTE);
    }

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

    public void convertTotalPrice(boolean toGroupCurrency) {
        double totalPrice = getTotalPrice();
        double exchangeRate = getExchangeRate();
        double totalPriceConverted = toGroupCurrency ? totalPrice * exchangeRate : totalPrice / exchangeRate;
        setTotalPrice(MoneyUtils.roundToFractionDigits(4, totalPriceConverted));
    }

    public double getTotalPriceForeign() {
        double totalPrice = getTotalPrice();
        double exchangeRate = getExchangeRate();
        if (exchangeRate == 1) {
            return totalPrice;
        }

        return totalPrice / getExchangeRate();
    }
}

