package ch.giantific.qwittig.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.constants.AppConstants;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.OnlineQuery;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.ui.adapter.PurchasesRecyclerAdapter;
import ch.giantific.qwittig.ui.widgets.InfiniteScrollListener;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

public class HomePurchasesFragment extends HomeBaseFragment implements
        PurchasesRecyclerAdapter.AdapterInteractionListener,
        OnlineQuery.PurchaseQueryListener,
        LocalQuery.PurchaseLocalQueryListener {

    public static final String INTENT_PURCHASE_ID = "purchase_id";
    public static final String INTENT_THEME = "intent_theme";
    private static final String LOG_TAG =  HomePurchasesFragment.class.getSimpleName();
    private PurchasesRecyclerAdapter mRecyclerAdapter;
    private InfiniteScrollListener mScrollListener;
    private List<ParseObject> mPurchases = new ArrayList<>();

    public HomePurchasesFragment() {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new PurchasesRecyclerAdapter(getActivity(),
                R.layout.row_purchases, mPurchases, this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mScrollListener = new InfiniteScrollListener(mLayoutManager, mRecyclerView) {
            @Override
            public void onLoadMore() {
                loadMoreData();
            }
        };
        mRecyclerView.addOnScrollListener(mScrollListener);
    }

    @Override
    protected void setEmptyViewDrawableAndText() {
        Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_shopping_cart_black_144dp);
        drawable.setAlpha(AppConstants.ICON_BLACK_ALPHA_RGB);
        mTextViewEmpty.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
        mTextViewEmpty.setText(R.string.no_purchases);
    }

    @Override
    public void updateAdapter() {
        super.updateAdapter();

        LocalQuery.queryPurchases(this);
    }

    @Override
    public void onPurchasesLocalQueried(List<ParseObject> purchases) {
        mPurchases.clear();

        if (!purchases.isEmpty()) {
            for (ParseObject purchase : purchases) {
                mPurchases.add(purchase);
            }
        }

        checkCurrentGroup();
    }

    @Override
    void toggleMainVisibility() {
        super.toggleMainVisibility();

        if (mPurchases.isEmpty()) {
            mTextViewEmpty.setVisibility(View.VISIBLE);
        } else {
            mTextViewEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    protected void updateView() {
        mRecyclerAdapter.setCurrentGroupCurrency(ParseUtils.getGroupCurrency());
        mRecyclerAdapter.notifyDataSetChanged();
        toggleMainVisibility();

        mScrollListener.resetPreviousTotal();
    }

    @Override
    public void onPurchaseRowItemClick(int position) {
        Purchase purchase = (Purchase) mPurchases.get(position);
        boolean isGreen = purchase.getBuyer().getObjectId().equals(mCurrentUser.getObjectId());

        Intent intent = new Intent(getActivity(), PurchaseDetailsActivity.class);
        intent.putExtra(INTENT_PURCHASE_ID, purchase.getObjectId());
        intent.putExtra(INTENT_THEME, isGreen);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity());
        getActivity().startActivityForResult(intent, HomeActivity.INTENT_REQUEST_PURCHASE_DETAILS,
                options.toBundle());
    }

    private void loadMoreData() {
        int purchasesSize = mPurchases.size();

        if (mPurchases.get(purchasesSize - 1) != null) {
            mPurchases.add(null);
            mRecyclerAdapter.notifyItemInserted(purchasesSize); // purchasesSize points to last item now

            if (Utils.isConnected(getActivity())) {
                OnlineQuery.queryPurchasesMore(getActivity(), this, purchasesSize);
            } else {
                onPurchasesQueryFailed(getString(R.string.toast_no_connection));
            }
        }
    }

    @Override
    public void onPurchasesQueried(List<ParseObject> purchases) {
        int progressBarPosition = mPurchases.size() - 1;
        mPurchases.remove(progressBarPosition);
        mRecyclerAdapter.notifyItemRemoved(progressBarPosition);

        for (ParseObject purchase : purchases) {
            mPurchases.add(purchase);
        }

        mRecyclerAdapter.notifyItemRangeInserted(progressBarPosition, purchases.size());
    }

    @Override
    public void onPurchasesQueryFailed(String errorMessage) {
        if (!mPurchases.isEmpty()) {
            int progressBarPosition = mPurchases.size() - 1;
            if (mPurchases.get(progressBarPosition) == null) {
                mPurchases.remove(progressBarPosition);
                mRecyclerAdapter.notifyItemRemoved(progressBarPosition);

                // scroll to top, otherwise another "load more cycle" will be triggered immediately
                mRecyclerView.smoothScrollToPosition(0);
                // Reset previousTotal to zero (after a short delay to let the smooth scroll finish)
                // Otherwise the user won't be able to start a new "load more cycle"
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScrollListener.resetPreviousTotal();
                    }
                }, 200);
            }
        }

        Snackbar snackbar = MessageUtils.getBasicSnackbar(mRecyclerView, errorMessage);
        snackbar.setAction(R.string.action_retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoreData();
            }
        });
        snackbar.show();
    }
}
