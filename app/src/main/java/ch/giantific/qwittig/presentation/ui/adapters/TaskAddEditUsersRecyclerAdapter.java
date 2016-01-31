/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowTaskAddEditUsersBinding;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.presentation.ui.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.viewmodels.TaskAddEditViewModel;
import ch.giantific.qwittig.presentation.viewmodels.rows.TaskUserInvolvedRowViewModel;

import static ch.giantific.qwittig.utils.ViewUtils.DISABLED_ALPHA;


/**
 * Handles the display of the users involved in a task including the reordering of the different
 * users.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class TaskAddEditUsersRecyclerAdapter extends RecyclerView.Adapter<TaskAddEditUsersRecyclerAdapter.TaskUserInvolvedRow> {

    private static final String LOG_TAG = TaskAddEditUsersRecyclerAdapter.class.getSimpleName();
    private TaskAddEditViewModel mViewModel;

    /**
     * Constructs a new {@link TaskAddEditUsersRecyclerAdapter}.
     *
     * @param viewModel the view model for the view
     */
    public TaskAddEditUsersRecyclerAdapter(@NonNull TaskAddEditViewModel viewModel) {
        mViewModel = viewModel;
    }

    @NonNull
    @Override
    public TaskUserInvolvedRow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowTaskAddEditUsersBinding binding = RowTaskAddEditUsersBinding.inflate(inflater, parent, false);
        return new TaskUserInvolvedRow(binding, mViewModel);
    }

    @Override
    public void onBindViewHolder(TaskUserInvolvedRow holder, int position) {
        final RowTaskAddEditUsersBinding binding = holder.getBinding();
        final TaskUserInvolvedRowViewModel viewModel = binding.getViewModel();

        final User user = mViewModel.getUserAvailableAtPosition(position);
        final float userAlpha = mViewModel.isUserAtPositionInvolved(position) ? 1f : DISABLED_ALPHA;
        if (viewModel == null) {
            binding.setViewModel(new TaskUserInvolvedRowViewModel(user, userAlpha, false));
        } else {
            viewModel.setUser(user);
            viewModel.setUserAlpha(userAlpha);
            viewModel.setUserNameBold(false);
        }

        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mViewModel.getItemCount();
    }

    /**
     * Defines the actions to take when a user clicks on a user or drags it to change the order.
     */
    public interface AdapterInteractionListener {
        /**
         * Handles the click on the user row itself.
         *
         * @param position the adapter position of the user row
         */
        void onUsersRowItemClick(int position);

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
    public static class TaskUserInvolvedRow extends BindingRow<RowTaskAddEditUsersBinding> {

        /**
         * Constructs a new {@link TaskUserInvolvedRow} and sets the click and drag listeners.
         *
         * @param binding  the binding for the row
         * @param listener the callback for user clicks and drags
         */
        public TaskUserInvolvedRow(@NonNull RowTaskAddEditUsersBinding binding,
                                   @NonNull final AdapterInteractionListener listener) {
            super(binding);

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onUsersRowItemClick(getAdapterPosition());
                }
            });

            final TaskUserInvolvedRow taskUserInvolvedRow = this;
            binding.ivReorder.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, @NonNull MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) ==
                            MotionEvent.ACTION_DOWN) {
                        listener.onStartDrag(taskUserInvolvedRow);
                    }

                    return false;
                }
            });
        }
    }
}
