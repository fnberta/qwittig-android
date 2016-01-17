/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
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

import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.utils.AvatarUtils;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.presentation.ui.adapters.rows.ProgressRow;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Handles the display of recent purchases.
 * <p/>
 * Subclass of {@link BaseLoadMoreRecyclerAdapter}.
 */
public class PurchasesRecyclerAdapter extends BaseLoadMoreRecyclerAdapter<ParseObject> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_PROGRESS = 1;
    private static final String LOG_TAG = PurchasesRecyclerAdapter.class.getSimpleName();
    private static final int VIEW_RESOURCE = R.layout.row_purchases;
    private AdapterInteractionListener mListener;
    private User mCurrentUser;

    /**
     * Constructs a new {@link PurchasesRecyclerAdapter}.
     *
     * @param context   the context to use in the adapter
     * @param purchases the purchases to display
     * @param listener  the callback for user clicks on the purchases
     */
    public PurchasesRecyclerAdapter(@NonNull Context context, @NonNull List<ParseObject> purchases,
                                    @NonNull User currentUser,
                                    @NonNull AdapterInteractionListener listener) {
        super(context, purchases);

        mListener = listener;
        mCurrentUser = currentUser;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM: {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(VIEW_RESOURCE, parent, false);
                return new PurchaseRow(view, mContext, mListener);
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
    public int getItemViewType(int position) {
        if (mItems.get(position) == null) {
            return TYPE_PROGRESS;
        }

        return TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_ITEM: {
                PurchaseRow purchaseRow = (PurchaseRow) viewHolder;
                Purchase purchase = (Purchase) mItems.get(position);

                final User buyer = purchase.getBuyer();
                final boolean buyerIsValid = buyer.getGroupIds().contains(
                        mCurrentUser.getCurrentGroup().getObjectId());

                purchaseRow.setAvatar(buyerIsValid ? buyer.getAvatar() : null);
                purchaseRow.setStore(purchase.getStore());

                String nickname = buyerIsValid ?
                        buyer.getNicknameOrMe(mContext, mCurrentUser) :
                        mContext.getString(R.string.user_deleted);
                purchaseRow.setBuyerAndDate(nickname, purchase.getDate());

                double totalPrice = purchase.getTotalPrice();
                purchaseRow.setTotal(MoneyUtils.formatMoneyNoSymbol(totalPrice, mCurrentGroupCurrency));
                double myShare = purchase.calculateUserShare(mCurrentUser);
                purchaseRow.setMyShare(MoneyUtils.formatMoneyNoSymbol(myShare, mCurrentGroupCurrency));

                if (!purchase.userHasReadPurchase(mCurrentUser)) {
                    purchaseRow.setWhiteBackground();
                } else {
                    purchaseRow.resetBackground();
                }

                break;
            }
            case TYPE_PROGRESS:
                // do nothing
                break;
        }
    }

    /**
     * Defines the actions to take when a user clicks on a purchase.
     */
    public interface AdapterInteractionListener {
        /**
         * Handles the click on a purchase.
         *
         * @param position the adapter position of the purchase
         */
        void onPurchaseRowItemClick(int position);
    }

    /**
     * Provides a {@link RecyclerView} row that displays a purchase.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class PurchaseRow extends RecyclerView.ViewHolder {

        private Context mContext;
        private View mView;
        private ImageView mImageViewAvatar;
        private TextView mTextViewStore;
        private TextView mTextViewBuyerDate;
        private TextView mTextViewMyShare;
        private TextView mTextViewTotal;

        /**
         * Constructs a new {@link PurchaseRow} and sets the click listener.
         *
         * @param view     the inflated row
         * @param context  the context to use in the adapter
         * @param listener the callback for user clicks on the purchase
         */
        public PurchaseRow(@NonNull View view, @NonNull Context context,
                           @NonNull final AdapterInteractionListener listener) {
            super(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onPurchaseRowItemClick(getAdapterPosition());
                }
            });

            mContext = context;
            mView = view.findViewById(R.id.rl_purchase);
            mImageViewAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
            mTextViewStore = (TextView) view.findViewById(R.id.tv_store);
            mTextViewBuyerDate = (TextView) view.findViewById(R.id.tv_user_date);
            mTextViewTotal = (TextView) view.findViewById(R.id.tv_total);
            mTextViewMyShare = (TextView) view.findViewById(R.id.tv_my_share);
        }

        /**
         * Sets the background of the purchase to white, marking it as unread. Uses a ripple effect
         * if running on Lollipop an higher, otherwise not.
         */
        public void setWhiteBackground() {
            if (Utils.isRunningLollipopAndHigher()) {
                mView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ripple_white));
            } else {
                mView.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.white));
            }
        }

        /**
         * Resets the background of the purchase to the default selectable item background.
         */
        public void resetBackground() {
            int[] attrs = new int[]{R.attr.selectableItemBackground};
            TypedArray typedArray = mContext.obtainStyledAttributes(attrs);
            int backgroundResource = typedArray.getResourceId(0, 0);
            typedArray.recycle();
            mView.setBackgroundResource(backgroundResource);
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
         * Sets the buyer's nickname and the properly formatted date of the purchase.
         *
         * @param buyer the buyer's nickname to set
         * @param date  the date to set
         */
        public void setBuyerAndDate(@NonNull String buyer, @NonNull Date date) {
            String userAndDate = String.format("%s, %s", buyer, DateUtils.formatDateShort(date));
            mTextViewBuyerDate.setText(userAndDate);
        }

        /**
         * Sets the store name of the purchase.
         *
         * @param store the store name to set
         */
        public void setStore(@NonNull String store) {
            mTextViewStore.setText(store);
        }

        /**
         * Sets the total price of the purchase.
         *
         * @param total the total price to set
         */
        public void setTotal(@NonNull String total) {
            mTextViewTotal.setText(total);
        }

        /**
         * Sets the share of the current user of the purchase.
         *
         * @param myShare the share of the current user to set
         */
        public void setMyShare(@NonNull String myShare) {
            mTextViewMyShare.setText(myShare);
        }
    }
}
