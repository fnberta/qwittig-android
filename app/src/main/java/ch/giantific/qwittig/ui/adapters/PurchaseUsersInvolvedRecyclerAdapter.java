/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseUser;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.ui.adapters.rows.UserAvatarRow;
import ch.giantific.qwittig.ui.fragments.PurchaseBaseFragment;

import static ch.giantific.qwittig.utils.ViewUtils.DISABLED_ALPHA;

/**
 * Handles the display of user avatars and nicknames in the user selection row of the
 * {@link PurchaseBaseFragment}.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchaseUsersInvolvedRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_RESOURCE = R.layout.row_users_involved_list;
    private Context mContext;
    private List<ParseUser> mUsersAvailable;
    private boolean[] mUsersInvolved;
    private AdapterInteractionListener mListener;
    private User mCurrentUser;

    /**
     * Constructs a new {@link PurchaseUsersInvolvedRecyclerAdapter}.
     *
     * @param context        the context to use in the adapter
     * @param usersAvailable the users available from which the user can make a selection
     * @param listener       the callback for user clicks on the users
     */
    public PurchaseUsersInvolvedRecyclerAdapter(@NonNull Context context,
                                                @NonNull List<ParseUser> usersAvailable,
                                                @NonNull User currentUser,
                                                @NonNull AdapterInteractionListener listener) {
        super();

        mContext = context;
        mUsersAvailable = usersAvailable;
        mCurrentUser = currentUser;
        mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(VIEW_RESOURCE, parent, false);

        return new UserInvolvedRow(v, mContext, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (mUsersInvolved == null) {
            return;
        }

        UserInvolvedRow involvedRow = (UserInvolvedRow) viewHolder;
        User user = (User) mUsersAvailable.get(position);

        involvedRow.setName(user.getNicknameOrMe(mContext, mCurrentUser));
        byte[] avatarByteArray = user.getAvatar();
        involvedRow.setAvatar(avatarByteArray, true);
        involvedRow.setAlpha(!mUsersInvolved[position] ? DISABLED_ALPHA : 1f);
    }

    @Override
    public int getItemCount() {
        return mUsersAvailable.size();
    }

    /**
     * Sets the users involved of the purchase. As long as this is not set, nothing will be
     * displayed in the adapter.
     *
     * @param usersInvolved the users involved in respective purchase
     */
    public void setUsersInvolved(boolean[] usersInvolved) {
        mUsersInvolved = usersInvolved;
    }

    /**
     * Defines the actions to take when a user clicks on a user
     */
    public interface AdapterInteractionListener {
        /**
         * Handles the click on a user item
         *
         * @param position the adapter position of the user item
         */
        void onPurchaseUserClick(int position);
    }

    /**
     * Provides a {@link RecyclerView} row that displays a user's avatar image and name and listens to
     * clicks on the items.
     * <p/>
     * Subclass of {@link UserAvatarRow}.
     */
    private static class UserInvolvedRow extends UserAvatarRow {

        /**
         * Constructs a new {@link UserInvolvedRow} and sets the click listener.
         *
         * @param view     the inflated view
         * @param context  the context to use in the row
         * @param listener the callback for when an item is clicked
         */
        public UserInvolvedRow(@NonNull View view, @NonNull Context context,
                               @NonNull final AdapterInteractionListener listener) {
            super(view, context);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onPurchaseUserClick(getAdapterPosition());
                }
            });
        }
    }

}
