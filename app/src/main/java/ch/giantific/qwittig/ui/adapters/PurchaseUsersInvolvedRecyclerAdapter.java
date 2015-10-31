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
import ch.giantific.qwittig.ui.adapters.rows.UserInvolvedRow;
import ch.giantific.qwittig.ui.fragments.PurchaseBaseFragment;

import static ch.giantific.qwittig.utils.AnimUtils.DISABLED_ALPHA;

/**
 * Handles the display of user avatars and nicknames in the user selection row of the
 * {@link PurchaseBaseFragment}.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchaseUsersInvolvedRecyclerAdapter extends
        RecyclerView.Adapter<UserInvolvedRow> {

    private static final int VIEW_RESOURCE = R.layout.row_users_involved_list;
    private Context mContext;
    private List<ParseUser> mUsersAvailable;
    private boolean[] mUsersInvolved;
    private AdapterInteractionListener mListener;

    /**
     * Constructs a new {@link PurchaseUsersInvolvedRecyclerAdapter}.
     *
     * @param context        the context to use in the adapter
     * @param usersAvailable the users available from which the user can make a selection
     * @param listener       the callback for user clicks on the users
     */
    public PurchaseUsersInvolvedRecyclerAdapter(@NonNull Context context,
                                                @NonNull List<ParseUser> usersAvailable,
                                                @NonNull AdapterInteractionListener listener) {
        super();

        mContext = context;
        mUsersAvailable = usersAvailable;
        mListener = listener;
    }

    @NonNull
    @Override
    public UserInvolvedRow onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(VIEW_RESOURCE, parent, false);

        return new UserInvolvedRow(mContext, v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserInvolvedRow viewHolder, int position) {
        if (mUsersInvolved == null) {
            return;
        }

        User user = (User) mUsersAvailable.get(position);

        viewHolder.setName(user.getNicknameOrMe(mContext));

        byte[] avatarByteArray = user.getAvatar();
        viewHolder.setAvatar(avatarByteArray, true);

        viewHolder.setAlpha(!mUsersInvolved[position] ? DISABLED_ALPHA : 1f);
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

}
