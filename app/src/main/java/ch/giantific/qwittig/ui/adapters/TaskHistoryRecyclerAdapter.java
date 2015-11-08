/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.TaskHistory;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.ui.adapters.rows.UserAvatarRow;
import ch.giantific.qwittig.ui.adapters.rows.HeaderRow;
import ch.giantific.qwittig.utils.DateUtils;


/**
 * Handles the display of users who already completed a task by showing the user and the date
 * he/she completed the task on.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class TaskHistoryRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;
    private static final int VIEW_RESOURCE = R.layout.row_task_details_history;
    private List<TaskHistory> mTaskHistory;
    private Context mContext;

    /**
     * Constructs a new {@link TaskHistoryRecyclerAdapter}.
     *
     * @param context     the context to use in the adapter
     * @param taskHistory the task history to display
     */
    public TaskHistoryRecyclerAdapter(@NonNull Context context,
                                      @NonNull List<TaskHistory> taskHistory) {
        super();

        mContext = context;
        mTaskHistory = taskHistory;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM: {
                View v = LayoutInflater.from(parent.getContext()).inflate(VIEW_RESOURCE, parent,
                        false);
                return new TaskHistoryRow(v, mContext);
            }
            case TYPE_HEADER: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(HeaderRow.VIEW_RESOURCE, parent, false);
                return new HeaderRow(v);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }

        return TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_ITEM: {
                final TaskHistoryRow historyRow = (TaskHistoryRow) viewHolder;
                TaskHistory taskHistory = mTaskHistory.get(position - 1);
                User user = taskHistory.getUser();

                historyRow.setDate(taskHistory.getDate());
                historyRow.setName(user.getNickname());
                historyRow.setAvatar(user.getAvatar(), false);
                break;
            }
            case TYPE_HEADER: {
                final HeaderRow headerRow = (HeaderRow) viewHolder;
                headerRow.setHeader(mContext.getString(R.string.header_task_history));
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mTaskHistory.size() + 1;
    }

    /**
     * Provides a {@link RecyclerView} row that displays a user's avatar image, the nickname
     * and the date he/she completed the task on.
     * <p/>
     * Subclass of {@link UserAvatarRow}.
     */
    private static class TaskHistoryRow extends UserAvatarRow {

        private TextView mTextViewDate;

        /**
         * Constructs a new {@link TaskHistoryRow}.
         *
         * @param view    the inflated view
         * @param context the context to use to load the avatar image
         */
        public TaskHistoryRow(@NonNull View view, @NonNull Context context) {
            super(view, context);

            mTextViewDate = (TextView) view.findViewById(R.id.tv_task_details_history_date);
        }

        /**
         * Sets the date the task was completed on
         *
         * @param date the date to set
         */
        public void setDate(@NonNull Date date) {
            mTextViewDate.setText(DateUtils.formatDateLong(date));
        }
    }
}
