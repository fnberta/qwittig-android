/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.ocr.models;

import com.google.gson.annotations.SerializedName;

/**
 * Represents an item of a purchase with a name and a price.
 */
public class ItemRest {

    @SerializedName("name")
    private String mName;
    @SerializedName("price")
    private String mPrice;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPrice() {
        return mPrice;
    }

    public void setPrice(String price) {
        mPrice = price;
    }
}