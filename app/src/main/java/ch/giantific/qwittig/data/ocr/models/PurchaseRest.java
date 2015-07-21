package ch.giantific.qwittig.data.ocr.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class PurchaseRest {

    @SerializedName("store")
    private String mStore;
    @SerializedName("total")
    private String mTotal;
    @SerializedName("date")
    private String mDate;
    @SerializedName("items")
    private List<ItemRest> mItems = new ArrayList<>();

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

    public List<ItemRest> getItems() {
        return mItems;
    }

    public void setItems(List<ItemRest> items) {
        mItems = items;
    }
}