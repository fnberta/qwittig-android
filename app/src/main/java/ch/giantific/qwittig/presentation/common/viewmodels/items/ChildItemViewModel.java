package ch.giantific.qwittig.presentation.common.viewmodels.items;

import android.databinding.Observable;

import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent.EventType;

/**
 * Created by fabio on 04.07.16.
 */
public interface ChildItemViewModel extends Observable {

    @EventType
    int getEventType();

    String getId();

    int getViewType();
}
