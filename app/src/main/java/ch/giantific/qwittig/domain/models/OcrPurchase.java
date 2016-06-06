package ch.giantific.qwittig.domain.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.Map;

/**
 * Created by fabio on 02.06.16.
 */
@ParseClassName("OcrPurchase")
public class OcrPurchase extends ParseObject {

    public static final String CLASS = "OcrPurchase";
    public static final String DATA = "data";
    public static final String RECEIPT = "receipt";


    public OcrPurchase() {
        // a default constructor is required.
    }

    public Map<String, Object> getData() {
        return getMap(DATA);
    }

    public ParseFile getReceipt() {
        return getParseFile(RECEIPT);
    }
}
