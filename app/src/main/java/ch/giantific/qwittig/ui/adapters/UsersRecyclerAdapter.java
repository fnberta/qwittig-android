/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.ui.adapters.rows.BaseUserAvatarRow;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.Utils;


/**
 * Handles the display of users with their avatar images, nicknames and current balances.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class UsersRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_RESOURCE = R.layout.row_users;
    private AdapterInteractionListener mListener;
    private List<ParseUser> mUsers;
    private Context mContext;
    private String mCurrentGroupCurrency;

    /**
     * Constructs a new {@link UsersRecyclerAdapter}.
     *
     * @param context  the context to use in the adapter
     * @param users    the users to display
     * @param listener the callback for user clicks on the users
     */
    public UsersRecyclerAdapter(@NonNull Context context, @NonNull List<ParseUser> users,
                                @NonNull AdapterInteractionListener listener) {
        super();

        mListener = listener;
        mContext = context;
        mUsers = users;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(VIEW_RESOURCE, parent,
                false);

        return new UsersRow(view, mContext, mListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final UsersRow usersRow = (UsersRow) viewHolder;
        User user = (User) mUsers.get(position);

        usersRow.setName(user.getNickname());

        byte[] avatarByteArray = user.getAvatar();
        usersRow.setAvatar(avatarByteArray, false);

        User currentUser = (User) ParseUser.getCurrentUser();
        Group currentGroup = currentUser.getCurrentGroup();
        BigFraction balance = user.getBalance(currentGroup);
        usersRow.setBalance(balance, mCurrentGroupCurrency);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    /**
     * Sets the current group currency field. As long this is not set, nothing will be displayed
     * in the adapter.
     *
     * @param currentGroupCurrency the currency code to set
     */
    public void setCurrentGroupCurrency(@NonNull String currentGroupCurrency) {
        mCurrentGroupCurrency = currentGroupCurrency;
    }

    /**
     * Defines the action to take when the user clicks on a user.
     */
    public interface AdapterInteractionListener {
        /**
         * Handles the click on the user row itself.
         *
         * @param position the adapter position of the user row
         */
        void onUsersRowItemClick(int position);
    }

    /**
     * Provides a {@link RecyclerView} row that displays the user's avatar image, the nickname and
     * the current balance.
     * <p/>
     * Subclass of {@link BaseUserAvatarRow}.
     */
    private static class UsersRow extends BaseUserAvatarRow {

        private TextView mTextViewBalance;

        /**
         * Constructs a new {@link UsersRow} and sets the click listener.
         *
         * @param view     the inflated view
         * @param context  the context to use in the row
         * @param listener the callback for user clicks
         */
        public UsersRow(@NonNull View view, @NonNull Context context,
                        @NonNull final AdapterInteractionListener listener) {
            super(view, context);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onUsersRowItemClick(getAdapterPosition());
                }
            });

            mTextViewBalance = (TextView) view.findViewById(R.id.user_balance);
        }

        /**
         * Sets the properly formatted balance of the user.
         *
         * @param balance  the balance to set
         * @param currency the currency code to use to format the balance
         */
        public void setBalance(@NonNull BigFraction balance, @NonNull String currency) {
            mTextViewBalance.setTextColor(Utils.isPositive(balance) ?
                    ContextCompat.getColor(mContext, R.color.green) :
                    ContextCompat.getColor(mContext, R.color.red));
            mTextViewBalance.setText(MoneyUtils.formatMoneyNoSymbol(balance, currency));
        }
    }
}
