/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.models;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.utils.DateUtils;

/**
 * Represents a standard month with a short name and a number (1-12).
 */
public class Month {

    private String mNameShort;
    private int mNumber;

    public int getNumber() {
        return mNumber;
    }

    public Month(@NonNull String shortName) {
        mNumber = 0;
        mNameShort = shortName;
    }

    public Month(@IntRange(from=1, to=12) int number) {
        mNumber = number;
        mNameShort = DateUtils.getMonthNameShort(number);
    }

    @Override
    public String toString() {
        return mNameShort;
    }
}
