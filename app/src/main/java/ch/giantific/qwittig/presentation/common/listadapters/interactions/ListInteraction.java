package ch.giantific.qwittig.presentation.common.listadapters.interactions;

/**
 * Created by fabio on 16.06.16.
 */
public interface ListInteraction {

    void notifyDataSetChanged();

    void notifyItemChanged(int position);

    void notifyItemRangeChanged(int positionStart, int itemCount);

    void notifyItemRemoved(int position);

    void notifyItemRangeRemoved(int positionStart, int itemCount);

    void notifyItemInserted(int position);

    void notifyItemRangeInserted(int positionStart, int itemCount);

    void notifyItemMoved(int fromPosition, int toPosition);

    void scrollToPosition(int position);
}
