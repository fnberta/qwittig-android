package ch.giantific.qwittig.data.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.support.v4.content.ContextCompat;

import java.io.ByteArrayOutputStream;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.constants.AppConstants;

/**
 * Created by fabio on 03.12.14.
 */
public class ImageAvatar extends Image {

    private static final int AVATAR_JPEG_COMPRESSION_RATE = 60;
    private static final int AVATAR_HEIGHT = 100;
    private static final int AVATAR_WIDTH = 100;

    public ImageAvatar(Context context, Uri uri) {
        super(context, uri, AVATAR_WIDTH, AVATAR_HEIGHT);

        mImage = cropBitmap(AVATAR_WIDTH, AVATAR_HEIGHT);
    }

    public ImageAvatar(Context context, Bitmap bitmap) {
        super(context, bitmap);
    }

    public ImageAvatar(Context context, byte[] data) {
        super(context, data);
    }

    public static Drawable getRoundedAvatar(Context context, byte[] avatarByteArray, boolean bigDefault) {
        Drawable avatar;
        if (avatarByteArray != null) {
            ImageAvatar imageAvatar = new ImageAvatar(context, avatarByteArray);
            avatar = imageAvatar.getRoundedDrawable();
        } else {
            if (bigDefault) {
                avatar = ContextCompat.getDrawable(context, R.drawable.ic_account_circle_black_80dp);
            } else {
                avatar = ContextCompat.getDrawable(context, R.drawable.ic_account_circle_black_40dp);
            }
            avatar.setAlpha(AppConstants.ICON_BLACK_ALPHA_RGB);
        }

        return avatar;
    }

    private Bitmap cropBitmap(int width, int height) {
        return ThumbnailUtils.extractThumbnail(mImage, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
    }

    public byte[] getByteArray() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mImage.compress(Bitmap.CompressFormat.JPEG, AVATAR_JPEG_COMPRESSION_RATE, outputStream);
        return outputStream.toByteArray();
    }
}
