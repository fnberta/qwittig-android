package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

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
    @PropertyName(PATH_NAME)
    private String mName;
    @PropertyName(PATH_PRICE)
    private double mPrice;
    @PropertyName(PATH_IDENTITIES)
    private Map<String, Boolean> mIdentities;

    public Item() {
        // required for firebase de-/serialization
    }

    public Item(@NonNull String name, double price, @NonNull List<String> identities) {
        mName = name;
        mPrice = price;
        mIdentities = new HashMap<>();
        for (String id : identities) {
            mIdentities.put(id, true);
        }
    }

    public String getName() {
        return mName;
    }

    public double getPrice() {
        return mPrice;
    }

    public Map<String, Boolean> getIdentities() {
        return mIdentities;
    }

    @Exclude
    public Set<String> getIdentitiesIds() {
        return mIdentities.keySet();
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
            return mPrice;
        }

        return mPrice / exchangeRate;
    }
}