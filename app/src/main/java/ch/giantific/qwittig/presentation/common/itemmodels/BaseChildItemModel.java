package ch.giantific.qwittig.presentation.common.itemmodels;

import android.databinding.BaseObservable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;

/**
 * Created by fabio on 03.07.16.
 */
public abstract class BaseChildItemModel extends BaseObservable implements ChildItemModel {

    private final int eventType;
    private final String id;

    public BaseChildItemModel(@EventType int eventType, @NonNull String id) {
        this.eventType = eventType;
        this.id = id;
    }

    @Override
    public int getEventType() {
        return eventType;
    }

    public String getId() {
        return id;
    }

    @Override
    public int getViewType() {
        return 0;
    }
}
