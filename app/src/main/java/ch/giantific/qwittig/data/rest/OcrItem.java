/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.data.rest;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Represents an item of a purchase with a name and a price.
 */
public class OcrItem {

    @SerializedName("name")
    private String mName;
    @SerializedName("price")
    private double mPrice;

    public String getName() {
        return mName;
    }

    public void setName(@NonNull String name) {
        mName = name;
    }

    public double getPrice() {
        return mPrice;
    }

    public void setPrice(double price) {
        mPrice = price;
    }
}