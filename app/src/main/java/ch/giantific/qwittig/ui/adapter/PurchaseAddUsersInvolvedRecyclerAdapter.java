package ch.giantific.qwittig.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseUser;

import java.util.List;

import ch.giantific.qwittig.data.models.ImageAvatar;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.adapter.rows.UserInvolvedRow;

import static ch.giantific.qwittig.constants.AppConstants.DISABLED_ALPHA;

/**
 * Created by fabio on 14.01.15.
 */
public class PurchaseAddUsersInvolvedRecyclerAdapter extends
        RecyclerView.Adapter<UserInvolvedRow> {

    private Context mContext;
    private int mViewResource;
    private List<ParseUser> mUsersAvailable;
    private List<Boolean> mUsersInvolved;
    private AdapterInteractionListener mListener;

    public PurchaseAddUsersInvolvedRecyclerAdapter(Context context, int viewResource,
                                                   List<ParseUser> usersAvailable,
                                                   List<Boolean> usersInvolved,
                                                   AdapterInteractionListener listener) {
        super();

        mContext = context;
        mViewResource = viewResource;
        mUsersAvailable = usersAvailable;
        mUsersInvolved = usersInvolved;
        mListener = listener;
    }

    @Override
    public UserInvolvedRow onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(mViewResource, parent, false);

        return new UserInvolvedRow(v, mListener);
    }

    @Override
    public void onBindViewHolder(UserInvolvedRow viewHolder,
                                 int position) {
        User user = (User) mUsersAvailable.get(position);

        viewHolder.setName(user.getNicknameOrMe(mContext));

        byte[] avatarByteArray = user.getAvatar();
        Drawable avatar = ImageAvatar.getRoundedAvatar(mContext, avatarByteArray, false);
        viewHolder.setAvatar(avatar);

        if (!mUsersInvolved.get(position)) {
            viewHolder.setAlpha(DISABLED_ALPHA);
        } else {
            viewHolder.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        return mUsersAvailable.size();
    }

    public interface AdapterInteractionListener {
        public void onPurchaseUserClick(int position);
    }

}
