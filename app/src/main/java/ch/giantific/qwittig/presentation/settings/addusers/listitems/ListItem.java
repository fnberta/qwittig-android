/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addusers.listitems;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by fabio on 07.02.16.
 */
public interface ListItem {

    @Type int getType();

    @IntDef({Type.INTRO, Type.NICKNAME, Type.USER})
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {
        int INTRO = 0;
        int NICKNAME = 1;
        int USER = 2;
    }
}
