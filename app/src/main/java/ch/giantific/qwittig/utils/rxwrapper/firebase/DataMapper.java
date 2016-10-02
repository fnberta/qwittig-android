package ch.giantific.qwittig.utils.rxwrapper.firebase;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.domain.models.FirebaseModel;
import rx.exceptions.Exceptions;
import rx.functions.Func1;

/**
 * Created by fabio on 24.09.16.
 */

public final class DataMapper {

    private DataMapper() {
        // class cannot be instantiated
    }

    public static <T> Func1<DataSnapshot, T> map(@NonNull Class<T> type) {
        return new SnapshotMapper<>(type);
    }

    public static <T extends FirebaseModel> Func1<DataSnapshot, T> mapWithId(@NonNull Class<T> type) {
        return new WithIdSnapshotMapper<>(type);
    }

    public static <T extends FirebaseModel> Func1<DataSnapshot, List<T>> mapListWithId(@NonNull Class<T> type) {
        return new ListWithIdSnapshotMapper<>(type);
    }

    public static <T extends FirebaseModel> Func1<RxChildSnapshotEvent, RxChildEvent<T>> mapChildWithId(@NonNull Class<T> type) {
        return new ChildWithIdSnapshotMapper<>(type);
    }

    private static class SnapshotMapper<T> implements Func1<DataSnapshot, T> {

        private final Class<T> type;

        SnapshotMapper(@NonNull Class<T> type) {
            this.type = type;
        }

        @Override
        public T call(DataSnapshot dataSnapshot) {
            final T value = dataSnapshot.getValue(type);
            if (value == null) {
                throw Exceptions.propagate(new Throwable("unable to cast firebase data response to " +
                        type.getSimpleName()));
            }

            return value;
        }
    }

    private static class WithIdSnapshotMapper<T extends FirebaseModel> implements Func1<DataSnapshot, T> {

        private final Class<T> type;

        WithIdSnapshotMapper(@NonNull Class<T> type) {
            this.type = type;
        }

        @Override
        public T call(DataSnapshot dataSnapshot) {
            final T value = dataSnapshot.getValue(type);
            if (value == null) {
                throw Exceptions.propagate(new Throwable("unable to cast firebase data response to " +
                        type.getSimpleName()));
            }

            value.setId(dataSnapshot.getKey());
            return value;
        }
    }

    private static class ListWithIdSnapshotMapper<T extends FirebaseModel> implements Func1<DataSnapshot, List<T>> {

        private final Class<T> type;

        ListWithIdSnapshotMapper(@NonNull Class<T> type) {
            this.type = type;
        }

        @Override
        public List<T> call(DataSnapshot dataSnapshot) {
            final List<T> items = new ArrayList<>();
            if (dataSnapshot.hasChildren()) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    final T value = childSnapshot.getValue(type);
                    if (value != null) {
                        value.setId(childSnapshot.getKey());
                        items.add(value);
                    } else {
                        throw Exceptions.propagate(new Throwable("unable to cast firebase data response to " + type.getSimpleName()));
                    }
                }
            }

            return items;
        }
    }

    private static class ChildWithIdSnapshotMapper<T extends FirebaseModel> implements Func1<RxChildSnapshotEvent, RxChildEvent<T>> {

        private final Class<T> type;

        ChildWithIdSnapshotMapper(@NonNull Class<T> type) {
            this.type = type;
        }

        @Override
        public RxChildEvent<T> call(RxChildSnapshotEvent rxChildEvent) {
            final DataSnapshot snapshot = rxChildEvent.getSnapshot();
            final T value = snapshot.getValue(type);
            if (value != null) {
                value.setId(snapshot.getKey());
            } else {
                throw Exceptions.propagate(new Throwable("unable to cast firebase data response to " + type.getSimpleName()));
            }

            return new RxChildEvent<>(rxChildEvent.getEventType(), value, rxChildEvent.getPreviousChildKey());
        }
    }
}
