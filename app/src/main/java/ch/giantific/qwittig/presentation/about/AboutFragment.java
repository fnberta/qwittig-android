package ch.giantific.qwittig.presentation.about;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.FragmentAboutBinding;
import ch.giantific.qwittig.presentation.about.di.AboutComponent;
import ch.giantific.qwittig.presentation.common.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends BaseFragment<AboutComponent, AboutContract.Presenter, BaseFragment.ActivityListener<AboutComponent>>
        implements AboutContract.ViewListener {

    private FragmentAboutBinding binding;

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupRecyclerView();
        presenter.attachView(this);
    }

    private void setupRecyclerView() {
        binding.rvAbout.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvAbout.setHasFixedSize(true);
        final AboutRecyclerAdapter adapter = new AboutRecyclerAdapter(presenter);
        binding.rvAbout.setAdapter(adapter);
    }

    @Override
    protected void injectDependencies(@NonNull AboutComponent component) {
        component.inject(this);
    }

    @Override
    protected View getSnackbarView() {
        return binding.rvAbout;
    }
}
