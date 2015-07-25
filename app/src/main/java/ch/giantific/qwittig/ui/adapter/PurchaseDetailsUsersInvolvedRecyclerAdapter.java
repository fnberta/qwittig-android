package ch.giantific.qwittig.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.ImageAvatar;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.adapter.rows.UserInvolvedRow;
import ch.giantific.qwittig.utils.ComparatorParseUserIgnoreCase;

import static ch.giantific.qwittig.constants.AppConstants.DISABLED_ALPHA;

/**
 * Created by fabio on 09.11.14.
 */
public class PurchaseDetailsUsersInvolvedRecyclerAdapter extends
        RecyclerView.Adapter<UserInvolvedRow> {

    private Context mContext;
    private int mViewResource;
    private List<ParseUser> mUsersInvolved;
    private List<ParseUser> mUsersInvolvedSorted = new ArrayList<>();
    private Purchase mPurchase;

    public PurchaseDetailsUsersInvolvedRecyclerAdapter(Context context, int viewResource,
                                                       ParseObject purchase) {

        mContext = context;
        mViewResource = viewResource;
        mPurchase = (Purchase) purchase;
        setupUsersInvolved();
    }

    private void setupUsersInvolved() {
        mUsersInvolved = mPurchase.getUsersInvolved();

        for (ParseUser parseUser : mUsersInvolved) {
            mUsersInvolvedSorted.add(parseUser);
        }

        User buyer = (User) mPurchase.getBuyer();
        if (mUsersInvolvedSorted.contains(buyer)) {
            mUsersInvolvedSorted.remove(buyer);
        }
        Collections.sort(mUsersInvolvedSorted, new ComparatorParseUserIgnoreCase());
        mUsersInvolvedSorted.add(0, buyer);
    }

    @Override
    public UserInvolvedRow onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(mViewResource, parent, false);

        return new UserInvolvedRow(v, null);
    }

    @Override
    public void onBindViewHolder(final UserInvolvedRow viewHolder, int position) {
        User user = (User) mUsersInvolvedSorted.get(position);

        User buyer = (User) mPurchase.getBuyer();
        if (buyer.getObjectId().equals(user.getObjectId())) {
            viewHolder.setNameBold();
        }

        User currentUser = (User) ParseUser.getCurrentUser();
        Group currentGroup = currentUser.getCurrentGroup();
        String nickname;
        if (user.getGroupIds().contains(currentGroup.getObjectId())) {
            nickname = user.getNicknameOrMe(mContext);
        } else {
            nickname = mContext.getString(R.string.user_deleted);
        }
        viewHolder.setName(nickname);

        byte[] avatarByteArray = user.getAvatar();
        Drawable avatar = ImageAvatar.getRoundedAvatar(mContext, avatarByteArray, false);
        viewHolder.setAvatar(avatar);

        if (!mUsersInvolved.contains(user)) {
            viewHolder.setAlpha(DISABLED_ALPHA);
        }
    }

    @Override
    public int getItemCount() {
        return mUsersInvolvedSorted.size();
    }
}
