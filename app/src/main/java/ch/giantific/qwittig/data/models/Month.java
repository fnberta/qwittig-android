package ch.giantific.qwittig.data.models;

import ch.giantific.qwittig.utils.DateUtils;

/**
 * Created by fabio on 25.01.15.
 */
public class Month {

    private String mNameShort;
    private int mNumber;

    public int getNumber() {
        return mNumber;
    }

    public Month(int number) {
        mNumber = number;
        mNameShort = DateUtils.getMonthNameShort(number);
    }

    @Override
    public String toString() {
        return mNameShort;
    }
}
