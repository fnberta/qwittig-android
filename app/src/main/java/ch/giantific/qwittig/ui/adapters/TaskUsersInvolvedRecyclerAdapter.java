package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
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
 * Created by fabio on 12.10.14.
 */
public class TaskUsersInvolvedRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ItemTouchHelperAdapter {

    private static final String LOG_TAG = TaskUsersInvolvedRecyclerAdapter.class.getSimpleName();

    private AdapterInteractionListener mListener;
    private int mViewResource;
    private List<ParseUser> mUsersAvailable;
    private List<TaskUser> mUsersInvolved;
    private Context mContext;

    public TaskUsersInvolvedRecyclerAdapter(Context context, int viewResource,
                                            List<ParseUser> usersAvailable,
                                            List<TaskUser> usersInvolved,
                                            AdapterInteractionListener listener) {
        super();

        mListener = listener;
        mContext = context;
        mViewResource = viewResource;
        mUsersAvailable = usersAvailable;
        mUsersInvolved = usersInvolved;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mViewResource, parent,
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

    public interface AdapterInteractionListener {
        void onUsersRowItemClick(int position);

        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    private static class UsersRow extends BaseUserAvatarRow {

        private ImageView mImageViewReorder;

        public UsersRow(final View view, Context context, final AdapterInteractionListener listener) {
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
                public boolean onTouch(View v, MotionEvent event) {
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
