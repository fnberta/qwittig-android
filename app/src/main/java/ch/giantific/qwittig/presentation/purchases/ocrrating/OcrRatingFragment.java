/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.ocrrating;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.FragmentOrcRatingBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.purchases.ocrrating.di.OcrRatingComponent;

/**
 * Shows the receipt image taken by the user when adding a new purchase.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class OcrRatingFragment extends BaseFragment<OcrRatingComponent, OcrRatingContract.Presenter, BaseFragment.ActivityListener<OcrRatingComponent>> {

    private FragmentOrcRatingBinding binding;

    public OcrRatingFragment() {
        // required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrcRatingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        binding.setPresenter(presenter);
    }

    @Override
    protected void injectDependencies(@NonNull OcrRatingComponent component) {
        component.inject(this);
    }

    @Override
    protected View getSnackbarView() {
        return binding.svOcrRatingMain;
    }
}
