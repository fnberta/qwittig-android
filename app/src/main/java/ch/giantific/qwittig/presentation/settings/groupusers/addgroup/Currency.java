/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.addgroup;

import android.support.annotation.NonNull;

/**
 * Represents a simple currency with a name and a currency code.
 */
public class Currency {

    private final String name;
    private final String code;

    public Currency(@NonNull String name, @NonNull String code) {
        this.name = name;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @NonNull
    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
