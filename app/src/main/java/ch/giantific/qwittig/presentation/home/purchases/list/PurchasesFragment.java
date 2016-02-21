/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.list;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mugen.Mugen;
import com.mugen.MugenCallbacks;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.databinding.FragmentHomePurchasesBinding;
import ch.giantific.qwittig.di.components.DaggerPurchasesListComponent;
import ch.giantific.qwittig.di.modules.HomePurchasesViewModelModule;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewOnlineFragment;
import ch.giantific.qwittig.presentation.home.purchases.details.PurchaseDetailsActivity;
import rx.Observable;

/**
 * Displays recent purchases in a {@link RecyclerView} list.
 * <p/>
 * Subclass of {@link BaseRecyclerViewOnlineFragment}.
 */
public class PurchasesFragment extends BaseRecyclerViewOnlineFragment<PurchasesViewModel, PurchasesFragment.ActivityListener>
        implements PurchasesViewModel.ViewListener {

    public static final String INTENT_PURCHASE_ID = "INTENT_PURCHASE_ID";
    private FragmentHomePurchasesBinding mBinding;

    public PurchasesFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerPurchasesListComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(getActivity()))
                .homePurchasesViewModelModule(new HomePurchasesViewModelModule(savedInstanceState, this))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentHomePurchasesBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
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

//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        final LoaderManager loaderManager = getLoaderManager();
//        loaderManager.initLoader(QUERY_LOCAL, null, this);
//        if (loaderManager.getLoader(QUERY_MORE_ONLINE) != null) {
//            loaderManager.initLoader(QUERY_MORE_ONLINE, null, this);
//        }
//        if (loaderManager.getLoader(UPDATE) != null) {
//            loaderManager.initLoader(UPDATE, null, this);
//        }
//    }
//
//    @Override
//    public Loader<Observable<Purchase>> onCreateLoader(int id, Bundle args) {
//        final PurchasesLoader loader = mComp.getPurchasesLoader();
//        if (id == QUERY_MORE_ONLINE) {
//            loader.setSkip(mViewModel.getItemCount());
//        }
//
//        return loader;
//    }
//
//    @Override
//    public void onLoadFinished(Loader<Observable<Purchase>> loader, Observable<Purchase> data) {
//        // hack because onLoadFinished gets called two times, bug in the framework
//        if (mLoaderData == data) {
//            return;
//        }
//
//        mLoaderData = data;
//        if (data == null) {
//            mViewModel.onLoaderError();
//            return;
//        }
//
//        final int id = loader.getId();
//        switch (id) {
//            case QUERY_LOCAL:
//                mViewModel.setDataStream(data);
//                break;
//            case QUERY_MORE_ONLINE:
//                getLoaderManager().destroyLoader(id);
//                mViewModel.setPurchasesQueryMoreStream(data, "");
//                break;
//            case UPDATE:
//                getLoaderManager().destroyLoader(id);
//                mViewModel.setPurchasesUpdateStream(data, "");
//                break;
//        }
//    }
//
//    @Override
//    public void onLoaderReset(Loader<Observable<Purchase>> loader) {
//        // nothing to do
//    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.srlRv.rvBase;
    }

    @Override
    protected RecyclerView.Adapter getRecyclerAdapter() {
        return new PurchasesRecyclerAdapter(mViewModel);
    }

    @Override
    protected SwipeRefreshLayout getSrl() {
        return mBinding.srlRv.srlBase;
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setPurchasesViewModel(mViewModel);
    }

    @Override
    public void loadUpdatePurchasesWorker() {
        PurchasesUpdateWorker.attach(getFragmentManager());
    }

    @Override
    public void loadQueryMorePurchasesWorker(int skip) {
        PurchasesQueryMoreWorker.attach(getFragmentManager(), skip);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void startPurchaseDetailsActivity(@NonNull Purchase purchase) {
        final Activity activity = getActivity();
        final Intent intent = new Intent(activity, PurchaseDetailsActivity.class);
        intent.putExtra(INTENT_PURCHASE_ID, purchase.getObjectId());

        final ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity);
        activity.startActivityForResult(intent, BaseActivity.INTENT_REQUEST_PURCHASE_DETAILS,
                options.toBundle());
    }

    public interface ActivityListener extends BaseRecyclerViewOnlineFragment.ActivityListener {
        void setPurchasesViewModel(@NonNull PurchasesViewModel viewModel);
    }
}
