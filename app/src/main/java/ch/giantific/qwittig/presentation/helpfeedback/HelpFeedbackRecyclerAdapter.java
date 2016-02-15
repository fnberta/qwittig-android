/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowGenericHeaderBinding;
import ch.giantific.qwittig.databinding.RowHelpFeedbackBinding;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.helpfeedback.items.ActionItem;
import ch.giantific.qwittig.presentation.helpfeedback.items.HeaderItem;
import ch.giantific.qwittig.presentation.helpfeedback.items.HelpFeedbackItem;
import ch.giantific.qwittig.presentation.helpfeedback.items.HelpFeedbackItem.Type;

/**
 * Handles the display of help and feedback items.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class HelpFeedbackRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private HelpFeedbackViewModel mViewModel;

    /**
     * Constructs a new {@link HelpFeedbackRecyclerAdapter}.
     *
     * @param viewModel the view model of the main view
     */
    public HelpFeedbackRecyclerAdapter(@NonNull HelpFeedbackViewModel viewModel) {

        mViewModel = viewModel;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case Type.HELP_FEEDBACK: {
                final RowHelpFeedbackBinding binding =
                        RowHelpFeedbackBinding.inflate(inflater, parent, false);
                return new ItemRow(binding, mViewModel);
            }
            case Type.HEADER: {
                final RowGenericHeaderBinding binding =
                        RowGenericHeaderBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType +
                        " + make sure your using types correctly");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final HelpFeedbackItem item = mViewModel.getItemAtPosition(position);

        int viewType = getItemViewType(position);
        switch (viewType) {
            case Type.HELP_FEEDBACK: {
                final ItemRow itemRow = (ItemRow) viewHolder;
                final RowHelpFeedbackBinding binding = itemRow.getBinding();

                binding.setItem((ActionItem) item);
                binding.executePendingBindings();
                break;
            }
            case Type.HEADER: {
                final BindingRow<RowGenericHeaderBinding> headerRow =
                        (BindingRow<RowGenericHeaderBinding>) viewHolder;
                final RowGenericHeaderBinding binding = headerRow.getBinding();

                binding.setViewModel((HeaderItem) item);
                binding.executePendingBindings();
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mViewModel.getItemAtPosition(position).getType();
    }

    @Override
    public int getItemCount() {
        return mViewModel.getItemCount();
    }

    /**
     * Defines the actions to take when the user clicks on an item.
     */
    public interface AdapterInteractionListener {
        /**
         * Handles the click on an item
         *
         * @param position the adapter position of the item
         */
        void onHelpFeedbackItemClicked(int position);
    }

    /**
     * Provides a {@link RecyclerView} row that displays help and feedback items.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    private static class ItemRow extends BindingRow<RowHelpFeedbackBinding> {

        /**
         * Constructs a new {@link ItemRow} and sets the click listener.
         *
         * @param binding  the binding for the view
         * @param listener the callback for user clicks on the item
         */
        public ItemRow(@NonNull RowHelpFeedbackBinding binding,
                       @NonNull final AdapterInteractionListener listener) {
            super(binding);

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onHelpFeedbackItemClicked(getAdapterPosition());
                }
            });
        }
    }
}
