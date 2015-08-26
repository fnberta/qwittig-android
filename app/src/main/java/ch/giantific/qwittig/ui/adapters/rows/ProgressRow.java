package ch.giantific.qwittig.ui.adapters.rows;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import ch.giantific.qwittig.R;

/**
* Created by fabio on 27.03.15.
*/
public class ProgressRow extends RecyclerView.ViewHolder {

    private ProgressBar mProgressBar;

    public ProgressRow(View view) {
        super(view);

        mProgressBar = (ProgressBar) view.findViewById(R.id.pb_more);
    }
}
