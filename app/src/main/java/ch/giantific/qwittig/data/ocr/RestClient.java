package ch.giantific.qwittig.data.ocr;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import ch.giantific.qwittig.data.ocr.models.PurchaseRest;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * Created by fabio on 01.02.15.
 */
public class RestClient {

    private static final String BASE_URL = "http://192.168.0.150:3000";
    private static final long READ_TIMEOUT = 300000;
    private static final RestAdapter REST_ADAPTER = new RestAdapter.Builder()
            .setEndpoint(BASE_URL)
            .setClient(new OkClient(generateOkHttp()))
            .build();
    private static final ReceiptOcr RECEIPT_OCR_SERVICE = REST_ADAPTER.create(ReceiptOcr.class);

    private RestClient() {
    }

    public static ReceiptOcr getService() {
        return RECEIPT_OCR_SERVICE;
    }

    private static OkHttpClient generateOkHttp() {
        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);
        return client;
    }

    public interface ReceiptOcr {
        @Multipart
        @POST("/api/receipt")
        void uploadReceipt(@Part("sessionToken") TypedString sessionToken,
                           @Part("receipt") TypedFile receipt,
                           Callback<PurchaseRest> callback);
    }
}
