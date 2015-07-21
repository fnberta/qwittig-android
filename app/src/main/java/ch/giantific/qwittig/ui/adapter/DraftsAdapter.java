package ch.giantific.qwittig.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Created by fabio on 15.03.15.
 */
public class DraftsAdapter extends BaseAdapter {

    private int mViewResource;
    private List<ParseObject> mDrafts;
    private String mCurrentGroupCurrency;

    public DraftsAdapter(int viewResource, List<ParseObject> drafts) {
        super();

        mViewResource = viewResource;
        mDrafts = drafts;
    }

    @Override
    public int getCount() {
        return mDrafts.size();
    }

    @Override
    public Object getItem(int position) {
        return mDrafts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setCurrentGroupCurrency(String currentGroupCurrency) {
        mCurrentGroupCurrency = currentGroupCurrency;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();

            convertView = LayoutInflater.from(parent.getContext()).inflate(mViewResource, parent,
                    false);
            viewHolder.tvDate = (TextView) convertView.findViewById(R.id.tv_date);
            viewHolder.tvStore = (TextView) convertView.findViewById(R.id.tv_store);
            viewHolder.tvTotal = (TextView) convertView.findViewById(R.id.tv_total_value);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Purchase draft = (Purchase) mDrafts.get(position);

        viewHolder.tvDate.setText(DateUtils.formatMonthDayLineSeparated(draft.getDate()));
        viewHolder.tvStore.setText(draft.getStore());

        double totalPrice = draft.getTotalPriceAdjusted();
        viewHolder.tvTotal.setText(MoneyUtils.formatMoneyNoSymbol(totalPrice, mCurrentGroupCurrency));

        return convertView;
    }

    private static class ViewHolder {
        private TextView tvDate;
        private TextView tvStore;
        private TextView tvTotal;
    }
}
