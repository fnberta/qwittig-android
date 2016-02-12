/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback;

/**
 * Represents a help item with a title and an icon, both referencing android resources.
 */
public class HelpFeedbackItem {

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

    public HelpFeedbackItem(int title, int icon) {
        mTitle = title;
        mIcon = icon;
    }

    public HelpFeedbackItem(int title) {
        mTitle = title;
    }
}
