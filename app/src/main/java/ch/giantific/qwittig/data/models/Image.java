package ch.giantific.qwittig.data.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by fabio on 13.12.14.
 */
public class Image {

    private Context mContext;
    Bitmap mImage;

    public Image(Context context) {
        mContext = context;
    }

    public Image(Context context, Uri uri, int width, int height) {
        mContext = context;
        try {
            mImage = decodeSampledBitmapFromUri(mContext, uri, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Image(Context context, String photoPath, int width, int height) {
        mContext = context;
        mImage = decodeSampledBitmapFromFile(photoPath, width, height);
    }

    public Image(Context context, Bitmap bitmap) {
        mContext = context;
        mImage = bitmap;
    }

    public Image(Context context, byte[] data) {
        mContext = context;
        mImage = BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public Bitmap getBitmap() {
        return mImage;
    }

    public Drawable getRoundedDrawable() {
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(
                mContext.getResources(), mImage);
        roundedBitmapDrawable.setCornerRadius(Math.min(roundedBitmapDrawable.getMinimumWidth(),
                roundedBitmapDrawable.getMinimumHeight()));

        return roundedBitmapDrawable;
    }

    Bitmap decodeSampledBitmapFromUri(Context context, Uri uri, int reqWidth, int reqHeight) throws IOException {
        // Open stream from URI
        InputStream imageStream = context.getContentResolver().openInputStream(uri);

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(imageStream, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Close and reopen stream, otherwise decodeStream will return null
        imageStream.close();
        imageStream = context.getContentResolver().openInputStream(uri);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(imageStream, null, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight &&
                    (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private Bitmap decodeSampledBitmapFromFile(String photoPath, int reqWidth,
                                               int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(photoPath, options);
    }
}
