package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseUser;

import java.util.List;

import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.adapters.rows.UserInvolvedRow;

import static ch.giantific.qwittig.utils.AnimUtils.DISABLED_ALPHA;

/**
 * Created by fabio on 14.01.15.
 */
public class PurchaseAddUsersInvolvedRecyclerAdapter extends
        RecyclerView.Adapter<UserInvolvedRow> {

    private Context mContext;
    private int mViewResource;
    private List<ParseUser> mUsersAvailable;
    private boolean[] mUsersInvolved;
    private AdapterInteractionListener mListener;

    public PurchaseAddUsersInvolvedRecyclerAdapter(Context context, int viewResource,
                                                   List<ParseUser> usersAvailable,
                                                   AdapterInteractionListener listener) {
        super();

        mContext = context;
        mViewResource = viewResource;
        mUsersAvailable = usersAvailable;
        mListener = listener;
    }

    @Override
    public UserInvolvedRow onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(mViewResource, parent, false);

        return new UserInvolvedRow(v, mContext, mListener);
    }

    @Override
    public void onBindViewHolder(UserInvolvedRow viewHolder, int position) {
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

    public void setUsersInvolved(boolean[] usersInvolved) {
        mUsersInvolved = usersInvolved;
    }

    public interface AdapterInteractionListener {
        void onPurchaseUserClick(int position);
    }

}
