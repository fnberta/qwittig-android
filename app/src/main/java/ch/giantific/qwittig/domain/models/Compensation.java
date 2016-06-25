/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.apache.commons.math3.fraction.BigFraction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.utils.parse.ParseUtils;

/**
 * Defines a compensation, a payment from one {@link User} to another. It includes the payer and
 * beneficiary, the corresponding group, the amount and whether it is paid or not.
 * <p>
 * Subclass of {@link ParseObject}.
 */
@ParseClassName("Compensation")
public class Compensation extends ParseObject {

    public static final String CLASS = "Compensation";
    public static final String GROUP = "group";
    public static final String DEBTOR = "debtor";
    public static final String CREDITOR = "creditor";
    public static final String AMOUNT = "amount";
    public static final String PAID = "paid";
    public static final String PIN_LABEL_PAID = "compensationPinLabelPaid";
    public static final String PIN_LABEL_UNPAID = "compensationPinLabelUnpaid";

    public Compensation() {
        // A default constructor is required.
    }

    public Compensation(@NonNull Group group, @NonNull Identity debtor,
                        @NonNull Identity creditor, @NonNull BigFraction amount,
                        boolean isPaid) {
        setGroup(group);
        setDebtor(debtor);
        setCreditor(creditor);
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

    public Identity getDebtor() {
        return (Identity) getParseObject(DEBTOR);
    }

    public void setDebtor(@NonNull Identity debtor) {
        put(DEBTOR, debtor);
    }

    public Identity getCreditor() {
        return (Identity) getParseObject(CREDITOR);
    }

    public void setCreditor(@NonNull Identity creditor) {
        put(CREDITOR, creditor);
    }

    public List<Number> getAmount() {
        return getList(AMOUNT);
    }

    public void setAmount(@NonNull List<Long> amount) {
        put(AMOUNT, amount);
    }

    public boolean isPaid() {
        return getBoolean(PAID);
    }

    public void setPaid(boolean isPaid) {
        put(PAID, isPaid);
    }

    private void setAccessRights(@NonNull Group group) {
        ParseACL acl = ParseUtils.getDefaultAcl(group, true);
        setACL(acl);
    }

    /**
     * Returns the amount of the compensation as a {@link BigFraction}.
     *
     * @return the amount of the compensation
     */
    public BigFraction getAmountFraction() {
        final List<Number> amountList = getAmount();
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
