package ch.giantific.qwittig.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.Date;
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
        final DraftRow draftRow;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(mViewResource, parent,
                    false);
            draftRow = new DraftRow(convertView);
            convertView.setTag(draftRow);
        } else {
            draftRow = (DraftRow) convertView.getTag();
        }

        Purchase draft = (Purchase) mDrafts.get(position);

        draftRow.setDate(draft.getDate());
        draftRow.setStore(draft.getStore());

        double totalPrice = draft.getTotalPrice();
        draftRow.setTotal(MoneyUtils.formatMoneyNoSymbol(totalPrice, mCurrentGroupCurrency));

        return convertView;
    }

    private static class DraftRow {
        private TextView mTextViewDate;
        private TextView mTextViewStore;
        private TextView mTestViewTotal;

        public DraftRow(View view) {
            mTextViewDate = (TextView) view.findViewById(R.id.tv_date);
            mTextViewStore = (TextView) view.findViewById(R.id.tv_store);
            mTestViewTotal = (TextView) view.findViewById(R.id.tv_total_value);
        }

        public void setDate(Date date) {
            mTextViewDate.setText(DateUtils.formatMonthDayLineSeparated(date));
        }

        public void setStore(String store) {
            mTextViewStore.setText(store);
        }

        public void setTotal(String total) {
            mTestViewTotal.setText(total);
        }
    }
}
