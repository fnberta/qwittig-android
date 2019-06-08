package ch.giantific.qwittig.data.rest.dynamiclinks;

/**
 * Created by fabio on 26.10.16.
 */

public class LinkRequest {

    private final LinkInfo dynamicLinkInfo;

    public LinkRequest(LinkInfo dynamicLinkInfo) {
        this.dynamicLinkInfo = dynamicLinkInfo;
    }

    public LinkInfo getDynamicLinkInfo() {
        return dynamicLinkInfo;
    }
}
