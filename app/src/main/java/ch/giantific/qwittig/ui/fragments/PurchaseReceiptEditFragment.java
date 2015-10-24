package ch.giantific.qwittig.ui.fragments;

import android.os.Bundle;

import com.parse.ParseObject;

import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Purchase;


/**
 * A simple {@link android.app.Fragment} subclass.
 */
public class PurchaseReceiptEditFragment extends PurchaseReceiptAddFragment {

    private static final String BUNDLE_PURCHASE_ID = "BUNDLE_PURCHASE_ID";
    private static final String BUNDLE_IS_DRAFT = "BUNDLE_IS_DRAFT";
    private String mPurchaseId;
    private boolean mIsDraft;

    public PurchaseReceiptEditFragment() {
    }

    public static PurchaseReceiptEditFragment newInstance(String purchaseId, boolean isDraft) {
        PurchaseReceiptEditFragment fragment = new PurchaseReceiptEditFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_PURCHASE_ID, purchaseId);
        args.putBoolean(BUNDLE_IS_DRAFT, isDraft);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    void getDataFromBundle() {
        Bundle args = getArguments();
        if (args != null) {
            mPurchaseId = args.getString(BUNDLE_PURCHASE_ID);
            mIsDraft = args.getBoolean(BUNDLE_IS_DRAFT);
        }
    }

    @Override
    void setData() {
        if (mIsDraft) {
            LocalQuery.queryDraft(mPurchaseId, new LocalQuery.ObjectLocalFetchListener() {
                @Override
                public void onObjectFetched(ParseObject object) {
                    Purchase purchase = (Purchase) object;
                    setImage(purchase.getReceiptData());
                }
            });
        } else {
            LocalQuery.fetchObjectFromId(Purchase.CLASS, mPurchaseId, new LocalQuery.ObjectLocalFetchListener() {
                @Override
                public void onObjectFetched(ParseObject object) {
                    Purchase purchase = (Purchase) object;
                    setReceiptImage(purchase.getReceiptParseFile());
                }
            });
        }
    }
}
