package ch.giantific.qwittig.data.rest.dynamiclinks;

/**
 * Created by fabio on 26.10.16.
 */

public class LinkInfo {

    private final String dynamicLinkDomain;
    private final String link;
    private final AndroidInfo androidInfo;
    private final IosInfo iosInfo;

    public LinkInfo(String dynamicLinkDomain, String link, AndroidInfo androidInfo, IosInfo iosInfo) {
        this.dynamicLinkDomain = dynamicLinkDomain;
        this.link = link;
        this.androidInfo = androidInfo;
        this.iosInfo = iosInfo;
    }

    public String getDynamicLinkDomain() {
        return dynamicLinkDomain;
    }

    public String getLink() {
        return link;
    }

    public AndroidInfo getAndroidInfo() {
        return androidInfo;
    }

    public IosInfo getIosInfo() {
        return iosInfo;
    }
}
