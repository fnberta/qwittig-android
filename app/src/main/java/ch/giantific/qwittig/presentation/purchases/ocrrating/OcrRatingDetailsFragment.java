/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.ocrrating;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.FragmentOrcRatingDetailsBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.purchases.ocrrating.di.OcrRatingComponent;

/**
 * Shows the receipt image taken by the user when adding a new purchase.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class OcrRatingDetailsFragment extends BaseFragment<OcrRatingComponent, OcrRatingViewModel, BaseFragment.ActivityListener<OcrRatingComponent>> {

    private FragmentOrcRatingDetailsBinding mBinding;

    public OcrRatingDetailsFragment() {
        // required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentOrcRatingDetailsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mBinding.setViewModel(mViewModel);
    }

    @Override
    protected void injectDependencies(@NonNull OcrRatingComponent component) {
        component.inject(this);
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.svOcrRatingDetailsMain;
    }
}