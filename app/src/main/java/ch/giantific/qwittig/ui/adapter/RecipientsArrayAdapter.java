package ch.giantific.qwittig.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import ch.giantific.qwittig.data.models.Avatar;
import ch.giantific.qwittig.data.models.ItemUserPicker;


/**
 * Created by fabio on 12.10.14.
 */
public class RecipientsArrayAdapter extends ArrayAdapter<ItemUserPicker> {

    private int mViewResource;
    private int mDropDownViewResource;
    private List<ItemUserPicker> mUsers;
    private Context mContext;

    public RecipientsArrayAdapter(Context context, int viewResource, int dropDownViewResource,
                                  List<ItemUserPicker> users) {
        super(context, viewResource, users);

        mContext = context;
        mViewResource = viewResource;
        mDropDownViewResource = dropDownViewResource;
        mUsers = users;
        setDropDownViewResource(dropDownViewResource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent, false);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent, true);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent,
                               boolean isDropDown) {
        final RecipientRow recipientRow;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    isDropDown ? mDropDownViewResource : mViewResource, parent, false);
            recipientRow = new RecipientRow(convertView);
            convertView.setTag(recipientRow);
        } else {
            recipientRow = (RecipientRow) convertView.getTag();
        }

        ItemUserPicker user = mUsers.get(position);
        recipientRow.setUsername(user.getNickname());

        byte[] avatarByteArray = user.getAvatar();
        recipientRow.setAvatar(avatarByteArray, mContext, false);

        return convertView;
    }

    private static class RecipientRow {

        private ImageView mImageViewAvatar;
        private TextView mTextViewName;

        public RecipientRow(View view) {
            mImageViewAvatar = (ImageView) view.findViewById(R.id.list_avatar);
            mTextViewName = (TextView) view.findViewById(R.id.list_name);
        }

        public void setUsername(String username) {
            mTextViewName.setText(username);
        }

        public void setAvatar(byte[] avatarBytes, final Context context, final boolean withRipple) {
            if (avatarBytes != null) {
                Glide.with(context)
                        .load(avatarBytes)
                        .asBitmap()
                        .into(new BitmapImageViewTarget(mImageViewAvatar) {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                view.setImageDrawable(Avatar.getRoundedDrawable(context, resource, withRipple));
                            }
                        });
            } else {
                setAvatar(Avatar.getFallbackDrawable(context, false, withRipple));
            }
        }

        public void setAvatar(Drawable avatar) {
            mImageViewAvatar.setImageDrawable(avatar);
        }
    }
}
