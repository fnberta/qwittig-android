/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
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
 * Displays the interface where the user can edit a purchase draft. The user can either save the
 * changes again in the draft or save the purchase online to the Parse.com database.
 * <p/>
 * Subclass of {@link PurchaseEditFragment}.
 */
public class PurchaseEditDraftFragment extends PurchaseEditFragment implements
        LocalQuery.ObjectLocalFetchListener {

    public PurchaseEditDraftFragment() {
    }

    /**
     * Returns a new instance of {@link PurchaseEditDraftFragment}.
     *
     * @param draftId the id of the draft to edit
     * @return a new instance of {@link PurchaseEditDraftFragment}
     */
    @NonNull
    public static PurchaseEditDraftFragment newInstance(@NonNull String draftId) {
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
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_purchase_edit_draft, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
            public void done(@Nullable ParseException e) {
                if (e == null) {
                    setResultForSnackbar(PURCHASE_DRAFT_DELETED);
                    finishPurchase();
                }
            }
        });
    }

    @Override
    void fetchPurchase() {
        LocalQuery.queryDraft(mEditPurchaseId, this);
    }

    @Override
    public void onObjectFetched(@NonNull ParseObject object) {
        super.onObjectFetched(object);

        processOldPurchase(object);
    }

    @Nullable
    @Override
    ParseFile getOldReceiptFile() {
        byte[] receiptData = mPurchase.getReceiptData();
        if (receiptData != null) {
            return new ParseFile(Receipt.PARSE_FILE_NAME, receiptData);
        }

        return null;
    }

    @Override
    protected void setPurchase() {
        mPurchase.removeDraftId();

        super.setPurchase();
    }

    @Override
    boolean isDraft() {
        return true;
    }

    @Override
    public void onParseError(@NonNull ParseException e) {
        mPurchase.setDraftId(mEditPurchaseId);
        super.onParseError(e);
    }

    @Override
    protected void savePurchaseAsDraft() {
        replacePurchaseData();

        if (TextUtils.isEmpty(mReceiptImagePath)) {
            pinPurchaseAsDraft();
        } else {
            getReceiptDataForDraft();
        }
    }
}
