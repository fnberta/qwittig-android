package ch.giantific.qwittig.ui.adapters.rows;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.Avatar;

/**
* Created by fabio on 28.03.15.
*/
public abstract class BaseUserAvatarRow extends RecyclerView.ViewHolder {

    private TextView mTextViewName;
    private ImageView mImageViewAvatar;
    protected Context mContext;

    public BaseUserAvatarRow(View view, Context context) {
        super(view);

        mContext = context;
        mTextViewName = (TextView) view.findViewById(R.id.user_name);
        mImageViewAvatar = (ImageView) view.findViewById(R.id.user_avatar);
    }

    public void setName(String name) {
        mTextViewName.setText(name);
    }

    public void setNameBold() {
        mTextViewName.setTypeface(null, Typeface.BOLD);
    }

    public void setAvatar(byte[] avatarBytes, final boolean withRipple) {
        if (avatarBytes != null) {
            Glide.with(mContext)
                    .load(avatarBytes)
                    .asBitmap()
                    .into(new BitmapImageViewTarget(mImageViewAvatar) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            view.setImageDrawable(Avatar.getRoundedDrawable(mContext, resource, withRipple));
                        }
                    });
        } else {
            setAvatar(Avatar.getFallbackDrawable(mContext, false, withRipple));
        }
    }

    public void setAvatar(Drawable avatar) {
        mImageViewAvatar.setImageDrawable(avatar);
    }

    public void setAlpha(float alpha) {
        mImageViewAvatar.setAlpha(alpha);
        mTextViewName.setAlpha(alpha);
    }
}
