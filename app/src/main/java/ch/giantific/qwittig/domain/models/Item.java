package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by fabio on 02.07.16.
 */
@IgnoreExtraProperties
public class Item {

    public static final String PATH_NAME = "name";
    public static final String PATH_PRICE = "price";
    public static final String PATH_IDENTITIES = "identities";
    
    private String name;
    private double price;
    private Map<String, Boolean> identities;

    public Item() {
        // required for firebase de-/serialization
    }

    public Item(@NonNull String name, double price, @NonNull List<String> identities) {
        this.name = name;
        this.price = price;
        this.identities = new HashMap<>();
        for (String id : identities) {
            this.identities.put(id, true);
        }
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public Map<String, Boolean> getIdentities() {
        return identities;
    }

    @Exclude
    public Set<String> getIdentitiesIds() {
        return identities.keySet();
    }

    /**
     * Returns the price converted to foreign currency using the provided exchange rate.
     *
     * @param exchangeRate the exchange rate to convert the price
     * @return the price in foreign currency
     */
    @Exclude
    public double getPriceForeign(double exchangeRate) {
        if (exchangeRate == 1) {
            return price;
        }

        return price / exchangeRate;
    }

    @Exclude
    public Map<String, Object> toMap() {
        final Map<String, Object> result = new HashMap<>();
        result.put(PATH_NAME, name);
        result.put(PATH_PRICE, price);
        result.put(PATH_IDENTITIES, identities);

        return result;
    }
}