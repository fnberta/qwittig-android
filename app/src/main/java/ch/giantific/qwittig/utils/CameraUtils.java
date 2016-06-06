/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Provides useful static utility methods related to the camera.
 */
public class CameraUtils {

    private CameraUtils() {
        // class cannot be instantiated
    }

    /**
     * Returns whether the device has a camera or not.
     * @param context the context to get the {@link PackageManager}
     * @return whether the device has a camera or not
     */
    public static boolean hasCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * Returns a safe instance of the {@link Camera} object.
     *
     * @return an instance of the {@link Camera} object
     * @throws Exception if the camera is not available (e.g. already in use)
     */
    public static Camera getCameraInstance() throws Exception {
        return Camera.open(0);
    }

    public static File createImageFile(Context context) throws IOException {
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        final String imageFileName = "RECEIPT_" + timeStamp;
        final File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    @Nullable
    public static Intent getCameraIntent(Context context, File imageFile) {
        final Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(context.getPackageManager()) != null) {
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            return cameraIntent;
        }

        return null;
    }
}
