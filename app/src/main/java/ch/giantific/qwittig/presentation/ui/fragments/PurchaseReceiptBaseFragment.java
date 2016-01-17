/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;

import ch.giantific.qwittig.ParseErrorHandler;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.ParsePurchaseRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;

/**
 * Provides an abstract base class for screens that display purchase receipt images.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public abstract class PurchaseReceiptBaseFragment extends BaseFragment {

    PurchaseRepository mPurchaseRepo;
    ImageView mImageViewReceipt;
    private ContentLoadingProgressBar mProgressBar;

    public PurchaseReceiptBaseFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPurchaseRepo = new ParsePurchaseRepository(getActivity());
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
        mProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.pb_base);
    }

    void setReceiptImage(ParseFile receiptFile) {
        if (receiptFile != null) {
            // TODO: does not handle configuration changes
            receiptFile.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, ParseException e) {
                    if (e != null) {
                        mProgressBar.hide();
                        Snackbar.make(mImageViewReceipt,
                                ParseErrorHandler.handleParseError(getActivity(), e),
                                Snackbar.LENGTH_LONG);
                        return;
                    }

                    setImage(bytes);
                }
            });
        }
    }

    void setImage(byte[] receiptBytes) {
        mProgressBar.hide();
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
