package ch.giantific.qwittig.presentation.common.views;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by fabio on 06.10.16.
 */

public interface SortedListView<T> {

    boolean isItemsEmpty();

    void addItem(@NonNull T item);

    void addItems(@NonNull List<T> items);

    void removeItem(@NonNull T item);

    void removeItemAtPosition(int position);

    void clearItems();

    void updateItemAt(int pos, @NonNull T item);

    T getItemAtPosition(int position);

    int getItemPositionForId(@NonNull String id);

    int getItemPositionForItem(@NonNull T item);
}
