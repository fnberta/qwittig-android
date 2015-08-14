package ch.giantific.qwittig.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.constants.AppConstants;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.ui.adapter.UsersRecyclerAdapter;
import ch.giantific.qwittig.utils.ComparatorParseUserIgnoreCase;
import ch.giantific.qwittig.utils.ParseUtils;


public class HomeUsersFragment extends HomeBaseFragment implements
        LocalQuery.UserLocalQueryListener,
        UsersRecyclerAdapter.AdapterInteractionListener {

    private UsersRecyclerAdapter mRecyclerAdapter;
    private List<ParseUser> mUsers = new ArrayList<>();

    public HomeUsersFragment() {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerAdapter = new UsersRecyclerAdapter(getActivity(),
                R.layout.row_users, mUsers, this);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    @Override
    protected void setEmptyViewDrawableAndText() {
        Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_people_black_144dp);
        drawable.setAlpha(AppConstants.ICON_BLACK_ALPHA_RGB);
        mTextViewEmpty.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
        mTextViewEmpty.setText(R.string.no_user);
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
    }

    @Override
    public void onUsersRowItemClick(int position) {
        // do nothing for the moment
    }
}
