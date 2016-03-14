/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.models;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.stats.StatsViewModel.StatsType;

/**
 * Represents a stats page with a title and the type number.
 */
public class StatsPage {

    private final String mTitle;
    @StatsType
    private final int mType;

    public StatsPage(@NonNull String title, @StatsType int type) {
        mTitle = title;
        mType = type;
    }

    @StatsType
    public int getType() {
        return mType;
    }

    @Override
    public String toString() {
        return mTitle;
    }
}
