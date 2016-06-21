/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.addgroup;

import android.support.annotation.NonNull;

/**
 * Represents a simple currency with a name and a currency code.
 */
public class Currency {

    private final String mName;
    private final String mCode;

    public String getCode() {
        return mCode;
    }

    public Currency(@NonNull String name, @NonNull String code) {
        mName = name;
        mCode = code;
    }

    @NonNull
    @Override
    public String toString() {
        return mName + " (" + mCode + ")";
    }
}
