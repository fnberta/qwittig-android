/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.models.parse;

import android.support.annotation.NonNull;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.utils.ParseUtils;

/**
 * Defines a compensation, a payment from one {@link User} to another. It includes the payer and
 * beneficiary, the corresponding group, the amount and whether it is paid or not.
 * <p/>
 * Subclass of {@link ParseObject}.
 */
@ParseClassName("Compensation")
public class Compensation extends ParseObject {

    public static final String CLASS = "Compensation";
    public static final String GROUP = "group";
    public static final String PAYER = "payer";
    public static final String BENEFICIARY = "beneficiary";
    public static final String AMOUNT = "amount";
    public static final String IS_PAID = "isPaid";
    public static final String PIN_LABEL_PAID = "compensationPinLabelPaid";
    public static final String PIN_LABEL_UNPAID = "compensationPinLabelUnpaid";
    private boolean mIsLoading;

    public Compensation() {
        // A default constructor is required.
    }

    public Compensation(@NonNull ParseObject group, @NonNull ParseUser payer,
                        @NonNull ParseUser beneficiary, @NonNull BigFraction amount,
                        boolean isPaid) {
        setGroup(group);
        setPayer(payer);
        setBeneficiary(beneficiary);
        setAmountFraction(amount);
        setPaid(isPaid);
        setAccessRights(group);
    }

    public Group getGroup() {
        return (Group) getParseObject(GROUP);
    }

    public void setGroup(@NonNull ParseObject group) {
        put(GROUP, group);
    }

    public User getPayer() {
        return (User) getParseUser(PAYER);
    }

    public void setPayer(@NonNull ParseUser payer) {
        put(PAYER, payer);
    }

    public User getBeneficiary() {
        return (User) getParseUser(BENEFICIARY);
    }

    public void setBeneficiary(@NonNull ParseUser beneficiary) {
        put(BENEFICIARY, beneficiary);
    }

    public List<Number> getAmount() {
        return getList(AMOUNT);
    }

    public void setAmount(@NonNull List<Long> amount) {
        put(AMOUNT, amount);
    }

    public boolean isPaid() {
        return getBoolean(IS_PAID);
    }

    public void setPaid(boolean isPaid) {
        put(IS_PAID, isPaid);
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public void setIsLoading(boolean isLoading) {
        mIsLoading = isLoading;
    }

    private void setAccessRights(@NonNull ParseObject group) {
        ParseACL acl = ParseUtils.getDefaultAcl(group);
        setACL(acl);
    }

    /**
     * Returns the amount of the compensation as a {@link BigFraction}.
     *
     * @return the amount of the compensation
     */
    public BigFraction getAmountFraction() {
        List<Number> amountList = getAmount();

        long numerator = amountList.get(0).longValue();
        long denominator = amountList.get(1).longValue();

        return new BigFraction(numerator, denominator);
    }

    /**
     * Sets the amount of the compensation by putting the numerator and denominator of the passed
     * in {@link BigFraction} into a list and storing it.
     *
     * @param amount the amount to store
     */
    public void setAmountFraction(@NonNull BigFraction amount) {
        BigInteger num = amount.getNumerator();
        BigInteger den = amount.getDenominator();

        List<Long> amountList = new ArrayList<>();
        amountList.add(num.longValue());
        amountList.add(den.longValue());

        setAmount(amountList);
    }
}
