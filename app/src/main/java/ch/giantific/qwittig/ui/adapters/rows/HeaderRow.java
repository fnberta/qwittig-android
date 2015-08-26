package ch.giantific.qwittig.ui.adapters.rows;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ch.giantific.qwittig.R;

/**
* Created by fabio on 27.03.15.
*/
public class HeaderRow extends RecyclerView.ViewHolder {
    private TextView mTextViewHeader;

    public HeaderRow(View view) {
        super(view);

        mTextViewHeader = (TextView) view.findViewById(R.id.tv_header);
    }

    public void setHeader(String header) {
        mTextViewHeader.setText(header);
    }
}
