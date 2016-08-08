package ch.giantific.qwittig.presentation.common.itemmodels;

import android.databinding.BaseObservable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;

/**
 * Created by fabio on 03.07.16.
 */
public abstract class BaseChildItemModel extends BaseObservable implements ChildItemModel {

    private final int mEventType;
    private final String mId;

    public BaseChildItemModel(@EventType int eventType, @NonNull String id) {
        mEventType = eventType;
        mId = id;
    }

    @Override
    public int getEventType() {
        return mEventType;
    }

    public String getId() {
        return mId;
    }

    @Override
    public int getViewType() {
        return 0;
    }
}
