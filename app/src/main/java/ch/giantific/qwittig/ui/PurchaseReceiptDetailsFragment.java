package ch.giantific.qwittig.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.parse.ParseFile;
import com.parse.ParseObject;

import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Purchase;


/**
 * A simple {@link android.app.Fragment} subclass.
 */
public class PurchaseReceiptDetailsFragment extends PurchaseReceiptBaseFragment implements
        LocalQuery.ObjectLocalFetchListener {

    private FragmentInteractionListener mListener;
    private static final String BUNDLE_PURCHASE_ID = "purchase_id";
    private String mPurchaseId;

    public PurchaseReceiptDetailsFragment() {
    }

    public static PurchaseReceiptDetailsFragment newInstance(String purchaseId) {
        PurchaseReceiptDetailsFragment fragment = new PurchaseReceiptDetailsFragment();
        Bundle args = new Bundle();
        args.putString(BUNDLE_PURCHASE_ID, purchaseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mPurchaseId = getArguments().getString(BUNDLE_PURCHASE_ID);
        }

        mListener.updateActionBarMenu(true, false, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LocalQuery.fetchObjectFromId(this, Purchase.CLASS, mPurchaseId);
    }

    @Override
    public void onObjectFetched(ParseObject object) {
        Purchase purchase = (Purchase) object;
        ParseFile receiptFile = purchase.getReceiptParseFile();
        setReceiptImage(receiptFile);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentInteractionListener {
        void updateActionBarMenu(boolean showEditOptions, boolean hasReceiptFile,
                                 boolean hasForeignCurrency);
    }
}
