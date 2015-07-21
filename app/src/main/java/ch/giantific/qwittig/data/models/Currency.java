package ch.giantific.qwittig.data.models;

/**
 * Created by fabio on 25.01.15.
 */
public class Currency {

    private String mName;
    private String mCode;

    public String getCode() {
        return mCode;
    }

    public Currency(String name, String code) {
        mName = name;
        mCode = code;
    }

    @Override
    public String toString() {
        return mName + " (" + mCode + ")";
    }
}
