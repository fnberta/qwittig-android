package ch.giantific.qwittig.ui;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.utils.MessageUtils;

/**
 * Created by fabio on 02.11.14.
 */
public abstract class BaseRecyclerViewFragment extends BaseFragment implements
        LocalQuery.ObjectLocalFetchListener {

    private static final String STATE_IS_LOADING = "state_is_loading";
    User mCurrentUser;
    Group mCurrentGroup;
    RecyclerView mRecyclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ProgressBar mProgressBarLoading;
    View mEmptyView;
    LinearLayoutManager mLayoutManager;

    public BaseRecyclerViewFragment() {
    }

    final void findBaseViews(View rootView) {
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_base);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.srl_base);
        mProgressBarLoading = (ProgressBar) rootView.findViewById(R.id.pb_base);
        mEmptyView = rootView.findViewById(R.id.empty_view);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary,
                R.color.accent,
                R.color.green,
                R.color.red);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onlineQuery();
            }
        });

        if (savedInstanceState != null) {
            final boolean isLoading = savedInstanceState.getBoolean(STATE_IS_LOADING, false);
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(isLoading);
                }
            });
        }
    }

    protected abstract void onlineQuery();

    final void showOnlineQueryErrorSnackbar(String errorMessage) {
        Snackbar snackbar = MessageUtils.getBasicSnackbar(mRecyclerView, errorMessage);
        snackbar.setAction(R.string.action_retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoading(true);
                onlineQuery();
            }
        });
        snackbar.show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_IS_LOADING, mSwipeRefreshLayout.isRefreshing());
    }

    public void setLoading(boolean isLoading) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(isLoading);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        updateAdapter();
    }

    /**
     * Updates the member variable for currentGroup and queries new data.
     */
    @CallSuper
    public void updateAdapter() {
        updateCurrentUserAndGroup();
    }

    private void updateCurrentUserAndGroup() {
        mCurrentUser = (User) ParseUser.getCurrentUser();
        if (mCurrentUser != null) {
            mCurrentGroup = mCurrentUser.getCurrentGroup();
        }
    }

    final void checkCurrentGroup() {
        if (mCurrentGroup != null) {
            if (mCurrentGroup.isDataAvailable()) {
                updateView();
            } else {
                LocalQuery.fetchObjectData(this, mCurrentGroup);
            }
        } else {
            updateView();
        }
    }

    @Override
    public void onObjectFetched(ParseObject object) {
        updateView();
    }

    protected abstract void updateView();

    /**
     * Hides the loading progressbar and displays the RecyclerView
     */
    @CallSuper
    void toggleMainVisibility() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mProgressBarLoading.setVisibility(View.GONE);
        toggleEmptyViewVisibility();
    }

    protected abstract void toggleEmptyViewVisibility();
}
