package ch.giantific.qwittig.presentation.common.subscribers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.presentation.common.viewmodels.EmptyViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.items.ChildItemViewModel;
import ch.giantific.qwittig.presentation.common.views.SortedListView;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent.EventType;

/**
 * Created by fabio on 28.09.16.
 */

public class ChildEventSubscriber<T extends ChildItemViewModel, S extends EmptyViewModel>
        extends IndefiniteSubscriber<T> {

    private final SortedListView<T> view;
    private final S viewModel;
    @Nullable
    private final DataErrorCallback errorCallback;

    public ChildEventSubscriber(@NonNull SortedListView<T> sortedListView,
                                @NonNull S viewModel,
                                @Nullable DataErrorCallback errorCallback) {
        this.view = sortedListView;
        this.viewModel = viewModel;
        this.errorCallback = errorCallback;
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
                view.addItem(item);
                viewModel.setEmpty(false);
                break;
            }
            case EventType.CHANGED:
                // fall through
            case EventType.MOVED: {
                final int pos = view.getItemPositionForId(item.getId());
                view.updateItemAt(pos, item);
                break;
            }
            case EventType.REMOVED: {
                view.removeItem(item);
                viewModel.setEmpty(view.isItemsEmpty());
                break;
            }
        }
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
