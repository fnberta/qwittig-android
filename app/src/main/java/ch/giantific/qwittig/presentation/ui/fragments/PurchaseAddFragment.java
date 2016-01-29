/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.di.components.DaggerPurchaseAddEditComponent;
import ch.giantific.qwittig.di.components.PurchaseAddEditComponent;
import ch.giantific.qwittig.di.modules.PurchaseAddAutoViewModelModule;
import ch.giantific.qwittig.di.modules.PurchaseAddViewModelModule;
import ch.giantific.qwittig.presentation.viewmodels.PurchaseAddEditViewModel;

/**
 * Displays the interface where the user can add a new purchase by setting store, date, users
 * involved and the different items.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
 */
public class PurchaseAddFragment extends PurchaseAddEditBaseFragment<PurchaseAddEditViewModel, PurchaseAddEditBaseFragment.ActivityListener>
        implements PurchaseAddEditViewModel.ViewListener {

    private static final String KEY_AUTO_MODE = "AUTO_MODE";

    public PurchaseAddFragment() {
        // required empty constructor
    }

    /**
     * Returns a new add instance of {@link PurchaseAddFragment}.
     *
     * @return a new instance of {@link PurchaseAddFragment}
     */
    @NonNull
    public static PurchaseAddFragment newAddInstance() {
        PurchaseAddFragment fragment = new PurchaseAddFragment();
        Bundle args = new Bundle();
        args.putBoolean(KEY_AUTO_MODE, false);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Returns a new add instance of {@link PurchaseAddFragment} in auto scanning mode.
     *
     * @return a new instance of {@link PurchaseAddFragment}
     */
    @NonNull
    public static PurchaseAddFragment newAddAutoInstance() {
        PurchaseAddFragment fragment = new PurchaseAddFragment();
        Bundle args = new Bundle();
        args.putBoolean(KEY_AUTO_MODE, true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        final boolean auto = getArguments().getBoolean(KEY_AUTO_MODE, false);
        final PurchaseAddEditComponent component;
        if (auto) {
            component = DaggerPurchaseAddEditComponent.builder()
                    .purchaseAddAutoViewModelModule(new PurchaseAddAutoViewModelModule(savedInstanceState))
                    .build();
        } else {
            component = DaggerPurchaseAddEditComponent.builder()
                    .purchaseAddViewModelModule(new PurchaseAddViewModelModule(savedInstanceState))
                    .build();
        }
        component.inject(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        if (mViewModel.isLoading()) {
            menu.clear();
            return;
        }

        inflater.inflate(R.menu.menu_purchase_add_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_purchase_save_draft:
                mViewModel.onSavePurchaseAsDraftClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
