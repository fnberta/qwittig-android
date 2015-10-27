/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters;

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
 * Handles the display of different paid compensations.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class CompensationsPaidRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_PROGRESS = 1;
    private static final int VIEW_RESOURCE = R.layout.row_compensations_paid;
    private Context mContext;
    private List<ParseObject> mCompensations;
    private String mCurrentGroupCurrency;

    /**
     * Constructs a new {@link CompensationsPaidRecyclerAdapter}.
     *
     * @param context       the context to use in the adapter
     * @param compensations the compensations to display
     */
    public CompensationsPaidRecyclerAdapter(@NonNull Context context,
                                            @NonNull List<ParseObject> compensations) {
        super();

        mContext = context;
        mCompensations = compensations;
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
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == TYPE_ITEM) {
            CompensationHistoryRow compensationHistoryRow = (CompensationHistoryRow) viewHolder;
            Compensation compensation = (Compensation) mCompensations.get(position);
            Date date = compensation.getCreatedAt();
            compensationHistoryRow.setDate(date);
            BigFraction amount = compensation.getAmountFraction();
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
    public void addCompensations(@NonNull List<ParseObject> compensations) {
        if (!compensations.isEmpty()) {
            mCompensations.addAll(compensations);
            notifyItemRangeInserted(getItemCount(), compensations.size());
        }
    }

    /**
     * Shows a progress bar in the last row as an indicator that more objects are being fetched.
     */
    public void showLoadMoreIndicator() {
        mCompensations.add(null);
        notifyItemInserted(getLastPosition());
    }

    /**
     * Hides the progress bar in the last row.
     */
    public void hideLoadMoreIndicator() {
        int position = getLastPosition();
        mCompensations.remove(position);
        notifyItemRemoved(position);
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
                                view.setImageDrawable(Avatar.getRoundedDrawable(mContext, resource, false));
                            }
                        });
            } else {
                mImageViewAvatar.setImageDrawable(Avatar.getFallbackDrawable(mContext, false, false));
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
