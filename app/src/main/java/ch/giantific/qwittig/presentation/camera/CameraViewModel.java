package ch.giantific.qwittig.presentation.camera;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import ch.giantific.qwittig.BR;

/**
 * Created by fabio on 29.09.16.
 */

public class CameraViewModel extends BaseObservable {

    private boolean imageTakenBarVisible;

    @Bindable
    public boolean isImageTakenBarVisible() {
        return imageTakenBarVisible;
    }

    public void setImageTakenBarVisible(boolean imageTakenBarVisible) {
        this.imageTakenBarVisible = imageTakenBarVisible;
        notifyPropertyChanged(BR.imageTakenBarVisible);
    }
}
