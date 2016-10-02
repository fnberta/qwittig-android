package ch.giantific.qwittig.presentation.common.subscribers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.SortedList;

import java.util.Objects;

import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.presentation.common.viewmodels.EmptyViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.items.ChildItemViewModel;

/**
 * Created by fabio on 28.09.16.
 */

public class ChildEventSubscriber<T extends ChildItemViewModel, S extends EmptyViewModel>
        extends IndefiniteSubscriber<T> {

    private final SortedList<T> items;
    private final S viewModel;
    @Nullable
    private final DataErrorCallback errorCallback;

    public ChildEventSubscriber(@NonNull SortedList<T> items,
                                @NonNull S viewModel,
                                @Nullable DataErrorCallback errorCallback) {
        this.errorCallback = errorCallback;
        this.items = items;
        this.viewModel = viewModel;
    }

    @Override
    public final void onNext(T item) {
        @EventType
        final int type = item.getEventType();
        switch (type) {
            case EventType.NONE:
                // do nothing
                break;
            case EventType.ADDED: {
                items.add(item);
                viewModel.setEmpty(false);
                break;
            }
            case EventType.CHANGED:
                // fall through
            case EventType.MOVED: {
                final int pos = getPositionForId(item.getId());
                items.updateItemAt(pos, item);
                break;
            }
            case EventType.REMOVED: {
                items.remove(item);
                viewModel.setEmpty(items.size() == 0);
                break;
            }
        }
    }

    public int getPositionForId(@NonNull String id) {
        for (int i = 0, size = items.size(); i < size; i++) {
            final T item = items.get(i);
            if (Objects.equals(item.getId(), id)) {
                return i;
            }
        }

        throw new IllegalArgumentException("id not found");
    }

    @Override
    public final void onError(Throwable e) {
        super.onError(e);

        if (errorCallback != null) {
            errorCallback.onDataError(e);
        }
    }

    public interface DataErrorCallback {
        void onDataError(@NonNull Throwable e);
    }
}
