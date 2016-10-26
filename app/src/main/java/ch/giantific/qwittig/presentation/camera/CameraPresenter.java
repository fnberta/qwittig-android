package ch.giantific.qwittig.presentation.camera;

import android.support.annotation.NonNull;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileOutputStream;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.helper.SharedPrefsHelper;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.presentation.camera.CameraContract.CameraResult;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by fabio on 11.09.16.
 */
public class CameraPresenter extends BasePresenterImpl<CameraContract.ViewListener>
        implements CameraContract.Presenter {

    private final CameraViewModel viewModel;
    private final SharedPrefsHelper prefsHelper;
    private byte[] takenData;

    @Inject
    public CameraPresenter(@NonNull Navigator navigator,
                           @NonNull CameraViewModel viewModel,
                           @NonNull SharedPrefsHelper prefsHelper,
                           @NonNull UserRepository userRepo) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
        this.prefsHelper = prefsHelper;
    }

    @Override
    public void onCameraLoadFailed() {
        navigator.finish(CameraResult.ERROR);
    }

    @Override
    public void onPictureTaken(@NonNull byte[] data) {
        takenData = data;
        viewModel.setImageTakenBarVisible(true);
    }

    @Override
    public void onCaptureClick(View v) {
        view.captureImage();
    }

    @Override
    public void onRedoClick(View v) {
        takenData = null;
        viewModel.setImageTakenBarVisible(false);
        view.showPreview();
    }

    @Override
    public void onDoneClick(View v) {
        if (takenData == null) {
            navigator.finish(RESULT_CANCELED);
            return;
        }

        subscriptions.add(view.createImageFile()
                .flatMap(file -> writeDataToFile(file, takenData))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<File>() {
                    @Override
                    public void onSuccess(File imageFile) {
                        navigator.finish(RESULT_OK, imageFile.getAbsolutePath());
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to save the picture with error:");
                        view.showMessage(R.string.camera_image_save_failed);
                    }
                })
        );
    }

    @NonNull
    private Single<File> writeDataToFile(@NonNull File file, @NonNull byte[] data) {
        return Single
                .fromCallable(() -> {
                    final FileOutputStream fos = new FileOutputStream(file);
                    fos.write(data);
                    fos.close();
                    return fos;
                })
                .map(fileOutputStream -> file);
    }

    @Override
    public void onFromGalleryClick(View view) {
        navigator.startImagePicker();
    }

    @Override
    public void onGalleryImageChosen(@NonNull String imagePath) {
        navigator.finish(RESULT_OK, imagePath);
    }

    @Override
    public void onTutorialOkClick(View v) {
        view.hideTutorial();
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        if (prefsHelper.isFirstCameraRun()) {
            view.showTutorial();
        }
    }
}