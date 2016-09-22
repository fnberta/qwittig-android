/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.camera;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.android.cameraview.CameraView;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.ActivityCamera2Binding;
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

public class CameraActivity2 extends BaseActivity<CameraComponent>
        implements CameraViewModel.ViewListener {

    @Inject
    CameraViewModel cameraViewModel;
    private final CameraView.Callback callback = new CameraView.Callback() {
        @Override
        public void onPictureTaken(CameraView cameraView, byte[] data) {
            super.onPictureTaken(cameraView, data);

            cameraViewModel.onPictureTaken(data);
        }
    };
    private ActivityCamera2Binding binding;
    private TutorialOverlayCameraBinding tutBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera2);
        binding.setViewModel(cameraViewModel);
        cameraViewModel.attachView(this);

        binding.camera.addCallback(callback);
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

    @Override
    protected void onStart() {
        super.onStart();

        cameraViewModel.onViewVisible();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setImmersiveMode();
        binding.camera.start();
    }

    private void setImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onPause() {
        binding.camera.stop();

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

        if (requestCode == Navigator.INTENT_REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            cameraViewModel.onGalleryImageChosen(imageUri.toString());
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
        binding.camera.takePicture();
    }

    @Override
    public Single<File> createImageFile() {
        return Single.fromCallable(() -> CameraUtils.createImageFile(this));
    }

    @Override
    public void showPreview() {
//        camera.cancelAutoFocus();
//        camera.startPreview();
    }
}
