package ch.giantific.qwittig.data.ocr.models;

import com.google.gson.annotations.SerializedName;

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