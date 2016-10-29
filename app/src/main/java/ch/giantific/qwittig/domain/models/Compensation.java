package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fabio on 02.07.16.
 */
@IgnoreExtraProperties
public class Compensation implements FirebaseModel {

    public static final String BASE_PATH = "compensations";
    public static final String BASE_PATH_PAID = "paid";
    public static final String BASE_PATH_UNPAID = "unpaid";

    public static final String PATH_IS_PAID = "isPaid";
    public static final String PATH_GROUP = "group";
    public static final String PATH_PAID_AT = "paidAt";
    public static final String PATH_AMOUNT = "amount";
    public static final String PATH_IS_AMOUNT_CHANGED = "isAmountChanged";
    public static final String PATH_DEBTOR = "debtor";
    public static final String PATH_CREDITOR = "creditor";
    public static final String NUMERATOR = "num";
    public static final String DENOMINATOR = "den";

    private String id;
    private long createdAt;
    private String group;
    private boolean paid;
    private long paidAt;
    private String debtor;
    private String creditor;
    private Map<String, Long> amount;
    private boolean amountChanged;

    public Compensation() {
        // required for firebase de-/serialization
    }

    public Compensation(@NonNull String group, boolean paid, @NonNull String debtor,
                        @NonNull String creditor, @NonNull Map<String, Long> amount) {
        this.group = group;
        this.paid = paid;
        this.debtor = debtor;
        this.creditor = creditor;
        this.amount = amount;
    }

    @Exclude
    @Override
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

    public boolean isPaid() {
        return paid;
    }

    public long getPaidAt() {
        return paidAt;
    }

    @Exclude
    public Date getPaidAtDate() {
        return new Date(paidAt);
    }

    public String getDebtor() {
        return debtor;
    }

    public String getCreditor() {
        return creditor;
    }

    public Map<String, Long> getAmount() {
        return amount;
    }

    @Exclude
    public BigFraction getAmountFraction() {
        final long numerator = amount.get(NUMERATOR);
        final long denominator = amount.get(DENOMINATOR);
        return new BigFraction(numerator, denominator);
    }

    public boolean isAmountChanged() {
        return amountChanged;
    }

    @Exclude
    public Map<String, Object> toMap() {
        final Map<String, Object> result = new HashMap<>();
        result.put(PATH_CREATED_AT, ServerValue.TIMESTAMP);
        result.put(PATH_GROUP, group);
        result.put(PATH_IS_PAID, paid);
        result.put(PATH_PAID_AT, paidAt);
        result.put(PATH_DEBTOR, debtor);
        result.put(PATH_CREDITOR, creditor);
        result.put(PATH_AMOUNT, amount);

        return result;
    }
}
