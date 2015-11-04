/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import ch.giantific.qwittig.domain.models.Avatar;
import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.utils.MoneyUtils;


/**
 * Handles the display of different unpaid compensations.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class CompensationsUnpaidRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_POS = 0;
    private static final int TYPE_NEG = 1;
    private static final int VIEW_RESOURCE_POS = R.layout.row_compensations_unpaid_pos;
    private static final int VIEW_RESOURCE_NEG = R.layout.row_compensations_unpaid_neg;
    private AdapterInteractionListener mListener;
    private Context mContext;
    private List<ParseObject> mCompensations;
    private String mCurrentGroupCurrency;
    private User mCurrentUser;

    /**
     * Constructs a new {@link CompensationsUnpaidRecyclerAdapter}.
     *
     * @param context       the context to use in the adapter
     * @param compensations the compensations to display
     * @param listener      the callback for user user clicks on compensations
     */
    public CompensationsUnpaidRecyclerAdapter(@NonNull Context context,
                                              @NonNull List<ParseObject> compensations,
                                              @NonNull User currentUser,
                                              @NonNull AdapterInteractionListener listener) {
        super();

        mListener = listener;
        mContext = context;
        mCompensations = compensations;
        mCurrentUser = currentUser;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_POS: {
                View view = LayoutInflater.from(parent.getContext()).inflate(VIEW_RESOURCE_POS,
                        parent, false);
                return new CompensationPosRow(view, mListener);
            }
            case TYPE_NEG: {
                View view = LayoutInflater.from(parent.getContext()).inflate(VIEW_RESOURCE_NEG,
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
        if (beneficiary.getObjectId().equals(mCurrentUser.getObjectId())) {
            return TYPE_POS;
        }

        return TYPE_NEG;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        CompensationRow compensationRow = (CompensationRow) viewHolder;
        Compensation compensation = (Compensation) mCompensations.get(position);

        compensationRow.setProgressBarVisibility(compensation.isLoading());

        String amount = MoneyUtils.formatMoney(compensation.getAmountFraction().doubleValue(),
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

        compensationRow.setAvatar(mContext, avatar);
        compensationRow.setNickname(nickname);
        compensationRow.setAmountMessage(message);
    }

    @Override
    public int getItemCount() {
        return mCompensations.size();
    }

    /**
     * Sets the current group currency field. As long this is not set, nothing will be displayed
     * in the adapter.
     *
     * @param currentGroupCurrency the currency code to set
     */
    public void setCurrentGroupCurrency(String currentGroupCurrency) {
        mCurrentGroupCurrency = currentGroupCurrency;
    }

    /**
     * Defines the actions to take when user clicks on the compensations.
     */
    public interface AdapterInteractionListener {
        /**
         * Handles the click on the compensation card itself.
         *
         * @param position the adapter position of the compensation
         */
        void onCompensationRowClick(int position);

        /**
         * Handles the click on the done button of a compensation.
         *
         * @param position the adapter position of the compensation
         */
        void onDoneButtonClick(int position);

        /**
         * Handles the click on the remind to pay button of a compensation.
         *
         * @param position the adapter position of the compensation
         */
        void onRemindButtonClick(int position);

        /**
         * Handles the click on the remind that already paid button of a compensation.
         *
         * @param position the adapter position of the compensation
         */
        void onRemindPaidButtonClick(int position);

        /**
         * Handles the click on the not now overflow menu item of a compensation.
         *
         * @param position the adapter position of the compensation
         */
        void onNotNowMenuClick(int position);

        /**
         * Handles the click on the change amount overflow menu item of a compensation.
         *
         * @param position the adapter position of the compensation
         */
        void onChangeAmountMenuClick(int position);
    }

    /**
     * Provides a {@link RecyclerView} row that displays unpaid compensations.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class CompensationRow extends RecyclerView.ViewHolder {
        ImageView mImageViewAvatar;
        TextView mTextViewUser;
        TextView mTextViewAmount;
        ProgressBar mProgressBar;

        /**
         * Constructs a new {@link CompensationRow}.
         *
         * @param view     the inflated view
         * @param listener the callback for user clicks on the compensations
         */
        public CompensationRow(@NonNull View view,
                               @NonNull final AdapterInteractionListener listener) {
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
            mProgressBar = (ProgressBar) view.findViewById(R.id.pb_card);
        }

        /**
         * Loads the avatar image into the image view if the user has an avatar, if not it loads a
         * fallback drawable.
         *
         * @param context     the context to use to load the image
         * @param avatarBytes the user's avatar image
         */
        public void setAvatar(@NonNull Context context, @Nullable byte[] avatarBytes) {
            if (avatarBytes != null) {
                Glide.with(context)
                        .load(avatarBytes)
                        .into(mImageViewAvatar);
            } else {
                mImageViewAvatar.setImageDrawable(Avatar.getFallbackDrawable(context, true, false));
            }
        }

        /**
         * Sets the nickname of the payer or beneficiary of the compensation.
         *
         * @param nickname the nickname to set
         */
        public void setNickname(@NonNull String nickname) {
            mTextViewUser.setText(nickname);
        }

        /**
         * Sets the amount message of the compensation.
         *
         * @param amount the message to set, includes the amount as a number and a description
         */
        public void setAmountMessage(@NonNull String amount) {
            mTextViewAmount.setText(amount);
        }

        /**
         * Sets the visibility of the progress bar indicating that a process is ongoing for the
         * compensation.
         *
         * @param show whether to show the progress bar or not
         */
        public void setProgressBarVisibility(boolean show) {
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Provides a {@link RecyclerView} row that displays unpaid compensations, where the current
     * user receives money.
     * <p/>
     * Subclass of {@link CompensationRow}.
     */
    private static class CompensationPosRow extends CompensationRow {

        private Button mButtonDone;
        private Button mButtonRemind;

        /**
         * Constructs a new {@link CompensationPosRow} and sets click listeners.
         *
         * @param view     the inflated view
         * @param listener the callback for user clicks on the compensation
         */
        public CompensationPosRow(@NonNull View view,
                                  @NonNull final AdapterInteractionListener listener) {
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

        /**
         * Sets the description text of the done button.
         *
         * @param buttonDone the description to set for the button
         */
        public void setButtonDone(String buttonDone) {
            mButtonDone.setText(buttonDone);
        }

        /**
         * Sets the description text of the remind button.
         *
         * @param buttonRemind the description to set for the button
         */
        public void setButtonRemind(String buttonRemind) {
            mButtonRemind.setText(buttonRemind);
        }
    }

    /**
     * Provides a {@link RecyclerView} row that displays unpaid compensations, where the current
     * user owes money.
     * <p/>
     * Subclass of {@link CompensationRow}.
     */
    private static class CompensationNegRow extends CompensationRow {

        private Button mButtonRemindPaid;
        private Toolbar mToolbar;

        /**
         * Constructs a new {@link CompensationNegRow} and sets the click listeners.
         *
         * @param view     the inflated view
         * @param listener the callback for user clicks on the compensation
         */
        public CompensationNegRow(@NonNull View view,
                                  @NonNull final AdapterInteractionListener listener) {
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
                public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
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

        /**
         * Sets the description text of the remind that paid button.
         *
         * @param buttonRemindPaid the description to set for the button
         */
        public void setButtonRemindPaid(String buttonRemindPaid) {
            mButtonRemindPaid.setText(buttonRemindPaid);
        }
    }
}
