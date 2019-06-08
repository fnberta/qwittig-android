/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit;

import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;

import ch.giantific.qwittig.databinding.RowAssignmentAddEditIdentityBinding;
import ch.giantific.qwittig.presentation.assignments.addedit.viewmodels.items.AssignmentAddEditIdentityItemViewModel;
import ch.giantific.qwittig.presentation.common.listadapters.rows.BindingRow;


/**
 * Handles the display of the users involved in a task including the reordering of the different
 * users.
 * <p>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class AssignmentAddEditIdentitiesRecyclerAdapter extends
        RecyclerView.Adapter<AssignmentAddEditIdentitiesRecyclerAdapter.AssignmentIdentityRow> {

    protected final ArrayList<AssignmentAddEditIdentityItemViewModel> items;
    private final AssignmentAddEditContract.Presenter presenter;

    /**
     * Constructs a new {@link AssignmentAddEditIdentitiesRecyclerAdapter}.
     *
     * @param presenter the view model for the view
     * @param items
     */
    public AssignmentAddEditIdentitiesRecyclerAdapter(@NonNull AssignmentAddEditContract.Presenter presenter,
                                                      @NonNull ArrayList<AssignmentAddEditIdentityItemViewModel> items) {
        this.presenter = presenter;
        this.items = items;
    }

    public ArrayList<AssignmentAddEditIdentityItemViewModel> getItems() {
        return items;
    }

    @NonNull
    @Override
    public AssignmentIdentityRow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowAssignmentAddEditIdentityBinding binding =
                RowAssignmentAddEditIdentityBinding.inflate(inflater, parent, false);
        return new AssignmentIdentityRow(binding, presenter);
    }

    @Override
    public void onBindViewHolder(AssignmentIdentityRow holder, int position) {
        final RowAssignmentAddEditIdentityBinding binding = holder.getBinding();
        final AssignmentAddEditIdentityItemViewModel viewModel = items.get(position);

        binding.setPresenter(presenter);
        binding.setViewModel(viewModel);
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(AssignmentAddEditIdentityItemViewModel item) {
        items.add(item);
        notifyItemInserted(items.indexOf(item));
    }

    public void swapItem(int fromPosition, int toPosition) {
        Collections.swap(items, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void removeItemAtPosition(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    public int getPositionForItem(@NonNull AssignmentAddEditIdentityItemViewModel item) {
        return items.indexOf(item);
    }

    /**
     * Defines the actions to take when a user clicks on a user or drags it to change the order.
     */
    public interface AdapterInteractionListener {
        /**
         * Handles the start of a user drag of the user row.
         *
         * @param viewHolder the view holder for the user row
         */
        void onStartDrag(@NonNull RecyclerView.ViewHolder viewHolder);
    }

    /**
     * Provides a {@link RecyclerView} row that displays the user's avatar, the nickname and a
     * drag handler.
     */
    public static class AssignmentIdentityRow extends BindingRow<RowAssignmentAddEditIdentityBinding> {

        /**
         * Constructs a new {@link AssignmentIdentityRow} and sets the click and drag listeners.
         *
         * @param binding  the binding for the row
         * @param listener the callback for user clicks and drags
         */
        public AssignmentIdentityRow(@NonNull RowAssignmentAddEditIdentityBinding binding,
                                     @NonNull final AdapterInteractionListener listener) {
            super(binding);

            final AssignmentIdentityRow assignmentIdentityRow = this;
            binding.ivReorder.setOnTouchListener((v, event) -> {
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_DOWN) {
                    listener.onStartDrag(assignmentIdentityRow);
                }

                return false;
            });
        }
    }
}
