/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters.rows;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import ch.giantific.qwittig.ui.adapters.PurchaseUsersInvolvedRecyclerAdapter;

/**
 * Provides a {@link RecyclerView} row that displays a user's avatar image and name and listens to
 * clicks on the items.
 * <p/>
 * Subclass of {@link BaseUserAvatarRow}.
 */
public class UserInvolvedRow extends BaseUserAvatarRow {

    /**
     * Constructs a new {@link UserInvolvedRow} and sets the click listener.
     *
     * @param context  the context to use in the row
     * @param view     the inflated view
     * @param listener the callback for when an item is clicked
     */
    public UserInvolvedRow(@NonNull Context context, @NonNull View view,
                           @Nullable final PurchaseUsersInvolvedRecyclerAdapter.
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
