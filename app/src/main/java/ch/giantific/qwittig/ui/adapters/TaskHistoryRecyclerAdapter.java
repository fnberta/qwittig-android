package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.TaskHistory;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.adapters.rows.BaseUserAvatarRow;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.Utils;


/**
 * Created by fabio on 12.10.14.
 */
public class TaskHistoryRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int mViewResource;
    private List<TaskHistory> mTaskHistory;
    private Context mContext;

    public TaskHistoryRecyclerAdapter(Context context, int viewResource,
                                      List<TaskHistory> taskHistory) {
        super();

        mContext = context;
        mViewResource = viewResource;
        mTaskHistory = taskHistory;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mViewResource, parent,
                false);

        return new TaskHistoryRow(view, mContext);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final TaskHistoryRow historyRow = (TaskHistoryRow) viewHolder;
        TaskHistory taskHistory = mTaskHistory.get(position);
        User user = taskHistory.getUser();

        historyRow.setDate(taskHistory.getDate());
        historyRow.setName(user.getNickname());
        historyRow.setAvatar(user.getAvatar(), false);
    }

    @Override
    public int getItemCount() {
        return mTaskHistory.size();
    }

    public interface AdapterInteractionListener {
    }

    private static class TaskHistoryRow extends BaseUserAvatarRow {

        private TextView mTextViewDate;

        public TaskHistoryRow(View view, Context context) {
            super(view, context);

            mTextViewDate = (TextView) view.findViewById(R.id.tv_task_details_history_date);
        }

        public void setDate(Date date) {
            mTextViewDate.setText(DateUtils.formatDateLong(date));
        }
    }
}
