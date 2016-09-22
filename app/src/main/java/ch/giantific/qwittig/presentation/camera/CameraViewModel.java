package ch.giantific.qwittig.presentation.camera;

import android.databinding.Bindable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.View;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import rx.Single;

/**
 * Created by fabio on 11.09.16.
 */

public interface CameraViewModel extends ViewModel<CameraViewModel.ViewListener> {

    @Bindable
    boolean isImageTakenBarVisible();

    void setImageTakenBarVisible(boolean visible);

    void onCameraLoadFailed();

    void onPictureTaken(@NonNull byte[] data);

    void onFabCaptureClick(View view);

    void onRedoClick(View view);

    void onDoneClick(View view);

    void onFromGalleryClick(View view);

    void onGalleryImageChosen(@NonNull String imagePath);

    void onTutorialOkClick(View view);

    @IntDef({CameraResult.ERROR})
    @Retention(RetentionPolicy.SOURCE)
    @interface CameraResult {
        int ERROR = 2;
    }

    interface ViewListener extends ViewModel.ViewListener {

        void showTutorial();

        void hideTutorial();

        void captureImage();

        Single<File> createImageFile();

        void showPreview();
    }
}
