package ch.giantific.qwittig.presentation.common.presenters;

/**
 * Created by fabio on 28.09.16.
 */

public interface ListPresenter<T> {

    /**
     * Returns the item at the position.
     *
     * @param position the position of the item
     * @return the item
     */
    T getItemAtPosition(int position);

    /**
     * Returns the total number of items in the data set hold by the view model.
     *
     * @return the total number of items in this view model.
     */
    int getItemCount();
}
