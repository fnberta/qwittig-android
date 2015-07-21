package ch.giantific.qwittig.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.OnlineQuery;
import ch.giantific.qwittig.ui.adapter.CompensationsPaidRecyclerAdapter;
import ch.giantific.qwittig.ui.widgets.InfiniteScrollListener;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class CompensationsPaidFragment extends CompensationsBaseFragment implements
        LocalQuery.UserLocalQueryListener,
        LocalQuery.CompensationLocalQueryListener,
        OnlineQuery.CompensationQueryListener {

    private static final String LOG_TAG = CompensationsPaidFragment.class.getSimpleName();
    private InfiniteScrollListener mScrollListener;
    private CompensationsPaidRecyclerAdapter mRecyclerAdapter;
    private List<ParseObject> mCompensations = new ArrayList<>();
    private List<ParseUser> mUsers = new ArrayList<>();

    public CompensationsPaidFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_compensations_paid, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.srl_compensations_history);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_compensations_history);
        mEmptyView = rootView.findViewById(R.id.tv_empty_view);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new CompensationsPaidRecyclerAdapter(getActivity(),
                R.layout.row_compensations_history, mCompensations, mUsers);
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
    public void updateAdapter() {
        super.updateAdapter();

        LocalQuery.queryUsers(this);
    }

    @Override
    public void onUsersLocalQueried(List<ParseUser> users) {
        mUsers.clear();
        if (!users.isEmpty()) {
            for (ParseUser user : users) {
                if (!user.getObjectId().equals(mCurrentUser.getObjectId())) {
                    mUsers.add(user);
                }
            }
        }

        LocalQuery.queryCompensationsPaid(this);
    }

    @Override
    public void onCompensationsLocalQueried(List<ParseObject> compensations) {
        mCompensations.clear();
        for (ParseObject compensation : compensations) {
            mCompensations.add(compensation);
        }

        checkCurrentGroup();
    }

    @Override
    protected void updateView() {
        mRecyclerAdapter.setCurrentGroupCurrency(ParseUtils.getGroupCurrency());
        mRecyclerAdapter.notifyDataSetChanged();
        toggleEmptyViewVisibility();
    }

    @Override
    protected void toggleEmptyViewVisibility() {
        if (mCompensations.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    private void loadMoreData() {
        int itemsToSkip = mCompensations.size();

        mCompensations.add(null);
        mRecyclerAdapter.notifyItemInserted(mCompensations.size() - 1);

        if (Utils.isConnected(getActivity())) {
            OnlineQuery.queryCompensationsPaidMore(getActivity(), this, itemsToSkip);
        } else {
            onCompensationsQueryFailed(getString(R.string.toast_no_connection));
        }
    }

    @Override
    public void onCompensationsQueried(List<ParseObject> compensations) {
        int progressBarPosition = mCompensations.size() - 1;
        mCompensations.remove(progressBarPosition);
        mRecyclerAdapter.notifyItemRemoved(progressBarPosition);

        for (ParseObject compensation : compensations) {
            mCompensations.add(compensation);
        }

        mRecyclerAdapter.notifyItemRangeInserted(progressBarPosition, compensations.size());
    }

    @Override
    public void onCompensationsQueryFailed(String errorMessage) {
        if (!mCompensations.isEmpty()) {
            int progressBarPosition = mCompensations.size() - 1;
            if (mCompensations.get(progressBarPosition) == null) {
                mCompensations.remove(progressBarPosition);
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

        MessageUtils.showBasicSnackbar(mRecyclerView, errorMessage);
    }
}
