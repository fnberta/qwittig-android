package ch.giantific.qwittig.ui.adapter;

import android.content.Context;
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

    private static final String LOG_TAG = PurchaseDetailsUsersInvolvedRecyclerAdapter.class.getSimpleName();
    private Context mContext;
    private int mViewResource;
    private Purchase mPurchase;
    private List<ParseUser> mUsersInvolved;
    private List<ParseUser> mUsersInvolvedSorted = new ArrayList<>();
    private User mBuyer;

    public PurchaseDetailsUsersInvolvedRecyclerAdapter(Context context, int viewResource) {

        mContext = context;
        mViewResource = viewResource;
    }

    @Override
    public UserInvolvedRow onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(mViewResource, parent, false);

        return new UserInvolvedRow(v, mContext, null);
    }

    @Override
    public void onBindViewHolder(final UserInvolvedRow viewHolder, int position) {
        if (mPurchase == null) {
            return;
        }

        User user = (User) mUsersInvolvedSorted.get(position);

        if (mBuyer.getObjectId().equals(user.getObjectId())) {
            viewHolder.setNameBold();
        }

        String nickname = userIsInGroup(user) ? user.getNicknameOrMe(mContext) :
                mContext.getString(R.string.user_deleted);
        viewHolder.setName(nickname);

        byte[] avatarByteArray = user.getAvatar();
        viewHolder.setAvatar(avatarByteArray, false);

        if (!mUsersInvolved.contains(user)) {
            viewHolder.setAlpha(DISABLED_ALPHA);
        }
    }

    private boolean userIsInGroup(User user) {
        return user.getGroupIds().contains(mPurchase.getGroup().getObjectId());
    }

    @Override
    public int getItemCount() {
        return mUsersInvolvedSorted.size();
    }

    public void setPurchase(ParseObject purchase) {
        mPurchase = (Purchase) purchase;
        mBuyer = mPurchase.getBuyer();
        setupUsersInvolved();
    }

    private void setupUsersInvolved() {
        mUsersInvolvedSorted.clear();
        mUsersInvolved = mPurchase.getUsersInvolved();

        for (ParseUser parseUser : mUsersInvolved) {
            mUsersInvolvedSorted.add(parseUser);
        }

        if (mUsersInvolvedSorted.contains(mBuyer)) {
            mUsersInvolvedSorted.remove(mBuyer);
        }
        Collections.sort(mUsersInvolvedSorted, new ComparatorParseUserIgnoreCase());
        mUsersInvolvedSorted.add(0, mBuyer);
    }
}
