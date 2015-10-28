/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

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
 * Provides an abstract base class for screens that display purchase receipt images.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public abstract class PurchaseReceiptBaseFragment extends BaseFragment {

    ImageView mImageViewReceipt;
    private ProgressBar mProgressBar;

    public PurchaseReceiptBaseFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchase_show_receipt, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mImageViewReceipt = (ImageView) view.findViewById(R.id.iv_receipt);
        mProgressBar = (ProgressBar) view.findViewById(R.id.pb_receipt);
    }

    void setReceiptImage(ParseFile receiptFile) {
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

    /**
     * Sets the image to the {@link ImageView}.
     *
     * @param receiptPath the path to the receipt image
     */
    public void setImage(String receiptPath) {
        Glide.with(this)
                .load(receiptPath)
                .into(mImageViewReceipt);
    }
}
