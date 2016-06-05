/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ch.giantific.qwittig.R;

/**
 * Provides useful methods to properly format a user's avatar image.
 */
public class AvatarUtils {

    private AvatarUtils() {
        // class cannot be instantiated
    }

    /**
     * Returns a rectangle fallback avatar drawable.
     *
     * @param context    the context to use to get the fallback drawable
     * @param withRipple whether to use a ripple effect on click
     * @return a rectangle fallback avatar drawable
     */
    @NonNull
    public static Drawable getFallbackDrawableRect(@NonNull Context context, boolean withRipple) {
        final Drawable avatar = ContextCompat.getDrawable(context, R.drawable.ic_account_box_black_144dp);

        return withRipple && Utils.isRunningLollipopAndHigher() ?
                createRippleDrawable(context, avatar) : avatar;
    }

    /**
     * Returns a rounded fallback avatar drawable.
     *
     * @param context    the context to use to get the fallback drawable
     * @param getBigSize whether to use 80dp drawable or 40dp
     * @param withRipple whether to use a ripple effect on click
     * @return a round fallback avatar drawable
     */
    @NonNull
    public static Drawable getFallbackDrawable(@NonNull Context context, boolean getBigSize,
                                               boolean withRipple) {
        final int drawableId = getBigSize ? R.drawable.ic_account_circle_black_80dp :
                R.drawable.ic_account_circle_black_40dp;
        final Drawable avatar = ContextCompat.getDrawable(context, drawableId);

        return withRipple && Utils.isRunningLollipopAndHigher() ?
                createRippleDrawable(context, avatar) : avatar;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    private static RippleDrawable createRippleDrawable(@NonNull Context context,
                                                       @NonNull Drawable drawable) {
        final int[] attrs = new int[]{R.attr.colorControlHighlight};
        final TypedArray typedArray = context.obtainStyledAttributes(attrs);
        final int rippleColor = typedArray.getResourceId(0, 0);
        typedArray.recycle();

        return new RippleDrawable(ColorStateList.valueOf(rippleColor), drawable, null);
    }

    /**
     * Returns a rounded drawable version of the inputted bitmap
     *
     * @param context    the context to use to get the resources
     * @param bitmap     the bitmap of which a rounded drawable should be created
     * @param withRipple whether to use a ripple effect on click
     * @return a rounded drawable
     */
    @NonNull
    public static Drawable getRoundedDrawable(@NonNull Context context, @NonNull Bitmap bitmap,
                                              boolean withRipple) {
        final RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(
                context.getResources(), bitmap);
        roundedDrawable.setCornerRadius(Math.min(roundedDrawable.getMinimumWidth(),
                roundedDrawable.getMinimumHeight()));
        roundedDrawable.setAntiAlias(true);

        return withRipple && Utils.isRunningLollipopAndHigher() ?
                createRippleDrawable(context, roundedDrawable) : roundedDrawable;
    }

    public static String copyAvatarLocal(@NonNull Context context,
                                         @NonNull Uri avatarUri) throws IOException {
        final String[] projection = {MediaStore.Images.Media.DATA};
        final Cursor cursor = context.getContentResolver().query(avatarUri, projection, null, null, null);
        if (cursor == null) {
            return "";
        }

        final String localAvatarPath;
        try {
            int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
            cursor.moveToFirst();
            localAvatarPath = cursor.getString(columnIndex);
        } finally {
            cursor.close();
        }

        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        final String imageFileName = "JPEG_" + timeStamp + "_" + "avatar";
        final File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        final File avatar = new File(storageDir, imageFileName);
        FileUtils.copyFile(new File(localAvatarPath), avatar);
        return avatar.getAbsolutePath();
    }
}
