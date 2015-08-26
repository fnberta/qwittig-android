package ch.giantific.qwittig.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.ui.widgets.PieChart;
import com.github.mikephil.charting.data.PieData;

import java.util.List;

import ch.giantific.qwittig.R;

/**
 * Created by fabio on 09.11.14.
 */
public class StatsPieChartRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String LOG_TAG = StatsPieChartRecyclerAdapter.class.getSimpleName();
    private int mItemsViewResource;
    private List<PieData> mPieData;
    private List<String> mNicknames;
    private boolean mShowPercent;

    public StatsPieChartRecyclerAdapter(int itemsViewResource,
                                        List<PieData> pieData, List<String> nicknames) {
        super();

        mItemsViewResource = itemsViewResource;
        mPieData = pieData;
        mNicknames = nicknames;
        mShowPercent = false;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(mItemsViewResource, parent, false);
        return new PieRow(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        PieRow pieRow = (PieRow) viewHolder;

        PieData pieData = mPieData.get(position);
        pieRow.setCenterText(mNicknames.get(position));
        pieRow.setData(pieData);
        pieRow.setUsePercentValues(mShowPercent);
    }

    public void showPercentValues(boolean showPercentValues) {
        mShowPercent = showPercentValues;
    }

    @Override
    public int getItemCount() {
        return mPieData.size();
    }

    public static class PieRow extends RecyclerView.ViewHolder {

        private PieChart mPieChart;

        public PieRow(View view) {
            super(view);

            mPieChart = (PieChart) view.findViewById(R.id.pc_stores_user);
            mPieChart.animateY(PieChart.ANIMATION_Y_TIME);

        }

        public void setData(PieData data) {
            mPieChart.setData(data);
        }

        public void setCenterText(String centerText) {
            mPieChart.setCenterText(centerText);
        }

        public void setUsePercentValues(boolean showPercentagValues) {
            mPieChart.setUsePercentValues(showPercentagValues);
        }
    }
}
