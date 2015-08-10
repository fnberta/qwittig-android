package ch.giantific.qwittig.ui.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.models.Item;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.adapter.rows.ProgressRow;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.Utils;


/**
 * Created by fabio on 12.10.14.
 */
public class PurchasesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_PROGRESS = 1;
    private static final String LOG_TAG = PurchasesRecyclerAdapter.class.getSimpleName();
    private AdapterInteractionListener mListener;
    private int mPurchasesViewResource;
    private Context mContext;
    private List<ParseObject> mPurchases;
    private String mCurrentGroupCurrency;

    public PurchasesRecyclerAdapter(Context context, int viewResource, List<ParseObject> purchases,
                                    AdapterInteractionListener listener) {
        super();

        mListener = listener;
        mContext = context;
        mPurchasesViewResource = viewResource;
        mPurchases = purchases;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM: {
                View view = LayoutInflater.from(parent.getContext()).inflate(mPurchasesViewResource, parent,
                        false);
                return new PurchaseRow(view, mListener, mContext);
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
    public int getItemViewType(int position) {
        if (mPurchases.get(position) == null) {
            return TYPE_PROGRESS;
        }

        return TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_ITEM: {
                PurchaseRow purchaseRow = (PurchaseRow) viewHolder;
                Purchase purchase = (Purchase) mPurchases.get(position);

                purchaseRow.setDate(purchase.getDate());
                purchaseRow.setStore(purchase.getStore());

                User buyer = purchase.getBuyer();
                User currentUser = (User) ParseUser.getCurrentUser();
                String nickname = buyer.getGroupIds().contains(currentUser.getCurrentGroup().getObjectId()) ?
                        buyer.getNicknameOrMe(mContext) : mContext.getString(R.string.user_deleted);
                purchaseRow.setBuyer(nickname);

                double myShareNumber = Utils.calculateMyShare(purchase);
                String myShare = MoneyUtils.formatMoneyNoSymbol(myShareNumber,
                        mCurrentGroupCurrency);
                purchaseRow.setMyShare(myShare);
                String balanceChange = MoneyUtils.formatMoneyNoSymbol(myShareNumber * -1,
                        mCurrentGroupCurrency);
                if (buyer.getObjectId().equals(currentUser.getObjectId())) {
                    balanceChange = "+" + MoneyUtils.formatMoneyNoSymbol(
                            calculateBalanceChange(purchase), mCurrentGroupCurrency);
                    purchaseRow.mTextViewBalanceChange.setTextColor(
                            mContext.getResources().getColor(R.color.green));
                } else {
                    purchaseRow.mTextViewBalanceChange.setTextColor(
                            mContext.getResources().getColor(R.color.red));
                }
                purchaseRow.setBalanceChange(balanceChange);

                if (!purchase.currentUserHasReadPurchase()) {
                    purchaseRow.setWhiteBackground();
                } else {
                    purchaseRow.resetBackground();
                }

                break;
            }
            case TYPE_PROGRESS:
                // do nothing
                break;
        }
    }

    private double calculateBalanceChange(ParseObject purchaseParse) {
        Purchase purchase = (Purchase) purchaseParse;

        String userId = ParseUser.getCurrentUser().getObjectId();
        double balanceChange = 0;
        List<ParseObject> items = purchase.getItems();

        for (ParseObject parseObject : items) {
            Item item = (Item) parseObject;

            List<String> usersInvolvedIds = item.getUsersInvolvedIds();
            int sizeUsersInvolved = usersInvolvedIds.size();
            double price = item.getPrice();
            if (usersInvolvedIds.contains(userId)) {
                balanceChange += price - (price / sizeUsersInvolved);
            } else {
                balanceChange += price;
            }
        }

        return balanceChange;
    }

    @Override
    public int getItemCount() {
        return mPurchases.size();
    }

    public void setCurrentGroupCurrency(String currentGroupCurrency) {
        mCurrentGroupCurrency = currentGroupCurrency;
    }

    public interface AdapterInteractionListener {
        void onPurchaseRowItemClick(int position);
    }

    private static class PurchaseRow extends RecyclerView.ViewHolder {

        private Context mContext;
        private View mView;
        private TextView mTextViewDate;
        private TextView mTextViewStore;
        private TextView mTextViewBuyer;
        private TextView mTextViewMyShare;
        private TextView mTextViewBalanceChange;

        public PurchaseRow(View view, final AdapterInteractionListener listener, Context context) {
            super(view);

            mContext = context;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onPurchaseRowItemClick(getAdapterPosition());
                }
            });

            mView = view.findViewById(R.id.rl_purchase);
            mTextViewDate = (TextView) view.findViewById(R.id.tv_date);
            mTextViewStore = (TextView) view.findViewById(R.id.tv_store);
            mTextViewBuyer = (TextView) view.findViewById(R.id.tv_user);
            mTextViewMyShare = (TextView) view.findViewById(R.id.tv_my_share);
            mTextViewBalanceChange = (TextView) view.findViewById(R.id.tv_balance_change);
        }

        public void setWhiteBackground() {
            if (Utils.isRunningLollipopAndHigher()) {
                mView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ripple_white));
            } else {
                mView.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
            }
        }

        public void resetBackground() {
            int[] attrs = new int[]{R.attr.selectableItemBackground};
            TypedArray typedArray = mContext.obtainStyledAttributes(attrs);
            int backgroundResource = typedArray.getResourceId(0, 0);
            typedArray.recycle();
            mView.setBackgroundResource(backgroundResource);
        }

        public void setDate(Date date) {
            mTextViewDate.setText(DateUtils.formatMonthDayLineSeparated(date));
        }

        public void setStore(String store) {
            mTextViewStore.setText(store);
        }

        public void setBuyer(String buyer) {
            mTextViewBuyer.setText(buyer);
        }

        public void setMyShare(String myShare) {
            mTextViewMyShare.setText(myShare);
        }

        public void setBalanceChange(String balanceChange) {
            mTextViewBalanceChange.setText(balanceChange);
        }
    }
}
