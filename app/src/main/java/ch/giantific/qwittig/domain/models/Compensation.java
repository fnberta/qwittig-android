package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import com.google.firebase.database.ServerValue;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.Date;
import java.util.Map;

/**
 * Created by fabio on 02.07.16.
 */
@IgnoreExtraProperties
public class Compensation implements FirebaseModel {

    public static final String PATH = "compensations";
    public static final String PAID = "paid";
    public static final String UNPAID = "unpaid";
    public static final String PATH_PAID = "paid";
    public static final String PATH_GROUP = "group";
    public static final String PATH_PAID_AT = "paidAt";
    public static final String PATH_AMOUNT = "amount";
    public static final String PATH_AMOUNT_CHANGED = "amountChanged";
    public static final String PATH_DEBTOR = "debtor";
    public static final String PATH_CREDITOR = "creditor";
    public static final String NUMERATOR = "num";
    public static final String DENOMINATOR = "den";
    private String mId;
    @PropertyName(PATH_CREATED_AT)
    private long mCreatedAt;
    @PropertyName(PATH_GROUP)
    private String mGroup;
    @PropertyName(PATH_PAID)
    private boolean mPaid;
    @PropertyName(PATH_PAID_AT)
    private long mPaidAt;
    @PropertyName(PATH_DEBTOR)
    private String mDebtor;
    @PropertyName(PATH_CREDITOR)
    private String mCreditor;
    @PropertyName(PATH_AMOUNT)
    private Map<String, Long> mAmount;
    @PropertyName(PATH_AMOUNT_CHANGED)
    private boolean mAmountChanged;

    public Compensation() {
        // required for firebase de-/serialization
    }

    public Compensation(@NonNull String group, boolean paid, @NonNull String debtor,
                        @NonNull String creditor, @NonNull Map<String, Long> amount) {
        mGroup = group;
        mPaid = paid;
        mDebtor = debtor;
        mCreditor = creditor;
        mAmount = amount;
    }

    @Exclude
    @Override
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

    public boolean isPaid() {
        return mPaid;
    }

    public long getPaidAt() {
        return mPaidAt;
    }

    @Exclude
    public Date getPaidAtDate() {
        return new Date(mPaidAt);
    }

    public String getDebtor() {
        return mDebtor;
    }

    public String getCreditor() {
        return mCreditor;
    }

    public Map<String, Long> getAmount() {
        return mAmount;
    }

    @Exclude
    public BigFraction getAmountFraction() {
        final long numerator = mAmount.get(NUMERATOR);
        final long denominator = mAmount.get(DENOMINATOR);
        return new BigFraction(numerator, denominator);
    }

    public boolean isAmountChanged() {
        return mAmountChanged;
    }
}
