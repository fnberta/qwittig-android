package ch.giantific.qwittig.presentation.common.viewmodels.items;

import android.databinding.BaseObservable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent.EventType;

/**
 * Created by fabio on 03.07.16.
 */
public abstract class BaseChildItemViewModel extends BaseObservable implements ChildItemViewModel {

    private final int eventType;
    private final String id;

    public BaseChildItemViewModel(@EventType int eventType, @NonNull String id) {
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
