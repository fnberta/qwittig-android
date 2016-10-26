package ch.giantific.qwittig.presentation.common.presenters;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.viewmodels.items.ChildItemViewModel;

/**
 * Created by fabio on 06.10.16.
 */

public interface SortedListPresenter<T extends ChildItemViewModel> {

    int compareItemViewModels(@NonNull T item1, @NonNull T item2);
}
