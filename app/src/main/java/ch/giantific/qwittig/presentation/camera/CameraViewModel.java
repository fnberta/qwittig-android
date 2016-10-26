package ch.giantific.qwittig.presentation.camera;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import javax.inject.Inject;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.di.scopes.PerActivity;

/**
 * Created by fabio on 29.09.16.
 */

@PerActivity
public class CameraViewModel extends BaseObservable {

    private boolean imageTakenBarVisible;

    @Inject
    public CameraViewModel() {
    }

    @Bindable
    public boolean isImageTakenBarVisible() {
        return imageTakenBarVisible;
    }

    public void setImageTakenBarVisible(boolean imageTakenBarVisible) {
        this.imageTakenBarVisible = imageTakenBarVisible;
        notifyPropertyChanged(BR.imageTakenBarVisible);
    }
}
