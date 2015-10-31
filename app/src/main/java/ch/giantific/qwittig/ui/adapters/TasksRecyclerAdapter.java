/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Avatar;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.ui.adapters.rows.HeaderRow;
import ch.giantific.qwittig.utils.DateUtils;

/**
 * Handles the display of recent tasks assigned to users in a group.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class TasksRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;
    private static final String LOG_TAG = TasksRecyclerAdapter.class.getSimpleName();
    private static final int VIEW_RESOURCE = R.layout.row_tasks;
    private AdapterInteractionListener mListener;
    private Context mContext;
    private List<ParseObject> mTasks;

    /**
     * Constructs a new {@link TasksRecyclerAdapter}.
     *
     * @param context  the context to use in the adapter
     * @param tasks    the tasks to display
     * @param listener the callback for user clicks on the tasks
     */
    public TasksRecyclerAdapter(@NonNull Context context, @NonNull List<ParseObject> tasks,
                                @NonNull AdapterInteractionListener listener) {

        mContext = context;
        mListener = listener;
        mTasks = tasks;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(VIEW_RESOURCE, parent, false);
                return new TaskRow(v, mContext, mListener);
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
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Task task = (Task) mTasks.get(position);

        switch (getItemViewType(position)) {
            case TYPE_ITEM: {
                TaskRow taskRow = (TaskRow) viewHolder;

                taskRow.setUsersInvolved(task.getUsersInvolved());
                taskRow.setTitle(task.getTitle());
                taskRow.setDeadline(task.getDeadline());
                taskRow.setTimeFrame(task.getTimeFrame());
                taskRow.setProgressBarVisibility(task.isLoading());

                break;
            }
            case TYPE_HEADER: {
                HeaderRow headerRow = (HeaderRow) viewHolder;
                String header;
                if (position == 0) {
                    header = mContext.getString(R.string.task_header_my);
                } else {
                    header = mContext.getString(R.string.task_header_group);
                }
                headerRow.setHeader(header);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mTasks.get(position) == null) {
            return TYPE_HEADER;
        }

        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    /**
     * Defines the actions to take when a user clicks on a task.
     */
    public interface AdapterInteractionListener {
        /**
         * Handles the click on the task row itself.
         *
         * @param position the adapter postition of the task
         */
        void onTaskRowClicked(int position);

        /**
         * Handles the click on the mark task as done button.
         *
         * @param position the adapter postition of the task
         */
        void onDoneButtonClicked(int position);

        /**
         * Handles the click on the remind user to finish a task button.
         *
         * @param position the adapter postition of the task
         */
        void onRemindButtonClicked(int position);
    }

    /**
     * Provides a {@link RecyclerView} row that displays a task with all its information.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class TaskRow extends RecyclerView.ViewHolder {

        private Context mContext;
        private ImageView mImageViewAvatar;
        private TextView mTextViewUserResponsible;
        private TextView mTexViewTitle;
        private TextView mTextViewDeadline;
        private TextView mTextViewTimeFrame;
        private TextView mTextViewUsersInvolved;
        private Button mButtonDone;
        private Button mButtonRemind;
        private ProgressBar mProgressBar;

        /**
         * Constructs a new {@link TaskRow} and sets the click listeners.
         *
         * @param view     the inflated view
         * @param context  the context to use in the row
         * @param listener the callback for user clicks on the task and its buttons
         */
        public TaskRow(@NonNull View view,
                       @NonNull Context context,
                       @NonNull final AdapterInteractionListener listener) {
            super(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onTaskRowClicked(getAdapterPosition());
                }
            });

            mContext = context;
            mImageViewAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
            mTextViewUserResponsible = (TextView) view.findViewById(R.id.tv_task_user_responsible);
            mTexViewTitle = (TextView) view.findViewById(R.id.tv_task_title);
            mTextViewDeadline = (TextView) view.findViewById(R.id.tv_task_deadline);
            mTextViewTimeFrame = (TextView) view.findViewById(R.id.tv_task_time_frame);
            mTextViewUsersInvolved = (TextView) view.findViewById(R.id.tv_task_users_involved);
            mButtonDone = (Button) view.findViewById(R.id.bt_task_done);
            mButtonDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDoneButtonClicked(getAdapterPosition());
                }
            });
            mButtonRemind = (Button) view.findViewById(R.id.bt_task_remind);
            mButtonRemind.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onRemindButtonClicked(getAdapterPosition());
                }
            });
            mProgressBar = (ProgressBar) view.findViewById(R.id.pb_card);
        }

        /**
         * Loads the avatar image into the image view if the user has an avatar, if not it loads a
         * fallback drawable.
         *
         * @param avatar the user's avatar image
         */
        public void setAvatar(@Nullable byte[] avatar) {
            if (avatar != null) {
                Glide.with(mContext)
                        .load(avatar)
                        .asBitmap()
                        .into(new BitmapImageViewTarget(mImageViewAvatar) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                view.setImageDrawable(Avatar.getRoundedDrawable(mContext, resource, false));
                            }
                        });
            } else {
                mImageViewAvatar.setImageDrawable(
                        Avatar.getFallbackDrawable(mContext, false, false));
            }
        }

        /**
         * Sets the title of the task.
         *
         * @param title the title to set
         */
        public void setTitle(@NonNull String title) {
            mTexViewTitle.setText(title);
        }

        /**
         * Sets the deadline when a task needs to be finished the next time. If null is passed in,
         * the deadline will be removed.
         *
         * @param deadline the deadline to set
         */
        public void setDeadline(@Nullable Date deadline) {
            String deadlineString;
            int color = 0;
            if (deadline == null) {
                deadlineString = "";
            } else {
                int daysToDeadline = getDaysToDeadline(deadline);
                if (daysToDeadline == 0) {
                    deadlineString = mContext.getString(R.string.deadline_today);
                    color = R.color.green;
                } else if (daysToDeadline == -1) {
                    deadlineString = mContext.getString(R.string.yesterday);
                    color = R.color.red;
                } else if (daysToDeadline < 0) {
                    deadlineString = mContext.getString(R.string.deadline_text_neg, daysToDeadline * -1);
                    color = R.color.red;
                } else {
                    deadlineString = mContext.getString(R.string.deadline_text_pos, daysToDeadline);
                    color = R.color.green;
                }
            }
            if (!deadlineString.equals(mTextViewDeadline.getText().toString())) {
                mTextViewDeadline.setText(deadlineString);
            }
            if (color != 0) {
                mTextViewDeadline.setTextColor(ContextCompat.getColor(mContext, color));
            }
        }

        /**
         * Calculates the days it takes from today until the deadline of the task is reached.
         *
         * @param deadlineDate the deadline of the task
         * @return the number of days until the deadline is reached
         */
        private int getDaysToDeadline(@NonNull Date deadlineDate) {
            Calendar today = DateUtils.getCalendarInstanceUTC();
            Calendar deadline = DateUtils.getCalendarInstanceUTC();
            deadline.setTime(deadlineDate);

            if (today.get(Calendar.YEAR) == deadline.get(Calendar.YEAR)) {
                return deadline.get(Calendar.DAY_OF_YEAR) - today.get(Calendar.DAY_OF_YEAR);
            }

            int extraDays = 0;
            if (deadline.get(Calendar.YEAR) > today.get(Calendar.YEAR)) {
                while (deadline.get(Calendar.YEAR) > today.get(Calendar.YEAR)) {
                    deadline.add(Calendar.YEAR, -1);
                    // getActualMaximum() important for leap years
                    extraDays += deadline.getActualMaximum(Calendar.DAY_OF_YEAR);
                }

                return extraDays - today.get(Calendar.DAY_OF_YEAR) + deadline.get(Calendar.DAY_OF_YEAR);
            }
            if (deadline.get(Calendar.YEAR) < today.get(Calendar.YEAR)) {
                while (deadline.get(Calendar.YEAR) < today.get(Calendar.YEAR)) {
                    deadline.add(Calendar.YEAR, 1);
                    // getActualMaximum() important for leap years
                    extraDays += deadline.getActualMaximum(Calendar.DAY_OF_YEAR);
                }

                return (extraDays - deadline.get(Calendar.DAY_OF_YEAR) + today.get(Calendar.DAY_OF_YEAR)) * -1;
            }

            return 0;
        }

        /**
         * Sets the time frame of the task, must be one of {@link Task.TimeFrame}.
         *
         * @param timeFrame the time frame to set
         */
        public void setTimeFrame(@NonNull @Task.TimeFrame String timeFrame) {
            String timeFrameLocalized = "";
            String doneButtonText = mContext.getString(R.string.task_done_single);
            switch (timeFrame) {
                case Task.TIME_FRAME_ONE_TIME:
                    timeFrameLocalized = mContext.getString(R.string.time_frame_one_time);
                    doneButtonText = mContext.getString(R.string.task_done_single);
                    break;
                case Task.TIME_FRAME_AS_NEEDED:
                    timeFrameLocalized = mContext.getString(R.string.time_frame_as_needed);
                    doneButtonText = mContext.getString(R.string.task_done_single);
                    break;
                case Task.TIME_FRAME_DAILY:
                    timeFrameLocalized = mContext.getString(R.string.time_frame_daily);
                    doneButtonText = mContext.getString(R.string.task_done_today);
                    break;
                case Task.TIME_FRAME_WEEKLY:
                    timeFrameLocalized = mContext.getString(R.string.time_frame_weekly);
                    doneButtonText = mContext.getString(R.string.task_done_week);
                    break;
                case Task.TIME_FRAME_MONTHLY:
                    timeFrameLocalized = mContext.getString(R.string.time_frame_monthly);
                    doneButtonText = mContext.getString(R.string.task_done_month);
                    break;
                case Task.TIME_FRAME_YEARLY:
                    timeFrameLocalized = mContext.getString(R.string.time_frame_yearly);
                    doneButtonText = mContext.getString(R.string.task_done_year);
                    break;
            }
            if (!timeFrameLocalized.equals(mTextViewTimeFrame.getText().toString())) {
                mTextViewTimeFrame.setText(timeFrameLocalized);
            }
            if (!doneButtonText.equals(mButtonDone.getText().toString())) {
                mButtonDone.setText(doneButtonText);
            }
        }

        /**
         * Sets the users involved in the task.
         *
         * @param usersInvolved the users involved to set
         */
        public void setUsersInvolved(@NonNull List<ParseUser> usersInvolved) {
            User userResponsible = (User) usersInvolved.get(0);
            String nickname = userResponsible.getNicknameOrMe(mContext);
            if (!nickname.equals(mTextViewUserResponsible.getText().toString())) {
                mTextViewUserResponsible.setText(nickname);
            }
            setAvatar(userResponsible.getAvatar());

            String usersInvolvedString = "";
            if (usersInvolved.size() > 1) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(mContext.getString(R.string.task_users_involved_next)).append(" ");
                for (ParseUser parseUser : usersInvolved) {
                    User user = (User) parseUser;
                    if (!user.getObjectId().equals(userResponsible.getObjectId())) {
                        stringBuilder.append(user.getNicknameOrMe(mContext)).append(" - ");
                    }
                }
                // delete last -
                int length = stringBuilder.length();
                stringBuilder.delete(length - 3, length - 1);
                usersInvolvedString = stringBuilder.toString();
            }
            if (!usersInvolvedString.equals(mTextViewUsersInvolved.getText().toString())) {
                mTextViewUsersInvolved.setText(usersInvolvedString);
            }

            toggleButtons(userResponsible);
        }

        private void toggleButtons(@NonNull User userResponsible) {
            ParseUser currentUser = ParseUser.getCurrentUser();

            if (currentUser.getObjectId().equals(userResponsible.getObjectId())) {
                mButtonDone.setVisibility(View.VISIBLE);
                mButtonRemind.setVisibility(View.GONE);
            } else {
                mButtonDone.setVisibility(View.GONE);
                mButtonRemind.setVisibility(View.VISIBLE);
                String remind = mContext.getString(R.string.task_remind_user, userResponsible.getNickname());
                if (!remind.equals(mButtonRemind.getText().toString())) {
                    mButtonRemind.setText(remind);
                }
            }
        }

        /**
         * Sets the visibility of the progress bar indicating that a process is ongoing for the
         * task.
         *
         * @param show whether to show the progress bar or not
         */
        public void setProgressBarVisibility(boolean show) {
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
