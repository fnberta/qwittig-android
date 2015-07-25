package ch.giantific.qwittig.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.models.Item;
import ch.giantific.qwittig.data.ocr.models.ItemRest;
import ch.giantific.qwittig.data.ocr.models.PurchaseRest;
import ch.giantific.qwittig.utils.MoneyUtils;


/**
 * A placeholder fragment containing a simple view.
 */
public class PurchaseAddAutoFragment extends PurchaseAddFragment {

    private static final String STATE_ITEMS_SET = "items_set";
    private static final String LOG_TAG = PurchaseAddAutoFragment.class.getSimpleName();
    private View mProgressView;
    private View mMainView;
    private boolean mOcrValuesAreSet;
    private boolean mInTrialMode;

    public static PurchaseAddAutoFragment newInstance(boolean inTrialMode) {
        PurchaseAddAutoFragment purchaseAddAutoFragment = new PurchaseAddAutoFragment();
        Bundle args = new Bundle();
        args.putBoolean(PurchaseAddActivity.INTENT_PURCHASE_NEW_TRIAL_MODE, inTrialMode);
        purchaseAddAutoFragment.setArguments(args);
        return purchaseAddAutoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mInTrialMode = getArguments().getBoolean(
                    PurchaseAddActivity.INTENT_PURCHASE_NEW_TRIAL_MODE, false);
        }

        if (savedInstanceState != null) {
            mOcrValuesAreSet = savedInstanceState.getBoolean(STATE_ITEMS_SET);
        } else {
            mOcrValuesAreSet = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_ITEMS_SET, mOcrValuesAreSet);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_purchase_add_auto, container, false);

        findViews(rootView);

        return rootView;
    }

    @Override
    void findViews(View rootView) {
        super.findViews(rootView);

        mProgressView = rootView.findViewById(R.id.ll_progress);
        mMainView = rootView.findViewById(R.id.view_add);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mOcrValuesAreSet) {
            showMainScreen();
        }
    }

    @Override
    void revealFab() {
        if (mOcrValuesAreSet) {
            mListener.showFab();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mOcrValuesAreSet) {
            super.onCreateOptionsMenu(menu, inflater);
        } else {
            menu.clear();
        }
    }

    @Override
    void setupRows() {
        if (mOcrValuesAreSet) {
            super.setupRows();
        }
    }

    public void setValuesFromOcr(PurchaseRest purchaseRest) {
        // set date
        //setDateSelected(purchaseRest.getDate());
        // set store
        mTextViewPickStore.setText(purchaseRest.getStore());

        // set item rows
        List<ParseObject> itemsNew = new ArrayList<>();
        List<ItemRest> itemsRest = purchaseRest.getItems();
        int itemsRestSize = itemsRest.size();
        for (int i = 0; i < itemsRestSize; i++) {
            final ItemRest itemRest = itemsRest.get(i);
            final Item itemNew = (Item) addNewItemRow(i + 1);
            itemNew.setEditTextName(itemRest.getName());
            String price = MoneyUtils.formatPrice(itemRest.getPrice(), mCurrencySelected);
            itemNew.setEditTextPrice(price);
            itemsNew.add(itemNew);

            // update ImeOptions
            setEditTextPriceImeOptions();
        }

        if (!itemsNew.isEmpty()) {
            Item firstItem = (Item) itemsNew.get(0);
            firstItem.requestFocusForName();
        }

        mItemRowCount = itemsRestSize;
        mOcrValuesAreSet = true;
        showMainScreen();
    }

    public void showMainScreen() {
        mProgressView.setVisibility(View.GONE);
        mMainView.setVisibility(View.VISIBLE);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    protected void onSaveSucceeded() {
        if (mInTrialMode) {
            mCurrentUser.incrementPremiumCount();
            mCurrentUser.saveEventually();
        }

        super.onSaveSucceeded();
    }

    @Override
    void onPinSucceeded() {
        mIsSaving = false;
        mListener.setResultForSnackbar(PurchaseBaseActivity.PURCHASE_SAVED_AUTO);
        mListener.progressCircleStartFinal();
    }
}
