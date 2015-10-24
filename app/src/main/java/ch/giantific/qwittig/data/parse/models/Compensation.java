package ch.giantific.qwittig.data.parse.models;

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
 * Created by fabio on 22.12.14.
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

    public Group getGroup() {
        return (Group) getParseObject(GROUP);
    }

    public void setGroup(ParseObject group) {
        put(GROUP, group);
    }

    public User getPayer() {
        return (User) getParseUser(PAYER);
    }

    public void setPayer(ParseUser payer) {
        put(PAYER, payer);
    }

    public User getBeneficiary() {
        return (User) getParseUser(BENEFICIARY);
    }

    public void setBeneficiary(ParseUser beneficiary) {
        put(BENEFICIARY, beneficiary);
    }

    public BigFraction getAmount() {
        List<Number> amountList = getList(AMOUNT);

        long numerator = amountList.get(0).longValue();
        long denominator = amountList.get(1).longValue();

        return new BigFraction(numerator, denominator);
    }

    public void setAmount(BigFraction amount) {
        BigInteger num = amount.getNumerator();
        BigInteger den = amount.getDenominator();

        List<Long> amountList = new ArrayList<>();
        amountList.add(num.longValue());
        amountList.add(den.longValue());

        put(AMOUNT, amountList);
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

    public Compensation() {
        // A default constructor is required.
    }

    public Compensation(ParseObject group, ParseUser payer, ParseUser beneficiary, BigFraction amount,
                        boolean isPaid) {
        setGroup(group);
        setPayer(payer);
        setBeneficiary(beneficiary);
        setAmount(amount);
        setPaid(isPaid);
        setAccessRights(getCurrentGroup());
    }

    private Group getCurrentGroup() {
        User currentUser = (User) ParseUser.getCurrentUser();
        return currentUser.getCurrentGroup();
    }

    private void setAccessRights(ParseObject group) {
        ParseACL acl = ParseUtils.getDefaultAcl(group);
        setACL(acl);
    }
}
