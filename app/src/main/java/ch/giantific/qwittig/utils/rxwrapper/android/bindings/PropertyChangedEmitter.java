package ch.giantific.qwittig.utils.rxwrapper.android.bindings;

import android.databinding.Observable;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;

import rx.AsyncEmitter;
import rx.functions.Action1;

/**
 * Created by fabio on 07.10.16.
 */

public class PropertyChangedEmitter implements Action1<AsyncEmitter<String>> {

    private final ObservableField<String> field;

    public PropertyChangedEmitter(@NonNull ObservableField<String> field) {
        this.field = field;
    }

    @Override
    public void call(AsyncEmitter<String> asyncEmitter) {
        final Observable.OnPropertyChangedCallback cb = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                asyncEmitter.onNext(field.get());
            }
        };
        asyncEmitter.setCancellation(() -> field.removeOnPropertyChangedCallback(cb));
        field.addOnPropertyChangedCallback(cb);
    }
}
