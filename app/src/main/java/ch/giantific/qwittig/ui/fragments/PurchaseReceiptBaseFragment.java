package ch.giantific.qwittig.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.utils.ParseErrorHandler;


/**
 * A simple {@link Fragment} subclass.
 */
public abstract class PurchaseReceiptBaseFragment extends BaseFragment {

    ImageView mImageViewReceipt;
    private ProgressBar mProgressBar;

    public PurchaseReceiptBaseFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_show_receipt, container, false);

        mImageViewReceipt = (ImageView) rootView.findViewById(R.id.iv_receipt);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.pb_receipt);

        return rootView;
    }

    public void setReceiptImage(ParseFile receiptFile) {
        if (receiptFile != null) {
            receiptFile.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, ParseException e) {
                    mProgressBar.setVisibility(View.GONE);

                    if (e != null) {
                        ParseErrorHandler.handleParseError(getActivity(), e);
                        return;
                    }

                    setImage(bytes);
                }
            });
        }
    }

    void setImage(byte[] receiptBytes) {
        Glide.with(this)
                .load(receiptBytes)
                .into(mImageViewReceipt);
    }

    void setReceiptImage(String receiptPath) {
        if (!TextUtils.isEmpty(receiptPath)) {
            setImage(receiptPath);
        }
    }

    public void setImage(String receiptPath) {
        Glide.with(this)
                .load(receiptPath)
                .into(mImageViewReceipt);
    }
}
