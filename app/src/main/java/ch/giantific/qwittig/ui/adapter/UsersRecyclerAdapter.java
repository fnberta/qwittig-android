package ch.giantific.qwittig.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.ImageAvatar;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;
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

        return new UsersRow(view, mListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final UsersRow usersRow = (UsersRow) viewHolder;
        User user = (User) mUsers.get(position);

        usersRow.mTextViewName.setText(user.getNickname());

        byte[] avatarByteArray = user.getAvatar();
        Drawable avatar = ImageAvatar.getRoundedAvatar(mContext, avatarByteArray, false);
        usersRow.mImageViewAvatar.setImageDrawable(avatar);

        User currentUser = (User) ParseUser.getCurrentUser();
        Group currentGroup = null;
        if (currentUser != null) {
            currentGroup = currentUser.getCurrentGroup();
        }
        BigFraction balance = user.getBalance(currentGroup);
        if (Utils.isPositive(balance)) {
            usersRow.mTextViewBalance.setTextColor(mContext.getResources().getColor(R.color.green));
        } else {
            usersRow.mTextViewBalance.setTextColor(mContext.getResources().getColor(R.color.red));
        }
        usersRow.mTextViewBalance.setText(MoneyUtils.formatMoneyNoSymbol(balance, mCurrentGroupCurrency));
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void setCurrentGroupCurrency(String currentGroupCurrency) {
        mCurrentGroupCurrency = currentGroupCurrency;
    }

    public interface AdapterInteractionListener {
        public void onUsersRowItemClick(int position);
    }

    private static class UsersRow extends RecyclerView.ViewHolder {

        private ImageView mImageViewAvatar;
        private TextView mTextViewName;
        private TextView mTextViewBalance;

        public UsersRow(View view, final AdapterInteractionListener listener) {
            super(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onUsersRowItemClick(getAdapterPosition());
                }
            });

            mImageViewAvatar = (ImageView) view.findViewById(R.id.list_avatar);
            mTextViewName = (TextView) view.findViewById(R.id.list_name);
            mTextViewBalance = (TextView) view.findViewById(R.id.list_balance);
        }
    }
}
