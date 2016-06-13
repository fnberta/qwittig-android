/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home;

import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import ch.giantific.qwittig.R;
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
    private static final int INTENT_REQUEST_IMAGE = 1;
    private File mFile;
    private Camera mCamera;
    private CameraPreview mPreview;
    private FloatingActionButton mFabCapture;
    private ImageView mTextViewDone;
    private ImageView mImageViewRedo;
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

            mFile = imageFile;
            toggleDoneVisibility();
        }
    };

    private File createImageFile() throws IOException {
        return CameraUtils.createImageFile(this);
    }

    private void showErrorMessage() {
        Toast.makeText(this, "Failed to capture the image. Please try again",
                Toast.LENGTH_LONG).show();
    }

    private void toggleDoneVisibility() {
        if (mFabCapture.getVisibility() == View.VISIBLE) {
            mFabCapture.setVisibility(View.GONE);
            mImageViewRedo.setVisibility(View.VISIBLE);
            mTextViewDone.setVisibility(View.VISIBLE);
        } else {
            mFabCapture.setVisibility(View.VISIBLE);
            mImageViewRedo.setVisibility(View.GONE);
            mTextViewDone.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        try {
            loadCamera();
        } catch (Exception e) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        final FrameLayout preview = (FrameLayout) findViewById(R.id.fl_camera_preview);
        if (preview != null) {
            preview.addView(mPreview);
        }

        mFabCapture = (FloatingActionButton) findViewById(R.id.fab_camera_capture);
        if (mFabCapture != null) {
            mFabCapture.setOnClickListener(this);
        }
        mTextViewDone = (ImageView) findViewById(R.id.iv_camera_bottom_done);
        if (mTextViewDone != null) {
            mTextViewDone.setOnClickListener(this);
        }
        mImageViewRedo = (ImageView) findViewById(R.id.iv_camera_bottom_redo);
        if (mImageViewRedo != null) {
            mImageViewRedo.setOnClickListener(this);
        }
        final TextView pick = (TextView) findViewById(R.id.tv_camera_pick_image);
        if (pick != null) {
            pick.setOnClickListener(this);
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
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            mCamera.takePicture(null, null, mPicture);
                        } else {
                            Timber.w("autoFocus failed");
                        }
                    }
                });
                break;
            case R.id.iv_camera_bottom_redo:
                if (!mFile.delete()) {
                    Timber.w("failed to delete file");
                }
                mFile = null;
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
        if (mFile == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        final Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_IMAGE_PATH, mFile.getAbsolutePath());
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
