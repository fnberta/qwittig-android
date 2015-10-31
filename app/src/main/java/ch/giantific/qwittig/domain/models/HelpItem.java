/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.models;

/**
 * Represents a help item with a title and an icon, both referencing android resources.
 */
public class HelpItem {

    private int mTitle;
    private int mIcon;

    public int getTitle() {
        return mTitle;
    }

    public void setTitle(int title) {
        mTitle = title;
    }

    public int getIcon() {
        return mIcon;
    }

    public void setIcon(int icon) {
        mIcon = icon;
    }

    public HelpItem(int title, int icon) {
        mTitle = title;
        mIcon = icon;
    }

    public HelpItem(int title) {
        mTitle = title;
    }
}
