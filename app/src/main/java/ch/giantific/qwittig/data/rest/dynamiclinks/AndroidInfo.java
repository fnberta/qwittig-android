package ch.giantific.qwittig.data.rest.dynamiclinks;

/**
 * Created by fabio on 26.10.16.
 */

public class AndroidInfo {

    private final String androidPackageName;

    public AndroidInfo(String androidPackageName) {
        this.androidPackageName = androidPackageName;
    }

    public String getAndroidPackageName() {
        return androidPackageName;
    }
}

