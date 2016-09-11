package ch.giantific.qwittig.presentation.common.itemmodels;

import android.databinding.Observable;

import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;

/**
 * Created by fabio on 04.07.16.
 */
public interface ChildItemModel extends Observable {

    @EventType
    int getEventType();

    String getId();

    int getViewType();
}
