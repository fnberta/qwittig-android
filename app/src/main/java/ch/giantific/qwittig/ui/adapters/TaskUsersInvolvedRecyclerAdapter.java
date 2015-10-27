/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.parse.ParseUser;

import java.util.Collections;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.TaskUser;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.adapters.rows.BaseUserAvatarRow;

import static ch.giantific.qwittig.utils.AnimUtils.DISABLED_ALPHA;


/**
 * Handles the display of the users involved in a task including the reordering of the different
 * users.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class TaskUsersInvolvedRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ItemTouchHelperAdapter {

    private static final String LOG_TAG = TaskUsersInvolvedRecyclerAdapter.class.getSimpleName();
    private static final int VIEW_RESOURCE = R.layout.row_task_users_involved;
    private AdapterInteractionListener mListener;
    private List<ParseUser> mUsersAvailable;
    private List<TaskUser> mUsersInvolved;
    private Context mContext;

    /**
     * Constructs a new {@link TaskUsersInvolvedRecyclerAdapter}.
     *
     * @param context        the context to use in the adapter
     * @param usersAvailable the users available from the user can make a selection
     * @param usersInvolved  the actual users involved in the task
     * @param listener       the callback for user clicks on the users available
     */
    public TaskUsersInvolvedRecyclerAdapter(@NonNull Context context,
                                            @NonNull List<ParseUser> usersAvailable,
                                            @NonNull List<TaskUser> usersInvolved,
                                            @NonNull AdapterInteractionListener listener) {
        super();

        mListener = listener;
        mContext = context;
        mUsersAvailable = usersAvailable;
        mUsersInvolved = usersInvolved;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(VIEW_RESOURCE, parent,
                false);

        return new UsersRow(view, mContext, mListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final UsersRow usersRow = (UsersRow) viewHolder;
        User user = (User) mUsersAvailable.get(position);
        usersRow.setName(user.getNickname());
        usersRow.setAvatar(user.getAvatar(), false);
        usersRow.setAlpha(mUsersInvolved.get(position).isInvolved() ? 1f : DISABLED_ALPHA);
    }

    @Override
    public int getItemCount() {
        return mUsersAvailable.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mUsersAvailable, fromPosition, toPosition);
        Collections.swap(mUsersInvolved, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        mUsersAvailable.remove(position);
        mUsersInvolved.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Defines the actions to take when a user clicks on a user or drags it to change the order.
     */
    public interface AdapterInteractionListener {
        /**
         * Handles the click on the user row itself.
         *
         * @param position the adapter position of the user row
         */
        void onUsersRowItemClick(int position);

        /**
         * Handles the start of a user drag of the user row.
         *
         * @param viewHolder the view holder for the user row
         */
        void onStartDrag(@NonNull RecyclerView.ViewHolder viewHolder);
    }

    /**
     * Provides a {@link RecyclerView} row that displays the user's avatar, the nickname and a
     * drag handler.
     */
    private static class UsersRow extends BaseUserAvatarRow {

        private ImageView mImageViewReorder;

        /**
         * Constructs a new {@link UsersRow} and sets the click and drag listeners.
         *
         * @param view     the inflated view
         * @param context  the context to use in the row
         * @param listener the callback for user clicks and drags
         */
        public UsersRow(@NonNull final View view, @NonNull Context context,
                        @NonNull final AdapterInteractionListener listener) {
            super(view, context);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onUsersRowItemClick(getAdapterPosition());
                }
            });

            mImageViewReorder = (ImageView) view.findViewById(R.id.iv_reorder);
            final UsersRow usersRow = this;
            mImageViewReorder.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, @NonNull MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) ==
                            MotionEvent.ACTION_DOWN) {
                        listener.onStartDrag(usersRow);
                    }

                    return false;
                }
            });
        }
    }
}
