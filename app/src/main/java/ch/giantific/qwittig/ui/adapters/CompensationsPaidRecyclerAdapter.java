package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.Avatar;
import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.adapters.rows.ProgressRow;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;


/**
 * Created by fabio on 12.10.14.
 */
public class CompensationsPaidRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_PROGRESS = 1;
    private int mViewResource;
    private Context mContext;
    private List<ParseObject> mCompensations;
    private String mCurrentGroupCurrency;

    public CompensationsPaidRecyclerAdapter(Context context, int viewResource,
                                            List<ParseObject> compensations) {
        super();

        mContext = context;
        mViewResource = viewResource;
        mCompensations = compensations;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM: {
                View view = LayoutInflater.from(parent.getContext()).inflate(mViewResource, parent, false);
                return new CompensationHistoryRow(view, mContext);
            }
            case TYPE_PROGRESS: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_progress, parent,
                        false);
                return new ProgressRow(view);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_ITEM: {
                CompensationHistoryRow compensationHistoryRow = (CompensationHistoryRow) viewHolder;
                Compensation compensation = (Compensation) mCompensations.get(position);

                Date date = compensation.getCreatedAt();
                compensationHistoryRow.setDate(date);

                BigFraction amount = compensation.getAmount();
                String amountString;
                String nickname;
                byte[] avatar;
                int color;

                User currentUser = (User) ParseUser.getCurrentUser();
                User beneficiary = compensation.getBeneficiary();
                if (beneficiary.getObjectId().equals(currentUser.getObjectId())) {
                    // positive
                    User payer = compensation.getPayer();
                    nickname = payer.getGroupIds().contains(currentUser.getCurrentGroup().getObjectId()) ?
                            payer.getNickname() : mContext.getString(R.string.user_deleted);
                    avatar = payer.getAvatar();
                    color = R.color.green;
                    amountString = MoneyUtils.formatMoney(amount, mCurrentGroupCurrency);
                } else {
                    // negative
                    nickname = beneficiary.getGroupIds().contains(currentUser.getCurrentGroup().getObjectId()) ?
                            beneficiary.getNickname() : mContext.getString(R.string.user_deleted);
                    avatar = beneficiary.getAvatar();
                    color = R.color.red;
                    amountString = MoneyUtils.formatMoney(amount.negate(), mCurrentGroupCurrency);
                }

                compensationHistoryRow.setAvatar(avatar);
                compensationHistoryRow.setUser(nickname);
                compensationHistoryRow.setAmount(amountString, ContextCompat.getColor(mContext, color));

//                int showDivider = position == getItemCount() - 1 ? View.GONE : View.VISIBLE;
//                compensationHistoryRow.toggleDividerVisibility(showDivider);

                break;
            }
            case TYPE_PROGRESS:
                // do nothing
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mCompensations.get(position) == null) {
            return TYPE_PROGRESS;
        }

        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mCompensations.size();
    }

    /**
     * Returns the position of the last movie in the adapter.
     *
     * @return the position of the last movie, -1 if there are no movies
     */
    public int getLastPosition() {
        return getItemCount() - 1;
    }

    public void setCurrentGroupCurrency(String currentGroupCurrency) {
        mCurrentGroupCurrency = currentGroupCurrency;
    }

    /**
     * Adds compensations to the adapter.
     *
     * @param compensations the compensations to be added
     */
    public void addCompensations(List<ParseObject> compensations) {
        if (!compensations.isEmpty()) {
            mCompensations.addAll(compensations);
            notifyItemRangeInserted(getItemCount(), compensations.size());
        }
    }

    /**
     * Shows a progressbar in the last row as an indicator that more objects are being fetched.
     */
    public void showLoadMoreIndicator() {
        mCompensations.add(null);
        notifyItemInserted(getLastPosition());
    }

    /**
     * Hides the progressbar in the last row.
     */
    public void hideLoadMoreIndicator() {
        int position = getLastPosition();
        mCompensations.remove(position);
        notifyItemRemoved(position);
    }

    private static class CompensationHistoryRow extends RecyclerView.ViewHolder {

        private Context mContext;
        private View mDivider;
        private ImageView mImageViewAvatar;
        private TextView mTextViewDate;
        private TextView mTextViewUser;
        private TextView mTextViewAmount;

        public CompensationHistoryRow(View view, Context context) {
            super(view);

            mContext = context;
            mDivider = view.findViewById(R.id.divider);
            mImageViewAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
            mTextViewDate = (TextView) view.findViewById(R.id.tv_date);
            mTextViewUser = (TextView) view.findViewById(R.id.tv_user);
            mTextViewAmount = (TextView) view.findViewById(R.id.tv_amount);
        }

        public void toggleDividerVisibility(int visibility) {
            mDivider.setVisibility(visibility);
        }

        public void setAvatar(byte[] avatar) {
            if (avatar != null) {
                Glide.with(mContext)
                        .load(avatar)
                        .asBitmap()
                        .into(new BitmapImageViewTarget(mImageViewAvatar) {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                view.setImageDrawable(Avatar.getRoundedDrawable(mContext, resource, false));
                            }
                        });
            } else {
                mImageViewAvatar.setImageDrawable(Avatar.getFallbackDrawable(mContext, false, false));
            }
        }

        public void setDate(Date date) {
            mTextViewDate.setText(DateUtils.formatDateShort(date));
        }

        public void setUser(String user) {
            mTextViewUser.setText(user);
        }

        public void setAmount(String amount, @ColorInt int color) {
            mTextViewAmount.setText(amount);
            mTextViewAmount.setTextColor(color);
        }
    }
}
