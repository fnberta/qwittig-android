package ch.giantific.qwittig.data.models;

import android.content.Context;
import android.graphics.Bitmap;

import com.parse.ParseFile;

import java.io.ByteArrayOutputStream;

/**
 * Created by fabio on 13.12.14.
 */
public class Receipt {

    public static final String PARSE_FILE_NAME = "receipt.jpg";
    public static final int JPEG_COMPRESSION_RATE = 80; // TODO: figure out real value
    public static final int HEIGHT = 720; // TODO: figure out real height and width
    public static final int WIDTH = 720;

    private Receipt() {
        // class cannot be instantiated;
    }
}
