package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
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
import ch.giantific.qwittig.ui.adapters.rows.HeaderRow;
import ch.giantific.qwittig.ui.widgets.CircleDisplay;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.ParseUtils;

import static ch.giantific.qwittig.utils.AnimUtils.DISABLED_ALPHA;
import static ch.giantific.qwittig.utils.AnimUtils.DISABLED_ALPHA_RGB;

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
    private String mCurrentGroupCurrency = ParseUtils.getGroupCurrency();

    public PurchaseDetailsRecyclerAdapter(Context context, int itemsViewResource) {

        mContext = context;
        mItemsViewResource = itemsViewResource;
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
                return new UserRecyclerRow(v, mContext);
            }
            case TYPE_TOTAL: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_purchase_details_total, parent, false);
                return new TotalRow(v);
            }
            case TYPE_MY_SHARE: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_purchase_details_my_share, parent, false);
                return new MyShareRow(v);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (mPurchase == null) {
            return;
        }

        switch (getItemViewType(position)) {
            case TYPE_ITEM: {
                ItemRow itemRow = (ItemRow) viewHolder;
                Item item = (Item) mItems.get(position - NUMBER_OF_USER_ROWS -
                        NUMBER_OF_HEADER_ROWS);

                itemRow.setName(item.getName());
                itemRow.setPrice(item.getPrice(), mCurrentGroupCurrency);
                itemRow.setAlpha(item.getUsersInvolved());

                break;
            }
            case TYPE_HEADER: {
                HeaderRow headerRow = (HeaderRow) viewHolder;
                String header = "";
                switch (position) {
                    case HEADER_POSITION_USER:
                        header = mContext.getString(R.string.header_users);
                        break;
                    case HEADER_POSITION_ITEMS:
                        header = mContext.getString(R.string.header_items);
                        break;
                }
                headerRow.setHeader(header);

                break;
            }
            case TYPE_USER_RECYCLER: {
                UserRecyclerRow userRecyclerRow = (UserRecyclerRow) viewHolder;
                userRecyclerRow.setPurchase(mPurchase);

                break;
            }
            case TYPE_TOTAL: {
                TotalRow totalRow = (TotalRow) viewHolder;
                totalRow.setTotalValue(mPurchase.getTotalPrice(), mCurrentGroupCurrency);
                totalRow.setTotalValueForeign(mPurchase.getTotalPriceForeign(),
                        mCurrentGroupCurrency, mPurchase.getCurrency());

                break;
            }
            case TYPE_MY_SHARE: {
                MyShareRow myShareRow = (MyShareRow) viewHolder;

                double myShare = PurchasesRecyclerAdapter.calculateMyShare(mPurchase);
                myShareRow.setMyShare(myShare, mPurchase.getTotalPrice(), mCurrentGroupCurrency);
                myShareRow.setMyShareForeign(myShare, mPurchase.getExchangeRate(),
                        mCurrentGroupCurrency, mPurchase.getCurrency());

                break;
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
        int numberOfExtraRows = NUMBER_OF_USER_ROWS + NUMBER_OF_HEADER_ROWS + NUMBER_OF_TOTAL_ROWS;
        return mItems == null ? numberOfExtraRows : mItems.size() + numberOfExtraRows ;
    }

    public void setPurchase(ParseObject purchase) {
        mPurchase = (Purchase) purchase;
        mItems = mPurchase.getItems();
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

            mCircleDisplayPercentage.setColor(ContextCompat.getColor(mContext, R.color.accent));
            mCircleDisplayPercentage.setAnimDuration(1000);
            mCircleDisplayPercentage.setDimAlpha(DISABLED_ALPHA_RGB);
        }

        public void setName(String name) {
            mTextViewName.setText(name);
        }

        public void setPrice (double price, String currencyCode) {
            String priceFormatted = MoneyUtils.formatMoney(price, currencyCode);
            mTextViewPrice.setText(priceFormatted);
        }

        public void setAlpha(List<ParseUser> usersInvolved) {
            User currentUser = (User) ParseUser.getCurrentUser();
            int usersInvolvedCount = usersInvolved.size();

            float percentageCurrentUser = 0;
            float alpha = 1f;
            if (usersInvolved.contains(currentUser)) {
                percentageCurrentUser = (1f / usersInvolvedCount) * 100;
            } else {
                alpha = DISABLED_ALPHA;
            }

            setAlpha(alpha, percentageCurrentUser, false);
        }

        private void setAlpha(float alpha, float circlePercentage, boolean animate) {
            itemView.setAlpha(alpha);
            mCircleDisplayPercentage.showValue(circlePercentage, 100f, animate);
        }
    }

    public static class UserRecyclerRow extends RecyclerView.ViewHolder {
        private RecyclerView mUsersInvolvedView;
        private PurchaseDetailsUsersInvolvedRecyclerAdapter mUsersInvolvedAdapter;

        public UserRecyclerRow(View view, Context context) {
            super(view);

            mUsersInvolvedView = (RecyclerView) view.findViewById(R.id.rv_users_involved);
            mUsersInvolvedAdapter = new PurchaseDetailsUsersInvolvedRecyclerAdapter(context,
                    R.layout.row_users_involved_list);
            mUsersInvolvedView.setHasFixedSize(true);
            mUsersInvolvedView.setLayoutManager(new LinearLayoutManager(context,
                    LinearLayoutManager.HORIZONTAL, false));
            mUsersInvolvedView.setAdapter(mUsersInvolvedAdapter);
        }

        public void setPurchase(ParseObject purchase) {
            mUsersInvolvedAdapter.setPurchase(purchase);
            mUsersInvolvedAdapter.notifyDataSetChanged();
        }
    }

    public static class TotalRow extends RecyclerView.ViewHolder {

        private TextView mTextViewTotalValue;
        private TextView mTextViewTotalValueForeign;

        public TotalRow(View view) {
            super(view);

            mTextViewTotalValue = (TextView) view.findViewById(R.id.tv_total_value);
            mTextViewTotalValueForeign = (TextView) view.findViewById(R.id.tv_total_value_foreign);
        }

        public void setTotalValue(double totalPrice, String currencyCode) {
            String totalFormatted = MoneyUtils.formatMoney(totalPrice, currencyCode);
            mTextViewTotalValue.setText(totalFormatted);
        }

        public void setTotalValueForeign(double totalPriceForeign, String groupCurrency,
                                         String purchaseCurrency) {
            if (!groupCurrency.equals(purchaseCurrency)) {
                setTotalValueForeignVisibility(true);
                String foreignFormatted = MoneyUtils.formatMoney(totalPriceForeign, purchaseCurrency);
                mTextViewTotalValueForeign.setText(foreignFormatted);
            } else {
                setTotalValueForeignVisibility(false);
            }
        }

        private void setTotalValueForeignVisibility(boolean show) {
            mTextViewTotalValueForeign.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public static class MyShareRow extends RecyclerView.ViewHolder {

        private TextView mTextViewMyShare;
        private TextView mTextViewMyShareForeign;

        public MyShareRow(View view) {
            super(view);

            mTextViewMyShare = (TextView) view.findViewById(R.id.tv_my_share_value);
            mTextViewMyShareForeign = (TextView) view.findViewById(R.id.tv_my_share_value_foreign);
        }

        public void setMyShare(double myShare, double totalPrice, String currencyCode) {
            if (myShare != totalPrice) {
                String shareFormatted = MoneyUtils.formatMoney(myShare, currencyCode);
                mTextViewMyShare.setText(shareFormatted);
            } else {
                hideView();
            }
        }

        private void hideView() {
            itemView.setVisibility(View.GONE);
        }

        public void setMyShareForeign(double myShare, double exchangeRate, String groupCurrency,
                                      String purchaseCurrency) {
            if (!groupCurrency.equals(purchaseCurrency)) {
                setMyShareForeignVisibility(true);
                String shareFormatted = MoneyUtils.formatMoney(myShare / exchangeRate, purchaseCurrency);
                mTextViewMyShareForeign.setText(shareFormatted);
            } else {
                setMyShareForeignVisibility(false);
            }
        }

        private void setMyShareForeignVisibility(boolean show) {
            mTextViewMyShareForeign.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
