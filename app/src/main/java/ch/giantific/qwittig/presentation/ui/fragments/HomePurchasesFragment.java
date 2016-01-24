/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mugen.Mugen;
import com.mugen.MugenCallbacks;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentHomePurchasesBinding;
import ch.giantific.qwittig.di.components.DaggerHomeComponent;
import ch.giantific.qwittig.di.modules.HomeViewModelModule;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.presentation.ui.activities.BaseActivity;
import ch.giantific.qwittig.presentation.ui.activities.HomeActivity;
import ch.giantific.qwittig.presentation.ui.activities.PurchaseDetailsActivity;
import ch.giantific.qwittig.presentation.ui.adapters.PurchasesRecyclerAdapter;
import ch.giantific.qwittig.presentation.viewmodels.HomePurchasesViewModel;
import ch.giantific.qwittig.presentation.workerfragments.query.PurchasesQueryMoreWorker;
import ch.giantific.qwittig.presentation.workerfragments.query.PurchasesUpdateWorker;
import ch.giantific.qwittig.utils.WorkerUtils;

/**
 * Displays recent purchases in a {@link RecyclerView} list.
 * <p/>
 * Subclass of {@link BaseRecyclerViewOnlineFragment}.
 */
public class HomePurchasesFragment extends BaseRecyclerViewOnlineFragment<HomePurchasesViewModel, HomePurchasesFragment.ActivityListener>
        implements HomePurchasesViewModel.ViewListener {

    public static final String INTENT_PURCHASE_ID = "INTENT_PURCHASE_ID";
    private static final String LOG_TAG = HomePurchasesFragment.class.getSimpleName();
    private FragmentHomePurchasesBinding mBinding;

    public HomePurchasesFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerHomeComponent.builder()
                .homeViewModelModule(new HomeViewModelModule(savedInstanceState))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentHomePurchasesBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Mugen.with(mRecyclerView, new MugenCallbacks() {
            @Override
            public void onLoadMore() {
                mViewModel.onLoadMore();
            }

            @Override
            public boolean isLoading() {
                return mViewModel.isLoadingMore();
            }

            @Override
            public boolean hasLoadedAllItems() {
                return false;
            }
        }).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case HomeActivity.INTENT_REQUEST_PURCHASE_DETAILS:
                switch (resultCode) {
                    case PurchaseDetailsActivity.RESULT_PURCHASE_DELETED:
                        showMessage(R.string.toast_purchase_deleted);
                        break;
                    case PurchaseDetailsActivity.RESULT_GROUP_CHANGED:
                        mActivity.updateNavDrawerSelectedGroup();
                        break;
                }
                break;
        }
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.srlRv.rvBase;
    }

    @Override
    protected RecyclerView.Adapter getRecyclerAdapter() {
        return new PurchasesRecyclerAdapter(mViewModel);
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setPurchasesViewModel(mViewModel);
    }

    @Override
    public void loadUpdatePurchasesWorker() {
        final FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = WorkerUtils.findWorker(fragmentManager, PurchasesUpdateWorker.WORKER_TAG);
        if (fragment == null) {
            fragment = new PurchasesUpdateWorker();

            fragmentManager.beginTransaction()
                    .add(fragment, PurchasesUpdateWorker.WORKER_TAG)
                    .commit();
        }
    }

    @Override
    public void loadQueryMorePurchasesWorker(int skip) {
        final FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = WorkerUtils.findWorker(fragmentManager, PurchasesQueryMoreWorker.WORKER_TAG);
        if (fragment == null) {
            fragment = PurchasesQueryMoreWorker.newInstance(skip);

            fragmentManager.beginTransaction()
                    .add(fragment, PurchasesQueryMoreWorker.WORKER_TAG)
                    .commit();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startPurchaseDetailsActivity(@NonNull Purchase purchase) {
        final Activity activity = getActivity();
        final Intent intent = new Intent(activity, PurchaseDetailsActivity.class);
        intent.putExtra(INTENT_PURCHASE_ID, purchase.getObjectId());

        final ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity);
        startActivityForResult(intent, BaseActivity.INTENT_REQUEST_PURCHASE_DETAILS,
                options.toBundle());
    }

    public interface ActivityListener extends BaseRecyclerViewOnlineFragment.ActivityListener {
        void setPurchasesViewModel(@NonNull HomePurchasesViewModel viewModel);

        void updateNavDrawerSelectedGroup();
    }
}
