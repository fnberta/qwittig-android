package ch.giantific.qwittig.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.adapter.rows.ProgressRow;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;


/**
 * Created by fabio on 12.10.14.
 */
public class CompensationsPaidRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_PROGRESS = 1;
    private int mViewResource;
    private Context mContext;
    private List<ParseObject> mCompensations;
    private List<ParseUser> mUsers;
    private String mCurrentGroupCurrency;

    public CompensationsPaidRecyclerAdapter(Context context, int viewResource,
                                            List<ParseObject> compensations,
                                            List<ParseUser> users) {
        super();

        mContext = context;
        mViewResource = viewResource;
        mCompensations = compensations;
        mUsers = users;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM: {
                View view = LayoutInflater.from(parent.getContext()).inflate(mViewResource, parent, false);
                return new CompensationHistoryRow(view);
            }
            case TYPE_PROGRESS: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_progress, parent,
                        false);
                return new ProgressRow(view);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_ITEM: {
                CompensationHistoryRow compensationHistoryRow = (CompensationHistoryRow) viewHolder;
                Compensation compensation = (Compensation) mCompensations.get(position);

                Date date = compensation.getCreatedAt();
                BigFraction amount = compensation.getAmount();
                String amountString;
                String nickname;
                int color;

                User currentUser = (User) ParseUser.getCurrentUser();
                User beneficiary = compensation.getBeneficiary();
                if (beneficiary.getObjectId().equals(currentUser.getObjectId())) {
                    // positive
                    User payer = compensation.getPayer();
                    if (userIsInCurrentGroup(payer)) {
                        nickname = payer.getNickname();
                    } else {
                        nickname = mContext.getString(R.string.user_deleted);
                    }
                    color = R.color.green;
                    amountString = MoneyUtils.formatMoney(amount, mCurrentGroupCurrency);
                } else {
                    // negative
                    if (userIsInCurrentGroup(beneficiary)) {
                        nickname = beneficiary.getNickname();
                    } else {
                        nickname = mContext.getString(R.string.user_deleted);
                    }
                    color = R.color.red;
                    amountString = MoneyUtils.formatMoney(amount.negate(), mCurrentGroupCurrency);
                }

                compensationHistoryRow.mTextViewDate.setText(DateUtils.formatMonthDayLineSeparated(date));
                compensationHistoryRow.mTextViewUser.setText(nickname);
                compensationHistoryRow.mTextViewAmount.setText(amountString);
                compensationHistoryRow.mTextViewAmount.setTextColor(mContext.getResources()
                        .getColor(color));

                break;
            }
            case TYPE_PROGRESS:
                // do nothing
                break;
        }
    }

    private boolean userIsInCurrentGroup(ParseUser user) {
        return mUsers.contains(user);
    }

    @Override
    public int getItemViewType(int position) {
        if (mCompensations.get(position) == null) {
            return TYPE_PROGRESS;
        }

        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mCompensations.size();
    }

    public void setCurrentGroupCurrency(String currentGroupCurrency) {
        mCurrentGroupCurrency = currentGroupCurrency;
    }

    private static class CompensationHistoryRow extends RecyclerView.ViewHolder {
        private TextView mTextViewDate;
        private TextView mTextViewUser;
        private TextView mTextViewAmount;

        public CompensationHistoryRow(View view) {
            super(view);

            mTextViewDate = (TextView) view.findViewById(R.id.tv_date);
            mTextViewUser = (TextView) view.findViewById(R.id.tv_user);
            mTextViewAmount = (TextView) view.findViewById(R.id.tv_amount);
        }
    }
}
