package ch.giantific.qwittig.presentation.about;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.FragmentAboutBinding;
import ch.giantific.qwittig.presentation.about.di.AboutComponent;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends BaseRecyclerViewFragment<AboutComponent, AboutViewModel, BaseFragment.ActivityListener<AboutComponent>>
        implements AboutViewModel.ViewListener {

    private FragmentAboutBinding mBinding;

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentAboutBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.attachView(this);
    }

    @Override
    protected void injectDependencies(@NonNull AboutComponent component) {
        component.inject(this);
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.rvAbout;
    }

    @Override
    protected BaseRecyclerAdapter getRecyclerAdapter() {
        return new AboutRecyclerAdapter(mViewModel);
    }
}
