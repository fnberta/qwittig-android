/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.giantific.qwittig.ComparatorParseUserIgnoreCase;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.ui.adapters.rows.UserAvatarRow;

import static ch.giantific.qwittig.utils.ViewUtils.DISABLED_ALPHA;

/**
 * Handles the display of users' avatars and nicknames inside of a
 * {@link PurchaseDetailsRecyclerAdapter}.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchaseDetailsUsersInvolvedRecyclerAdapter extends
        RecyclerView.Adapter<UserAvatarRow> {

    private static final String LOG_TAG = PurchaseDetailsUsersInvolvedRecyclerAdapter.class.getSimpleName();
    private static final int VIEW_RESOURCE = R.layout.row_users_involved_list;
    private Context mContext;
    private Purchase mPurchase;
    private List<ParseUser> mUsersInvolved;
    @NonNull
    private List<ParseUser> mUsersInvolvedSorted = new ArrayList<>();
    private User mBuyer;
    private User mCurrentUser;

    /**
     * Constructs a new {@link PurchaseDetailsUsersInvolvedRecyclerAdapter}.
     *
     * @param context the context to use in the adapters
     */
    public PurchaseDetailsUsersInvolvedRecyclerAdapter(@NonNull Context context,
                                                       @NonNull User currentUser) {
        super();

        mContext = context;
        mCurrentUser = currentUser;
    }

    @Nullable
    @Override
    public UserAvatarRow onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(VIEW_RESOURCE, parent, false);

        return new UserAvatarRow(v, mContext);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserAvatarRow viewHolder, int position) {
        if (mPurchase == null) {
            return;
        }

        User user = (User) mUsersInvolvedSorted.get(position);

        if (mBuyer.getObjectId().equals(user.getObjectId())) {
            viewHolder.setNameBold();
        }

        String nickname = userIsInGroup(user) ? user.getNicknameOrMe(mContext, mCurrentUser) :
                mContext.getString(R.string.user_deleted);
        viewHolder.setName(nickname);

        byte[] avatarByteArray = user.getAvatar();
        viewHolder.setAvatar(avatarByteArray, false);

        if (!mUsersInvolved.contains(user)) {
            viewHolder.setAlpha(DISABLED_ALPHA);
        }
    }

    private boolean userIsInGroup(@NonNull User user) {
        return user.getGroupIds().contains(mPurchase.getGroup().getObjectId());
    }

    @Override
    public int getItemCount() {
        return mUsersInvolvedSorted.size();
    }

    /**
     * Sets the purchase of the adapter. As long as this is not set, nothing will be displayed in
     * the adapter.
     *
     * @param purchase the purchase to set
     */
    public void setPurchase(@NonNull ParseObject purchase) {
        mPurchase = (Purchase) purchase;
        mBuyer = mPurchase.getBuyer();
        setupUsersInvolved();
    }

    private void setupUsersInvolved() {
        mUsersInvolvedSorted.clear();
        mUsersInvolved = mPurchase.getUsersInvolved();

        mUsersInvolvedSorted.addAll(mUsersInvolved);

        if (mUsersInvolvedSorted.contains(mBuyer)) {
            mUsersInvolvedSorted.remove(mBuyer);
        }
        Collections.sort(mUsersInvolvedSorted, new ComparatorParseUserIgnoreCase());
        mUsersInvolvedSorted.add(0, mBuyer);
    }
}
