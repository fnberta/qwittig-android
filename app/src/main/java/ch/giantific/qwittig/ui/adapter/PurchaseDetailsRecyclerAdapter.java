package ch.giantific.qwittig.ui.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.models.Item;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.adapter.rows.HeaderRow;
import ch.giantific.qwittig.ui.widgets.CircleDisplay;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

import static ch.giantific.qwittig.constants.AppConstants.DISABLED_ALPHA;
import static ch.giantific.qwittig.constants.AppConstants.DISABLED_ALPHA_RGB;

/**
 * Created by fabio on 09.11.14.
 */
public class PurchaseDetailsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_USER_RECYCLER = 2;
    private static final int TYPE_TOTAL = 3;
    private static final int TYPE_MY_SHARE = 4;
    private static final int NUMBER_OF_HEADER_ROWS = 2;
    private static final int NUMBER_OF_USER_ROWS = 1;
    private static final int NUMBER_OF_TOTAL_ROWS = 2;
    private static final int HEADER_POSITION_USER = 0;
    private static final int HEADER_POSITION_ITEMS = 2;
    private static final String LOG_TAG = PurchaseDetailsRecyclerAdapter.class.getSimpleName();
    private Context mContext;
    private int mItemsViewResource;
    private Purchase mPurchase;
    private List<ParseObject> mItems;
    private List<ParseUser> mCurrentGroupUsers;
    private String mCurrentGroupCurrency = ParseUtils.getGroupCurrency();

    public PurchaseDetailsRecyclerAdapter(Context context, int itemsViewResource,
                                          ParseObject purchase, List<ParseUser> currentGroupUsers) {

        mContext = context;
        mItemsViewResource = itemsViewResource;
        mPurchase = (Purchase) purchase;
        mItems = mPurchase.getItems();
        mCurrentGroupUsers = currentGroupUsers;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(mItemsViewResource, parent, false);
                return new ItemRow(v, mContext);
            }
            case TYPE_HEADER: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_header, parent, false);
                return new HeaderRow(v);
            }
            case TYPE_USER_RECYCLER: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_recycler_user, parent, false);
                return new UserRecyclerRow(v, mContext, mPurchase, mCurrentGroupUsers);
            }
            case TYPE_TOTAL: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_purchase_total, parent, false);
                return new TotalRow(v);
            }
            case TYPE_MY_SHARE: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_purchase_my_share, parent, false);
                return new MyShareRow(v);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_ITEM: {
                ItemRow itemRow = (ItemRow) viewHolder;
                Item item = (Item) mItems.get(position - NUMBER_OF_USER_ROWS -
                        NUMBER_OF_HEADER_ROWS);

                itemRow.mTextViewName.setText(item.getName());

                double price = item.getPrice();
                itemRow.mTextViewPrice.setText(
                        MoneyUtils.formatMoney(price, mCurrentGroupCurrency));

                User currentUser = (User) ParseUser.getCurrentUser();
                List<ParseUser> usersInvolved = item.getUsersInvolved();
                int usersInvolvedCount = usersInvolved.size();
                float percentageCurrentUser = 0;

                if (usersInvolved.contains(currentUser)) {
                    percentageCurrentUser = (1f / usersInvolvedCount) * 100;
                    viewHolder.itemView.setAlpha(1f);
                } else {
                    viewHolder.itemView.setAlpha(DISABLED_ALPHA);
                }
                itemRow.mCircleDisplayPercentage.showValue(percentageCurrentUser, 100f, false);

                break;
            }
            case TYPE_HEADER: {
                HeaderRow headerRow = (HeaderRow) viewHolder;
                String header = "";
                switch (position) {
                    case HEADER_POSITION_USER:
                        header = mContext.getString(R.string.header_user);
                        break;
                    case HEADER_POSITION_ITEMS:
                        header = mContext.getString(R.string.header_items);
                        break;
                }
                headerRow.setHeader(header);
                break;
            }
            case TYPE_USER_RECYCLER: {
                break;
            }
            case TYPE_TOTAL: {
                TotalRow totalRow = (TotalRow) viewHolder;

                totalRow.mTextViewTotalValue.setText(MoneyUtils.formatMoney(
                        mPurchase.getTotalPrice(), mCurrentGroupCurrency));
                break;
            }
            case TYPE_MY_SHARE: {
                MyShareRow myShareRow = (MyShareRow) viewHolder;

                double myShare = Utils.calculateMyShare(mPurchase);
                if (myShare != mPurchase.getTotalPrice()) {
                    myShareRow.mTextViewMyShareValue.setText(MoneyUtils.formatMoney(myShare,
                            mCurrentGroupCurrency));
                } else {
                    myShareRow.mViewMyShare.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == HEADER_POSITION_USER) {
            return TYPE_HEADER;
        }
        if (position == 1) {
            return TYPE_USER_RECYCLER;
        }
        if (position == HEADER_POSITION_ITEMS) {
            return TYPE_HEADER;
        }
        if (position == getItemCount() - 2) { // -1 because first position is 0, not 1
            return TYPE_TOTAL;
        }
        if (position == getItemCount() - 1) {
            return TYPE_MY_SHARE;
        }
        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mItems.size() + NUMBER_OF_USER_ROWS + NUMBER_OF_HEADER_ROWS +
                NUMBER_OF_TOTAL_ROWS;
    }

    public static class ItemRow extends RecyclerView.ViewHolder {

        private Context mContext;
        private TextView mTextViewName;
        private CircleDisplay mCircleDisplayPercentage;
        private TextView mTextViewPrice;

        public ItemRow(View view, Context context) {
            super(view);

            mContext = context;

            mTextViewName = (TextView) view.findViewById(R.id.list_item_name);
            mTextViewPrice = (TextView) view.findViewById(R.id.list_item_final_price);
            mCircleDisplayPercentage = (CircleDisplay) view.findViewById(R.id.list_item_percentage);

            mCircleDisplayPercentage.setColor(mContext.getResources().getColor(R.color.accent));
            mCircleDisplayPercentage.setAnimDuration(1000);
            mCircleDisplayPercentage.setDimAlpha(DISABLED_ALPHA_RGB);
        }

    }

    public static class UserRecyclerRow extends RecyclerView.ViewHolder {
        private RecyclerView mUsersInvolvedView;
        private RecyclerView.Adapter mUsersInvolvedAdapter;

        public UserRecyclerRow(View view, Context context, ParseObject purchase,
                               List<ParseUser> currentGroupUsers) {
            super(view);

            mUsersInvolvedView = (RecyclerView) view.findViewById(R.id.rv_users_involved);
            mUsersInvolvedAdapter = new PurchaseDetailsUsersInvolvedRecyclerAdapter(context,
                    R.layout.row_users_involved_list, purchase, currentGroupUsers);
            mUsersInvolvedView.setHasFixedSize(true);
            mUsersInvolvedView.setLayoutManager(new LinearLayoutManager(context,
                    LinearLayoutManager.HORIZONTAL, false));
            mUsersInvolvedView.setAdapter(mUsersInvolvedAdapter);
        }
    }

    public static class TotalRow extends RecyclerView.ViewHolder {

        private TextView mTextViewTotalValue;

        public TotalRow(View view) {
            super(view);

            mTextViewTotalValue = (TextView) view.findViewById(R.id.tv_total_value);
        }

    }

    public static class MyShareRow extends RecyclerView.ViewHolder {

        private View mViewMyShare;
        private TextView mTextViewMyShareValue;

        public MyShareRow(View view) {
            super(view);

            mViewMyShare = view.findViewById(R.id.rl_my_share);
            mTextViewMyShareValue = (TextView) view.findViewById(R.id.tv_my_share_value);
        }

    }
}
