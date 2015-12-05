/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Item;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.ui.adapters.rows.HeaderRow;
import ch.giantific.qwittig.ui.widgets.CircleDisplay;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.parse.ParseUtils;

import static ch.giantific.qwittig.utils.ViewUtils.DISABLED_ALPHA;
import static ch.giantific.qwittig.utils.ViewUtils.DISABLED_ALPHA_RGB;

/**
 * Handles the display of the detail view of a purchase including the different headers,
 * the users involved, the items and the total value.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class PurchaseDetailsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_USER_RECYCLER = 2;
    private static final int TYPE_TOTAL = 3;
    private static final int TYPE_MY_SHARE = 4;
    private static final int TYPE_NOTE = 5;
    private static final int ROWS_UNTIL_ITEMS_START = 3;
    private static final int TOTAL_ROWS = 2;
    private static final int NOTE_ROWS = 2;
    private static final int HEADER_POSITION_USER = 0;
    private static final int HEADER_POSITION_ITEMS = 2;
    private static final String LOG_TAG = PurchaseDetailsRecyclerAdapter.class.getSimpleName();
    private static final int VIEW_RESOURCE_ITEM = R.layout.row_details_item_list;
    private static final int VIEW_RESOURCE_RECYCLER_USER = R.layout.row_recycler_user;
    private static final int VIEW_RESOURCE_TOTAL = R.layout.row_purchase_details_total;
    private static final int VIEW_RESOURCE_MY_SHARE = R.layout.row_purchase_details_my_share;
    private static final int VIEW_RESOURCE_NOTE = R.layout.row_purchase_details_note;
    private Context mContext;
    private Purchase mPurchase;
    private List<ParseObject> mItems;
    private boolean mHasNote;
    private String mCurrentGroupCurrency;
    private User mCurrentUser;

    public PurchaseDetailsRecyclerAdapter(Context context, @NonNull User currentUser,
                                          @Nullable Group currentGroup) {
        super();

        mContext = context;
        mCurrentUser = currentUser;
        mCurrentGroupCurrency = ParseUtils.getGroupCurrencyWithFallback(currentGroup);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_ITEM: {
                View v = inflater.inflate(VIEW_RESOURCE_ITEM, parent, false);
                return new ItemRow(v, mContext);
            }
            case TYPE_HEADER: {
                View v = inflater.inflate(HeaderRow.VIEW_RESOURCE, parent, false);
                return new HeaderRow(v);
            }
            case TYPE_USER_RECYCLER: {
                View v = inflater.inflate(VIEW_RESOURCE_RECYCLER_USER, parent, false);
                return new UserRecyclerRow(v, mContext, mCurrentUser);
            }
            case TYPE_TOTAL: {
                View v = inflater.inflate(VIEW_RESOURCE_TOTAL, parent, false);
                return new TotalRow(v);
            }
            case TYPE_MY_SHARE: {
                View v = inflater.inflate(VIEW_RESOURCE_MY_SHARE, parent, false);
                return new MyShareRow(v);
            }
            case TYPE_NOTE: {
                View v = inflater.inflate(VIEW_RESOURCE_NOTE, parent, false);
                return new NoteRow(v);
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

        int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_ITEM: {
                ItemRow itemRow = (ItemRow) viewHolder;
                Item item = (Item) mItems.get(position - ROWS_UNTIL_ITEMS_START);

                itemRow.setName(item.getName());
                itemRow.setPrice(item.getPrice(), mCurrentGroupCurrency);
                itemRow.setAlpha(mCurrentUser, item.getUsersInvolved());

                break;
            }
            case TYPE_HEADER: {
                HeaderRow headerRow = (HeaderRow) viewHolder;
                if (position == HEADER_POSITION_USER) {
                    headerRow.setHeader(mContext.getString(R.string.header_users));
                } else if (position == HEADER_POSITION_ITEMS) {
                    headerRow.setHeader(mContext.getString(R.string.header_items));
                } else if (position == getLastPosition() - 1) {
                    if (mHasNote) {
                        headerRow.setHeader(mContext.getString(R.string.header_note));
                    } else {
                        headerRow.hideRow();
                    }
                }

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

                double myShare = mPurchase.calculateUserShare(mCurrentUser);
                myShareRow.setMyShare(myShare, mPurchase.getTotalPrice(), mCurrentGroupCurrency);
                myShareRow.setMyShareForeign(myShare, mPurchase.getExchangeRate(),
                        mCurrentGroupCurrency, mPurchase.getCurrency());

                break;
            }
            case TYPE_NOTE: {
                NoteRow noteRow = (NoteRow) viewHolder;
                if (mHasNote) {
                    noteRow.setNote(mPurchase.getNote());
                } else {
                    noteRow.hideRow();
                }

                break;
            }
        }
    }

    /**
     * Returns the last position of the adapter..
     *
     * @return the last position of the adapter
     */
    private int getLastPosition() {
        return getItemCount() - 1;
    }

    @Override
    public int getItemViewType(int position) {
        // first position is 0, not 1
        final int lastPosition = getLastPosition();

        if (position == HEADER_POSITION_USER) {
            return TYPE_HEADER;
        }
        if (position == 1) {
            return TYPE_USER_RECYCLER;
        }
        if (position == HEADER_POSITION_ITEMS) {
            return TYPE_HEADER;
        }
        if (position == lastPosition - (mHasNote ? NOTE_ROWS + 1 : 1)) {
            return TYPE_TOTAL;
        }
        if (position == (mHasNote ? (lastPosition - NOTE_ROWS) : lastPosition)) {
            return TYPE_MY_SHARE;
        }

        if (mHasNote) {
            if (position == lastPosition - 1) {
                return TYPE_HEADER;
            }
            if (position == lastPosition) {
                return TYPE_NOTE;
            }
        }

        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        int numberOfExtraRows = ROWS_UNTIL_ITEMS_START + TOTAL_ROWS
                + (mHasNote ? NOTE_ROWS : 0);
        return mItems == null ? numberOfExtraRows : mItems.size() + numberOfExtraRows;
    }

    /**
     * Sets the purchase for the adapter. Until this is set, nothing is displayed in the adapter.
     *
     * @param purchase the purchase to set for the adapter
     */
    public void setPurchase(@NonNull ParseObject purchase) {
        mPurchase = (Purchase) purchase;
        mItems = mPurchase.getItems();
        mHasNote = !TextUtils.isEmpty(mPurchase.getNote());
    }

    /**
     * Provides a {@link RecyclerView} row that displays the details for an item of a purchase.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class ItemRow extends RecyclerView.ViewHolder {

        private Context mContext;
        private TextView mTextViewName;
        private CircleDisplay mCircleDisplayPercentage;
        private TextView mTextViewPrice;

        /**
         * Constructs a new {@link ItemRow} and sets the default values for the
         * {@link CircleDisplay} that represents the share of the current user.
         *
         * @param view    the inflated view
         * @param context the context to use in the adapter
         */
        public ItemRow(@NonNull View view, @NonNull Context context) {
            super(view);

            mContext = context;

            mTextViewName = (TextView) view.findViewById(R.id.list_item_name);
            mTextViewPrice = (TextView) view.findViewById(R.id.list_item_final_price);
            mCircleDisplayPercentage = (CircleDisplay) view.findViewById(R.id.list_item_percentage);

            mCircleDisplayPercentage.setColor(ContextCompat.getColor(mContext, R.color.accent));
            mCircleDisplayPercentage.setAnimDuration(1000);
            mCircleDisplayPercentage.setDimAlpha(DISABLED_ALPHA_RGB);
        }

        /**
         * Sets the name of the item.
         *
         * @param name the name of the item to set
         */
        public void setName(@NonNull String name) {
            mTextViewName.setText(name);
        }

        /**
         * Sets the properly formatted price of the item
         *
         * @param price        the price of the item to set
         * @param currencyCode the currency code to use to format the price
         */
        public void setPrice(double price, @NonNull String currencyCode) {
            String priceFormatted = MoneyUtils.formatMoney(price, currencyCode);
            mTextViewPrice.setText(priceFormatted);
        }

        /**
         * Sets the alpha of the item view, depending on whether the current user is involved in
         * the item or not.
         *
         * @param usersInvolved the users involved for the item
         */
        public void setAlpha(@NonNull User currentUser, @NonNull List<ParseUser> usersInvolved) {
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

        private void setAlpha(@FloatRange(from = 0.0, to = 1.0f) float alpha,
                              float circlePercentage, boolean animate) {
            itemView.setAlpha(alpha);
            mCircleDisplayPercentage.showValue(circlePercentage, 100f, animate);
        }
    }

    /**
     * Provides a {@link RecyclerView} row that displays the users involved in the purchase by
     * displaying them in another {@link RecyclerView}.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class UserRecyclerRow extends RecyclerView.ViewHolder {
        private RecyclerView mUsersInvolvedView;
        private PurchaseDetailsUsersInvolvedRecyclerAdapter mUsersInvolvedAdapter;

        /**
         * Constructs a new {@link UserRecyclerRow} by initialising a new
         * {@link PurchaseDetailsUsersInvolvedRecyclerAdapter}.
         *
         * @param view    the inflated view
         * @param context the context to use in the adapter
         */
        public UserRecyclerRow(@NonNull View view, @NonNull Context context,
                               @NonNull User currentUser) {
            super(view);

            mUsersInvolvedView = (RecyclerView) view.findViewById(R.id.rv_users_involved);
            mUsersInvolvedAdapter = new PurchaseDetailsUsersInvolvedRecyclerAdapter(context, currentUser);
            mUsersInvolvedView.setHasFixedSize(true);
            mUsersInvolvedView.setLayoutManager(new LinearLayoutManager(context,
                    LinearLayoutManager.HORIZONTAL, false));
            mUsersInvolvedView.setAdapter(mUsersInvolvedAdapter);
        }

        /**
         * Sets the purchase in the {@link PurchaseDetailsUsersInvolvedRecyclerAdapter}.
         *
         * @param purchase the purchase to set
         */
        public void setPurchase(@NonNull ParseObject purchase) {
            mUsersInvolvedAdapter.setPurchase(purchase);
            mUsersInvolvedAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Provides a {@link RecyclerView} row that displays the total value of the purchase in the
     * group's currency and if available in the foregin one the purchase was saved with.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class TotalRow extends RecyclerView.ViewHolder {

        private TextView mTextViewTotalValue;
        private TextView mTextViewTotalValueForeign;

        /**
         * Constructs a new {@link TotalRow}.
         *
         * @param view the inflated view
         */
        public TotalRow(@NonNull View view) {
            super(view);

            mTextViewTotalValue = (TextView) view.findViewById(R.id.tv_total_value);
            mTextViewTotalValueForeign = (TextView) view.findViewById(R.id.tv_total_value_foreign);
        }

        /**
         * Sets the properly formatted total value in the group's currency.
         *
         * @param totalPrice   the total price value to set
         * @param currencyCode the currency code to use to format the value
         */
        public void setTotalValue(double totalPrice, @NonNull String currencyCode) {
            String totalFormatted = MoneyUtils.formatMoney(totalPrice, currencyCode);
            mTextViewTotalValue.setText(totalFormatted);
        }

        /**
         * Sets the properly formatted total value in foreign currency.
         *
         * @param totalPriceForeign the total price value to set
         * @param groupCurrency     the group's currency
         * @param purchaseCurrency  the foreign currency of the purchase
         */
        public void setTotalValueForeign(double totalPriceForeign, @NonNull String groupCurrency,
                                         @NonNull String purchaseCurrency) {
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

    /**
     * Provides a {@link RecyclerView} row that displays the share of the current user of the
     * purchase.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class MyShareRow extends RecyclerView.ViewHolder {

        private TextView mTextViewMyShare;
        private TextView mTextViewMyShareForeign;

        /**
         * Constructs a new {@link MyShareRow}.
         *
         * @param view the inflated view
         */
        public MyShareRow(@NonNull View view) {
            super(view);

            mTextViewMyShare = (TextView) view.findViewById(R.id.tv_my_share_value);
            mTextViewMyShareForeign = (TextView) view.findViewById(R.id.tv_my_share_value_foreign);
        }

        /**
         * Sets the properly formatted my share value of the purchase or hides the view if the my
         * share value is equal to the purchases' total price.
         *
         * @param myShare      the my share value to set
         * @param totalPrice   the total price of the purchase
         * @param currencyCode the currency code to use to format the my share value
         */
        public void setMyShare(double myShare, double totalPrice, @NonNull String currencyCode) {
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

        /**
         * Sets the properly formatted my share value of the purchase in the foreign currency of
         * the purchase or hides the view currency of the purchase equals the group's currency.
         *
         * @param myShare          the my share value to set
         * @param exchangeRate     the currency exchange rate to use to calculate the foreign value
         * @param groupCurrency    the group's currency
         * @param purchaseCurrency the purchases' currency
         */
        public void setMyShareForeign(double myShare, double exchangeRate, @NonNull String groupCurrency,
                                      @NonNull String purchaseCurrency) {
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

    /**
     * Provides a {@link RecyclerView} row that displays the optional note of a purchase.
     */
    private static class NoteRow extends RecyclerView.ViewHolder {

        private TextView mTextViewNote;

        /**
         * Constructs a new {@link NoteRow}.
         *
         * @param view the inflated view
         */
        public NoteRow(@NonNull View view) {
            super(view);

            mTextViewNote = (TextView) view.findViewById(R.id.tv_purchase_details_note);
        }

        /**
         * Sets the note text.
         *
         * @param note the note to set
         */
        public void setNote(@NonNull String note) {
            mTextViewNote.setText(note);
        }

        public void hideRow() {
            itemView.setVisibility(View.GONE);
        }
    }
}
