/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.ParseUserRepository;
import ch.giantific.qwittig.workerfragments.query.UserQueryWorker;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.ui.adapters.UsersRecyclerAdapter;
import ch.giantific.qwittig.ComparatorParseUserIgnoreCase;
import ch.giantific.qwittig.utils.WorkerUtils;
import ch.giantific.qwittig.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Displays the users of a group and their current balances in a {@link RecyclerView} list. Does not
 * include the current user whose balance is displayed in the {@link Toolbar} of the hosting
 * {@link Activity}.
 * <p/>
 * Subclass of {@link BaseRecyclerViewOnlineFragment}.
 */
public class FinanceUserBalancesFragment extends BaseRecyclerViewOnlineFragment implements
        UserRepository.GetUsersLocalListener,
        UsersRecyclerAdapter.AdapterInteractionListener {

    private static final String USER_QUERY_WORKER = "USER_QUERY_WORKER";
    private UsersRecyclerAdapter mRecyclerAdapter;
    @NonNull
    private List<ParseUser> mUsers = new ArrayList<>();
    private UserRepository mUserRepo;

    public FinanceUserBalancesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserRepo = new ParseUserRepository();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_finance_users, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new UsersRecyclerAdapter(getActivity(), mUsers, mCurrentUser, this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    protected void onlineQuery() {
        if (!Utils.isConnected(getActivity())) {
            setLoading(false);
            showErrorSnackbar(getString(R.string.toast_no_connection), getOnlineQueryRetryAction());
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        Fragment userQueryWorker = WorkerUtils.findWorker(fragmentManager, USER_QUERY_WORKER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (userQueryWorker == null) {
            userQueryWorker = new UserQueryWorker();
            fragmentManager.beginTransaction()
                    .add(userQueryWorker, USER_QUERY_WORKER)
                    .commit();
        }
    }

    /**
     * Passes the error code to the generic error handler, shows the user an error message and
     * removes the retained worker fragment and loading indicators.
     *
     * @param errorCode the error code of the exception thrown in the process
     */
    public void onUserUpdateFailed(int errorCode) {
        ParseErrorHandler.handleParseError(getActivity(), errorCode);
        showErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), errorCode),
                getOnlineQueryRetryAction());
        WorkerUtils.removeWorker(getFragmentManager(), USER_QUERY_WORKER);

        setLoading(false);
    }

    /**
     * Tells the adapter of the {@link RecyclerView} to re-query its data, removes the retained
     * worker fragment and removes loading indicators.
     */
    public void onUsersUpdated() {
        WorkerUtils.removeWorker(getFragmentManager(), USER_QUERY_WORKER);
        setLoading(false);

        updateAdapter();
    }

    @Override
    protected void updateAdapter() {
        mUserRepo.getUsersLocalAsync(mCurrentGroup, this);
    }

    @Override
    public void onUsersLocalLoaded(@NonNull List<ParseUser> users) {
        mUsers.clear();

        if (!users.isEmpty()) {
            for (ParseUser user : users) {
                if (!user.getObjectId().equals(mCurrentUser.getObjectId())) {
                    mUsers.add(user);
                }
            }
        }

        if (!mUsers.isEmpty()) {
            Collections.sort(mUsers, new ComparatorParseUserIgnoreCase());
        }

        checkCurrentGroup();
    }

    @Override
    protected void toggleEmptyViewVisibility() {
        if (mUsers.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void updateView() {
        mRecyclerAdapter.setCurrentGroupCurrency(ParseUtils.getGroupCurrencyWithFallback(mCurrentGroup));
        mRecyclerAdapter.notifyDataSetChanged();
        showMainView();
    }

    @Override
    public void onUsersRowItemClick(int position) {
        // do nothing for the moment
    }
}
