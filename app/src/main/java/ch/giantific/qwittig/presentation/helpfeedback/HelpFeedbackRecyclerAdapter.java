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
import ch.giantific.qwittig.presentation.common.listadapters.rows.BindingRow;
import ch.giantific.qwittig.presentation.helpfeedback.viewmodels.items.BaseHelpFeedbackItemViewModel;
import ch.giantific.qwittig.presentation.helpfeedback.viewmodels.items.BaseHelpFeedbackItemViewModel.ViewType;
import ch.giantific.qwittig.presentation.helpfeedback.viewmodels.items.HelpFeedbackHeaderViewModel;
import ch.giantific.qwittig.presentation.helpfeedback.viewmodels.items.HelpFeedbackItemViewModel;

/**
 * Handles the display of help and feedback items.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class HelpFeedbackRecyclerAdapter extends RecyclerView.Adapter {

    private final HelpFeedbackContract.Presenter presenter;
    private final BaseHelpFeedbackItemViewModel[] items;

    /**
     * Constructs a new {@link HelpFeedbackRecyclerAdapter}.
     *
     * @param presenter the view model of the main view
     */
    public HelpFeedbackRecyclerAdapter(@NonNull HelpFeedbackContract.Presenter presenter) {
        super();

        this.presenter = presenter;
        this.items = presenter.getHelpFeedbackItems();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ViewType.HELP_FEEDBACK: {
                final RowHelpFeedbackBinding binding =
                        RowHelpFeedbackBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case ViewType.HEADER: {
                final RowGenericHeaderBinding binding =
                        RowGenericHeaderBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            default:
                throw new RuntimeException("There is no type that matches the type " + viewType +
                        ", make sure your using types correctly!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final BaseHelpFeedbackItemViewModel viewModel = items[position];
        int viewType = getItemViewType(position);
        switch (viewType) {
            case ViewType.HELP_FEEDBACK: {
                final BindingRow<RowHelpFeedbackBinding> row =
                        (BindingRow<RowHelpFeedbackBinding>) viewHolder;
                final RowHelpFeedbackBinding binding = row.getBinding();

                binding.setViewModel((HelpFeedbackItemViewModel) viewModel);
                binding.setPresenter(presenter);
                binding.executePendingBindings();
                break;
            }
            case ViewType.HEADER: {
                final BindingRow<RowGenericHeaderBinding> headerRow =
                        (BindingRow<RowGenericHeaderBinding>) viewHolder;
                final RowGenericHeaderBinding binding = headerRow.getBinding();

                binding.setViewModel((HelpFeedbackHeaderViewModel) viewModel);
                binding.executePendingBindings();
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items[position].getViewType();
    }

    @Override
    public int getItemCount() {
        return items.length;
    }
}
