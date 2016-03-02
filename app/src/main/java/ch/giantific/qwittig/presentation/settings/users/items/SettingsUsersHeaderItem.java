/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users.items;

import android.annotation.SuppressLint;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.viewmodels.HeaderRowViewModelBaseImpl;

/**
 * Provides an implementation of {@link SettingsUsersBaseItem} for a header row.
 * <p/>
 * Subclass of {@link HeaderRowViewModelBaseImpl}.
 */
@SuppressLint("ParcelCreator")
public class SettingsUsersHeaderItem extends HeaderRowViewModelBaseImpl implements SettingsUsersBaseItem {

    public SettingsUsersHeaderItem(@StringRes int header) {
        super(header);
    }

    @Override
    public int getType() {
        return Type.HEADER;
    }
}
