/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.listadapters.rows;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

/**
 * Provides a base class for a {@link RecyclerView} row that uses the Android data binding
 * framework to bind its views.
 * <p/>
 * Subclass {@link RecyclerView.ViewHolder}.
 */
public class BindingRow<T extends ViewDataBinding> extends RecyclerView.ViewHolder {

    private final T binding;

    /**
     * Constructs a new {@link BindingRow}.
     *
     * @param binding the binding to use
     */
    public BindingRow(@NonNull T binding) {
        super(binding.getRoot());

        this.binding = binding;
    }

    public T getBinding() {
        return binding;
    }
}
