package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.Avatar;
import ch.giantific.qwittig.data.parse.models.Item;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.adapters.rows.ProgressRow;
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
                User buyer = purchase.getBuyer();

                purchaseRow.setAvatar(buyer.getAvatar());
                purchaseRow.setDate(purchase.getDate());
                purchaseRow.setStore(purchase.getStore());

                User currentUser = (User) ParseUser.getCurrentUser();
                String nickname = buyer.getGroupIds().contains(currentUser.getCurrentGroup().getObjectId()) ?
                        buyer.getNicknameOrMe(mContext) : mContext.getString(R.string.user_deleted);
                purchaseRow.setBuyer(nickname);

                double totalPrice = purchase.getTotalPrice();
                purchaseRow.setTotal(MoneyUtils.formatMoneyNoSymbol(totalPrice, mCurrentGroupCurrency));
                double myShare = Utils.calculateMyShare(purchase);
                purchaseRow.setMyShare(MoneyUtils.formatMoneyNoSymbol(myShare, mCurrentGroupCurrency));

                if (!purchase.currentUserHasReadPurchase()) {
                    purchaseRow.setWhiteBackground();
                } else {
                    purchaseRow.resetBackground();
                }

                int showDivider = position == getItemCount() - 1 ? View.GONE : View.VISIBLE;
                purchaseRow.toggleDividerVisibility(showDivider);

                break;
            }
            case TYPE_PROGRESS:
                // do nothing
                break;
        }
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
        private View mDivider;
        private ImageView mImageViewAvatar;
        private TextView mTextViewDate;
        private TextView mTextViewStore;
        private TextView mTextViewBuyer;
        private TextView mTextViewMyShare;
        private TextView mTextViewTotal;

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
            mDivider = view.findViewById(R.id.divider);
            mImageViewAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
            mTextViewDate = (TextView) view.findViewById(R.id.tv_date);
            mTextViewStore = (TextView) view.findViewById(R.id.tv_store);
            mTextViewBuyer = (TextView) view.findViewById(R.id.tv_user);
            mTextViewTotal = (TextView) view.findViewById(R.id.tv_total);
            mTextViewMyShare = (TextView) view.findViewById(R.id.tv_my_share);
        }

        public void toggleDividerVisibility(int showDivider) {
            mDivider.setVisibility(showDivider);
        }

        public void setWhiteBackground() {
            if (Utils.isRunningLollipopAndHigher()) {
                mView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ripple_white));
            } else {
                mView.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.white));
            }
        }

        public void resetBackground() {
            int[] attrs = new int[]{R.attr.selectableItemBackground};
            TypedArray typedArray = mContext.obtainStyledAttributes(attrs);
            int backgroundResource = typedArray.getResourceId(0, 0);
            typedArray.recycle();
            mView.setBackgroundResource(backgroundResource);
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
                mImageViewAvatar.setImageDrawable(Avatar.getFallbackDrawable(mContext, false, false));
            }
        }

        public void setDate(Date date) {
            mTextViewDate.setText(DateUtils.formatDateShort(date));
        }

        public void setStore(String store) {
            mTextViewStore.setText(store);
        }

        public void setBuyer(String buyer) {
            mTextViewBuyer.setText(buyer);
        }

        public void setTotal(String total) {
            mTextViewTotal.setText(total);
        }

        public void setMyShare(String myShare) {
            mTextViewMyShare.setText(myShare);
        }
    }
}
