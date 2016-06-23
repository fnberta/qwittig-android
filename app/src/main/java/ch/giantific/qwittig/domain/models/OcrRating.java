package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by fabio on 02.06.16.
 */
@ParseClassName("OcrRating")
public class OcrRating extends ParseObject {

    public static final String CLASS = "OcrRating";
    public static final String SATISFACTION = "satisfaction";
    public static final String NAMES = "names";
    public static final String PRICES = "prices";
    public static final String MISSING_ARTICLES = "missingArticles";
    public static final String SPEED = "speed";
    public static final String OCR_DATA = "ocrData";

    public OcrRating() {
        // a default constructor is required.
    }

    public OcrRating(int satisfaction, int names, int prices, int missingArticles, int speed,
                     @NonNull OcrData ocrData) {
        setSatisfaction(satisfaction);
        setNames(names);
        setPrices(prices);
        setMissingArticles(missingArticles);
        setSpeed(speed);
        setOcrData(ocrData);
    }

    public int getSatisfaction() {
        return getInt(SATISFACTION);
    }

    public void setSatisfaction(int satisfaction) {
        put(SATISFACTION, satisfaction);
    }

    public int getNames() {
        return getInt(NAMES);
    }

    public void setNames(int names) {
        put(NAMES, names);
    }

    public int getPrices() {
        return getInt(PRICES);
    }

    public void setPrices(int prices) {
        put(PRICES, prices);
    }

    public int getMissingArticles() {
        return getInt(MISSING_ARTICLES);
    }

    public void setMissingArticles(int missingArticles) {
        put(MISSING_ARTICLES, missingArticles);
    }

    public int getSpeed() {
        return getInt(SPEED);
    }

    public void setSpeed(int speed) {
        put(SPEED, speed);
    }

    public OcrData getOcrData() {
        return (OcrData) getParseObject(OCR_DATA);
    }

    public void setOcrData(@NonNull OcrData ocrData) {
        put(OCR_DATA, ocrData);
    }
}
