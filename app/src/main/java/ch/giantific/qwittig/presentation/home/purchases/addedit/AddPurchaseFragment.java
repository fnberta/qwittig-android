/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;
import ch.giantific.qwittig.presentation.home.purchases.addedit.di.AddPurchaseAutoViewModelModule;
import ch.giantific.qwittig.presentation.home.purchases.addedit.di.AddPurchaseViewModelModule;
import ch.giantific.qwittig.presentation.home.purchases.addedit.di.DaggerAddPurchaseAutoComponent;
import ch.giantific.qwittig.presentation.home.purchases.addedit.di.DaggerAddPurchaseComponent;

/**
 * Displays the interface where the user can add a new purchase by setting store, date, users
 * involved and the different items.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
 */
public class AddPurchaseFragment extends AddEditPurchaseBaseFragment<AddEditPurchaseViewModel, AddEditPurchaseBaseFragment.ActivityListener>
        implements AddEditPurchaseViewModel.ViewListener {

    private static final String KEY_AUTO_MODE = "AUTO_MODE";

    public AddPurchaseFragment() {
        // required empty constructor
    }

    /**
     * Returns a new add instance of {@link AddPurchaseFragment}.
     *
     * @return a new instance of {@link AddPurchaseFragment}
     */
    @NonNull
    public static AddPurchaseFragment newAddInstance() {
        final AddPurchaseFragment fragment = new AddPurchaseFragment();
        final Bundle args = new Bundle();
        args.putBoolean(KEY_AUTO_MODE, false);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Returns a new add instance of {@link AddPurchaseFragment} in auto scanning mode.
     *
     * @return a new instance of {@link AddPurchaseFragment}
     */
    @NonNull
    public static AddPurchaseFragment newAddAutoInstance() {
        final AddPurchaseFragment fragment = new AddPurchaseFragment();
        final Bundle args = new Bundle();
        args.putBoolean(KEY_AUTO_MODE, true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        injectDependencies(savedInstanceState);
    }

    private void injectDependencies(@Nullable Bundle savedInstanceState) {
        final boolean auto = getArguments().getBoolean(KEY_AUTO_MODE, false);
        if (auto) {
            DaggerAddPurchaseAutoComponent.builder()
                    .applicationComponent(Qwittig.getAppComponent(getActivity()))
                    .addPurchaseAutoViewModelModule(new AddPurchaseAutoViewModelModule(savedInstanceState, this))
                    .build()
                    .inject(this);
        } else {
            DaggerAddPurchaseComponent.builder()
                    .applicationComponent(Qwittig.getAppComponent(getActivity()))
                    .addPurchaseViewModelModule(new AddPurchaseViewModelModule(savedInstanceState, this))
                    .build()
                    .inject(this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        if (mViewModel.isLoading()) {
            setMenuVisibility(false);
            return;
        }

        inflater.inflate(R.menu.menu_purchase_add_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_purchase_save_draft:
                mViewModel.onSaveAsDraftMenuClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
