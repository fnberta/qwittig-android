package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
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
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.adapters.rows.BaseUserAvatarRow;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.Utils;


/**
 * Created by fabio on 12.10.14.
 */
public class UsersRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private AdapterInteractionListener mListener;
    private int mUsersViewResource;
    private List<ParseUser> mUsers;
    private Context mContext;
    private String mCurrentGroupCurrency;

    public UsersRecyclerAdapter(Context context, int usersViewResource, List<ParseUser> users,
                                AdapterInteractionListener listener) {
        super();

        mListener = listener;
        mContext = context;
        mUsersViewResource = usersViewResource;
        mUsers = users;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mUsersViewResource, parent,
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

    public void setCurrentGroupCurrency(String currentGroupCurrency) {
        mCurrentGroupCurrency = currentGroupCurrency;
    }

    public interface AdapterInteractionListener {
        void onUsersRowItemClick(int position);
    }

    private static class UsersRow extends BaseUserAvatarRow {

        private TextView mTextViewBalance;

        public UsersRow(View view, Context context, final AdapterInteractionListener listener) {
            super(view, context);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onUsersRowItemClick(getAdapterPosition());
                }
            });

            mTextViewBalance = (TextView) view.findViewById(R.id.user_balance);
        }

        public void setBalance(BigFraction balance, String currency) {
            mTextViewBalance.setTextColor(Utils.isPositive(balance) ?
                    ContextCompat.getColor(mContext, R.color.green) :
                    ContextCompat.getColor(mContext, R.color.red));
            mTextViewBalance.setText(MoneyUtils.formatMoneyNoSymbol(balance, currency));
        }
    }
}
