package ch.giantific.qwittig.data.models;

import android.content.Context;
import android.graphics.Bitmap;

import com.parse.ParseFile;

import java.io.ByteArrayOutputStream;

/**
 * Created by fabio on 13.12.14.
 */
public class ImageReceipt extends Image {

    public static final String RECEIPT_PARSE_FILE_NAME = "receipt.jpg";
    private static final int RECEIPT_JPEG_COMPRESSION_RATE = 80; // TODO: figure out real value
    private static final int RECEIPT_HEIGHT = 720; // TODO: figure out real height and width
    private static final int RECEIPT_WIDTH = 720;

    public ImageReceipt(Context context, String photoPath) {
        super(context, photoPath, RECEIPT_WIDTH, RECEIPT_HEIGHT);
    }

    public ImageReceipt(Context context, byte[] data) {
        super(context, data);
    }

    public ParseFile getParseFile() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mImage.compress(Bitmap.CompressFormat.JPEG, RECEIPT_JPEG_COMPRESSION_RATE, outputStream);
        byte[] imgArray = outputStream.toByteArray();
        return new ParseFile(RECEIPT_PARSE_FILE_NAME, imgArray);
    }
}
