package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.Date;
import java.util.Map;

/**
 * Created by fabio on 02.06.16.
 */
public class OcrRating implements FirebaseModel {

    public static final String BASE_PATH = "ocrRatings";

    public static final String PATH_SATISFACTION = "satisfaction";
    public static final String PATH_NAMES = "names";
    public static final String PATH_PRICES = "prices";
    public static final String PATH_MISSING_ARTICLES = "missingArticles";
    public static final String PATH_SPEED = "speed";
    public static final String PATH_OCR_DATA = "ocrData";

    private String id;
    private long createdAt;
    private int satisfaction;
    private int names;
    private int prices;
    private int missingArticles;
    private int speed;
    private String ocrData;

    public OcrRating() {
        // required for firebase de-/serialization
    }

    public OcrRating(int satisfaction, int names, int prices, int missingArticles, int speed,
                     @NonNull String ocrData) {
        this.satisfaction = satisfaction;
        this.names = names;
        this.prices = prices;
        this.missingArticles = missingArticles;
        this.speed = speed;
        this.ocrData = ocrData;
    }

    @Exclude
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

    public int getSatisfaction() {
        return satisfaction;
    }

    public int getNames() {
        return names;
    }

    public int getPrices() {
        return prices;
    }

    public int getMissingArticles() {
        return missingArticles;
    }

    public int getSpeed() {
        return speed;
    }

    public String getOcrData() {
        return ocrData;
    }
}
