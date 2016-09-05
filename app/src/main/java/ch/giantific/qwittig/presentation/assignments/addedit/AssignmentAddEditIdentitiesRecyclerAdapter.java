/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit;

import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowAssignmentAddEditIdentityBinding;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.assignments.addedit.itemmodels.AssignmentAddEditIdentityItemModel;


/**
 * Handles the display of the users involved in a task including the reordering of the different
 * users.
 * <p>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class AssignmentAddEditIdentitiesRecyclerAdapter extends BaseRecyclerAdapter<AssignmentAddEditIdentitiesRecyclerAdapter.AssignmentIdentityRow> {

    private final AssignmentAddEditViewModel viewModel;

    /**
     * Constructs a new {@link AssignmentAddEditIdentitiesRecyclerAdapter}.
     *
     * @param viewModel the view model for the view
     */
    public AssignmentAddEditIdentitiesRecyclerAdapter(@NonNull AssignmentAddEditViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public AssignmentIdentityRow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowAssignmentAddEditIdentityBinding binding =
                RowAssignmentAddEditIdentityBinding.inflate(inflater, parent, false);
        return new AssignmentIdentityRow(binding, viewModel);
    }

    @Override
    public void onBindViewHolder(AssignmentIdentityRow holder, int position) {
        final RowAssignmentAddEditIdentityBinding binding = holder.getBinding();
        final AssignmentAddEditIdentityItemModel itemModel = viewModel.getItemAtPosition(position);

        binding.setItemModel(itemModel);
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return viewModel.getItemCount();
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
            binding.ivReorder.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, @NonNull MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) ==
                            MotionEvent.ACTION_DOWN) {
                        listener.onStartDrag(assignmentIdentityRow);
                    }

                    return false;
                }
            });
        }
    }
}
