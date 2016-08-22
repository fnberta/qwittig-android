/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.about;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowGenericHeaderBinding;
import ch.giantific.qwittig.databinding.RowAboutBinding;
import ch.giantific.qwittig.presentation.about.itemmodels.AboutHeader;
import ch.giantific.qwittig.presentation.about.itemmodels.AboutItem;
import ch.giantific.qwittig.presentation.about.itemmodels.AboutItemModel;
import ch.giantific.qwittig.presentation.about.itemmodels.AboutItemModel.Type;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.adapters.rows.BindingRow;

/**
 * Handles the display of information about Qwittig.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class AboutRecyclerAdapter extends BaseRecyclerAdapter {

    private final AboutViewModel viewModel;

    /**
     * Constructs a new {@link AboutRecyclerAdapter}.
     *
     * @param viewModel the view model of the main view
     */
    public AboutRecyclerAdapter(@NonNull AboutViewModel viewModel) {

        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case Type.ABOUT: {
                final RowAboutBinding binding =
                        RowAboutBinding.inflate(inflater, parent, false);
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
        final AboutItemModel itemModel = viewModel.getItemAtPosition(position);

        int viewType = getItemViewType(position);
        switch (viewType) {
            case Type.ABOUT: {
                final BindingRow<RowAboutBinding> row = (BindingRow<RowAboutBinding>) viewHolder;
                final RowAboutBinding binding = row.getBinding();

                binding.setItemModel((AboutItem) itemModel);
                binding.setViewModel(viewModel);
                binding.executePendingBindings();
                break;
            }
            case Type.HEADER: {
                final BindingRow<RowGenericHeaderBinding> headerRow =
                        (BindingRow<RowGenericHeaderBinding>) viewHolder;
                final RowGenericHeaderBinding binding = headerRow.getBinding();

                binding.setItemModel((AboutHeader) itemModel);
                binding.executePendingBindings();
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return viewModel.getItemAtPosition(position).getType();
    }

    @Override
    public int getItemCount() {
        return viewModel.getItemCount();
    }
}
