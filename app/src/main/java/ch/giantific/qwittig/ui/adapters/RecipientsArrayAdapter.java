/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.utils.AvatarUtils;
import ch.giantific.qwittig.domain.models.ItemUserPicker;
import ch.giantific.qwittig.ui.fragments.dialogs.CompensationSingleDialogFragment;


/**
 * Handles the display of the recipients in the {@link CompensationSingleDialogFragment}, includes
 * the avatar image and the nickname of the user
 * <p/>
 * Subclass of {@link ArrayAdapter}.
 */
public class RecipientsArrayAdapter extends ArrayAdapter<ItemUserPicker> {

    private static final int VIEW_RESOURCE = R.layout.spinner_item_with_image;
    private static final int DROP_DOWN_VIEW_RESOURCE = R.layout.row_spinner_recipients;
    private List<ItemUserPicker> mUsers;
    private Context mContext;

    /**
     * Constructs a new {@link RecipientsArrayAdapter}.
     *
     * @param context the context to use in the adapter
     * @param users   the users (recipients) to display
     */
    public RecipientsArrayAdapter(@NonNull Context context, @NonNull List<ItemUserPicker> users) {
        super(context, VIEW_RESOURCE, users);

        mContext = context;
        mUsers = users;
    }

    @Nullable
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent, false);
    }

    @Nullable
    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent, true);
    }

    @Nullable
    private View getCustomView(int position, @Nullable View convertView, @NonNull ViewGroup parent,
                               boolean isDropDown) {
        final RecipientRow recipientRow;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    isDropDown ? DROP_DOWN_VIEW_RESOURCE : VIEW_RESOURCE, parent, false);
            recipientRow = new RecipientRow(convertView);
            convertView.setTag(recipientRow);
        } else {
            recipientRow = (RecipientRow) convertView.getTag();
        }

        ItemUserPicker user = mUsers.get(position);
        recipientRow.setNickname(user.getNickname());

        byte[] avatarByteArray = user.getAvatar();
        recipientRow.setAvatar(avatarByteArray, mContext, false);

        return convertView;
    }

    /**
     * Provides an adapter row that displays a recipient's avatar image and nickname.
     */
    private static class RecipientRow {

        private ImageView mImageViewAvatar;
        private TextView mTextViewName;

        /**
         * Constructs a new {@link RecipientRow}.
         *
         * @param view the inflated view
         */
        public RecipientRow(@NonNull View view) {
            mImageViewAvatar = (ImageView) view.findViewById(R.id.list_avatar);
            mTextViewName = (TextView) view.findViewById(R.id.list_name);
        }

        /**
         * Sets the nickname of the recipient.
         *
         * @param nickname the nickname to set
         */
        public void setNickname(@NonNull String nickname) {
            mTextViewName.setText(nickname);
        }

        /**
         * Loads the avatar image into the image view if the user has an avatar, if not it loads a
         * fallback drawable.
         *
         * @param avatarBytes the avatar image of the user
         * @param context     the context to use to load the image
         * @param withRipple  whether to use a ripple effect on click or not
         */
        public void setAvatar(@Nullable byte[] avatarBytes, @NonNull final Context context,
                              final boolean withRipple) {
            if (avatarBytes != null) {
                Glide.with(context)
                        .load(avatarBytes)
                        .asBitmap()
                        .into(new BitmapImageViewTarget(mImageViewAvatar) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                view.setImageDrawable(AvatarUtils.getRoundedDrawable(context, resource, withRipple));
                            }
                        });
            } else {
                mImageViewAvatar.setImageDrawable(
                        AvatarUtils.getFallbackDrawable(context, false, withRipple));
            }
        }
    }
}
