/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.BuildConfig;
import ch.giantific.qwittig.ComparatorParseUserIgnoreCase;
import ch.giantific.qwittig.ParseErrorHandler;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.helpers.RatesHelper;
import ch.giantific.qwittig.data.helpers.save.PurchaseSaveHelper;
import ch.giantific.qwittig.data.repositories.ParseUserRepository;
import ch.giantific.qwittig.domain.models.ItemRow;
import ch.giantific.qwittig.domain.models.Receipt;
import ch.giantific.qwittig.domain.models.parse.Config;
import ch.giantific.qwittig.domain.models.parse.Item;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.ui.activities.CameraActivity;
import ch.giantific.qwittig.ui.adapters.PurchaseUsersInvolvedRecyclerAdapter;
import ch.giantific.qwittig.ui.fragments.dialogs.DatePickerDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.ManualExchangeRateDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.PurchaseNoteEditDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.PurchaseUserSelectionDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.StoreSelectionDialogFragment;
import ch.giantific.qwittig.ui.listeners.SwipeDismissTouchListener;
import ch.giantific.qwittig.ui.widgets.ListCheckBox;
import ch.giantific.qwittig.utils.CameraUtils;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.HelperUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Provides an abstract base class for the add and edit purchase screens.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public abstract class PurchaseBaseFragment extends BaseFragment implements
        PurchaseUsersInvolvedRecyclerAdapter.AdapterInteractionListener {

    @IntDef({RESULT_PURCHASE_SAVED, RESULT_PURCHASE_SAVED_AUTO, RESULT_PURCHASE_DRAFT, RESULT_PURCHASE_ERROR,
            RESULT_PURCHASE_DISCARDED, RESULT_PURCHASE_DRAFT_DELETED, Activity.RESULT_CANCELED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PurchaseResults {}
    public static final int RESULT_PURCHASE_SAVED = 2;
    public static final int RESULT_PURCHASE_SAVED_AUTO = 3;
    public static final int RESULT_PURCHASE_DRAFT = 4;
    public static final int RESULT_PURCHASE_ERROR = 5;
    public static final int RESULT_PURCHASE_DISCARDED = 6;
    public static final int RESULT_PURCHASE_DRAFT_DELETED = 7;
    public static final String PURCHASE_NOTE_FRAGMENT = "PURCHASE_NOTE_FRAGMENT";
    static final String PURCHASE_SAVE_HELPER = "PURCHASE_SAVE_HELPER";
    static final int INTENT_REQUEST_IMAGE_CAPTURE = 1;
    // add permission to manifest when enabling!
    static final boolean USE_CUSTOM_CAMERA = false;
    private static final String DATE_PICKER_DIALOG = "DATE_PICKER_DIALOG";
    private static final String STORE_SELECTOR_DIALOG = "STORE_SELECTOR_DIALOG";
    private static final String USER_PICKER_DIALOG = "USER_PICKER_DIALOG";
    private static final String MANUAL_EXCHANGE_RATE_DIALOG = "MANUAL_EXCHANGE_RATE_DIALOG";
    private static final String EDIT_NOTE_DIALOG = "EDIT_NOTE_DIALOG";
    private static final String RATES_HELPER = "RATES_HELPER";
    private static final String PURCHASE_RECEIPT_FRAGMENT = "PURCHASE_RECEIPT_FRAGMENT";
    private static final String STATE_ROW_COUNT = "STATE_ROW_COUNT";
    private static final String STATE_STORE_SELECTED = "STATE_STORE_SELECTED";
    private static final String STATE_DATE_SELECTED = "STATE_DATE_SELECTED";
    private static final String STATE_PURCHASE_USERS_INVOLVED = "STATE_PURCHASE_USERS_INVOLVED";
    private static final String STATE_CURRENCY_SELECTED = "STATE_CURRENCY_SELECTED";
    private static final String STATE_NOTE = "STATE_NOTE";
    private static final String STATE_IS_SAVING = "STATE_IS_SAVING";
    private static final String STATE_IS_FETCHING_RATES = "STATE_IS_FETCHING_RATES";
    private static final String STATE_RECEIPT_IMAGES_PATHS = "STATE_RECEIPT_IMAGES_PATHS";
    private static final String STATE_RECEIPT_IMAGES_PATH = "STATE_RECEIPT_IMAGES_PATH";
    private static final String EXCHANGE_RATE_LAST_FETCHED_TIME = "EXCHANGE_RATE_LAST_FETCHED_TIME";
    private static final long EXCHANGE_RATE_REFRESH_INTERVAL = 24 * 60 * 60 * 1000;
    private static final String LOG_TAG = PurchaseBaseFragment.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_CAPTURE_IMAGES = 1;
    FragmentInteractionListener mListener;
    Purchase mPurchase;
    Date mDateSelected;
    String mStoreSelected;
    int mItemRowCount;
    @NonNull
    List<ParseObject> mItems = new ArrayList<>();
    double mTotalPrice;
    @NonNull
    List<ParseUser> mUsersAvailableParse = new ArrayList<>();
    boolean[] mPurchaseUsersInvolved;
    String mCurrencySelected;
    String mCurrentGroupCurrency;
    Button mButtonAddRow;
    @NonNull
    List<ItemRow> mItemRows = new ArrayList<>();
    float mExchangeRate;
    ArrayList<String> mReceiptImagePaths;
    String mReceiptImagePath;
    String mNote;
    private UserRepository mUserRepo;
    private TextView mTextViewPickStore;
    private boolean mIsSaving;
    private boolean mIsFetchingExchangeRates;
    private SharedPreferences mSharedPreferences;
    private TextView mTextViewPickDate;
    private Spinner mSpinnerCurrency;
    private TextView mTextViewExchangeRateDesc;
    private TextView mTextViewExchangeRate;
    private ArrayAdapter<String> mSpinnerCurrencySelectionAdapter;
    private TextView mTextViewTotalValue;
    private TextView mTextViewMyShareValue;
    private TextView mTextViewMyShareCurrency;
    private LinearLayout mLayoutTotalItemRow;
    private PurchaseUsersInvolvedRecyclerAdapter mRecyclerAdapter;
    private int mSelectedItemPosition;
    private CharSequence[] mUsersAvailableNicknames;

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserRepo = new ParseUserRepository();
        updateCurrentUserAndGroup();
        mCurrentGroupCurrency = mCurrentGroup.getCurrency();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (savedInstanceState != null) {
            mItemRowCount = savedInstanceState.getInt(STATE_ROW_COUNT);
            mDateSelected = DateUtils.parseLongToDate(savedInstanceState.getLong(STATE_DATE_SELECTED));
            mPurchaseUsersInvolved = savedInstanceState.getBooleanArray(STATE_PURCHASE_USERS_INVOLVED);
            mCurrencySelected = savedInstanceState.getString(STATE_CURRENCY_SELECTED, mCurrentGroupCurrency);
            mStoreSelected = savedInstanceState.getString(STATE_STORE_SELECTED, "");
            mNote = savedInstanceState.getString(STATE_NOTE, "");
            mIsSaving = savedInstanceState.getBoolean(STATE_IS_SAVING);
            mIsFetchingExchangeRates = savedInstanceState.getBoolean(STATE_IS_FETCHING_RATES);
            if (USE_CUSTOM_CAMERA) {
                ArrayList<String> imagePaths = savedInstanceState.getStringArrayList(STATE_RECEIPT_IMAGES_PATHS);
                mReceiptImagePaths = imagePaths != null ? imagePaths : new ArrayList<String>();
            }
            mReceiptImagePath = savedInstanceState.getString(STATE_RECEIPT_IMAGES_PATH, "");
        } else {
            mItemRowCount = 1;
            mDateSelected = new Date();
            mCurrencySelected = mCurrentGroupCurrency;
            if (USE_CUSTOM_CAMERA) {
                mReceiptImagePaths = new ArrayList<>();
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_ROW_COUNT, mItemRowCount);
        outState.putLong(STATE_DATE_SELECTED, DateUtils.parseDateToLong(mDateSelected));
        outState.putString(STATE_STORE_SELECTED, mStoreSelected);
        outState.putBooleanArray(STATE_PURCHASE_USERS_INVOLVED, mPurchaseUsersInvolved);
        outState.putString(STATE_CURRENCY_SELECTED, mCurrencySelected);
        outState.putString(STATE_NOTE, mNote);
        outState.putBoolean(STATE_IS_SAVING, mIsSaving);
        outState.putBoolean(STATE_IS_FETCHING_RATES, mIsFetchingExchangeRates);
        if (USE_CUSTOM_CAMERA) {
            outState.putStringArrayList(STATE_RECEIPT_IMAGES_PATHS, mReceiptImagePaths);
        }
        outState.putString(STATE_RECEIPT_IMAGES_PATH, mReceiptImagePath);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchase_add_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLayoutTotalItemRow = (LinearLayout) view.findViewById(R.id.ll_items);
        mButtonAddRow = (Button) view.findViewById(R.id.bt_item_add);
        mTextViewTotalValue = (TextView) view.findViewById(R.id.tv_total_value);
        mTextViewMyShareValue = (TextView) view.findViewById(R.id.tv_my_share_value);
        mTextViewMyShareCurrency = (TextView) view.findViewById(R.id.tv_my_share_currency);

        mTextViewPickDate = (TextView) view.findViewById(R.id.tv_date);
        mTextViewPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
        mTextViewPickDate.setText(DateUtils.formatDateLong(mDateSelected));

        mTextViewPickStore = (TextView) view.findViewById(R.id.tv_store);
        mTextViewPickStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStorePickerDialog(mTextViewPickStore.getText().toString());
            }
        });
        if (TextUtils.isEmpty(mStoreSelected)) {
            setStore(mCurrentUser.getStoresFavoriteFirstInList(), false);
        } else {
            setStore(mStoreSelected, false);
        }

        mButtonAddRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItemRowCount++;
                addNewItemRow(mItemRowCount);
                setEditTextPriceImeOptions();
                mSelectedItemPosition = mItemRowCount - 1;
                updatePurchaseUsersInvolved();
            }
        });

        RecyclerView recyclerViewUsersInvolved = (RecyclerView) view.findViewById(R.id.rv_users_involved);
        mRecyclerAdapter = new PurchaseUsersInvolvedRecyclerAdapter(getActivity(),
                mUsersAvailableParse, mCurrentUser, this);
        recyclerViewUsersInvolved.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));
        recyclerViewUsersInvolved.setHasFixedSize(true);
        recyclerViewUsersInvolved.setAdapter(mRecyclerAdapter);

        mSpinnerCurrency = (Spinner) view.findViewById(R.id.sp_currency);
        mSpinnerCurrencySelectionAdapter = new ArrayAdapter<>(
                getActivity(), R.layout.spinner_item_title, ParseUtils.getSupportedCurrencyCodes());
        mSpinnerCurrencySelectionAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCurrency.setAdapter(mSpinnerCurrencySelectionAdapter);
        mSpinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
                String currencyCode = (String) parent.getItemAtPosition(position);
                handleCurrencyChange(currencyCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        setCurrency(mCurrencySelected);

        mTextViewExchangeRateDesc = (TextView) view.findViewById(R.id.tv_exchange_rate_text);
        mTextViewExchangeRate = (TextView) view.findViewById(R.id.tv_exchange_rate_value);
        mTextViewExchangeRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                showManualExchangeRateSelectorDialog(((TextView) v).getText().toString());
            }
        });

        setupRows();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            revealFab();
        }
    }

    private void showDatePickerDialog() {
        DatePickerDialogFragment datePickerDialogFragment = new DatePickerDialogFragment();
        datePickerDialogFragment.show(getFragmentManager(), DATE_PICKER_DIALOG);
    }

    private void showStorePickerDialog(@NonNull String defaultStore) {
        StoreSelectionDialogFragment storeSelectionDialogFragment = StoreSelectionDialogFragment
                .newInstance(defaultStore);
        storeSelectionDialogFragment.show(getFragmentManager(), STORE_SELECTOR_DIALOG);
    }

    private void showManualExchangeRateSelectorDialog(@NonNull String exchangeRate) {
        ManualExchangeRateDialogFragment manualExchangeRateDialogFragment =
                ManualExchangeRateDialogFragment.newInstance(exchangeRate);
        manualExchangeRateDialogFragment.show(getFragmentManager(), MANUAL_EXCHANGE_RATE_DIALOG);
    }

    void revealFab() {
        mListener.showFab(mIsSaving);
    }

    /**
     * Sets the currency spinner to the position of the specified currency code.
     *
     * @param currencyCode the currency code to set the spinner to
     */
    final void setCurrency(String currencyCode) {
        int position = mSpinnerCurrencySelectionAdapter.getPosition(currencyCode);
        mSpinnerCurrency.setSelection(position);
    }

    private void handleCurrencyChange(@NonNull String currencyCode) {
        mCurrencySelected = currencyCode;
        int maxFractionDigits = MoneyUtils.getMaximumFractionDigits(currencyCode);

        // format total
        String totalString = mTextViewTotalValue.getText().toString();
        BigDecimal total = MoneyUtils.parsePrice(totalString)
                .setScale(maxFractionDigits, BigDecimal.ROUND_HALF_UP);
        setTotalValue(total);

        // format myShare
        String myShareString = mTextViewMyShareValue.getText().toString();
        BigDecimal myShare = MoneyUtils.parsePrice(myShareString)
                .setScale(maxFractionDigits, BigDecimal.ROUND_HALF_UP);
        setMyShareValue(myShare);

        // format item prices
        for (ItemRow itemRow : mItemRows) {
            itemRow.formatPrice(currencyCode);
        }

        // update exchangeRate
        updateExchangeRate();
    }

    void updateExchangeRate() {
        if (mCurrencySelected.equals(mCurrentGroupCurrency)) {
            mExchangeRate = 1;
            toggleExchangeRateViewVisibility();
        } else {
            mExchangeRate = mSharedPreferences.getFloat(mCurrencySelected, 1);
            if (mExchangeRate == 1) {
                fetchExchangeRateWithHelper();
            } else {
                long lastFetched = mSharedPreferences.getLong(EXCHANGE_RATE_LAST_FETCHED_TIME, 0);
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastFetched > EXCHANGE_RATE_REFRESH_INTERVAL) {
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putLong(EXCHANGE_RATE_LAST_FETCHED_TIME, currentTime);
                    editor.apply();

                    fetchExchangeRateWithHelper();
                } else {
                    setExchangeRate();
                }
            }
        }
    }

    private void setTotalValue(@NonNull BigDecimal totalValue) {
        mTextViewTotalValue.setText(MoneyUtils.formatMoneyNoSymbol(totalValue, mCurrencySelected));
    }

    private void setMyShareValue(@NonNull BigDecimal myShareValue) {
        mTextViewMyShareValue.setText(MoneyUtils.formatMoneyNoSymbol(myShareValue,
                mCurrencySelected));
        mTextViewMyShareCurrency.setText(mCurrencySelected);
    }

    final void setExchangeRate() {
        mTextViewExchangeRate.setText(MoneyUtils.formatMoneyNoSymbol(mExchangeRate,
                MoneyUtils.EXCHANGE_RATE_FRACTION_DIGITS));
        toggleExchangeRateViewVisibility();
    }

    private void toggleExchangeRateViewVisibility() {
        int visibility = mExchangeRate != 1 ? View.VISIBLE : View.GONE;
        mTextViewExchangeRateDesc.setVisibility(visibility);
        mTextViewExchangeRate.setVisibility(visibility);
    }

    /**
     * Sets the user set currency exchange rate to the member variable and currency spinner.
     *
     * @param exchangeRate the currency exchange rate to set
     */
    public void onExchangeRateSet(float exchangeRate) {
        BigDecimal roundedExchangeRate = MoneyUtils.roundToFractionDigits(
                MoneyUtils.EXCHANGE_RATE_FRACTION_DIGITS, exchangeRate);
        mExchangeRate = roundedExchangeRate.floatValue();
        setExchangeRate();
    }

    private void fetchExchangeRateWithHelper() {
        mIsFetchingExchangeRates = true;

        FragmentManager fragmentManager = getFragmentManager();
        Fragment ratesHelper = HelperUtils.findHelper(fragmentManager, RATES_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (ratesHelper == null) {
            ratesHelper = RatesHelper.newInstance(mCurrentGroupCurrency);

            fragmentManager.beginTransaction()
                    .add(ratesHelper, RATES_HELPER)
                    .commit();
        }
    }

    /**
     * Removes the retained helper fragment and indicates that there is no long a currency fetch
     * going on.
     *
     * @param errorMessage the network error message
     */
    public void onRatesFetchFailed(@NonNull String errorMessage) {
        HelperUtils.removeHelper(getFragmentManager(), RATES_HELPER);
        mIsFetchingExchangeRates = false;
    }

    /**
     * Removes the retained helper fragment and saves the fetched currencies in SharedPrefenreces.
     * <p/>
     * Sets the currency spinner to the exchange rate selected.
     *
     * @param exchangeRates the fetched exchange rates
     */
    public void onRatesFetched(@NonNull Map<String, Float> exchangeRates) {
        HelperUtils.removeHelper(getFragmentManager(), RATES_HELPER);

        mIsFetchingExchangeRates = false;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        for (Map.Entry<String, Float> exchangeRate : exchangeRates.entrySet()) {
            BigDecimal roundedExchangeRate = MoneyUtils.roundToFractionDigits(
                    MoneyUtils.EXCHANGE_RATE_FRACTION_DIGITS, 1 / exchangeRate.getValue());
            editor.putFloat(exchangeRate.getKey(), roundedExchangeRate.floatValue());
        }
        editor.apply();

        mExchangeRate = exchangeRates.get(mCurrencySelected);
        setExchangeRate();
    }

    @CallSuper
    void setupRows() {
        mItemRows.clear();
        for (int i = 0; i < mItemRowCount; i++) {
            addNewItemRow(i + 1);
        }

        setEditTextPriceImeOptions();
    }

    /**
     * Adds a new item row and sets the appropriate listeners.
     *
     * @param idCounter the number used to give the views unique ids
     * @return the newly instantiated {@link ItemRow}
     */
    @NonNull
    final ItemRow addNewItemRow(int idCounter) {
        View itemRowView = setupItemRow(idCounter);
        TextInputLayout tilItemName = setupItemRowName(itemRowView);
        TextInputLayout tilItemPrice = setupItemRowPrice(itemRowView);
        ListCheckBox cbEnabled = setupItemRowCheckbox(itemRowView);
        mLayoutTotalItemRow.addView(itemRowView);

        ItemRow itemRow = new ItemRow(getActivity().getApplicationContext(), itemRowView,
                idCounter, tilItemName, tilItemPrice, cbEnabled);
        mItemRows.add(itemRow);

        return itemRow;
    }

    @NonNull
    private View setupItemRow(int idCounter) {
        View itemRowView = getActivity().getLayoutInflater()
                .inflate(R.layout.row_purchase_add, mLayoutTotalItemRow, false);
        itemRowView.setTag(idCounter - 1); // tag will be used in the ClickListener to get the position of the row, -1 because List index starts at 0
        itemRowView.setOnClickListener(null); // SwipeDismissTouchListener doesn't work without an OnClickListener
        itemRowView.setOnTouchListener(new SwipeDismissTouchListener(itemRowView, null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(@NonNull View view, Object token) {
                        int position = Utils.getViewPositionFromTag(view);

                        mLayoutTotalItemRow.removeView(view);
                        mItemRows.remove(position);
                        mItemRowCount--;
                        resetIdsAndTags();
                        updatePurchaseUsersInvolved();
                        updateCheckBoxesColor();
                        updateTotalAndMyShareValues();
                    }
                }));
        return itemRowView;
    }

    @NonNull
    private TextInputLayout setupItemRowName(View itemRowView) {
        TextInputLayout tilItemName = (TextInputLayout) itemRowView.findViewById(R.id.til_item_name);
        tilItemName.requestFocus(); // somehow needed on Android 5.0+, otherwise etItemPrice gets focused
        return tilItemName;
    }

    @NonNull
    private TextInputLayout setupItemRowPrice(View itemRowView) {
        TextInputLayout tilItemPrice = (TextInputLayout) itemRowView.findViewById(R.id.til_item_price);
        EditText etItemPrice = tilItemPrice.getEditText();
        if (etItemPrice == null) {
            return tilItemPrice;
        }

        etItemPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTotalAndMyShareValues();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etItemPrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    EditText etPrice = (EditText) v;
                    String price = etPrice.getText().toString();
                    if (!TextUtils.isEmpty(price)) {
                        etPrice.setText(MoneyUtils.formatPrice(price, mCurrencySelected));
                    }
                }
            }
        });

        return tilItemPrice;
    }

    @NonNull
    private ListCheckBox setupItemRowCheckbox(View itemRowView) {
        ListCheckBox cbUsersInvolved = (ListCheckBox) itemRowView.findViewById(R.id.cb_item_enabled);
        cbUsersInvolved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                ListCheckBox cb = (ListCheckBox) v;
                mSelectedItemPosition = Utils.getViewPositionFromTag(v);

                int buyerPosition = mUsersAvailableParse.indexOf(mCurrentUser);
                cb.updateUsersCheckedAfterCheckedChange(buyerPosition, mPurchaseUsersInvolved);
                cb.setCheckBoxColor(mPurchaseUsersInvolved);
                updatePurchaseUsersInvolved();
                updateCheckBoxesColor();
                updateTotalAndMyShareValues();
            }
        });
        cbUsersInvolved.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(@NonNull View v) {
                ListCheckBox cb = (ListCheckBox) v;
                mSelectedItemPosition = Utils.getViewPositionFromTag(v);

                boolean[] usersChecked = cb.getUsersChecked();
                if (usersChecked != null) {
                    showUserPickerDialog(mUsersAvailableNicknames, usersChecked);
                }

                return true;
            }
        });

        // set a default value for the usersInvolved, this would fail for the first row in AddFragment
        // because the user lists are not yet setup. Hence we call another method in AddFragment to set
        // a default value for the first row when the user lists are ready. On recreation,
        // usersChecked of the checkbox will already be filled with values, hence no new values will
        // be added
        if (cbUsersInvolved.getUsersChecked() == null && mPurchaseUsersInvolved != null) {
            int buyerPosition = mUsersAvailableParse.indexOf(mCurrentUser);
            cbUsersInvolved.updateUsersCheckedAfterCheckedChange(buyerPosition, mPurchaseUsersInvolved);
        }

        return cbUsersInvolved;
    }

    private void showUserPickerDialog(@NonNull CharSequence[] usersAvailable,
                                      @NonNull boolean[] usersChecked) {
        PurchaseUserSelectionDialogFragment purchaseUserSelectionDialogFragment =
                PurchaseUserSelectionDialogFragment.newInstance(usersAvailable, usersChecked);
        purchaseUserSelectionDialogFragment.show(getFragmentManager(), USER_PICKER_DIALOG);
    }

    /**
     * Renumbers ids and tags after a row was dismissed. Needed for proper recreation and correct
     * determination of the position of an item row.
     */
    private void resetIdsAndTags() {
        for (int i = 0; i < mItemRows.size(); i++) {
            ItemRow itemRow = mItemRows.get(i);
            itemRow.setIds(i + 1);
            itemRow.setViewTag(i);
        }
    }

    private void updateTotalAndMyShareValues() {
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal myShare = BigDecimal.ZERO;

        for (int i = 0, mItemsSize = mItemRows.size(); i < mItemsSize; i++) {
            ItemRow itemRow = mItemRows.get(i);
            BigDecimal finalPrice = itemRow.getEditTextPrice(mCurrencySelected);

            // update total price
            totalPrice = totalPrice.add(finalPrice);

            // update my share
            boolean[] itemUsersChecked = itemRow.getUsersChecked();
            List<ParseUser> usersChecked =
                    getParseUsersInvolvedFromBoolean(itemUsersChecked);
            if (usersChecked.contains(mCurrentUser)) {
                BigDecimal usersCount = new BigDecimal(usersChecked.size());
                myShare = myShare.add(finalPrice.divide(usersCount,
                        MoneyUtils.getMaximumFractionDigits(mCurrencySelected),
                        BigDecimal.ROUND_HALF_UP));
            }
        }

        setTotalValue(totalPrice);
        setMyShareValue(myShare);
    }

    /**
     * Set ImeOptions to NEXT for all Price EditTexts, except the last row, set it to DONE there.
     * ImeOptions must be set explicitly, because otherwise NEXT would jump to the next vertical
     * EditText instead of the next horizontal.
     */
    final void setEditTextPriceImeOptions() {
        for (ItemRow itemRow : mItemRows) {
            itemRow.setPriceImeOptions(EditorInfo.IME_ACTION_NEXT);
        }
        ItemRow lastItemRow = Utils.getLastInList(mItemRows);
        if (lastItemRow != null) {
            lastItemRow.setPriceImeOptions(EditorInfo.IME_ACTION_DONE);
        }
    }

    final void fetchUsersAvailable() {
        mUserRepo.getUsersLocalAsync(mCurrentGroup, new UserRepository.GetUsersLocalListener() {
            @Override
            public void onUsersLocalLoaded(@NonNull List<ParseUser> users) {
                setupUserLists(users);
            }
        });
    }

    @CallSuper
    void setupUserLists(@NonNull List<ParseUser> users) {
        List<ParseUser> usersSorted = sortUserList(users);
        setupUsersAvailable(usersSorted);
        if (mPurchaseUsersInvolved == null) {
            setupPurchaseUsersInvolved();
        }
        mRecyclerAdapter.setUsersInvolved(mPurchaseUsersInvolved);
        mRecyclerAdapter.notifyDataSetChanged();
    }

    @NonNull
    private List<ParseUser> sortUserList(@NonNull List<ParseUser> users) {
        users.remove(mCurrentUser);
        Collections.sort(users, new ComparatorParseUserIgnoreCase());
        users.add(0, mCurrentUser);

        return users;
    }

    private void setupUsersAvailable(@NonNull List<ParseUser> users) {
        // list with user names and list with ParseUser objects
        mUsersAvailableParse.clear();
        int usersSize = users.size();
        mUsersAvailableNicknames = new CharSequence[usersSize];
        final Activity context = getActivity();
        for (int i = 0; i < usersSize; i++) {
            User user = (User) users.get(i);
            mUsersAvailableParse.add(user);
            mUsersAvailableNicknames[i] = user.getNicknameOrMe(context, mCurrentUser);
        }
    }

    /**
     * Different implementations in Add- and EditFragment. In {@link PurchaseAddFragment} the
     * default purchase wide usersInvolved will equal to the usersAvailable. in the
     * {@link PurchaseEditFragment} they  will be set according to the values in the original
     * purchase.
     */
    protected abstract void setupPurchaseUsersInvolved();

    @Override
    public void onPurchaseUserClick(int position) {
        if (mPurchaseUsersInvolved[position]) {
            if (!userIsLastOneChecked()) {
                mPurchaseUsersInvolved[position] = false;
                updateItemsUsersChecked(position, false);
                mRecyclerAdapter.notifyItemChanged(position);
            } else {
                selectMinOneUser();
            }
        } else {
            mPurchaseUsersInvolved[position] = true;
            updateItemsUsersChecked(position, true);
            mRecyclerAdapter.notifyItemChanged(position);
        }

        updateTotalAndMyShareValues();
    }

    private void updateItemsUsersChecked(int purchaseUserPosition, boolean purchaseUserIsChecked) {
        for (ItemRow itemRow : mItemRows) {
            itemRow.updateUsersCheckedAfterPurchaseUserClick(purchaseUserPosition, purchaseUserIsChecked);
            itemRow.updateCheckedStatus(mUsersAvailableParse.indexOf(mCurrentUser));
            itemRow.setCheckBoxColor(mPurchaseUsersInvolved);
        }
    }

    /**
     * Returns whether the user clicked is the last one checked. The user needs to select at least
     * one user.
     *
     * @return whether a clicked checked user is the only one checked
     */
    private boolean userIsLastOneChecked() {
        int countUsersChecked = Utils.countTrue(mPurchaseUsersInvolved);
        return countUsersChecked < 2;
    }

    private void selectMinOneUser() {
        MessageUtils.showBasicSnackbar(mButtonAddRow, getString(R.string.toast_min_one_user));
    }

    /**
     * Sets the users involved for an item row that were selected by the user in the dialog.
     * Infers the correct users from the users available member variable.
     *
     * @param usersInvolvedInt the users involved to set for the item row
     */
    public void onItemUsersInvolvedSet(@NonNull List<Integer> usersInvolvedInt) {
        List<ParseUser> usersInvolved = new ArrayList<>(usersInvolvedInt.size());
        for (Integer i : usersInvolvedInt) {
            usersInvolved.add(mUsersAvailableParse.get(i));
        }

        ItemRow itemRow = mItemRows.get(mSelectedItemPosition);
        boolean[] usersChecked = itemRow.getUsersChecked();
        int usersAvailableParseSize = mUsersAvailableParse.size();
        for (int i = 0; i < usersAvailableParseSize; i++) {
            ParseUser parseUser = mUsersAvailableParse.get(i);
            usersChecked[i] = usersInvolved.contains(parseUser);
        }
        itemRow.updateCheckedStatus(mUsersAvailableParse.indexOf(mCurrentUser));

        updatePurchaseUsersInvolved();
        updateCheckBoxesColor();
        updateTotalAndMyShareValues();
    }

    /**
     * Checks if a user is unselected in ALL items' users involved. If yes, un-selects user from
     * purchase wide users involved. If user is selected in at least one item, selects him in
     * purchase wide users involved as well.
     */
    private void updatePurchaseUsersInvolved() {
        for (int i = 0, usersAvailableSize = mUsersAvailableParse.size(); i < usersAvailableSize; i++) {
            final boolean currentIsChecked = mPurchaseUsersInvolved[i];
            boolean newIsChecked = false;

            for (ItemRow itemRow : mItemRows) {
                final boolean[] usersChecked = itemRow.getUsersChecked();

                if (usersChecked[i]) {
                    newIsChecked = true;
                }
            }

            if (currentIsChecked != newIsChecked) {
                mPurchaseUsersInvolved[i] = newIsChecked;
                mRecyclerAdapter.notifyItemChanged(i);
            }
        }
    }

    /**
     * Iterates trough all items and sets their checkbox' color appropriately
     */
    private void updateCheckBoxesColor() {
        for (ItemRow itemRow : mItemRows) {
            itemRow.setCheckBoxColor(mPurchaseUsersInvolved);
        }
    }

    /**
     * Sets the date picker to the given date and updates the class wide date selected variable.
     *
     * @param dateSelected the selected date
     */
    public void setDate(@NonNull Date dateSelected) {
        mTextViewPickDate.setText(DateUtils.formatDateLong(dateSelected));
        mDateSelected = dateSelected;
    }

    /**
     * Sets the store {@link TextView} to the selected date and updates the class wide store
     * selected variable. If the stores was added by manually by the user, it will save it in the
     * user's list of stores.
     *
     * @param storeSelected the selected store
     */
    public void setStore(@NonNull String storeSelected, boolean manuallyEntered) {
        mTextViewPickStore.setText(storeSelected);
        mStoreSelected = storeSelected;

        if (manuallyEntered && !ParseUtils.isTestUser(mCurrentUser)) {
            addNewStore(storeSelected);
        }
    }

    private void addNewStore(@NonNull String storeSelected) {
        ParseConfig config = ParseConfig.getCurrentConfig();
        List<String> defaultStores = config.getList(Config.DEFAULT_STORES);
        List<String> addedStores = mCurrentUser.getStoresAdded();
        boolean storeIsNew = true;

        for (String defaultStore : defaultStores) {
            if (defaultStore.equalsIgnoreCase(storeSelected)) {
                mTextViewPickStore.setText(defaultStore);
                mStoreSelected = storeSelected;
                storeIsNew = false;
            }
        }

        for (String addedStore : addedStores) {
            if (addedStore.equalsIgnoreCase(storeSelected)) {
                mTextViewPickStore.setText(addedStore);
                mStoreSelected = storeSelected;
                storeIsNew = false;
            }
        }

        if (storeIsNew) {
            mCurrentUser.addStoreAdded(storeSelected);
            mCurrentUser.addStoreFavorites(storeSelected);
            // user will be saved when purchase gets saved
        }
    }

    /**
     * Checks whether the permissions to take an image are granted and if yes initiates the creation
     * of the image file.
     */
    public void captureImage() {
        if (!CameraUtils.hasCameraHardware(getActivity())) {
            MessageUtils.showBasicSnackbar(mButtonAddRow, getString(R.string.toast_no_camera));
            return;
        }

        if (permissionsAreGranted()) {
            getImage();
        }
    }

    private boolean permissionsAreGranted() {
        if (USE_CUSTOM_CAMERA) {
            int hasCameraPerm = ContextCompat.checkSelfPermission(
                    getActivity(), Manifest.permission.CAMERA);
            if (hasCameraPerm != PackageManager.PERMISSION_GRANTED) {
                FragmentCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                        PERMISSIONS_REQUEST_CAPTURE_IMAGES);
                return false;
            }

            return true;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAPTURE_IMAGES:
                if (Utils.verifyPermissions(grantResults)) {
                    getImage();
                } else {
                    Snackbar snackbar = MessageUtils.getBasicSnackbar(mButtonAddRow,
                            getString(R.string.snackbar_permission_storage_denied));
                    snackbar.setAction(R.string.snackbar_action_open_settings, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startSystemSettings();
                        }
                    }).show();
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void getImage() {
        if (USE_CUSTOM_CAMERA) {
            Intent intent = new Intent(getActivity(), CameraActivity.class);
            startActivityForResult(intent, INTENT_REQUEST_IMAGE_CAPTURE);
        } else {
            final Context context = getActivity();
            File imageFile;
            try {
                imageFile = CameraUtils.createImageFile(context);
            } catch (IOException e) {
                // Error occurred while creating the File
                setResultForSnackbar(RESULT_PURCHASE_ERROR);
                finishPurchase();
                return;
            }

            mReceiptImagePath = imageFile.getAbsolutePath();
            Intent cameraIntent = CameraUtils.getCameraIntent(context, imageFile);
            if (cameraIntent != null) {
                startActivityForResult(cameraIntent, INTENT_REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void startSystemSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_IMAGE_CAPTURE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mListener.setHasReceiptFile(true);

                        if (USE_CUSTOM_CAMERA) {
                            List<String> paths = data.getStringArrayListExtra(CameraActivity.INTENT_EXTRA_PATHS);
                            setReceiptImagePaths(paths);
                        }

                        showImageTakenSnackbar();

                        break;
                }
                break;
        }
    }

    void showImageTakenSnackbar() {
        MessageUtils.showBasicSnackbar(mButtonAddRow, getString(R.string.toast_receipt_added));
    }

    /**
     * Sets the receipt image paths, only used when custom camera is enabled
     *
     * @param receiptImagePaths the paths of the receipt images
     */
    private void setReceiptImagePaths(@NonNull List<String> receiptImagePaths) {
        mReceiptImagePaths.clear();

        if (!receiptImagePaths.isEmpty()) {
            mReceiptImagePaths.addAll(receiptImagePaths);
        }

        mReceiptImagePath = mReceiptImagePaths.get(0);
    }

    /**
     * Sets the receipt image path field. Call from hosting {@link Activity} when receipt image
     * was changed in {@link PurchaseReceiptAddFragment}.
     *
     * @param receiptImagePath the path to the receipt image
     */
    public void setReceiptImagePath(String receiptImagePath) {
        mReceiptImagePath = receiptImagePath;
    }

    /**
     * Replaces the current fragment with a new instance of {@link PurchaseReceiptBaseFragment}.
     */
    public void showReceiptFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        PurchaseReceiptBaseFragment receiptFragment = getReceiptFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.container, receiptFragment, PURCHASE_RECEIPT_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    protected abstract PurchaseReceiptBaseFragment getReceiptFragment();

    /**
     * Deletes the receipt file of the purchase.
     */
    public abstract void deleteReceipt();

    /**
     * Sets the note for the purchase and displays a message to the user that it was added.
     */
    public void onNoteSet(@NonNull String note) {
        mNote = note;
    }

    /**
     * Sets the note field to an empty string. When the user saves the purchase it will get deleted
     * in the Parse.com online database.
     */
    public void deleteNote() {
        mNote = "";
    }

    /**
     * Opens a dialog that allows the user to edit the note.
     */
    public void editNote() {
        PurchaseNoteEditDialogFragment dialog = PurchaseNoteEditDialogFragment.newInstance(mNote);
        dialog.show(getFragmentManager(), EDIT_NOTE_DIALOG);
    }

    /**
     * Replaces the current fragment with a new instance of {@link PurchaseNoteFragment}.
     */
    public void showNoteFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        PurchaseNoteFragment noteFragment = PurchaseNoteFragment.newInstance(mNote);
        fragmentManager.beginTransaction()
                .replace(R.id.container, noteFragment, PURCHASE_NOTE_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Initiates the save of the purchase if there is not currently a save ongoing and exchange
     * rate data is available for foreign currencies.
     */
    public void savePurchase(boolean saveAsDraft) {
        if (mIsFetchingExchangeRates) {
            MessageUtils.showBasicSnackbar(mButtonAddRow, getString(R.string.toast_exchange_rate_fetching));
            return;
        }

        if (!saveAsDraft && !mCurrencySelected.equals(mCurrentGroupCurrency) && mExchangeRate == 1) {
            Snackbar snackbar = MessageUtils.getBasicSnackbar(mButtonAddRow, getString(R.string.toast_exchange_no_data));
            snackbar.setAction(R.string.action_purchase_save_draft, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    savePurchase(true);
                }
            });

            return;
        }

        if (!mIsSaving) {
            boolean itemsAreComplete = setItemValues(saveAsDraft);
            if (itemsAreComplete) {
                if (saveAsDraft) {
                    savePurchaseAsDraft();
                } else if (ParseUtils.isTestUser(mCurrentUser)) {
                    mListener.showAccountCreateDialog();
                } else if (!Utils.isConnected(getActivity())) {
                    showErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(),
                            ParseException.CONNECTION_FAILED));
                } else {
                    mIsSaving = true;
                    mListener.progressCircleShow();
                    setPurchase();
                    savePurchaseWithHelper();
                }
            }
        }
    }

    /**
     * Returns whether all the fields in the item rows have a valid value. Reads values from the
     * item rows and creates new {@link Item} objects if they are valid.
     *
     * @param acceptEmptyFields whether to accept empty fields or not, useful for drafts where
     *                          empty fields are allowed
     * @return whether all required fields have a valid value
     */
    private boolean setItemValues(boolean acceptEmptyFields) {
        if (mItemRows.size() < 1) {
            MessageUtils.showBasicSnackbar(mButtonAddRow, getString(R.string.toast_min_one_item));
            return false;
        }

        boolean itemsAreComplete = true;
        for (ItemRow itemRow : mItemRows) {
            boolean itemIsComplete = itemRow.setValuesFromEditTexts(acceptEmptyFields, mCurrencySelected);

            if (!itemIsComplete) {
                itemsAreComplete = false;
            }
        }

        if (itemsAreComplete) {
            mTotalPrice = 0;
            for (int i = 0, mItemRowsSize = mItemRows.size(); i < mItemRowsSize; i++) {
                ItemRow itemRow = mItemRows.get(i);

                // add item price to purchase totalPrice
                mTotalPrice += itemRow.getPrice().doubleValue();

                // get usersInvolved for the item
                boolean[] usersChecked = itemRow.getUsersChecked();
                List<ParseUser> usersInvolved = getParseUsersInvolvedFromBoolean(usersChecked);

                // create new Item object and add to list
                Item item = new Item(itemRow.getName(), itemRow.getPrice(), usersInvolved,
                        mCurrentGroup);
                mItems.add(item);
            }

            return true;
        }

        return false;
    }

    /**
     * {@link PurchaseAddFragment} creates a new purchase and {@link PurchaseEditFragment} updates
     * the original one.
     */
    protected abstract void setPurchase();

    private void savePurchaseWithHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment purchaseSaveHelper = HelperUtils.findHelper(fragmentManager, PURCHASE_SAVE_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (purchaseSaveHelper == null) {
            purchaseSaveHelper = getSaveHelper();

            fragmentManager.beginTransaction()
                    .add(purchaseSaveHelper, PURCHASE_SAVE_HELPER)
                    .commit();
        }
    }

    protected abstract PurchaseSaveHelper getSaveHelper();

    /**
     * Provides an error handler for error codes. Passes the code to the generic error handler,
     * shows the user an appropriate error message and hides loading indicators.
     *
     * @param errorCode the error code of the exception to handle
     */
    @CallSuper
    public void onSaveError(int errorCode) {
        final Activity context = getActivity();
        ParseErrorHandler.handleParseError(context, errorCode);
        showErrorSnackbar(ParseErrorHandler.getErrorMessage(context, errorCode));

        mIsSaving = false;
        mListener.progressCircleHide();
    }

    void showErrorSnackbar(@NonNull String message) {
        MessageUtils.showBasicSnackbar(mButtonAddRow, message);
    }

    /**
     * Sets the activity result and starts the final animation of the {@link FABProgressCircle}.
     */
    public void onPurchaseSavedAndPinned() {
        mIsSaving = false;
        setResultForSnackbar(getPurchaseSavedAction());
        mListener.progressCircleStartFinal();
    }

    @PurchaseResults
    int getPurchaseSavedAction() {
        return RESULT_PURCHASE_SAVED;
    }

    /**
     * Passes the error code to the error handler and removes the retained helper fragment.
     *
     * @param errorCode the error code of the exception thrown in the process
     */
    public void onPurchaseSaveFailed(int errorCode) {
        onSaveError(errorCode);
        HelperUtils.removeHelper(getFragmentManager(), PURCHASE_SAVE_HELPER);
    }

    /**
     * Saves the purchase as local draft in {@link PurchaseAddFragment}. Saves changes to draft in
     * {@link PurchaseEditDraftFragment}. Does nothing in {@link PurchaseEditFragment} (option so
     * save as draft n/a).
     */
    protected abstract void savePurchaseAsDraft();

    final void getReceiptDataForDraft() {
        Glide.with(this).load(mReceiptImagePath)
                .asBitmap()
                .toBytes(Bitmap.CompressFormat.JPEG, Receipt.JPEG_COMPRESSION_RATE)
                .centerCrop()
                .into(new SimpleTarget<byte[]>(Receipt.WIDTH, Receipt.HEIGHT) {
                    @Override
                    public void onResourceReady(@NonNull byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                        mPurchase.setReceiptData(resource);
                        pinPurchaseAsDraft();
                    }
                });
    }

    /**
     * Sets new random draft id of the purchase if is not already set and swaps the parse file to a
     * byte array.
     */
    final void pinPurchaseAsDraft() {
        mPurchase.setRandomDraftId();
        mPurchase.pinInBackground(new SaveCallback() {
            @Override
            public void done(@Nullable ParseException e) {
                if (e != null) {
                    onSaveError(e.getCode());
                    return;
                }

                onPinAsDraftSucceeded();
            }
        });
    }

    private void onPinAsDraftSucceeded() {
        setResultForSnackbar(RESULT_PURCHASE_DRAFT);
        finishPurchase();
    }

    /**
     * Sets the activity result to discarded and finishes.
     */
    public void onDiscardPurchaseSelected() {
        setResultForSnackbar(RESULT_PURCHASE_DISCARDED);
        finishPurchase();
    }

    final void setResultForSnackbar(@PurchaseResults int resultCode) {
        getActivity().setResult(resultCode);
    }

    /**
     * Finishes the activity after the return transition and delete all images the might have
     * taken.
     */
    public final void finishPurchase() {
        ActivityCompat.finishAfterTransition(getActivity());
        deleteTakenImages();
    }

    final void deleteTakenImages() {
        if (USE_CUSTOM_CAMERA) {
            if (!mReceiptImagePaths.isEmpty()) {
                for (String path : mReceiptImagePaths) {
                    boolean fileDeleted = new File(path).delete();
                    if (!fileDeleted && BuildConfig.DEBUG) {
                        Log.e(LOG_TAG, "failed to delete file");
                    }
                }
            }
        } else if (!TextUtils.isEmpty(mReceiptImagePath)) {
            boolean fileDeleted = new File(mReceiptImagePath).delete();
            if (!fileDeleted && BuildConfig.DEBUG) {
                Log.e(LOG_TAG, "failed to delete file");
            }
        }
    }

    /**
     * Returns the {@link User} objects for a given boolean array. Needed because the users involved
     * (both purchase wide and for each item) are stored in boolean arrays.
     *
     * @param usersInvolvedBoolean the users involved as booleans
     * @return the corresponding {@link User} objects for the booleans
     */
    @NonNull
    final List<ParseUser> getParseUsersInvolvedFromBoolean(@NonNull boolean[] usersInvolvedBoolean) {
        final List<ParseUser> usersInvolved = new ArrayList<>();
        for (int i = 0, mUsersAvailableParseSize = mUsersAvailableParse.size();
             i < mUsersAvailableParseSize; i++) {
            ParseUser parseUser = mUsersAvailableParse.get(i);
            if (usersInvolvedBoolean[i]) {
                usersInvolved.add(parseUser);
            }
        }
        return usersInvolved;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     */
    public interface FragmentInteractionListener extends BaseFragmentInteractionListener {
        /**
         * Handles the update of action bar menu of the activity regarding the receipt file menu
         * option.
         *
         * @param hasReceiptFile whether to show the option to show the receipt file or not
         */
        void setHasReceiptFile(boolean hasReceiptFile);

        /**
         * Handles the update of action bar menu of the activity regarding the note file menu
         * option.
         *
         * @param hasNote whether to show the option to show the note or not
         */
        void setHasNote(boolean hasNote);

        /**
         * Indicates that the {@link FloatingActionButton} should be revealed and if or without the
         * loading animation
         *
         * @param isSaving whether to show the loading animation or not
         */
        void showFab(boolean isSaving);

        /**
         * Indicates to start the loading animation of the {@link FABProgressCircle}.
         */
        void progressCircleShow();

        /**
         * Indicates to start the final loading animation of the {@link FABProgressCircle}.
         */
        void progressCircleStartFinal();

        /**
         * Indicates to hide the loading animation of the {@link FABProgressCircle}.
         */
        void progressCircleHide();
    }
}
