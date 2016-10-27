package ch.giantific.qwittig.data.rest.dynamiclinks;

/**
 * Created by fabio on 26.10.16.
 */

public class LinkResult {

    private final String shortLink;
    private final String previewLink;

    public LinkResult(String shortLink, String previewLink) {
        this.shortLink = shortLink;
        this.previewLink = previewLink;
    }

    public String getShortLink() {
        return shortLink;
    }

    public String getPreviewLink() {
        return previewLink;
    }
}
