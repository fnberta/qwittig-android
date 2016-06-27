/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.helpfeedback;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowGenericHeaderBinding;
import ch.giantific.qwittig.databinding.RowHelpFeedbackBinding;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.helpfeedback.itemmodels.HelpFeedbackItem;
import ch.giantific.qwittig.presentation.helpfeedback.itemmodels.HelpFeedbackHeader;
import ch.giantific.qwittig.presentation.helpfeedback.itemmodels.HelpFeedbackItemModel;
import ch.giantific.qwittig.presentation.helpfeedback.itemmodels.HelpFeedbackItemModel.Type;

/**
 * Handles the display of help and feedback items.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class HelpFeedbackRecyclerAdapter extends BaseRecyclerAdapter {

    private final HelpFeedbackViewModel mViewModel;

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
                return new BindingRow<>(binding);
            }
            case Type.HEADER: {
                final RowGenericHeaderBinding binding =
                        RowGenericHeaderBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            default:
                return super.onCreateViewHolder(parent, viewType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final HelpFeedbackItemModel item = mViewModel.getItemAtPosition(position);

        int viewType = getItemViewType(position);
        switch (viewType) {
            case Type.HELP_FEEDBACK: {
                final BindingRow<RowHelpFeedbackBinding> row = (BindingRow<RowHelpFeedbackBinding>) viewHolder;
                final RowHelpFeedbackBinding binding = row.getBinding();

                binding.setItemModel((HelpFeedbackItem) item);
                binding.setViewModel(mViewModel);
                binding.executePendingBindings();
                break;
            }
            case Type.HEADER: {
                final BindingRow<RowGenericHeaderBinding> headerRow =
                        (BindingRow<RowGenericHeaderBinding>) viewHolder;
                final RowGenericHeaderBinding binding = headerRow.getBinding();

                binding.setItemModel((HelpFeedbackHeader) item);
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
}
