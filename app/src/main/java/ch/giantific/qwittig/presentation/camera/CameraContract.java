package ch.giantific.qwittig.presentation.camera;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.View;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;
import rx.Single;

/**
 * Created by fabio on 11.09.16.
 */

public interface CameraContract {

    interface Presenter extends BasePresenter<ViewListener> {

        void onCameraLoadFailed();

        void onPictureTaken(@NonNull byte[] data);

        void onCaptureClick(View view);

        void onRedoClick(View view);

        void onDoneClick(View view);

        void onFromGalleryClick(View view);

        void onGalleryImageChosen(@NonNull String imagePath);

        void onTutorialOkClick(View view);
    }

    interface ViewListener extends BaseView {

        void showTutorial();

        void hideTutorial();

        void captureImage();

        Single<File> createImageFile();

        void showPreview();
    }

    @IntDef({CameraResult.ERROR})
    @Retention(RetentionPolicy.SOURCE)
    @interface CameraResult {
        int ERROR = 2;
    }
}
