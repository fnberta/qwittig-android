/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import org.apache.commons.math3.fraction.BigFraction;

import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.utils.AvatarUtils;
import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.presentation.ui.adapters.rows.ProgressRow;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;


/**
 * Handles the display of different paid compensations.
 * <p/>
 * Subclass of {@link BaseLoadMoreRecyclerAdapter}.
 */
public class CompensationsPaidRecyclerAdapter extends BaseLoadMoreRecyclerAdapter<ParseObject> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_PROGRESS = 1;
    private static final int VIEW_RESOURCE = R.layout.row_compensations_paid;
    private User mCurrentUser;

    /**
     * Constructs a new {@link CompensationsPaidRecyclerAdapter}.
     *
     * @param context       the context to use in the adapter
     * @param compensations the compensations to display
     */
    public CompensationsPaidRecyclerAdapter(@NonNull Context context,
                                            @NonNull List<ParseObject> compensations,
                                            @NonNull User currentUser) {
        super(context, compensations);

        mCurrentUser = currentUser;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM: {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(VIEW_RESOURCE, parent, false);
                return new CompensationHistoryRow(view, mContext);
            }
            case TYPE_PROGRESS: {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(ProgressRow.VIEW_RESOURCE, parent, false);
                return new ProgressRow(view);
            }
            default:
                return super.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == TYPE_ITEM) {
            CompensationHistoryRow compensationHistoryRow = (CompensationHistoryRow) viewHolder;
            Compensation compensation = (Compensation) mItems.get(position);

            Date date = compensation.getCreatedAt();
            compensationHistoryRow.setDate(date);

            BigFraction amount = compensation.getAmountFraction();
            String amountString;
            String nickname;
            byte[] avatar;
            int color;
            final User beneficiary = compensation.getBeneficiary();
            if (beneficiary.getObjectId().equals(mCurrentUser.getObjectId())) {
                // positive
                User payer = compensation.getPayer();
                nickname = payer.getGroupIds().contains(mCurrentUser.getCurrentGroup().getObjectId()) ?
                        payer.getNickname() : mContext.getString(R.string.user_deleted);
                avatar = payer.getAvatar();
                color = R.color.green;
                amountString = MoneyUtils.formatMoney(amount, mCurrentGroupCurrency);
            } else {
                // negative
                nickname = beneficiary.getGroupIds().contains(mCurrentUser.getCurrentGroup().getObjectId()) ?
                        beneficiary.getNickname() : mContext.getString(R.string.user_deleted);
                avatar = beneficiary.getAvatar();
                color = R.color.red;
                amountString = MoneyUtils.formatMoney(amount.negate(), mCurrentGroupCurrency);
            }
            compensationHistoryRow.setAvatar(avatar);
            compensationHistoryRow.setUser(nickname);
            compensationHistoryRow.setAmount(amountString, ContextCompat.getColor(mContext, color));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mItems.get(position) == null) {
            return TYPE_PROGRESS;
        }

        return TYPE_ITEM;
    }

    /**
     * Provides a {@link RecyclerView} row that displays paid compensations.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class CompensationHistoryRow extends RecyclerView.ViewHolder {

        private Context mContext;
        private ImageView mImageViewAvatar;
        private TextView mTextViewDate;
        private TextView mTextViewUser;
        private TextView mTextViewAmount;

        /**
         * Constructs a new {@link CompensationHistoryRow}.
         *
         * @param view    the inflated view
         * @param context the context to use in the row
         */
        public CompensationHistoryRow(@NonNull View view, @NonNull Context context) {
            super(view);

            mContext = context;
            mImageViewAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
            mTextViewDate = (TextView) view.findViewById(R.id.tv_date);
            mTextViewUser = (TextView) view.findViewById(R.id.tv_user);
            mTextViewAmount = (TextView) view.findViewById(R.id.tv_amount);
        }

        /**
         * Loads the avatar image into the image view if the user has an avatar, if not it loads a
         * fallback drawable.
         *
         * @param avatar the user's avatar image
         */
        public void setAvatar(@Nullable byte[] avatar) {
            if (avatar != null) {
                Glide.with(mContext)
                        .load(avatar)
                        .asBitmap()
                        .into(new BitmapImageViewTarget(mImageViewAvatar) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                view.setImageDrawable(AvatarUtils.getRoundedDrawable(mContext, resource, false));
                            }
                        });
            } else {
                mImageViewAvatar.setImageDrawable(AvatarUtils.getFallbackDrawable(mContext, false, false));
            }
        }

        /**
         * Sets the date of the compensation.
         *
         * @param date the date to set
         */
        public void setDate(@NonNull Date date) {
            mTextViewDate.setText(DateUtils.formatDateShort(date));
        }

        /**
         * Sets the nickname of the payer or beneficiary
         *
         * @param nickname the nickname to set
         */
        public void setUser(@NonNull String nickname) {
            mTextViewUser.setText(nickname);
        }

        /**
         * Sets the amount of the compensation
         *
         * @param amount the amount to set
         * @param color  the color to mark the text, red if user owes money or green if he receives
         *               money
         */
        public void setAmount(@NonNull String amount, @ColorInt int color) {
            mTextViewAmount.setText(amount);
            mTextViewAmount.setTextColor(color);
        }
    }
}
