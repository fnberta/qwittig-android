package ch.giantific.qwittig.data.rates.models;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by fabio on 28.04.15.
 */
public class CurrencyRates {

    @SerializedName("base")
    private String mBase;
    @SerializedName("date")
    private String mDate;
    @SerializedName("rates")
    private Map<String, Float> mRates;

    public String getBase() {
        return mBase;
    }

    public void setBase(String base) {
        mBase = base;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public Map<String, Float> getRates() {
        return mRates;
    }

    public void setRates(Map<String, Float> rates) {
        mRates = rates;
    }
}
