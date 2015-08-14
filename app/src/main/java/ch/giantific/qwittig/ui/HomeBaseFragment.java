package ch.giantific.qwittig.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.CallSuper;
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

/**
 * Created by fabio on 02.11.14.
 */
public abstract class HomeBaseFragment extends BaseFragment implements
        LocalQuery.ObjectLocalFetchListener {

    private static final String STATE_IS_LOADING = "state_is_loading";
    User mCurrentUser;
    Group mCurrentGroup;
    FragmentInteractionListener mListener;
    RecyclerView mRecyclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ProgressBar mProgressBarLoading;
    TextView mTextViewEmpty;
    LinearLayoutManager mLayoutManager;

    public HomeBaseFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_tabs, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_home);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.srl_home);
        mProgressBarLoading = (ProgressBar) rootView.findViewById(R.id.pb_home);
        mTextViewEmpty = (TextView) rootView.findViewById(R.id.tv_empty_view);

        return rootView;
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
                mListener.onlineQuery();
            }
        });

        setEmptyViewDrawableAndText();

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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_IS_LOADING, mSwipeRefreshLayout.isRefreshing());
    }

    protected abstract void setEmptyViewDrawableAndText();

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
    final void toggleMainVisibility() {
        if (!mListener.isNewQueryNeeded()) {
            mProgressBarLoading.setVisibility(View.GONE);
            toggleEmptyViewVisibility();
        }
    }

    protected abstract void toggleEmptyViewVisibility();

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentInteractionListener {
        void onlineQuery();

        boolean isNewQueryNeeded();
    }
}
