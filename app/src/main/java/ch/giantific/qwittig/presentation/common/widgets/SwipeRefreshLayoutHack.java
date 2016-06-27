/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.widgets;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

/**
 * Provides a hack to work around this bug: https://code.google.com/p/android/issues/detail?id=77712
 * The state of the swipe refresh layout can only be changed after it has been measured.
 * TODO: switch back to normal layout once bug is fixed
 */
public class SwipeRefreshLayoutHack extends SwipeRefreshLayout {

    private boolean mMeasured = false;
    private boolean mPreMeasureRefreshing = false;

    public SwipeRefreshLayoutHack(Context context) {
        super(context);
    }

    public SwipeRefreshLayoutHack(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (!mMeasured) {
            mMeasured = true;
            setRefreshing(mPreMeasureRefreshing);
        }
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        if (mMeasured) {
            super.setRefreshing(refreshing);
        } else {
            mPreMeasureRefreshing = refreshing;
        }
    }
}
