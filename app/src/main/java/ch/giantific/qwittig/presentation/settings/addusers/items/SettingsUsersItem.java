/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addusers.items;

import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by fabio on 07.02.16.
 */
public interface SettingsUsersItem extends Parcelable {

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
