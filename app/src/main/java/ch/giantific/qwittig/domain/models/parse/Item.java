/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.models.parse;

import android.support.annotation.NonNull;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.ParseUtils;

/**
 * Represents an item of a purchase with a name, a price and the users involved.
 * <p/>
 * Subclass of {@link ParseObject}.
 */
@ParseClassName("Item")
public class Item extends ParseObject {

    public static final String CLASS = "Item";
    public static final String NAME = "name";
    public static final String PRICE = "price";
    public static final String USERS_INVOLVED = "usersInvolved";

    public Item() {
        // A default constructor is required.
    }

    public Item(@NonNull String name, @NonNull BigDecimal price,
                @NonNull List<ParseUser> usersInvolved) {
        this(name, price);
        setUsersInvolved(usersInvolved);
    }

    public Item(@NonNull String name,
                @NonNull BigDecimal price) {
        setName(name);
        setPrice(price);
        setAccessRights(getCurrentGroup());
    }

    private void setAccessRights(@NonNull ParseObject group) {
        ParseACL acl = ParseUtils.getDefaultAcl(group);
        setACL(acl);
    }

    private Group getCurrentGroup() {
        User currentUser = (User) ParseUser.getCurrentUser();
        return currentUser.getCurrentGroup();
    }

    public String getName() {
        return getString(NAME);
    }

    public void setName(@NonNull String name) {
        put(NAME, name);
    }

    public double getPrice() {
        return getDouble(PRICE);
    }

    public void setPrice(@NonNull Number finalPrice) {
        put(PRICE, finalPrice);
    }

    public List<ParseUser> getUsersInvolved() {
        return getList(USERS_INVOLVED);
    }

    public void setUsersInvolved(@NonNull List<ParseUser> usersInvolved) {
        put(USERS_INVOLVED, usersInvolved);
    }

    /**
     * Returns the price converted to foreign currency using the provided exchange rate.
     *
     * @param exchangeRate the exchange rate to convert the price
     * @return the price in foreign currency
     */
    public double getPriceForeign(float exchangeRate) {
        double price = getPrice();
        if (exchangeRate == 1) {
            return price;
        }

        return price / exchangeRate;
    }

    /**
     * Converts the item's price either to the group's currency or the foreign currency using the
     * exchange rate provided.
     *
     * @param exchangeRate    the exchange rate to be used to convert the price
     * @param toGroupCurrency whether to convert to the group's currency or to a foreign one
     */
    public void convertPrice(float exchangeRate, boolean toGroupCurrency) {
        double price = getPrice();
        double priceConverted = toGroupCurrency ? price * exchangeRate : price / exchangeRate;
        setPrice(MoneyUtils.roundToFractionDigits(4, priceConverted));
    }

    /**
     * Returns the object ids of the item's involved users.
     *
     * @return the object ids of the involved users
     */
    @NonNull
    public List<String> getUsersInvolvedIds() {
        List<String> listIds = new ArrayList<>();
        List<ParseUser> list = getUsersInvolved();
        for (ParseUser user : list) {
            listIds.add(user.getObjectId());
        }
        return listIds;
    }
}
