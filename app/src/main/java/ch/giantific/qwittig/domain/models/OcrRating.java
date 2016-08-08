package ch.giantific.qwittig.domain.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;
import com.google.firebase.database.ServerValue;

import java.util.Date;
import java.util.Map;

/**
 * Created by fabio on 02.06.16.
 */
public class OcrRating implements FirebaseModel {

    public static final String PATH = "ocrRatings";
    public static final String PATH_MODIFIED_AT = "modifiedAt";
    public static final String PATH_SATISFACTION = "satisfaction";
    public static final String PATH_NAMES = "names";
    public static final String PATH_PRICES = "prices";
    public static final String PATH_MISSING_ARTICLES = "missingArticles";
    public static final String PATH_SPEED = "speed";
    public static final String PATH_OCR_DATA = "ocrData";
    private String mId;
    @PropertyName(PATH_CREATED_AT)
    private long mCreatedAt;
    @PropertyName(PATH_MODIFIED_AT)
    private long mModifiedAt;
    @PropertyName(PATH_SATISFACTION)
    private int mSatisfaction;
    @PropertyName(PATH_NAMES)
    private int mNames;
    @PropertyName(PATH_PRICES)
    private int mPrices;
    @PropertyName(PATH_MISSING_ARTICLES)
    private int mMissingArticles;
    @PropertyName(PATH_SPEED)
    private int mSpeed;
    @PropertyName(PATH_OCR_DATA)
    private String mOcrData;

    public OcrRating() {
        // required for firebase de-/serialization
    }

    public OcrRating(int satisfaction, int names, int prices, int missingArticles, int speed,
                     @NonNull String ocrData) {
        mSatisfaction = satisfaction;
        mNames = names;
        mPrices = prices;
        mMissingArticles = missingArticles;
        mSpeed = speed;
        mOcrData = ocrData;
    }

    @Exclude
    public String getId() {
        return mId;
    }

    @Override
    public void setId(@NonNull String id) {
        mId = id;
    }

    @Override
    public Map<String, String> getCreatedAt() {
        return ServerValue.TIMESTAMP;
    }

    @Exclude
    public Date getCreatedAtDate() {
        return new Date(mCreatedAt);
    }

    public int getSatisfaction() {
        return mSatisfaction;
    }

    public int getNames() {
        return mNames;
    }

    public int getPrices() {
        return mPrices;
    }

    public int getMissingArticles() {
        return mMissingArticles;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public String getOcrData() {
        return mOcrData;
    }
}
