/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.SortedList;

import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.presentation.common.ListInteraction;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.SortedListCallback;
import ch.giantific.qwittig.presentation.common.itemmodels.ChildItemModel;
import rx.Observer;
import rx.Subscription;
import timber.log.Timber;

/**
 * Provides an abstract base implementation of the {@link ListViewModel}.
 */
public abstract class ListViewModelBaseImpl<T extends ChildItemModel, S extends ListViewModel.ViewListener>
        extends ViewModelBaseImpl<S> implements ListViewModel<T, S>, Observer<T> {

    protected final SortedList<T> items;
    private final SortedListCallback<T> listCallback;
    protected ListInteraction listInteraction;
    protected boolean initialDataLoaded;
    private Subscription initialDataSub;
    private Subscription dataListenerSub;

    public ListViewModelBaseImpl(@Nullable Bundle savedState,
                                 @NonNull Navigator navigator,
                                 @NonNull RxBus<Object> eventBus,
                                 @NonNull UserRepository userRepository) {
        super(savedState, navigator, eventBus, userRepository);

        listCallback = new SortedListCallback<T>() {
            @Override
            public int compare(T o1, T o2) {
                return compareItemModels(o1, o2);
            }
        };
        items = new SortedList<>(getItemModelClass(), listCallback);

        if (savedState == null) {
            loading = true;
        }
    }

    protected abstract Class<T> getItemModelClass();

    protected abstract int compareItemModels(T o1, T o2);

    @Override
    public void setListInteraction(@NonNull ListInteraction listInteraction) {
        this.listInteraction = listInteraction;
        listCallback.setListInteraction(listInteraction);
    }

    @Override
    @Bindable
    public boolean isEmpty() {
        return items.size() == 0;
    }

    @Override
    public final T getItemAtPosition(int position) {
        return items.get(position);
    }

    @Override
    public final int getItemViewType(int position) {
        return items.get(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public final void onCompleted() {
        // never gets called, do nothing
    }

    @Override
    public final void onNext(T itemModel) {
        @EventType
        final int type = itemModel.getEventType();
        switch (type) {
            case EventType.NONE:
                // do nothing
                break;
            case EventType.ADDED: {
                items.add(itemModel);
                notifyPropertyChanged(BR.empty);
                break;
            }
            case EventType.CHANGED:
                // fall through
            case EventType.MOVED: {
                final int pos = getPositionForId(itemModel.getId());
                items.updateItemAt(pos, itemModel);
                break;
            }
            case EventType.REMOVED: {
                items.remove(itemModel);
                notifyPropertyChanged(BR.empty);
                break;
            }
        }
    }

    @Override
    public final void onError(Throwable e) {
        onDataError(e);
    }

    @CallSuper
    protected void onDataError(@NonNull Throwable e) {
        Timber.e(e, "subscription failed with error:");
    }

    protected final int getPositionForId(@NonNull String id) {
        for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
            final T itemModel = items.get(i);
            if (Objects.equals(itemModel.getId(), id)) {
                return i;
            }
        }

        throw new IllegalArgumentException("id not found");
    }

    protected final void setDataListenerSub(@NonNull Subscription sub) {
        if (initialDataSub != null && !initialDataSub.isUnsubscribed()) {
            initialDataSub.unsubscribe();
        }

        initialDataSub = sub;
        getSubscriptions().add(sub);
    }

    protected final void setInitialDataSub(@NonNull Subscription sub) {
        if (dataListenerSub != null && !dataListenerSub.isUnsubscribed()) {
            dataListenerSub.unsubscribe();
        }

        dataListenerSub = sub;
        getSubscriptions().add(sub);
    }
}
