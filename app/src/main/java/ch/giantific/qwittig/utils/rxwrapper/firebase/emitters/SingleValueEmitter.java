package ch.giantific.qwittig.utils.rxwrapper.firebase.emitters;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;

import ch.giantific.qwittig.utils.rxwrapper.firebase.listeners.RxValueEventListener;
import rx.AsyncEmitter;
import rx.functions.Action1;

/**
 * Created by fabio on 20.08.16.
 */

public class SingleValueEmitter implements Action1<AsyncEmitter<DataSnapshot>> {

    private final Query query;

    public SingleValueEmitter(@NonNull Query query) {
        this.query = query;
    }

    @Override
    public void call(AsyncEmitter<DataSnapshot> asyncEmitter) {
        query.addListenerForSingleValueEvent(new RxValueEventListener(asyncEmitter, true));
    }
}