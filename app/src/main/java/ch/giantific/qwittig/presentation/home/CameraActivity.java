/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ch.giantific.qwittig.BuildConfig;
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

    public static final String INTENT_EXTRA_PATHS = "INTENT_EXTRA_PATHS";
    private static final int MAX_NUMBER_IMAGES = 3;
    private Camera mCamera;
    private CameraPreview mPreview;
    private LinearLayout mLinearLayoutTaken;
    private FloatingActionButton mFabCapture;
    private View mViewBottom;
    @NonNull
    private final List<File> mImageFiles = new ArrayList<>();
    @NonNull
    private final Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(@NonNull byte[] data, Camera camera) {
            File imageFile;
            try {
                imageFile = createImageFile();
                FileOutputStream fos = new FileOutputStream(imageFile);
                fos.write(data);
                fos.close();
            } catch (IOException e) {
                showErrorToast();
                return;
            }

            mImageFiles.add(imageFile);
            toggleBottomVisibility();
        }
    };

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    private void showErrorToast() {
        Toast.makeText(this, "Failed to capture the image. Pleas try again",
                Toast.LENGTH_LONG).show();
    }

    private void toggleBottomVisibility() {
        if (mFabCapture.getVisibility() == View.VISIBLE) {
            mFabCapture.setVisibility(View.GONE);
            mViewBottom.setVisibility(View.VISIBLE);
        } else {
            mFabCapture.setVisibility(View.VISIBLE);
            mViewBottom.setVisibility(View.GONE);
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
        FrameLayout preview = (FrameLayout) findViewById(R.id.fl_camera_preview);
        preview.addView(mPreview);

        mLinearLayoutTaken = (LinearLayout) findViewById(R.id.ll_camera_image_taken);
        mFabCapture = (FloatingActionButton) findViewById(R.id.fab_camera_capture);
        mFabCapture.setOnClickListener(this);
        mViewBottom = findViewById(R.id.rl_camera_bottom);

        ImageView done = (ImageView) findViewById(R.id.iv_camera_bottom_done);
        done.setOnClickListener(this);
        ImageView add = (ImageView) findViewById(R.id.iv_camera_bottom_add);
        add.setOnClickListener(this);
        ImageView redo = (ImageView) findViewById(R.id.iv_camera_bottom_redo);
        redo.setOnClickListener(this);
    }

    private void loadCamera() throws Exception {
        mCamera = CameraUtils.getCameraInstance();

        Camera.Parameters params = mCamera.getParameters();
        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        List<String> flashModes = params.getSupportedFlashModes();
        if (flashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        }
        setRotation(params, 0);
        mCamera.setParameters(params);
    }

    @NonNull
    private Camera.Parameters setRotation(@NonNull Camera.Parameters params, int orientation) {
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return params;
        }

        Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        Camera.getCameraInfo(0, info);
        orientation = (orientation + 45) / 90 * 90;
        Timber.e("info.orientation %1$s", info.orientation);
        int rotation;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else {  // back-facing camera
            rotation = (info.orientation + orientation) % 360;
        }

        Timber.e("rotation %1$s", rotation);
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

    // TODO: think about api 17
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
                            Timber.e("autoFocus failed");
                        }
                    }
                });
                break;
            case R.id.iv_camera_bottom_redo:
                File image = mImageFiles.get(mImageFiles.size() - 1);
                boolean isDeleted = image.delete();
                if (!isDeleted && BuildConfig.DEBUG) {
                    Timber.e("failed to delete file");
                }
                mImageFiles.remove(image);
                showPreview();
                break;
            case R.id.iv_camera_bottom_done:
                finishCapture();
                break;
            case R.id.iv_camera_bottom_add:
                if (mImageFiles.size() < MAX_NUMBER_IMAGES) {
                    showPreview();
                    showImageThumbnail();
                } else {
                    Toast.makeText(this, "You can maximally take 3 photos per receipt",
                            Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void showPreview() {
        mCamera.cancelAutoFocus();
        mCamera.startPreview();
        toggleBottomVisibility();
    }

    private void showImageThumbnail() {
        final ImageView target = (ImageView) getLayoutInflater()
                .inflate(R.layout.camera_image_taken, mLinearLayoutTaken, false);
        Glide.with(this)
                .load(mImageFiles.get(mImageFiles.size() - 1))
                .asBitmap()
                .into(new BitmapImageViewTarget(target) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        super.onResourceReady(resource, glideAnimation);
                        mLinearLayoutTaken.addView(target);
                    }
                });
    }

    private void finishCapture() {
        int filesSize = mImageFiles.size();
        if (filesSize == 0) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        ArrayList<String> paths = new ArrayList<>(filesSize);
        for (File file : mImageFiles) {
            paths.add(file.getAbsolutePath());
        }

        Intent intent = new Intent();
        intent.putStringArrayListExtra(INTENT_EXTRA_PATHS, paths);
        setResult(RESULT_OK, intent);
        finish();
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
