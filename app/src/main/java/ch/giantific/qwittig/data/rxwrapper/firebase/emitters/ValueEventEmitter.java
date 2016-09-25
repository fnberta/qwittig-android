package ch.giantific.qwittig.data.rxwrapper.firebase.emitters;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import ch.giantific.qwittig.data.rxwrapper.firebase.listeners.RxValueEventListener;
import rx.AsyncEmitter;
import rx.functions.Action1;

/**
 * Created by fabio on 20.08.16.
 */

public class ValueEventEmitter implements Action1<AsyncEmitter<DataSnapshot>> {

    private final Query query;

    public ValueEventEmitter(@NonNull Query query) {
        this.query = query;
    }

    @Override
    public void call(AsyncEmitter<DataSnapshot> asyncEmitter) {
        final ValueEventListener valueEventListener =
                query.addValueEventListener(new RxValueEventListener(asyncEmitter, false));
        asyncEmitter.setCancellation(() -> query.removeEventListener(valueEventListener));
    }
}
