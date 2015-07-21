package ch.giantific.qwittig.data.models;

/**
 * Created by fabio on 17.02.15.
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
