package ch.giantific.qwittig.ui.adapter.rows;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.ui.adapter.PurchaseAddUsersInvolvedRecyclerAdapter;

/**
* Created by fabio on 28.03.15.
*/
public class UserInvolvedRow extends RecyclerView.ViewHolder {

    private TextView mTextViewName;
    private ImageView mImageViewAvatar;

    public UserInvolvedRow(View view, final PurchaseAddUsersInvolvedRecyclerAdapter.
            AdapterInteractionListener listener) {
        super(view);

        if (listener != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onPurchaseUserClick(getAdapterPosition());
                }
            });
        }

        mTextViewName = (TextView) view.findViewById(R.id.user_name);
        mImageViewAvatar = (ImageView) view.findViewById(R.id.user_avatar);
    }

    public void setName(String name) {
        mTextViewName.setText(name);
    }

    public void setNameBold() {
        mTextViewName.setTypeface(null, Typeface.BOLD);
    }

    public void setAvatar(Drawable avatar) {
        mImageViewAvatar.setImageDrawable(avatar);
    }

    public void setAlpha(float alpha) {
        mImageViewAvatar.setAlpha(alpha);
        mTextViewName.setAlpha(alpha);
    }
}
