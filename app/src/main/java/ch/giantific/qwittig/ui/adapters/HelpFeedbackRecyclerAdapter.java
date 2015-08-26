package ch.giantific.qwittig.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.constants.AppConstants;
import ch.giantific.qwittig.data.models.HelpItem;
import ch.giantific.qwittig.ui.adapters.rows.HeaderRow;

/**
 * Created by fabio on 09.11.14.
 */
public class HelpFeedbackRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;
    private AdapterInteractionListener mListener;
    private Context mContext;
    private int mItemsViewResource;
    private HelpItem[] mItems;

    public HelpFeedbackRecyclerAdapter(Context context, AdapterInteractionListener listener,
                                       int itemsViewResource, HelpItem[] items) {

        mContext = context;
        mListener = listener;
        mItemsViewResource = itemsViewResource;
        mItems = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(mItemsViewResource, parent, false);
                return new ItemRow(v, mListener);
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
        HelpItem item = mItems[position];

        switch (getItemViewType(position)) {
            case TYPE_ITEM: {
                ItemRow itemRow = (ItemRow) viewHolder;

                itemRow.setTitleWithDrawable(mContext.getString(item.getTitle()),
                        ContextCompat.getDrawable(mContext, item.getIcon()));
                break;
            }
            case TYPE_HEADER: {
                HeaderRow headerRow = (HeaderRow) viewHolder;
                headerRow.setHeader(mContext.getString(item.getTitle()));
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mItems[position].getIcon() == 0) {
            return TYPE_HEADER;
        }

        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mItems.length;
    }

    public interface AdapterInteractionListener {
        void onHelpFeedbackItemClicked(int position);
    }

    public static class ItemRow extends RecyclerView.ViewHolder {

        private TextView mTextViewTitle;

        public ItemRow(View view, final AdapterInteractionListener listener) {
            super(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onHelpFeedbackItemClicked(getAdapterPosition());
                }
            });

            mTextViewTitle = (TextView) view.findViewById(R.id.tv_help_title);
        }

        public void setTitleWithDrawable(String title, Drawable drawable) {
            mTextViewTitle.setText(title);
            drawable.setAlpha(AppConstants.ICON_BLACK_ALPHA_RGB);
            mTextViewTitle.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }
    }
}
