package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by fabio on 02.07.16.
 */
@IgnoreExtraProperties
public class Purchase implements FirebaseModel {

    public static final String BASE_PATH_PURCHASES = "purchases";
    public static final String BASE_PATH_DRAFTS = "drafts";

    public static final String PATH_GROUP = "group";
    public static final String PATH_BUYER = "buyer";
    public static final String PATH_DATE = "date";
    public static final String PATH_STORE = "store";
    public static final String PATH_TOTAL = "total";
    public static final String PATH_CURRENCY = "currency";
    public static final String PATH_EXCHANGE_RATE = "exchangeRate";
    public static final String PATH_RECEIPT = "receipt";
    public static final String PATH_NOTE = "note";
    public static final String PATH_DRAFT = "draft";
    public static final String PATH_OCR_DATA = "ocrData";
    public static final String PATH_IDENTITIES = "identities";
    public static final String PATH_ITEMS = "items";
    public static final String PATH_READ_BY = "readBy";

    private String id;
    private long createdAt;
    private String group;
    private String buyer;
    private long date;
    private String store;
    private double total;
    private String currency;
    private double exchangeRate;
    private String receipt;
    private String note;
    private boolean draft;
    private String ocrData;
    private Map<String, Boolean> identities;
    private List<Item> items;
    private Map<String, Boolean> readBy;

    public Purchase() {
        // required for firebase de-/serialization
    }

    public Purchase(@NonNull String group, @NonNull String buyer, @NonNull Date date,
                    @Nullable String store, double total, @NonNull String currency,
                    double exchangeRate, @Nullable String receipt, @Nullable String note,
                    boolean draft, @Nullable String ocrData,
                    @NonNull List<String> identities, @NonNull List<Item> items) {
        this.group = group;
        this.buyer = buyer;
        this.date = date.getTime();
        this.store = store;
        this.total = total;
        this.currency = currency;
        this.exchangeRate = exchangeRate;
        this.receipt = receipt;
        this.note = note;
        this.draft = draft;
        this.ocrData = ocrData;
        this.identities = new HashMap<>();
        for (String id : identities) {
            this.identities.put(id, true);
        }
        this.items = items;
        readBy = new HashMap<>();
        readBy.put(buyer, true);
    }

    @Override
    @Exclude
    public String getId() {
        return id;
    }

    @Override
    public void setId(@NonNull String id) {
        this.id = id;
    }

    @Override
    public Map<String, String> getCreatedAt() {
        return ServerValue.TIMESTAMP;
    }

    @Exclude
    public Date getCreatedAtDate() {
        return new Date(createdAt);
    }

    public String getGroup() {
        return group;
    }

    public String getBuyer() {
        return buyer;
    }

    public long getDate() {
        return date;
    }

    @Exclude
    public Date getDateDate() {
        return new Date(date);
    }

    public String getStore() {
        return store;
    }

    public double getTotal() {
        return total;
    }

    public String getCurrency() {
        return currency;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public String getReceipt() {
        return receipt;
    }

    public String getNote() {
        return note;
    }

    public boolean isDraft() {
        return draft;
    }

    public String getOcrData() {
        return ocrData;
    }

    public Map<String, Boolean> getIdentities() {
        return identities;
    }

    @Exclude
    public Set<String> getIdentitiesIds() {
        return identities.keySet();
    }

    public List<Item> getItems() {
        return items;
    }

    public Map<String, Boolean> getReadBy() {
        return readBy;
    }

    @Exclude
    public Set<String> getReadByIds() {
        return readBy.keySet();
    }

    /**
     * Returns whether the identity has already read the purchase or not.
     *
     * @param identityId the identity id to check the read status for
     * @return whether the identity has read the purchase or not
     */
    @Exclude
    public boolean isRead(@NonNull String identityId) {
        return getReadByIds().contains(identityId);
    }

    /**
     * Returns the total price converted to foreign currency using the provided exchange rate.
     *
     * @return the total price in foreign currency
     */
    @Exclude
    public double getTotalForeign() {
        if (exchangeRate == 1) {
            return total;
        }

        return total / exchangeRate;
    }

    /**
     * Returns a identity's share from the purchase.
     *
     * @param identityId the identity id to calculate the share for
     * @return the share of the identity
     */
    @Exclude
    public double calculateUserShare(@NonNull String identityId) {
        double userShare = 0;
        for (Item item : items) {
            final Set<String> identitiesIds = item.getIdentitiesIds();
            if (identitiesIds.contains(identityId)) {
                userShare += (item.getPrice() * exchangeRate / identitiesIds.size());
            }
        }

        return userShare;
    }

    @Exclude
    public Map<String, Object> toMap() {
        final Map<String, Object> result = new HashMap<>();
        result.put(PATH_CREATED_AT, ServerValue.TIMESTAMP);
        result.put(PATH_GROUP, group);
        result.put(PATH_BUYER, buyer);
        result.put(PATH_DATE, date);
        result.put(PATH_STORE, store);
        result.put(PATH_TOTAL, total);
        result.put(PATH_CURRENCY, currency);
        result.put(PATH_EXCHANGE_RATE, exchangeRate);
        result.put(PATH_RECEIPT, receipt);
        result.put(PATH_NOTE, note);
        result.put(PATH_DRAFT, draft);
        result.put(PATH_OCR_DATA, ocrData);
        result.put(PATH_IDENTITIES, identities);
        final List<Map<String, Object>> items = new ArrayList<>();
        for (Item item : this.items) {
            items.add(item.toMap());
        }
        result.put(PATH_ITEMS, items);
        result.put(PATH_READ_BY, readBy);

        return result;
    }
}

