package ch.giantific.qwittig.presentation.common.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import ch.giantific.qwittig.presentation.common.ListInteraction;

/**
 * Created by fabio on 17.06.16.
 */
public abstract class BaseRecyclerAdapter<T extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<T>
        implements ListInteraction {

    protected RecyclerView recyclerView;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        this.recyclerView = recyclerView;
    }

    @Override
    public void scrollToPosition(int position) {
        recyclerView.scrollToPosition(position);
    }

    @Override
    public T onCreateViewHolder(ViewGroup parent, int viewType) {
        throw new RuntimeException("there is no type that matches the type " + viewType +
                " + make sure your using types correctly");
    }
}
