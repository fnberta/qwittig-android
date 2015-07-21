package ch.giantific.qwittig.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.parse.ParseObject;
import com.parse.ParseUser;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;

/**
 * A placeholder fragment containing a simple view.
 */
public abstract class CompensationsBaseFragment extends BaseFragment implements
    LocalQuery.ObjectLocalFetchListener {

    private static final String LOG_TAG = CompensationsBaseFragment.class.getSimpleName();
    FragmentInteractionListener mListener;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    SwipeRefreshLayout mSwipeRefreshLayout;
    View mEmptyView;
    User mCurrentUser;
    Group mCurrentGroup;

    public CompensationsBaseFragment() {
        // Required empty public constructor
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.accent, R.color.green,
                R.color.red);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mListener.onlineQuery();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        updateAdapter();
    }

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

    public void setLoading(boolean isLoading) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(isLoading);
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

    protected abstract void toggleEmptyViewVisibility();

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentInteractionListener {
        void onlineQuery();

        void showAccountCreateDialog();
    }
}
