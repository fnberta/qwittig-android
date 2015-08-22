package ch.giantific.qwittig.ui.listeners;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/*
* This class is a ScrollListener for RecyclerView that allows to show/hide
* views when list is scrolled.
* */
public abstract class InfiniteScrollListener extends RecyclerView.OnScrollListener {

    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;

    private static final int VISIBLE_THRESHOLD = 0;
    private int mPreviousTotal = 0;
    private boolean mIsLoading = true;

    public InfiniteScrollListener(LinearLayoutManager linearLayoutManager,
                                  RecyclerView recyclerView) {
        mLayoutManager = linearLayoutManager;
        mRecyclerView = recyclerView;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        infiniteScroll();
    }

    private void infiniteScroll() {
        int totalItemCount = mLayoutManager.getItemCount();
        int lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();
        int visibleItemCount = mRecyclerView.getChildCount();

        if (mIsLoading) {
            if (totalItemCount > mPreviousTotal + 1) { // +1 to account for progress bar row
                mIsLoading = false;
                mPreviousTotal = totalItemCount;
            }
        }

        if (!mIsLoading && (lastVisibleItemPosition + 1) == (totalItemCount - VISIBLE_THRESHOLD) &&
                totalItemCount > visibleItemCount) {
            onLoadMore();
            mIsLoading = true;
        }
    }

    public void resetPreviousTotal() {
        mPreviousTotal = 0;
    }

    public abstract void onLoadMore();
}