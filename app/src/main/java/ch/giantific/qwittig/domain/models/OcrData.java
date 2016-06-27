package ch.giantific.qwittig.domain.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.Map;

/**
 * Created by fabio on 02.06.16.
 */
@ParseClassName("OcrData")
public class OcrData extends ParseObject {

    public static final String CLASS = "OcrData";
    public static final String DATA = "data";
    public static final String RECEIPT = "receipt";


    public OcrData() {
        // a default constructor is required.
    }

    public Map<String, Object> getData() {
        return getMap(DATA);
    }

    public ParseFile getReceipt() {
        return getParseFile(RECEIPT);
    }
}
