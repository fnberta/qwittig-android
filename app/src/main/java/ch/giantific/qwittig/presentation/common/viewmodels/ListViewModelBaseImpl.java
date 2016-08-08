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

    protected final SortedList<T> mItems;
    private final SortedListCallback<T> mListCallback;
    protected ListInteraction mListInteraction;
    protected boolean mInitialDataLoaded;
    private Subscription mInitialDataSub;
    private Subscription mDataListenerSub;

    public ListViewModelBaseImpl(@Nullable Bundle savedState,
                                 @NonNull Navigator navigator,
                                 @NonNull RxBus<Object> eventBus,
                                 @NonNull UserRepository userRepository) {
        super(savedState, navigator, eventBus, userRepository);

        mListCallback = new SortedListCallback<T>() {
            @Override
            public int compare(T o1, T o2) {
                return compareItemModels(o1, o2);
            }
        };
        mItems = new SortedList<>(getItemModelClass(), mListCallback);

        if (savedState == null) {
            mLoading = true;
        }
    }

    protected abstract Class<T> getItemModelClass();

    protected abstract int compareItemModels(T o1, T o2);

    @Override
    public void setListInteraction(@NonNull ListInteraction listInteraction) {
        mListInteraction = listInteraction;
        mListCallback.setListInteraction(listInteraction);
    }

    @Override
    @Bindable
    public boolean isEmpty() {
        return mItems.size() == 0;
    }

    @Override
    public final T getItemAtPosition(int position) {
        return mItems.get(position);
    }

    @Override
    public final int getItemViewType(int position) {
        return mItems.get(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
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
            case EventType.ADDED: {
                mItems.add(itemModel);
                notifyPropertyChanged(BR.empty);
                break;
            }
            case EventType.CHANGED:
                // fall through
            case EventType.MOVED: {
                final int pos = getPositionForId(itemModel.getId());
                mItems.updateItemAt(pos, itemModel);
                break;
            }
            case EventType.REMOVED: {
                mItems.remove(itemModel);
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
        for (int i = 0, itemsSize = mItems.size(); i < itemsSize; i++) {
            final T itemModel = mItems.get(i);
            if (Objects.equals(itemModel.getId(), id)) {
                return i;
            }
        }

        throw new IllegalArgumentException("id not found");
    }

    protected final void setDataListenerSub(@NonNull Subscription sub) {
        if (mInitialDataSub != null && !mInitialDataSub.isUnsubscribed()) {
            mInitialDataSub.unsubscribe();
        }

        mInitialDataSub = sub;
        getSubscriptions().add(sub);
    }

    protected final void setInitialDataSub(@NonNull Subscription sub) {
        if (mDataListenerSub != null && !mDataListenerSub.isUnsubscribed()) {
            mDataListenerSub.unsubscribe();
        }

        mDataListenerSub = sub;
        getSubscriptions().add(sub);
    }
}
