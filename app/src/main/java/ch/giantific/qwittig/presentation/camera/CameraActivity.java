/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.camera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.ActivityCameraBinding;
import ch.giantific.qwittig.databinding.TutorialOverlayCameraBinding;
import ch.giantific.qwittig.utils.CameraUtils;
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
 * Subclass of {@link AppCompatActivity}.
 */
@SuppressWarnings("deprecation")
public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String INTENT_EXTRA_IMAGE_PATH = "INTENT_EXTRA_IMAGE_PATH";
    public static final int RESULT_ERROR = 2;
    private static final int INTENT_REQUEST_IMAGE = 1;
    private static final String PREF_FIRST_CAMERA_RUN = "PREF_FIRST_CAMERA_RUN";

    private ActivityCameraBinding binding;
    private File file;
    @NonNull
    private final Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(@NonNull byte[] data, Camera camera) {
            final File imageFile;
            try {
                imageFile = createImageFile();
                final FileOutputStream fos = new FileOutputStream(imageFile);
                fos.write(data);
                fos.close();
            } catch (IOException e) {
                showErrorMessage();
                return;
            }

            file = imageFile;
            toggleDoneVisibility();
        }
    };
    private Camera mCamera;
    private CameraPreview mPreview;

    private File createImageFile() throws IOException {
        return CameraUtils.createImageFile(this);
    }

    private void showErrorMessage() {
        Toast.makeText(this, "Failed to capture the image. Please try again",
                Toast.LENGTH_LONG).show();
    }

    private void toggleDoneVisibility() {
        if (binding.fabCameraCapture.getVisibility() == View.VISIBLE) {
            binding.fabCameraCapture.setVisibility(View.GONE);
            binding.ivCameraBottomRedo.setVisibility(View.VISIBLE);
            binding.ivCameraBottomDone.setVisibility(View.VISIBLE);
        } else {
            binding.fabCameraCapture.setVisibility(View.VISIBLE);
            binding.ivCameraBottomRedo.setVisibility(View.GONE);
            binding.ivCameraBottomDone.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera);

        try {
            loadCamera();
        } catch (Exception e) {
            setResult(RESULT_ERROR);
            finish();
            return;
        }

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        binding.flCameraPreview.addView(mPreview);

        binding.fabCameraCapture.setOnClickListener(this);
        binding.ivCameraBottomDone.setOnClickListener(this);
        binding.ivCameraBottomRedo.setOnClickListener(this);
        binding.tvCameraPickImage.setOnClickListener(this);

        if (isFirstRun()) {
            final TutorialOverlayCameraBinding tutBinding =
                    TutorialOverlayCameraBinding.inflate(getLayoutInflater(), binding.flCameraMain, false);
            binding.flCameraMain.addView(tutBinding.svTutCamera);
            tutBinding.fabTutCameraDone.setOnClickListener(v -> binding.flCameraMain.removeView(tutBinding.svTutCamera));
        }
    }

    private void loadCamera() throws Exception {
        mCamera = CameraUtils.getCameraInstance();

        final Camera.Parameters params = mCamera.getParameters();
        final List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        final List<String> flashModes = params.getSupportedFlashModes();
        if (flashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        setRotation(params, 0);
        mCamera.setParameters(params);
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

    private boolean isFirstRun() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean isFirstRun = prefs.getBoolean(PREF_FIRST_CAMERA_RUN, true);
        if (isFirstRun) {
            prefs.edit().putBoolean(PREF_FIRST_CAMERA_RUN, false).apply();
        }

        return isFirstRun;
    }

    @Override
    protected void onResume() {
        super.onResume();

        setImmersiveMode();

        if (mCamera == null) {
            try {
                loadCamera();
                mPreview.setCamera(mCamera);
            } catch (Exception e) {
                setResult(RESULT_CANCELED);
                finish();
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
    public void onClick(@NonNull View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab_camera_capture:
                mCamera.autoFocus((success, camera) -> {
                    if (success) {
                        mCamera.takePicture(null, null, mPicture);
                    } else {
                        Timber.w("autoFocus failed");
                    }
                });
                break;
            case R.id.iv_camera_bottom_redo:
                if (!file.delete()) {
                    Timber.w("failed to delete file");
                }
                file = null;
                showPreview();
                break;
            case R.id.iv_camera_bottom_done:
                finishCapture();
                break;
            case R.id.tv_camera_pick_image:
                showImagePicker();
                break;
        }
    }

    private void showPreview() {
        mCamera.cancelAutoFocus();
        mCamera.startPreview();
        toggleDoneVisibility();
    }

    private void finishCapture() {
        if (file == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        final Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_IMAGE_PATH, file.getAbsolutePath());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void showImagePicker() {
        final Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, INTENT_REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_IMAGE: {
                if (resultCode == RESULT_OK) {
                    final Uri imageUri = data.getData();
                    final Intent intent = new Intent();
                    intent.putExtra(INTENT_EXTRA_IMAGE_PATH, imageUri.toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        releaseCamera();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);

        super.onBackPressed();
    }
}
