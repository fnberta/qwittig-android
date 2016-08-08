package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import com.google.firebase.database.ServerValue;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by fabio on 02.07.16.
 */
@IgnoreExtraProperties
public class Purchase implements FirebaseModel {

    public static final String PATH_PURCHASES = "purchases";
    public static final String PATH_DRAFTS = "drafts";
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
    private String mId;
    @PropertyName(PATH_CREATED_AT)
    private long mCreatedAt;
    @PropertyName(PATH_GROUP)
    private String mGroup;
    @PropertyName(PATH_BUYER)
    private String mBuyer;
    @PropertyName(PATH_DATE)
    private long mDate;
    @PropertyName(PATH_STORE)
    private String mStore;
    @PropertyName(PATH_TOTAL)
    private double mTotal;
    @PropertyName(PATH_CURRENCY)
    private String mCurrency;
    @PropertyName(PATH_EXCHANGE_RATE)
    private double mExchangeRate;
    @PropertyName(PATH_RECEIPT)
    private String mReceipt;
    @PropertyName(PATH_NOTE)
    private String mNote;
    @PropertyName(PATH_DRAFT)
    private boolean mDraft;
    @PropertyName(PATH_OCR_DATA)
    private String mOcrData;
    @PropertyName(PATH_IDENTITIES)
    private Map<String, Boolean> mIdentities;
    @PropertyName(PATH_ITEMS)
    private List<Item> mItems;
    @PropertyName(PATH_READ_BY)
    private Map<String, Boolean> mReadBy;

    public Purchase() {
        // required for firebase de-/serialization
    }

    public Purchase(@NonNull String group, @NonNull String buyerId, @NonNull Date date,
                    @NonNull String store, double total, @NonNull String currency,
                    double exchangeRate, @Nullable String receipt, @Nullable String note,
                    boolean isDraft, @Nullable String ocrData,
                    @NonNull List<String> identities, @NonNull List<Item> items) {
        mGroup = group;
        mBuyer = buyerId;
        mDate = date.getTime();
        mStore = store;
        mTotal = total;
        mCurrency = currency;
        mExchangeRate = exchangeRate;
        mReceipt = receipt;
        mNote = note;
        mDraft = isDraft;
        mOcrData = ocrData;
        mIdentities = new HashMap<>();
        for (String id : identities) {
            mIdentities.put(id, true);
        }
        mItems = items;
        mReadBy = new HashMap<>();
        mReadBy.put(buyerId, true);
    }

    @Override
    @Exclude
    public String getId() {
        return mId;
    }

    @Override
    public void setId(@NonNull String id) {
        mId = id;
    }

    @Override
    public Map<String, String> getCreatedAt() {
        return ServerValue.TIMESTAMP;
    }

    @Exclude
    public Date getCreatedAtDate() {
        return new Date(mCreatedAt);
    }

    public String getGroup() {
        return mGroup;
    }

    public String getBuyer() {
        return mBuyer;
    }

    public long getDate() {
        return mDate;
    }

    @Exclude
    public Date getDateDate() {
        return new Date(mDate);
    }

    public String getStore() {
        return mStore;
    }

    public double getTotal() {
        return mTotal;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public double getExchangeRate() {
        return mExchangeRate;
    }

    public String getReceipt() {
        return mReceipt;
    }

    public String getNote() {
        return mNote;
    }

    public boolean isDraft() {
        return mDraft;
    }

    public String getOcrData() {
        return mOcrData;
    }

    public Map<String, Boolean> getIdentities() {
        return mIdentities;
    }

    @Exclude
    public Set<String> getIdentitiesIds() {
        return mIdentities.keySet();
    }

    public List<Item> getItems() {
        return mItems;
    }

    public Map<String, Boolean> getReadBy() {
        return mReadBy;
    }

    @Exclude
    public Set<String> getReadByIds() {
        return mReadBy.keySet();
    }

    /**
     * Returns whether the identity has already read the purchase or not.
     *
     * @param identityId the identity id to check the read status for
     * @return whether the identity has read the purchase or not
     */
    @Exclude
    public boolean isRead(@NonNull String identityId) {
        for (String readById : getReadByIds()) {
            if (Objects.equals(readById, identityId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the total price converted to foreign currency using the provided exchange rate.
     *
     * @return the total price in foreign currency
     */
    @Exclude
    public double getTotalForeign() {
        if (mExchangeRate == 1) {
            return mTotal;
        }

        return mTotal / mExchangeRate;
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
        for (Item item : mItems) {
            final Set<String> identitiesIds = item.getIdentitiesIds();
            if (identitiesIds.contains(identityId)) {
                userShare += (item.getPrice() * mExchangeRate / identitiesIds.size());
            }
        }

        return userShare;
    }
}

