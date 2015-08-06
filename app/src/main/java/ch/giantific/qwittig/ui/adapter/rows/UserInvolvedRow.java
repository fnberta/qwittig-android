package ch.giantific.qwittig.ui.adapter.rows;

import android.content.Context;
import android.view.View;

import ch.giantific.qwittig.ui.adapter.PurchaseAddUsersInvolvedRecyclerAdapter;

/**
* Created by fabio on 28.03.15.
*/
public class UserInvolvedRow extends BaseUserAvatarRow {

    public UserInvolvedRow(View view, Context context, final PurchaseAddUsersInvolvedRecyclerAdapter.
            AdapterInteractionListener listener) {
        super(view, context);

        if (listener != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onPurchaseUserClick(getAdapterPosition());
                }
            });
        }
    }
}
