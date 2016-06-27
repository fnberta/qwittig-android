/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.parse.ParseUtils;

/**
 * Represents an item of a purchase with a name, a price and the identities involved in the item.
 * <p/>
 * Subclass of {@link ParseObject}.
 */
@ParseClassName("Item")
public class Item extends ParseObject {

    public static final String CLASS = "Item";
    public static final String NAME = "name";
    public static final String PRICE = "price";
    public static final String IDENTITIES = "identities";

    public Item() {
        // A default constructor is required.
    }

    public Item(@NonNull String name, @NonNull BigDecimal price,
                @NonNull List<Identity> identities, @NonNull Group group) {
        setName(name);
        setPrice(price);
        setIdentities(identities);
        setAccessRights(group);
    }

    private void setAccessRights(@NonNull Group group) {
        final ParseACL acl = ParseUtils.getDefaultAcl(group, true);
        setACL(acl);
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

    /**
     * Returns the price converted to foreign currency using the provided exchange rate.
     *
     * @param exchangeRate the exchange rate to convert the price
     * @return the price in foreign currency
     */
    public double getPriceForeign(double exchangeRate) {
        final double price = getPrice();
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
    public void convertPrice(double exchangeRate, boolean toGroupCurrency) {
        final double price = getPrice();
        final double priceConverted = toGroupCurrency ? price * exchangeRate : price / exchangeRate;
        setPrice(MoneyUtils.roundConvertedPrice(priceConverted));
    }

    public List<Identity> getIdentities() {
        return getList(IDENTITIES);
    }

    public void setIdentities(@NonNull List<Identity> identities) {
        put(IDENTITIES, identities);
    }

    /**
     * Returns the object ids of the item's involved users.
     *
     * @return the object ids of the involved users
     */
    @NonNull
    public List<String> getIdentitiesIds() {
        final List<String> ids = new ArrayList<>();
        final List<Identity> identities = getIdentities();
        for (Identity identity : identities) {
            ids.add(identity.getObjectId());
        }
        return ids;
    }
}