package ch.giantific.qwittig.data.models;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.constants.AppConstants;
import ch.giantific.qwittig.utils.Utils;

/**
 * Created by fabio on 03.12.14.
 */
public class Avatar {

    public static final int JPEG_COMPRESSION_RATE = 60;
    public static final int HEIGHT = 250;
    public static final int WIDTH = 250;

    private Avatar() {
        // class cannot be instantiated
    }

    public static Drawable getFallbackDrawable(Context context, boolean getBigSize,
                                               boolean withRipple) {
        int drawableId = getBigSize ? R.drawable.ic_account_circle_black_80dp :
                R.drawable.ic_account_circle_black_40dp;
        Drawable avatar = ContextCompat.getDrawable(context, drawableId);
        avatar.setAlpha(AppConstants.ICON_BLACK_ALPHA_RGB);

        return withRipple && Utils.isRunningLollipopAndHigher() ?
                createRippleDrawable(context, avatar) : avatar;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    private static RippleDrawable createRippleDrawable(Context context, Drawable drawable) {
        int[] attrs = new int[]{R.attr.colorControlHighlight};
        TypedArray typedArray = context.obtainStyledAttributes(attrs);
        int rippleColor = typedArray.getResourceId(0, 0);
        typedArray.recycle();

        return new RippleDrawable(ColorStateList.valueOf(rippleColor), drawable, null);
    }

    public static Drawable getRoundedDrawable(Context context, Bitmap bitmap,
                                              boolean withRipple) {
        RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(
                context.getResources(), bitmap);
        roundedDrawable.setCornerRadius(Math.min(roundedDrawable.getMinimumWidth(),
                roundedDrawable.getMinimumHeight()));
        roundedDrawable.setAntiAlias(true);

        return withRipple && Utils.isRunningLollipopAndHigher() ?
                createRippleDrawable(context, roundedDrawable) : roundedDrawable;
    }
}
