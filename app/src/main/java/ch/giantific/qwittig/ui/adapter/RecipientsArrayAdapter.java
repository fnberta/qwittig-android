package ch.giantific.qwittig.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.ImageAvatar;
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
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();

            int viewResource;
            if (isDropDown) {
                viewResource = mDropDownViewResource;
            } else {
                viewResource = mViewResource;
            }
            convertView = LayoutInflater.from(parent.getContext()).inflate(viewResource, parent,
                    false);
            viewHolder.ivAvatar = (ImageView) convertView.findViewById(R.id.list_avatar);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.list_name);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ItemUserPicker user = mUsers.get(position);
        viewHolder.tvName.setText(user.getNickname());

        byte[] avatarByteArray = user.getAvatar();
        Drawable avatar = ImageAvatar.getRoundedAvatar(mContext, avatarByteArray, false);
        viewHolder.ivAvatar.setImageDrawable(avatar);

        return convertView;
    }

    private static class ViewHolder {

        private ImageView ivAvatar;
        private TextView tvName;
    }
}
