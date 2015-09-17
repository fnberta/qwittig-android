package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import ch.giantific.qwittig.data.models.Avatar;
import ch.giantific.qwittig.data.parse.models.Task;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.adapters.rows.HeaderRow;
import ch.giantific.qwittig.utils.DateUtils;

/**
 * Created by fabio on 09.11.14.
 */
public class TasksRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;
    private AdapterInteractionListener mListener;
    private Context mContext;
    private int mViewResource;
    private List<ParseObject> mTasks;

    private static final String LOG_TAG = TasksRecyclerAdapter.class.getSimpleName();

    public TasksRecyclerAdapter(Context context, AdapterInteractionListener listener,
                                int viewResource, List<ParseObject> tasks) {

        mContext = context;
        mListener = listener;
        mViewResource = viewResource;
        mTasks = tasks;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(mViewResource, parent, false);
                return new TaskRow(v, mContext, mListener);
            }
            case TYPE_HEADER: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_header, parent, false);
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

    public interface AdapterInteractionListener {
        void onTaskRowClicked(int position);

        void onDoneButtonClicked(int position);

        void onRemindButtonClicked(int position);
    }

    public static class TaskRow extends RecyclerView.ViewHolder {

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

        public TaskRow(View view, Context context, final AdapterInteractionListener listener) {
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

        public void setAvatar(byte[] avatar) {
            if (avatar != null) {
                Glide.with(mContext)
                        .load(avatar)
                        .asBitmap()
                        .into(new BitmapImageViewTarget(mImageViewAvatar) {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                view.setImageDrawable(Avatar.getRoundedDrawable(mContext, resource, false));
                            }
                        });
            } else {
                setAvatar(Avatar.getFallbackDrawable(mContext, false, false));
            }
        }

        public void setAvatar(Drawable avatar) {
            mImageViewAvatar.setImageDrawable(avatar);
        }

        public void setTitle(String title) {
            mTexViewTitle.setText(title);
        }

        public void setDeadline(Date deadline) {
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

        private int getDaysToDeadline(Date deadlineDate) {
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

        public void setTimeFrame(String timeFrame) {
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

        public void setUsersInvolved(List<ParseUser> usersInvolved) {
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

        private void toggleButtons(User userResponsible) {
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

        public void setProgressBarVisibility(boolean show) {
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
