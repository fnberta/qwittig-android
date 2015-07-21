package ch.giantific.qwittig.data.parse.models;

import android.text.TextUtils;
import android.util.Log;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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

    public Purchase() {
        // A default constructor is required.
    }

    public Purchase(ParseObject wg, Date date, String store, List<ParseObject> items,
                    double totalPrice, List<ParseUser> userInvolved, String currency,
                    double exchangeRate, ParseFile receipt) {
        this(wg, date, store, items, totalPrice, userInvolved, currency, exchangeRate);
        put(RECEIPT, receipt);
    }

    public Purchase(ParseObject wg, Date date, String store, List<ParseObject> items,
                    double totalPrice, List<ParseUser> userInvolved, String currency,
                    ParseFile receipt) {
        this(wg, date, store, items, totalPrice, userInvolved, currency, 1);
        put(RECEIPT, receipt);
    }

    public Purchase(ParseObject wg, Date date, String store, List<ParseObject> items,
                    double totalPrice, List<ParseUser> userInvolved, String currency) {
        this(wg, date, store, items, totalPrice, userInvolved, currency, 1);
    }

    public Purchase(ParseObject wg, Date date, String store, List<ParseObject> items,
                    double totalPrice, List<ParseUser> userInvolved, String currency,
                    double exchangeRate) {
        put(BUYER, ParseUser.getCurrentUser());
        put(GROUP, wg);
        put(DATE, date);
        put(STORE, store);
        put(ITEMS, items);
        put(TOTAL_PRICE, totalPrice);
        put(USERS_INVOLVED, userInvolved);
        put(CURRENCY, currency);
        put(EXCHANGE_RATE, exchangeRate);
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

    public ParseObject getGroup() {
        return getParseObject(GROUP);
    }

    public void setGroup(Group group) {
        put(GROUP, group);
    }

    public ParseUser getBuyer() {
        return getParseUser(BUYER);
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

    public List<ParseUser> getUsersInvolved() {
        return getList(USERS_INVOLVED);
    }

    public void addUsersInvolved(List<ParseUser> usersInvolved) {
        addAll(USERS_INVOLVED, usersInvolved);
    }

    public void removeUsersInvolved(List<ParseUser> usersInvolved) {
        removeAll(USERS_INVOLVED, usersInvolved);
    }

    public void replaceUsersInvolved(List<ParseUser> usersInvolved) {
        put(USERS_INVOLVED, usersInvolved);
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

    public List<ParseUser> getReadBy() {
        List<ParseUser> readBy = getList(READ_BY);
        if (readBy == null) {
            return Collections.emptyList();
        }

        return readBy;
    }

    public void addCurrentUserToReadBy() {
        addReadBy(ParseUser.getCurrentUser());
    }

    public void addReadBy(ParseUser user) {
        addUnique(READ_BY, user);
    }

    public void setReadBy(List<ParseUser> users) {
        put(READ_BY, users);
    }

    public void resetReadBy() {
        List<ParseUser> readBy = new ArrayList<>();
        readBy.add(ParseUser.getCurrentUser());
        put(READ_BY, readBy);
    }

    public ParseFile getReceiptParseFile() {
        return getParseFile(RECEIPT);
    }

    public void setReceiptParseFile(ParseFile file) {
        put(RECEIPT, file);
    }

    public void removeReceiptParseFile() {
        remove(RECEIPT);
    }

    public void swapReceiptParseFileToData() {
        ParseFile receiptFile = getReceiptParseFile();
        if (receiptFile != null) {
            try {
                byte[] receiptData = receiptFile.getData();
                put(RECEIPT_BYTE, receiptData);
                removeReceiptParseFile();
            } catch (ParseException e) {
                Log.e("qwittig", "swap " + e.toString());
                e.printStackTrace();
            }
        }
    }

    public byte[] getReceiptData() {
        return getBytes(RECEIPT_BYTE);
    }

    public void removeReceiptData() {
        remove(RECEIPT_BYTE);
    }

    public String getDraftId() {
        return getString(DRAFT_ID);
    }

    public void setDraftId(String draftId) {
        put(DRAFT_ID, draftId);
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

    public void setTotalPrice(double totalPrice) {
        put(TOTAL_PRICE, totalPrice);
    }

    public double getTotalPrice() {
        return getDouble(TOTAL_PRICE);
    }

    public double getTotalPriceAdjusted() {
        double totalPrice = getTotalPrice();
        double exchangeRate = getExchangeRate();

        return totalPrice * exchangeRate;
    }

    public void setCurrency(String currency) {
        put(CURRENCY, currency);
    }

    public String getCurrency() {
        return getString(CURRENCY);
    }

    public void setExchangeRate(double exchangeRate) {
        put(EXCHANGE_RATE, exchangeRate);
    }

    public double getExchangeRate() {
        return getDouble(EXCHANGE_RATE);
    }
}

