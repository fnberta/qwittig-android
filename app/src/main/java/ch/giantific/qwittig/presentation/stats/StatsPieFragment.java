package ch.giantific.qwittig.presentation.stats;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import ch.giantific.qwittig.databinding.FragmentStatsPieBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.stats.di.StatsSubcomponent;

/**
 * Created by fabio on 14.08.16.
 */
public class StatsPieFragment extends BaseFragment<StatsSubcomponent, StatsContract.Presenter,
        BaseFragment.ActivityListener<StatsSubcomponent>> {

    @Inject
    StatsViewModel viewModel;
    private FragmentStatsPieBinding binding;

    public StatsPieFragment() {
        // required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatsPieBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        binding.setViewModel(viewModel);
    }

    @Override
    protected void injectDependencies(@NonNull StatsSubcomponent component) {
        component.inject(this);
    }

    @Override
    protected View getSnackbarView() {
        return binding.pcStores;
    }
}
