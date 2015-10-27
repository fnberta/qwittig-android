/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.models;

/**
 * Provides constants for the handling of user taken receipt images.
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
