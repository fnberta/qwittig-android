/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.data.PieData;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.ui.widgets.PieChart;

/**
 * Handles the display of separate stats pie charts for each user of a group.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class StatsPieChartRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String LOG_TAG = StatsPieChartRecyclerAdapter.class.getSimpleName();
    private static final int VIEW_RESOURCE = R.layout.row_stats_stores_user;
    private List<PieData> mPieData;
    private List<String> mNicknames;
    private boolean mShowPercent;

    /**
     * Constructs a new {@link StatsPieChartRecyclerAdapter}.
     *
     * @param pieData   the pie data to display for each user
     * @param nicknames the nicknames of the users in the group
     */
    public StatsPieChartRecyclerAdapter(@NonNull List<PieData> pieData,
                                        @NonNull List<String> nicknames) {
        super();

        mPieData = pieData;
        mNicknames = nicknames;
        mShowPercent = false;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(VIEW_RESOURCE, parent, false);
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

    /**
     * Sets whether to display the data in percent values or the actual numbers.
     *
     * @param showPercentValues whether to display the data in percent values
     */
    public void showPercentValues(boolean showPercentValues) {
        mShowPercent = showPercentValues;
    }

    @Override
    public int getItemCount() {
        return mPieData.size();
    }

    /**
     * Provides a {@link RecyclerView} row that displays a pie chart with data.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class PieRow extends RecyclerView.ViewHolder {

        private PieChart mPieChart;

        /**
         * Constructs a new {@link PieRow} and sets default animation time for the pie chart.
         *
         * @param view the inflated view
         */
        public PieRow(@NonNull View view) {
            super(view);

            mPieChart = (PieChart) view.findViewById(R.id.pc_stores_user);
            mPieChart.animateY(PieChart.ANIMATION_Y_TIME);

        }

        /**
         * Sets the data of the pie chart to display
         *
         * @param data the data to set
         */
        public void setData(@NonNull PieData data) {
            mPieChart.setData(data);
        }

        /**
         * Sets the center text of the pie chart
         *
         * @param centerText the center text to set
         */
        public void setCenterText(@NonNull String centerText) {
            mPieChart.setCenterText(centerText);
        }

        /**
         * Sets whether to show percent values or the actual numbers
         *
         * @param showPercentagValues whether to show percent values or not
         */
        public void setUsePercentValues(boolean showPercentagValues) {
            mPieChart.setUsePercentValues(showPercentagValues);
        }
    }
}
