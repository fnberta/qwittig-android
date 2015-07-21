package ch.giantific.qwittig.ui.adapter;

import android.content.Context;
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
    private Context mContext;
    private int mItemsViewResource;
    private List<PieData> mPieData;
    private List<String> mNicknames;
    private boolean mShowPercent;

    public StatsPieChartRecyclerAdapter(Context context, int itemsViewResource,
                                        List<PieData> pieData, List<String> nicknames) {

        mContext = context;
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
        pieRow.mPieChart.setCenterText(mNicknames.get(position));
        pieRow.mPieChart.setData(pieData);
        pieRow.mPieChart.setUsePercentValues(mShowPercent);
        pieRow.mPieChart.animateY(PieChart.ANIMATION_Y_TIME);
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
        }

    }
}
