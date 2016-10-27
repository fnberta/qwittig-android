package ch.giantific.qwittig.data.rest.dynamiclinks;

/**
 * Created by fabio on 26.10.16.
 */

public class IosInfo {

    private final String iosBundleId;

    public IosInfo(String iosBundleId) {
        this.iosBundleId = iosBundleId;
    }

    public String getIosBundleId() {
        return iosBundleId;
    }
}
