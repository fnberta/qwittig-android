/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters.rows;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Avatar;

/**
 * Provides an abstract base class for a {@link RecyclerView} row with a user's avatar and nickname.
 * <p/>
 * Subclass of {@link RecyclerView.ViewHolder}.
 */
public class UserAvatarRow extends RecyclerView.ViewHolder {

    protected Context mContext;
    private TextView mTextViewName;
    private ImageView mImageViewAvatar;

    /**
     * Constructs a new {@link UserAvatarRow}.
     *
     * @param view    the inflated view
     * @param context the context to use in the row
     */
    public UserAvatarRow(@NonNull View view, @NonNull Context context) {
        super(view);

        mContext = context;
        mTextViewName = (TextView) view.findViewById(R.id.user_name);
        mImageViewAvatar = (ImageView) view.findViewById(R.id.user_avatar);
    }

    /**
     * Sets the name of the user.
     *
     * @param name the name to set
     */
    public void setName(@NonNull String name) {
        mTextViewName.setText(name);
    }

    /**
     * Makes the name bold.
     */
    public void setNameBold() {
        mTextViewName.setTypeface(null, Typeface.BOLD);
    }

    /**
     * Loads the avatar image into the image view if the user has an avatar, if not it loads a
     * fallback drawable.
     *
     * @param avatarBytes the user's avatar image
     * @param withRipple  whether to show a ripple effect on click
     */
    public void setAvatar(@Nullable byte[] avatarBytes, final boolean withRipple) {
        if (avatarBytes != null) {
            Glide.with(mContext)
                    .load(avatarBytes)
                    .asBitmap()
                    .into(new BitmapImageViewTarget(mImageViewAvatar) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            view.setImageDrawable(Avatar.getRoundedDrawable(mContext, resource, withRipple));
                        }
                    });
        } else {
            mImageViewAvatar.setImageDrawable(Avatar.getFallbackDrawable(mContext, false, withRipple));
        }
    }

    /**
     * Sets the alpha of the avatar image and the nickname to the specified value.
     *
     * @param alpha the alpha value to set
     */
    public void setAlpha(@FloatRange(from=0.0, to=1.0) float alpha) {
        mImageViewAvatar.setAlpha(alpha);
        mTextViewName.setAlpha(alpha);
    }
}
