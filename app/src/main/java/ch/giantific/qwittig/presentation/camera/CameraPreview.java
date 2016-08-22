/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import timber.log.Timber;

/**
 * Provides a custom camera preview that is locked to portrait orientation.
 * <p/>
 * Subclass of {@link SurfaceView}.
 */
@SuppressLint("ViewConstructor")
@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera camera;

    /**
     * Constructs a new {@link CameraPreview} and sets the holders callback to it.
     *
     * @param context the context to pass to super
     * @param camera  the camera to use for the preview
     */
    public CameraPreview(Context context, Camera camera) {
        super(context);

        this.camera = camera;
        final SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }

    public void setCamera(@NonNull Camera camera) {
        this.camera = camera;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            camera.startPreview();
        } catch (IOException e) {
            Timber.e(e, "Error setting camera preview:");
            // TODO: tell user
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // we are locked to portrait, hence the surface should not be able to be changed
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera = null;
        // TODO: do we need to do something else here?
    }
}
