package ch.giantific.qwittig.data.rest;

/**
 * Represents the results from a Google shorten URL call.
 */
public class UrlShortenerResult {

    private final String kind;
    private final String id;
    private final String longUrl;

    public UrlShortenerResult(String kind, String id, String longUrl) {
        this.kind = kind;
        this.id = id;
        this.longUrl = longUrl;
    }

    public String getKind() {
        return kind;
    }

    public String getId() {
        return id;
    }

    public String getLongUrl() {
        return longUrl;
    }
}
