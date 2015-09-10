package ch.giantific.qwittig.ui;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.helpers.UserQueryHelper;
import ch.giantific.qwittig.ui.adapters.UsersRecyclerAdapter;
import ch.giantific.qwittig.utils.ComparatorParseUserIgnoreCase;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;


public class FinanceUserBalancesFragment extends BaseRecyclerViewFragment implements
        LocalQuery.UserLocalQueryListener,
        UsersRecyclerAdapter.AdapterInteractionListener {

    private static final String USER_QUERY_HELPER = "user_query_helper";
    private UsersRecyclerAdapter mRecyclerAdapter;
    private List<ParseUser> mUsers = new ArrayList<>();

    public FinanceUserBalancesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_finance_users, container, false);
        findBaseViews(rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new UsersRecyclerAdapter(getActivity(),
                R.layout.row_users, mUsers, this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    protected void onlineQuery() {
        if (!Utils.isConnected(getActivity())) {
            setLoading(false);
            showOnlineQueryErrorSnackbar(getString(R.string.toast_no_connection));
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        UserQueryHelper userQueryHelper = findQueryHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (userQueryHelper == null) {
            userQueryHelper = new UserQueryHelper();

            fragmentManager.beginTransaction()
                    .add(userQueryHelper, USER_QUERY_HELPER)
                    .commit();
        }
    }

    private UserQueryHelper findQueryHelper(FragmentManager fragmentManager) {
        return (UserQueryHelper) fragmentManager.findFragmentByTag(USER_QUERY_HELPER);
    }

    /**
     * Called from activity when helper fails to pin new purchases
     * @param e
     */
    public void onUsersPinFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        showOnlineQueryErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), e));
        removeQueryHelper();

        setLoading(false);
    }

    /**
     * Called from activity when helper finished pinning new users
     */
    public void onUsersPinned() {
        updateAdapter();
    }

    /**
     * Called from activity when all purchases queries are finished
     */
    public void onAllUserQueriesFinished() {
        removeQueryHelper();
        setLoading(false);
    }

    private void removeQueryHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        UserQueryHelper UserQueryHelper = findQueryHelper(fragmentManager);

        if (UserQueryHelper != null) {
            fragmentManager.beginTransaction().remove(UserQueryHelper).commitAllowingStateLoss();
        }
    }

    @Override
    public void updateAdapter() {
        super.updateAdapter();

        if (mCurrentUser != null) {
            LocalQuery.queryUsers(this);
        }
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
        mRecyclerAdapter.setCurrentGroupCurrency(ParseUtils.getGroupCurrency());
        mRecyclerAdapter.notifyDataSetChanged();
        toggleMainVisibility();
    }

    @Override
    public void onUsersRowItemClick(int position) {
        // do nothing for the moment
    }
}
