/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowDraftsBinding;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;

/**
 * Handles the display of the user's drafts.
 * <p/>
 * Subclass of {@link android.support.v7.widget.RecyclerView.Adapter}.
 */
public class DraftsRecyclerAdapter extends RecyclerView.Adapter<DraftsRecyclerAdapter.DraftRow> {

    private HomeDraftsViewModel mViewModel;

    /**
     * Constructs a new {@link DraftsRecyclerAdapter}.
     *
     * @param viewModel the view's model
     */
    public DraftsRecyclerAdapter(@NonNull HomeDraftsViewModel viewModel) {
        super();

        mViewModel = viewModel;
    }

    @NonNull
    @Override
    public DraftRow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RowDraftsBinding binding = RowDraftsBinding.inflate(inflater, parent, false);
        return new DraftRow(binding, mViewModel);
    }

    @Override
    public void onBindViewHolder(DraftRow draftRow, int position) {
        final RowDraftsBinding binding = draftRow.getBinding();
        final Purchase draft = mViewModel.getItemAtPosition(position);

        DraftsRowViewModel viewModel = binding.getViewModel();
        if (viewModel == null) {
            final String currency = mViewModel.getCurrentIdentity().getGroup().getCurrency();
            viewModel = new DraftsRowViewModel(draft, mViewModel.isSelected(draft), currency);
            binding.setViewModel(viewModel);
        } else {
            viewModel.updateDraftInfo(draft, mViewModel.isSelected(draft));
        }

        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mViewModel.getItemCount();
    }

    /**
     * Defines the actions to take when a user clicks on a purchase.
     */
    public interface AdapterInteractionListener {
        /**
         * Handles the click on a draft.
         *
         * @param position the adapter position of the draft
         */
        void onDraftRowClick(int position);

        /**
         * Handles the long click on a draft.
         *
         * @param position the adapter position of the draft
         */
        void onDraftRowLongClick(int position);
    }

    /**
     * Provides a {@link RecyclerView} row that displays a draft.
     * <p/>
     * Subclass of {@link RecyclerView.ViewHolder}.
     */
    public static class DraftRow extends BindingRow<RowDraftsBinding> {

        /**
         * Constructs a new {@link DraftRow} and sets the click listener.
         *
         * @param binding  the view's binding
         * @param listener the callback for user clicks on the draft
         */
        public DraftRow(@NonNull RowDraftsBinding binding,
                        @NonNull final AdapterInteractionListener listener) {
            super(binding);

            final View root = binding.getRoot();
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDraftRowClick(getAdapterPosition());
                }
            });
            root.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onDraftRowLongClick(getAdapterPosition());
                    return true;
                }
            });
        }
    }
}
