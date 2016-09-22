/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.camera;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.OrientationEventListener;
import android.view.View;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.ActivityCameraBinding;
import ch.giantific.qwittig.databinding.TutorialOverlayCameraBinding;
import ch.giantific.qwittig.presentation.camera.di.CameraComponent;
import ch.giantific.qwittig.presentation.camera.di.CameraViewModelModule;
import ch.giantific.qwittig.presentation.camera.di.DaggerCameraComponent;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.utils.CameraUtils;
import rx.Single;
import timber.log.Timber;

/**
 * Provides a custom camera interface that is locked to portrait orientation and allows the user
 * to take multiple images before finishing.
 * <p/>
 * The idea is that for long receipts, the user takes multiple images of the receipt instead of
 * just one.
 * <p/>
 * Uses the deprecated {@link Camera} api because {@link android.hardware.camera2} is only
 * available on api >21 and currently we support api >19.
 * <p/>
 *
 * @see CameraPreview
 */
@SuppressWarnings("deprecation")
public class CameraActivity extends BaseActivity<CameraComponent>
        implements CameraViewModel.ViewListener {

    @Inject
    CameraViewModel cameraViewModel;
    private final Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(@NonNull byte[] data, Camera camera) {
            cameraViewModel.onPictureTaken(data);
        }
    };
    private ActivityCameraBinding binding;
    private TutorialOverlayCameraBinding tutBinding;
    private Camera camera;
    private CameraPreview preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera);
        binding.setViewModel(cameraViewModel);
        cameraViewModel.attachView(this);

        try {
            loadCamera();
        } catch (Exception e) {
            cameraViewModel.onCameraLoadFailed();
            return;
        }
        preview = new CameraPreview(this, camera);
        binding.flCameraPreview.addView(preview);
    }

    @Override
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        DaggerCameraComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .cameraViewModelModule(new CameraViewModelModule(savedInstanceState))
                .build()
                .inject(this);
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{cameraViewModel});
    }

    private void loadCamera() throws Exception {
        camera = CameraUtils.getCameraInstance();

        final Camera.Parameters params = camera.getParameters();
        final List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        final List<String> flashModes = params.getSupportedFlashModes();
        if (flashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        setRotation(params, 0);
        camera.setParameters(params);
    }

    @NonNull
    private Camera.Parameters setRotation(@NonNull Camera.Parameters params, int orientation) {
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return params;
        }

        final Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);
        orientation = (orientation + 45) / 90 * 90;
        final int rotation = info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT
                ? (info.orientation - orientation + 360) % 360
                : (info.orientation + orientation) % 360;
        params.setRotation(rotation);
        return params;
    }

    @Override
    protected void onStart() {
        super.onStart();

        cameraViewModel.onViewVisible();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setImmersiveMode();

        if (camera == null) {
            try {
                loadCamera();
                preview.setCamera(camera);
            } catch (Exception e) {
                cameraViewModel.onCameraLoadFailed();
            }
        }
    }

    private void setImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onPause() {
        if (camera != null) {
            camera.release();
            camera = null;
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        cameraViewModel.onViewGone();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);

        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Navigator.INTENT_REQUEST_IMAGE_PICK: {
                if (resultCode == RESULT_OK) {
                    final Uri imageUri = data.getData();
                    cameraViewModel.onGalleryImageChosen(imageUri.toString());
                }
            }
        }
    }

    @Override
    public void showTutorial() {
        tutBinding = TutorialOverlayCameraBinding.inflate(getLayoutInflater(), binding.flCameraMain, false);
        tutBinding.setViewModel(cameraViewModel);
        binding.flCameraMain.addView(tutBinding.svTutCamera);
        tutBinding.fabTutCameraDone.setOnClickListener(v -> binding.flCameraMain.removeView(tutBinding.svTutCamera));
    }

    @Override
    public void hideTutorial() {
        binding.flCameraMain.removeView(tutBinding.svTutCamera);
    }

    @Override
    public void captureImage() {
        camera.cancelAutoFocus();
        camera.autoFocus((success, camera) -> {
            if (success) {
                this.camera.takePicture(null, null, pictureCallback);
            } else {
                Timber.w("autoFocus failed");
            }
        });
    }

    @Override
    public Single<File> createImageFile() {
        return Single.fromCallable(() -> CameraUtils.createImageFile(this));
    }

    @Override
    public void showPreview() {
        camera.cancelAutoFocus();
        camera.startPreview();
    }
}
