package ch.giantific.qwittig.utils.rxwrapper.firebase.emitters;

import android.support.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.Query;

import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildSnapshotEvent;
import ch.giantific.qwittig.utils.rxwrapper.firebase.listeners.RxChildEventListener;
import rx.AsyncEmitter;
import rx.functions.Action1;

/**
 * Created by fabio on 20.08.16.
 */

public class ChildEventEmitter implements Action1<AsyncEmitter<RxChildSnapshotEvent>> {

    private final Query query;

    public ChildEventEmitter(@NonNull Query query) {
        this.query = query;
    }

    @Override
    public void call(AsyncEmitter<RxChildSnapshotEvent> asyncEmitter) {
        final ChildEventListener childEventListener =
                query.addChildEventListener(new RxChildEventListener(asyncEmitter));
        asyncEmitter.setCancellation(() -> query.removeEventListener(childEventListener));
    }
}
