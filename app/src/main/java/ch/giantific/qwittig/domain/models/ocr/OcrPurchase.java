/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.models.ocr;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a purchase with a store name, a total price, a date and the list of items included in
 * the purchase.
 */
public class OcrPurchase {

    @SerializedName("store")
    private String mStore;
    @SerializedName("total")
    private String mTotal;
    @SerializedName("date")
    private String mDate;
    @SerializedName("items")
    private List<OcrItem> mItems = new ArrayList<>();

    public String getStore() {
        return mStore;
    }

    public void setStore(String store) {
        mStore = store;
    }

    public String getTotal() {
        return mTotal;
    }

    public void setTotal(String total) {
        mTotal = total;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public List<OcrItem> getItems() {
        return mItems;
    }

    public void setItems(List<OcrItem> items) {
        mItems = items;
    }
}