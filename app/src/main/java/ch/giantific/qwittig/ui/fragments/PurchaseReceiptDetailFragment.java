package ch.giantific.qwittig.ui.fragments;

import android.os.Bundle;
import android.view.View;

import com.parse.ParseFile;
import com.parse.ParseObject;

import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Purchase;


/**
 * A simple {@link android.app.Fragment} subclass.
 */
public class PurchaseReceiptDetailFragment extends PurchaseReceiptBaseFragment implements
        LocalQuery.ObjectLocalFetchListener {

    static final String BUNDLE_PURCHASE_ID = "BUNDLE_PURCHASE_ID";
    private String mPurchaseId;

    public PurchaseReceiptDetailFragment() {
    }

    public static PurchaseReceiptDetailFragment newInstance(String purchaseId) {
        PurchaseReceiptDetailFragment fragment = new PurchaseReceiptDetailFragment();
        Bundle args = new Bundle();
        args.putString(BUNDLE_PURCHASE_ID, purchaseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mPurchaseId = args.getString(BUNDLE_PURCHASE_ID);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LocalQuery.fetchObjectFromId(Purchase.CLASS, mPurchaseId, this);
    }

    @Override
    public void onObjectFetched(ParseObject object) {
        Purchase purchase = (Purchase) object;
        ParseFile receiptFile = purchase.getReceiptParseFile();
        setReceiptImage(receiptFile);
    }
}
