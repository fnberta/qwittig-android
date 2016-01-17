/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Provides a custom camera preview that is locked to portrait orientation.
 * <p/>
 * Subclass of {@link SurfaceView}.
 */
@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String LOG_TAG = CameraPreview.class.getSimpleName();
    private Camera mCamera;

    /**
     * Constructs a new {@link CameraPreview} and sets the holders callback to it.
     *
     * @param context the context to pass to super
     * @param camera  the camera to use for the preview
     */
    public CameraPreview(Context context, Camera camera) {
        super(context);

        mCamera = camera;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error setting camera preview: " + e.getMessage());
            // TODO: tell user
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // we are locked to portrait, hence the surface should not able to be changed
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera = null;
        // TODO: do we need to do something else here?
    }
}
