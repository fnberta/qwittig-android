/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.about;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.RowAboutBinding;
import ch.giantific.qwittig.databinding.RowGenericHeaderBinding;
import ch.giantific.qwittig.presentation.about.viewmodels.items.AboutHeaderViewModel;
import ch.giantific.qwittig.presentation.about.viewmodels.items.AboutItemViewModel;
import ch.giantific.qwittig.presentation.about.viewmodels.items.BaseAboutItemViewModel;
import ch.giantific.qwittig.presentation.about.viewmodels.items.BaseAboutItemViewModel.ViewType;
import ch.giantific.qwittig.presentation.common.listadapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.listadapters.rows.BindingRow;

/**
 * Handles the display of information about Qwittig.
 * <p/>
 * Subclass of {@link RecyclerView.Adapter}.
 */
public class AboutRecyclerAdapter extends BaseRecyclerAdapter {

    private final AboutContract.Presenter presenter;

    /**
     * Constructs a new {@link AboutRecyclerAdapter}.
     *
     * @param presenter the view model of the main view
     */
    public AboutRecyclerAdapter(@NonNull AboutContract.Presenter presenter) {
        super();

        this.presenter = presenter;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case ViewType.ABOUT: {
                final RowAboutBinding binding =
                        RowAboutBinding.inflate(inflater, parent, false);
                return new BindingRow<>(binding);
            }
            case ViewType.HEADER: {
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
        final BaseAboutItemViewModel viewModel = presenter.getItemAtPosition(position);

        int viewType = getItemViewType(position);
        switch (viewType) {
            case ViewType.ABOUT: {
                final BindingRow<RowAboutBinding> row = (BindingRow<RowAboutBinding>) viewHolder;
                final RowAboutBinding binding = row.getBinding();

                binding.setViewModel((AboutItemViewModel) viewModel);
                binding.setPresenter(presenter);
                binding.executePendingBindings();
                break;
            }
            case ViewType.HEADER: {
                final BindingRow<RowGenericHeaderBinding> headerRow =
                        (BindingRow<RowGenericHeaderBinding>) viewHolder;
                final RowGenericHeaderBinding binding = headerRow.getBinding();

                binding.setViewModel((AboutHeaderViewModel) viewModel);
                binding.executePendingBindings();
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return presenter.getItemAtPosition(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return presenter.getItemCount();
    }
}
