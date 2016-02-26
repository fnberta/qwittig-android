/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users.items;

/**
 * Provides an implementation of the {@link SettingsUsersBaseItem} for the intro text row.
 */
public class SettingsUsersIntroItem implements SettingsUsersBaseItem {

    @Override
    public int getType() {
        return Type.INTRO;
    }
}
