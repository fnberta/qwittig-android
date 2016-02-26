/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users.items;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines a list item in the manage users settings screen.
 */
public interface SettingsUsersBaseItem {

    @Type
    int getType();

    @IntDef({Type.HEADER, Type.INTRO, Type.NICKNAME, Type.USER})
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {
        int HEADER = 0;
        int INTRO = 1;
        int NICKNAME = 2;
        int USER = 3;
    }
}
