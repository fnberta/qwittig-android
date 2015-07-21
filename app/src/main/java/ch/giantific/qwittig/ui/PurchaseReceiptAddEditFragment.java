package ch.giantific.qwittig.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.parse.ParseFile;

import ch.giantific.qwittig.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class PurchaseReceiptAddEditFragment extends PurchaseReceiptBaseFragment {

    private FragmentInteractionListener mListener;

    public PurchaseReceiptAddEditFragment() {
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

        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ParseFile receiptFile = mListener.getReceiptParseFile();
        setReceiptImage(receiptFile);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_purchase_edit_receipt, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_purchase_edit_receipt_edit:
                mListener.captureImage();
                return true;
            case R.id.action_purchase_edit_receipt_delete:
                mListener.deleteReceipt();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentInteractionListener {
        ParseFile getReceiptParseFile();
        void deleteReceipt();
        void captureImage();
    }
}
