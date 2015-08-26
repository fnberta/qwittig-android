package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.Avatar;
import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.utils.MoneyUtils;


/**
 * Created by fabio on 12.10.14.
 */
public class CompensationsUnpaidRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_POS = 0;
    private static final int TYPE_NEG = 1;
    private AdapterInteractionListener mListener;
    private int mViewResourcePos;
    private int mViewResourceNeg;
    private Context mContext;
    private List<ParseObject> mCompensations;
    private String mCurrentGroupCurrency;

    public CompensationsUnpaidRecyclerAdapter(Context context, int viewResourcePos, int viewResourceNeg,
                                              List<ParseObject> compensations,
                                              AdapterInteractionListener listener) {
        super();

        mListener = listener;
        mContext = context;
        mViewResourcePos = viewResourcePos;
        mViewResourceNeg = viewResourceNeg;
        mCompensations = compensations;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_POS: {
                View view = LayoutInflater.from(parent.getContext()).inflate(mViewResourcePos,
                        parent, false);
                return new CompensationPosRow(view, mListener);
            }
            case TYPE_NEG: {
                View view = LayoutInflater.from(parent.getContext()).inflate(mViewResourceNeg,
                        parent, false);
                return new CompensationNegRow(view, mListener);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    @Override
    public int getItemViewType(int position) {
        Compensation compensation = (Compensation) mCompensations.get(position);
        User beneficiary = compensation.getBeneficiary();
        User currentUser = (User) ParseUser.getCurrentUser();

        if (beneficiary.getObjectId().equals(currentUser.getObjectId())) {
            return TYPE_POS;
        }

        return TYPE_NEG;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        CompensationRow compensationRow = (CompensationRow) viewHolder;
        Compensation compensation = (Compensation) mCompensations.get(position);

        compensationRow.setProgressBarVisibility(compensation.isLoading());

        String amount = MoneyUtils.formatMoney(compensation.getAmount().doubleValue(),
                mCurrentGroupCurrency);
        byte[] avatar;
        String nickname;
        String message;

        switch (getItemViewType(position)) {
            case TYPE_POS:
                User payer = compensation.getPayer();
                avatar = payer.getAvatar();
                nickname = payer.getNickname();
                message = mContext.getString(R.string.balance_owe_you, amount);
                ((CompensationPosRow) viewHolder).setButtonRemind(
                        mContext.getString(R.string.button_remind, nickname));
                break;
            case TYPE_NEG:
                User beneficiary = compensation.getBeneficiary();
                avatar = beneficiary.getAvatar();
                nickname = beneficiary.getNickname();
                message = mContext.getString(R.string.balance_gets_from_you, amount);
                ((CompensationNegRow) viewHolder).setButtonRemindPaid(
                        mContext.getString(R.string.button_remind_paid, nickname));
                break;
            default:
                throw new RuntimeException("there is no type that matches the type for position " +
                        position + " + make sure your using types correctly");
        }

        compensationRow.setAvatar(avatar, mContext);
        compensationRow.setUsername(nickname);
        compensationRow.setAmountMessage(message);
    }

    @Override
    public int getItemCount() {
        return mCompensations.size();
    }

    public void setCurrentGroupCurrency(String currentGroupCurrency) {
        mCurrentGroupCurrency = currentGroupCurrency;
    }

    public interface AdapterInteractionListener {
        void onCompensationRowClick(int position);

        void onDoneButtonClick(int position);

        void onRemindButtonClick(int position);

        void onRemindPaidButtonClick(int position);

        void onNotNowMenuClick(int position);

        void onChangeAmountMenuClick(int position);
    }

    private static class CompensationRow extends RecyclerView.ViewHolder {
        ImageView mImageViewAvatar;
        TextView mTextViewUser;
        TextView mTextViewAmount;
        ProgressBar mProgressBar;

        public CompensationRow(View view, final AdapterInteractionListener listener) {
            super(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onCompensationRowClick(getAdapterPosition());
                }
            });

            mImageViewAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
            mTextViewUser = (TextView) view.findViewById(R.id.tv_payer);
            mTextViewAmount = (TextView) view.findViewById(R.id.tv_amount);
            mProgressBar = (ProgressBar) view.findViewById(R.id.pb_compensation);
        }

        public void setAvatar(byte[] avatarBytes, Context context) {
            if (avatarBytes != null) {
                Glide.with(context)
                        .load(avatarBytes)
                        .into(mImageViewAvatar);
            } else {
                setAvatar(Avatar.getFallbackDrawable(context, true, false));
            }
        }

        public void setAvatar(Drawable avatar) {
            mImageViewAvatar.setImageDrawable(avatar);
        }

        public void setUsername(String username) {
            mTextViewUser.setText(username);
        }

        public void setAmountMessage(String amount) {
            mTextViewAmount.setText(amount);
        }

        public void setProgressBarVisibility(boolean show) {
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private static class CompensationPosRow extends CompensationRow {

        private Button mButtonDone;
        private Button mButtonRemind;

        public CompensationPosRow(View view, final AdapterInteractionListener listener) {
            super(view, listener);

            mButtonDone = (Button) view.findViewById(R.id.bt_done);
            mButtonDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDoneButtonClick(getAdapterPosition());
                }
            });
            mButtonRemind = (Button) view.findViewById(R.id.bt_remind);
            mButtonRemind.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onRemindButtonClick(getAdapterPosition());
                }
            });
        }

        public void setButtonDone(String buttonDone) {
            mButtonDone.setText(buttonDone);
        }

        public void setButtonRemind(String buttonRemind) {
            mButtonRemind.setText(buttonRemind);
        }
    }

    private static class CompensationNegRow extends CompensationRow {

        private Button mButtonRemindPaid;
        private Toolbar mToolbar;

        public CompensationNegRow(View view, final AdapterInteractionListener listener) {
            super(view, listener);

            mButtonRemindPaid = (Button) view.findViewById(R.id.bt_remind_paid);
            mButtonRemindPaid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onRemindPaidButtonClick(getAdapterPosition());
                }
            });

            mToolbar = (Toolbar) view.findViewById(R.id.tb_card_neg);
            mToolbar.inflateMenu(R.menu.menu_compensation_card_neg);
            mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.action_compensation_not_now:
                            listener.onNotNowMenuClick(getAdapterPosition());
                            return true;
                        case R.id.action_compensation_change_amount:
                            listener.onChangeAmountMenuClick(getAdapterPosition());
                            return true;
                        default:
                            return true;
                    }
                }
            });
        }

        public void setButtonRemindPaid(String buttonRemindPaid) {
            mButtonRemindPaid.setText(buttonRemindPaid);
        }
    }
}
