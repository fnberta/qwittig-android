package ch.giantific.qwittig.presentation.common.listadapters.interactions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

/**
 * Created by fabio on 17.06.16.
 */
public interface ListDragInteraction {

    void attachToRecyclerView(@Nullable RecyclerView recyclerView);

    void startDrag(@NonNull RecyclerView.ViewHolder viewHolder);
}
