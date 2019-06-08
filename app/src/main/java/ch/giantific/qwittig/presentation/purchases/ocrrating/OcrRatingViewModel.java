package ch.giantific.qwittig.presentation.purchases.ocrrating;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fabio on 08.10.16.
 */

public class OcrRatingViewModel extends BaseObservable
        implements Parcelable {

    public static final Creator<OcrRatingViewModel> CREATOR = new Creator<OcrRatingViewModel>() {
        @Override
        public OcrRatingViewModel createFromParcel(Parcel in) {
            return new OcrRatingViewModel(in);
        }

        @Override
        public OcrRatingViewModel[] newArray(int size) {
            return new OcrRatingViewModel[size];
        }
    };
    public static final String TAG = OcrRatingViewModel.class.getCanonicalName();
    private float satisfaction;
    private float ratingNames;
    private float ratingPrices;
    private float ratingMissing;
    private float ratingSpeed;

    public OcrRatingViewModel() {
    }

    private OcrRatingViewModel(Parcel in) {
        satisfaction = in.readFloat();
        ratingNames = in.readFloat();
        ratingPrices = in.readFloat();
        ratingMissing = in.readFloat();
        ratingSpeed = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(satisfaction);
        dest.writeFloat(ratingNames);
        dest.writeFloat(ratingPrices);
        dest.writeFloat(ratingMissing);
        dest.writeFloat(ratingSpeed);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Bindable
    public float getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(float satisfaction) {
        this.satisfaction = satisfaction;
    }

    @Bindable
    public float getRatingNames() {
        return ratingNames;
    }

    public void setRatingNames(float ratingNames) {
        this.ratingNames = ratingNames;
    }

    @Bindable
    public float getRatingPrices() {
        return ratingPrices;
    }

    public void setRatingPrices(float ratingPrices) {
        this.ratingPrices = ratingPrices;
    }

    @Bindable
    public float getRatingMissing() {
        return ratingMissing;
    }

    public void setRatingMissing(float ratingMissing) {
        this.ratingMissing = ratingMissing;
    }

    @Bindable
    public float getRatingSpeed() {
        return ratingSpeed;
    }

    public void setRatingSpeed(float ratingSpeed) {
        this.ratingSpeed = ratingSpeed;
    }
}
