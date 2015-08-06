package ch.giantific.qwittig.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.Receipt;
import ch.giantific.qwittig.data.parse.LocalQuery;

/**
 * A placeholder fragment containing a simple view.
 */
public class PurchaseEditDraftFragment extends PurchaseEditFragment implements
        LocalQuery.ObjectLocalFetchListener {

    public PurchaseEditDraftFragment() {
    }

    public static PurchaseEditDraftFragment newInstance(String draftId) {
        PurchaseEditDraftFragment fragment = new PurchaseEditDraftFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_EDIT_PURCHASE_ID, draftId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_purchase_edit_draft, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_purchase_edit_save_changes_draft:
                savePurchase(true);
                return true;
            case R.id.action_purchase_edit_draft_delete:
                deleteDraft();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteDraft() {
        mPurchase.unpinInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    mListener.setResultForSnackbar(PurchaseBaseActivity.PURCHASE_DRAFT_DELETED);
                    mListener.finishPurchase();
                }
            }
        });
    }

    @Override
    void fetchPurchase() {
        LocalQuery.queryDraft(this, mEditPurchaseId);
    }

    @Override
    public void onObjectFetched(ParseObject object) {
        super.onObjectFetched(object);

        processOldPurchase(object);
    }

    @Override
    void checkForReceiptFile() {
        byte[] receiptData = mPurchase.getReceiptData();
        if (receiptData != null && mListener.getReceiptParseFile() == null) {
            mReceiptFileOld = new ParseFile(Receipt.PARSE_FILE_NAME, receiptData);
            mListener.setReceiptParseFile(mReceiptFileOld);
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    protected void setPurchase() {
        mPurchase.removeDraftId();

        super.setPurchase();
    }

    @Override
    void checkIfReceiptNull() {
        if (mReceiptFileNew != null) {
            saveReceiptFile();
        } else {
            savePurchaseInParse();
        }
    }

    @Override
    void onReceiptFileSaved() {
        mPurchase.removeReceiptData();
        super.onReceiptFileSaved();
    }

    @Override
    void onParseError(ParseException e) {
        mPurchase.setDraftId(mEditPurchaseId);
        super.onParseError(e);
    }

    @Override
    protected void onSaveSucceeded() {
        mPurchase.unpinInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    return;
                }

                pinPurchase(false);
            }
        });
    }

    @Override
    protected void savePurchaseAsDraft() {
        replacePurchaseData();

        pinPurchaseAsDraft();
    }
}
