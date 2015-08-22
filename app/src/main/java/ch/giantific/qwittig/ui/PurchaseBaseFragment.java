package ch.giantific.qwittig.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.CallSuper;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import com.google.common.collect.Iterables;
import com.google.common.primitives.Booleans;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.ItemRow;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Config;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.Item;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.helper.PurchaseSaveHelper;
import ch.giantific.qwittig.helper.RatesHelper;
import ch.giantific.qwittig.ui.adapter.PurchaseAddUsersInvolvedRecyclerAdapter;
import ch.giantific.qwittig.ui.widgets.ListCheckBox;
import ch.giantific.qwittig.ui.listeners.SwipeDismissTouchListener;
import ch.giantific.qwittig.utils.ComparatorParseUserIgnoreCase;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Created by fabio on 14.01.15.
 */
public abstract class PurchaseBaseFragment extends BaseFragment implements
        PurchaseAddUsersInvolvedRecyclerAdapter.AdapterInteractionListener,
        LocalQuery.UserLocalQueryListener {

    static final String RATES_HELPER = "rates_helper";
    static final String PURCHASE_SAVE_HELPER = "save_helper";
    private static final String STATE_ROW_COUNT = "row_count";
    private static final String STATE_STORE_SELECTED = "state_store_selected";
    private static final String STATE_DATE_SELECTED = "state_date_selected";
    private static final String STATE_PURCHASE_USERS_INVOLVED = "state_purchase_users_involved";
    private static final String STATE_CURRENCY_SELECTED = "state_currency_selected";
    private static final String STATE_IS_SAVING = "state_is_saving";
    private static final String STATE_IS_FETCHING_RATES = "state_is_fetching_rates";
    private static final String EXCHANGE_RATE_LAST_FETCHED_TIME = "rates_last_fetched";
    private static final long EXCHANGE_RATE_REFRESH_INTERVAL = 24 * 60 * 60 * 1000;
    private static final String LOG_TAG = PurchaseBaseFragment.class.getSimpleName();
    FragmentInteractionListener mListener;
    Purchase mPurchase;
    Date mDateSelected;
    TextView mTextViewPickStore;
    String mStoreSelected;
    int mItemRowCount;
    List<ParseObject> mItems = new ArrayList<>();
    double mTotalPrice;
    List<ParseUser> mUsersAvailableParse = new ArrayList<>();
    List<Boolean> mPurchaseUsersInvolved;
    Group mCurrentGroup;
    String mCurrencySelected;
    String mCurrentGroupCurrency;
    User mCurrentUser;
    boolean mIsSaving;
    Button mButtonAddRow;
    List<ItemRow> mItemRows = new ArrayList<>();
    float mExchangeRate;
    private boolean mIsFetchingExchangeRates;
    private SharedPreferences mSharedPreferences;
    private View mViewDate;
    private View mViewStore;
    private TextView mTextViewPickDate;
    private Spinner mSpinnerCurrency;
    private TextView mTextViewExchangeRateDesc;
    private TextView mTextViewExchangeRate;
    private ArrayAdapter<String> mSpinnerCurrencySelectionAdapter;
    private TextView mTextViewTotalValue;
    private TextView mTextViewMyShareValue;
    private TextView mTextViewMyShareCurrency;
    private LinearLayout mLayoutTotalItemRow;
    private RecyclerView mRecyclerViewUsersInvolved;
    private PurchaseAddUsersInvolvedRecyclerAdapter mRecyclerAdapter;
    private int mSelectedItemPosition;
    private List<String> mUsersAvailable = new ArrayList<>();
    private CharSequence[] mUsersAvailableArray;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurrentUser = (User) ParseUser.getCurrentUser();
        mCurrentGroup = mCurrentUser.getCurrentGroup();
        mCurrentGroupCurrency = mCurrentGroup.getCurrency();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (savedInstanceState != null) {
            mItemRowCount = savedInstanceState.getInt(STATE_ROW_COUNT);
            mDateSelected = DateUtils.parseLongToDate(savedInstanceState.getLong(STATE_DATE_SELECTED));
            boolean[] usersInvolved = savedInstanceState.getBooleanArray(STATE_PURCHASE_USERS_INVOLVED);
            mPurchaseUsersInvolved = usersInvolved != null ? Booleans.asList(usersInvolved) : new ArrayList<Boolean>();
            mCurrencySelected = savedInstanceState.getString(STATE_CURRENCY_SELECTED);
            mStoreSelected = savedInstanceState.getString(STATE_STORE_SELECTED);
            mIsSaving = savedInstanceState.getBoolean(STATE_IS_SAVING);
            mIsFetchingExchangeRates = savedInstanceState.getBoolean(STATE_IS_FETCHING_RATES);
        } else {
            mItemRowCount = 1;
            mDateSelected = new Date();
            mPurchaseUsersInvolved = new ArrayList<>();
            mCurrencySelected = mCurrentGroupCurrency;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_ROW_COUNT, mItemRowCount);
        outState.putLong(STATE_DATE_SELECTED, DateUtils.parseDateToLong(mDateSelected));
        outState.putString(STATE_STORE_SELECTED, mStoreSelected);
        outState.putBooleanArray(STATE_PURCHASE_USERS_INVOLVED,
                Booleans.toArray(mPurchaseUsersInvolved));
        outState.putString(STATE_CURRENCY_SELECTED, mCurrencySelected);
        outState.putBoolean(STATE_IS_SAVING, mIsSaving);
        outState.putBoolean(STATE_IS_FETCHING_RATES, mIsFetchingExchangeRates);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_purchase_add_edit, container, false);
        findViews(rootView);

        return rootView;
    }

    @CallSuper
    void findViews(View rootView) {
        mViewDate = rootView.findViewById(R.id.ll_date);
        mTextViewPickDate = (TextView) rootView.findViewById(R.id.tv_date);
        mViewStore = rootView.findViewById(R.id.ll_store);
        mTextViewPickStore = (TextView) rootView.findViewById(R.id.tv_store);
        mLayoutTotalItemRow = (LinearLayout) rootView.findViewById(R.id.ll_items);
        mButtonAddRow = (Button) rootView.findViewById(R.id.bt_item_add);
        mTextViewTotalValue = (TextView) rootView.findViewById(R.id.tv_total_value);
        mSpinnerCurrency = (Spinner) rootView.findViewById(R.id.sp_currency);
        mTextViewMyShareValue = (TextView) rootView.findViewById(R.id.tv_my_share_value);
        mTextViewMyShareCurrency = (TextView) rootView.findViewById(R.id.tv_my_share_currency);
        mTextViewExchangeRateDesc = (TextView) rootView.findViewById(R.id.tv_exchange_rate_text);
        mTextViewExchangeRate = (TextView) rootView.findViewById(R.id.tv_exchange_rate_value);
        mRecyclerViewUsersInvolved = (RecyclerView) rootView.findViewById(R.id.rv_users_involved);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.showDatePickerDialog();
            }
        });
        mTextViewPickDate.setText(DateUtils.formatDateLong(mDateSelected));

        mViewStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showStorePickerDialog(mTextViewPickStore.getText().toString());
            }
        });

        if (TextUtils.isEmpty(mStoreSelected)) {
            setStore(mCurrentUser.getStoresFavoriteFirstInList(getString(R.string.dialog_store_other)), false);
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

        mRecyclerAdapter = new PurchaseAddUsersInvolvedRecyclerAdapter(getActivity(),
                R.layout.row_users_involved_list, mUsersAvailableParse, mPurchaseUsersInvolved,
                this);
        mRecyclerViewUsersInvolved.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));
        mRecyclerViewUsersInvolved.setHasFixedSize(true);
        mRecyclerViewUsersInvolved.setAdapter(mRecyclerAdapter);

        mSpinnerCurrencySelectionAdapter = new ArrayAdapter<>(
                getActivity(), R.layout.spinner_item_title, ParseUtils.getSupportedCurrencyCodes());
        mSpinnerCurrencySelectionAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCurrency.setAdapter(mSpinnerCurrencySelectionAdapter);
        mSpinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String currencyCode = (String) parent.getItemAtPosition(position);
                handleCurrencyChange(currencyCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        setCurrency(mCurrencySelected);

        mTextViewExchangeRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showManualExchangeRateSelectorDialog(((TextView) v).getText().toString());
            }
        });

        if (savedInstanceState != null) {
            revealFab();
        }

        setupRows();
    }

    void revealFab() {
        mListener.showFab(mIsSaving);
    }

    /**
     * Sets the currency spinner to the position of the specified currency code.
     *
     * @param currencyCode
     */
    final void setCurrency(String currencyCode) {
        int position = mSpinnerCurrencySelectionAdapter.getPosition(currencyCode);
        mSpinnerCurrency.setSelection(position);
    }

    private void handleCurrencyChange(String currencyCode) {
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

    private void setTotalValue(BigDecimal totalValue) {
        mTextViewTotalValue.setText(MoneyUtils.formatMoneyNoSymbol(totalValue, mCurrencySelected));
    }

    private void setMyShareValue(BigDecimal myShareValue) {
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
     * Called from activity when user set a manual exchange rate in the dialog
     * @param exchangeRate
     */
    public void setExchangeRateManual(float exchangeRate) {
        BigDecimal roundedExchangeRate = MoneyUtils.roundToFractionDigits(
                MoneyUtils.EXCHANGE_RATE_FRACTION_DIGITS, exchangeRate);
        mExchangeRate = roundedExchangeRate.floatValue();
        setExchangeRate();
    }

    private void fetchExchangeRateWithHelper() {
        mIsFetchingExchangeRates = true;

        FragmentManager fragmentManager = getFragmentManager();
        RatesHelper ratesHelper = findRatesHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (ratesHelper == null) {
            ratesHelper = RatesHelper.newInstance(mCurrentGroupCurrency);

            fragmentManager.beginTransaction()
                    .add(ratesHelper, RATES_HELPER)
                    .commit();
        }
    }

    private RatesHelper findRatesHelper(FragmentManager fragmentManager) {
        return (RatesHelper) fragmentManager.findFragmentByTag(RATES_HELPER);
    }

    private void removeRatesHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        RatesHelper ratesHelper = findRatesHelper(fragmentManager);

        if (ratesHelper != null) {
            fragmentManager.beginTransaction().remove(ratesHelper).commitAllowingStateLoss();
        }
    }

    /**
     * Called from activity when helper failed to fetch rates
     *
     * @param errorMessage the network error message
     */
    public void onRatesFetchFailed(String errorMessage) {
        removeRatesHelper();
        mIsFetchingExchangeRates = false;
    }

    /**
     * Called from activity when helper successfully fetched currency rates
     *
     * @param exchangeRates
     */
    public void onRatesFetchSuccessful(Map<String, Float> exchangeRates) {
        removeRatesHelper();

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
     * @param idCounter int that is used to give the views unique ids.
     * @return the newly created Item Object
     */
    final ItemRow addNewItemRow(int idCounter) {
        View itemRowView = getActivity().getLayoutInflater()
                .inflate(R.layout.row_add_purchase, mLayoutTotalItemRow, false);
        itemRowView.setTag(idCounter - 1); // tag will be used in the ClickListener to get the position of the row, -1 because List index starts at 0

        TextInputLayout tilItemName = (TextInputLayout) itemRowView.findViewById(R.id.til_item_name);
        tilItemName.requestFocus(); // somehow needed on Android 5.0+, otherwise etItemPrice gets focused

        TextInputLayout tilItemPrice = (TextInputLayout) itemRowView.findViewById(R.id.til_item_price);
        EditText etItemPrice = tilItemPrice.getEditText();
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

        final ListCheckBox cbEnabled = (ListCheckBox) itemRowView.findViewById(R.id.cb_item_enabled);
        cbEnabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedItemPosition = Utils.getViewPositionFromTag(v);

                int buyerPosition = mUsersAvailableParse.indexOf(ParseUser.getCurrentUser());
                cbEnabled.updateUsersCheckedAfterCheckedChange(buyerPosition, mPurchaseUsersInvolved);
                cbEnabled.setCheckBoxColor(mPurchaseUsersInvolved);
                updatePurchaseUsersInvolved();
                updateTotalAndMyShareValues();
            }
        });
        cbEnabled.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mSelectedItemPosition = Utils.getViewPositionFromTag(v);

                boolean[] usersChecked = cbEnabled.getUsersChecked();
                mListener.showUserPickerDialog(mUsersAvailableArray, usersChecked);

                return true;
            }
        });

        // set a default value for the usersInvolved, this would fail for the first row in AddFragment
        // because the user lists are not yet setup. Hence we call another method in AddFragment to set
        // a default value for the first row when the user lists are ready. On recreation,
        // mItemsUsersChecked will already be filled with values, hence no new values will be added
        // (size() will be bigger than idCounter)
        if (cbEnabled.getUsersChecked() == null && !mPurchaseUsersInvolved.isEmpty()) {
            int buyerPosition = mUsersAvailableParse.indexOf(ParseUser.getCurrentUser());
            cbEnabled.updateUsersCheckedAfterCheckedChange(buyerPosition, mPurchaseUsersInvolved);
        }

        itemRowView.setOnClickListener(null); // SwipeDismissTouchListener doesn't work without an OnClickListener
        itemRowView.setOnTouchListener(new SwipeDismissTouchListener(itemRowView, null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        int position = Utils.getViewPositionFromTag(view);

                        mLayoutTotalItemRow.removeView(view);
                        mItemRows.remove(position);
                        mItemRowCount--;
                        resetIdsAndTags();
                        updatePurchaseUsersInvolved();
                        updateTotalAndMyShareValues();
                    }
                }));

        mLayoutTotalItemRow.addView(itemRowView);

        ItemRow itemRow = new ItemRow(getActivity().getApplicationContext(), itemRowView,
                idCounter, tilItemName, tilItemPrice, cbEnabled);
        mItemRows.add(itemRow);

        return itemRow;
    }

    /**
     * Renumbers ids and tags after a row was dismissed. Needed for proper recreation and correct
     * determination of itemRow position
     */
    private void resetIdsAndTags() {
        for (int i = 0; i < mItemRows.size(); i++) {
            ItemRow itemRow = mItemRows.get(i);
            itemRow.setIds(i + 1);

            View itemRowView = itemRow.getItemRowView();
            itemRowView.setTag(i);
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
                    getParseUsersInvolvedFromBoolean(Booleans.asList(itemUsersChecked));
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
        ItemRow lastItemRow = Iterables.getLast(mItemRows);
        lastItemRow.setPriceImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    final void fetchUsersAvailable() {
        LocalQuery.queryUsers(this);
    }

    @Override
    public void onUsersLocalQueried(List<ParseUser> users) {
        setupUserLists(users);
    }

    /**
     * sets up the class wide users lists
     *
     * @param users
     */
    @CallSuper
    void setupUserLists(List<ParseUser> users) {
        List<ParseUser> usersSorted = sortUserList(users);
        setupUsersAvailable(usersSorted);
        if (mPurchaseUsersInvolved.isEmpty()) {
            setupPurchaseUsersInvolved();
        }
    }

    private List<ParseUser> sortUserList(List<ParseUser> users) {
        users.remove(mCurrentUser);
        Collections.sort(users, new ComparatorParseUserIgnoreCase());
        users.add(0, mCurrentUser);

        return users;
    }

    private void setupUsersAvailable(List<ParseUser> users) {
        // list with user names and list with ParseUser objects
        mUsersAvailable.clear();
        mUsersAvailableParse.clear();
        for (ParseUser user : users) {
            mUsersAvailable.add(((User) user).getNicknameOrMe(getActivity()));
            mUsersAvailableParse.add(user);
        }
        mRecyclerAdapter.notifyDataSetChanged();

        mUsersAvailableArray = mUsersAvailable.toArray(
                new CharSequence[mUsersAvailable.size()]);
    }

    /**
     * Different implementations in Add- and EditFragment. In AddFragment the default purchase wide
     * usersInvolved will equal to the usersAvailable. in the EditFragment they will be set
     * according to the values in the original purchase.
     */
    protected abstract void setupPurchaseUsersInvolved();

    /**
     * Callback when a user avatar is clicked. Adds/deletes user to the purchase wide usersInvolved
     *
     * @param position
     */
    @Override
    public void onPurchaseUserClick(int position) {
        if (mPurchaseUsersInvolved.get(position)) {
            if (!userIsLastOneChecked()) {
                mPurchaseUsersInvolved.set(position, false);
                updateItemsUsersChecked(position, false);
                mRecyclerAdapter.notifyItemChanged(position);
            } else {
                selectMinOneUser();
            }
        } else {
            mPurchaseUsersInvolved.set(position, true);
            updateItemsUsersChecked(position, true);
            mRecyclerAdapter.notifyItemChanged(position);
        }

        updateTotalAndMyShareValues();
    }

    private void updateItemsUsersChecked(int purchaseUserPosition, boolean purchaseUserIsChecked) {
        for (ItemRow itemRow : mItemRows) {
            itemRow.updateUsersCheckedAfterPurchaseUserClick(purchaseUserPosition, purchaseUserIsChecked);
            itemRow.updateCheckedStatus(mUsersAvailableParse.indexOf(ParseUser.getCurrentUser()));
            itemRow.setCheckBoxColor(mPurchaseUsersInvolved);
        }
    }

    /**
     * The user needs to have at least one user selected.
     *
     * @return whether a clicked checked user is the only one checked
     */
    private boolean userIsLastOneChecked() {
        int countUsersChecked = Booleans.countTrue(Booleans.toArray(mPurchaseUsersInvolved));

        return countUsersChecked < 2;
    }

    private void selectMinOneUser() {
        MessageUtils.showBasicSnackbar(mButtonAddRow, getString(R.string.toast_min_one_user));
    }

    /**
     * Gets called from the activity when the dialog for setting the usersInvolved for an item is
     * closed.
     *
     * @param usersInvolvedInt the users selected in the dialog
     */
    public void setItemUsersInvolved(List<Integer> usersInvolvedInt) {
        List<ParseUser> usersInvolved = new ArrayList<>();
        for (Integer i : usersInvolvedInt) {
            usersInvolved.add(mUsersAvailableParse.get(i));
        }
        List<Boolean> usersChecked = new ArrayList<>();
        for (ParseUser parseUser : mUsersAvailableParse) {
            if (usersInvolved.contains(parseUser)) {
                usersChecked.add(true);
            } else {
                usersChecked.add(false);
            }
        }

        ItemRow itemRow = mItemRows.get(mSelectedItemPosition);
        itemRow.setUsersChecked(Booleans.toArray(usersChecked));
        itemRow.updateCheckedStatus(mUsersAvailableParse.indexOf(ParseUser.getCurrentUser()));

        updatePurchaseUsersInvolved();
        updateTotalAndMyShareValues();
    }

    /**
     * Checks if a user is unselected in ALL items' usersInvolved. If yes, unselect user from
     * purchase wide usersInvolved. If user is selected in at least one item, select him in
     * purchase wide usersInvolved as well.
     */
    private void updatePurchaseUsersInvolved() {
        boolean[] usersInvolved = new boolean[mUsersAvailableParse.size()];

        for (int i = 0, mUsersAvailableParseSize = mUsersAvailableParse.size();
             i < mUsersAvailableParseSize; i++) {
            usersInvolved[i] = false;

            for (ItemRow itemRow : mItemRows) {
                boolean[] usersChecked = itemRow.getUsersChecked();

                if (usersChecked[i]) {
                    usersInvolved[i] = true;
                }
            }
        }

        for (int i = 0, usersInvolvedLength = usersInvolved.length; i < usersInvolvedLength; i++) {
            if (usersInvolved[i]) {
                mPurchaseUsersInvolved.set(i, true);
            } else {
                mPurchaseUsersInvolved.set(i, false);
            }
            mRecyclerAdapter.notifyItemChanged(i);
        }

        updateCheckBoxesColor();
    }

    /**
     * Iterates trough all items and sets their checkbox' color appropriately
     */
    final void updateCheckBoxesColor() {
        for (ItemRow itemRow : mItemRows) {
            itemRow.setCheckBoxColor(mPurchaseUsersInvolved);
        }
    }

    /**
     * Sets the datePicker to the given date and update the class wide DateSelected variable.
     *
     * @param dateSelected
     */
    public void setDate(Date dateSelected) {
        mTextViewPickDate.setText(DateUtils.formatDateLong(dateSelected));
        mDateSelected = dateSelected;
    }

    /**
     * Sets the Store TextView to the selected date and updates the class wide StoreSelected
     * variable.
     *
     * @param storeSelected
     */
    public void setStore(String storeSelected, boolean manuallyEntered) {
        mTextViewPickStore.setText(storeSelected);
        mStoreSelected = storeSelected;

        if (!manuallyEntered || ParseUtils.isTestUser(mCurrentUser)) {
            return;
        }

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
     * Gets called from the activity when the "save" action item is clicked. Initiates the save of
     * the purchase.
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
                            ParseUtils.getNoConnectionException()));
                } else {
                    mIsSaving = true;
                    mListener.progressCircleShow();
                    setPurchase();
                }
            }
        }
    }

    /**
     * Reads values in editTexts and saves them in the Item objects.
     *
     * @return true if all required fields have a value, false if not
     */
    final boolean setItemValues(boolean acceptEmptyFields) {
        // Read values from editTexts, check if items are complete and enabled. If they are, add
        // them to totalPrice. If there are no items, immediately return false.
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
                List<ParseUser> usersInvolved = getParseUsersInvolvedFromBoolean(Booleans.asList(usersChecked));

                // create new Item object and add to list
                Item item = new Item(itemRow.getName(), itemRow.getPrice(), usersInvolved);
                mItems.add(item);
            }

            return true;
        }

        return false;
    }

    /**
     * AddFragment creates a new purchase and EditFragment updates the original one
     */
    protected abstract void setPurchase();

    @CallSuper
    public void onParseError(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        showErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), e));

        mIsSaving = false;
        mListener.progressCircleHide();
    }

    void showErrorSnackbar(String message) {
        MessageUtils.showBasicSnackbar(mButtonAddRow, message);
    }

    /**
     * Called from activity when helper saved and pinned new purchase
     */
    public void onPurchaseSaveAndPinSucceeded() {
        mIsSaving = false;
        mListener.setResultForSnackbar(getPurchaseSavedAction());
        mListener.progressCircleStartFinal();
    }

    int getPurchaseSavedAction() {
        return PurchaseBaseActivity.PURCHASE_SAVED;
    }

    /**
     * Called from activity when helper fails to save purchase
     */
    public void onPurchaseSaveFailed(ParseException e) {
        onParseError(e);
        removeSaveHelper();
    }

    private void removeSaveHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        PurchaseSaveHelper purchaseSaveHelper = findPurchaseSaveHelper(fragmentManager);

        if (purchaseSaveHelper != null) {
            fragmentManager.beginTransaction().remove(purchaseSaveHelper).commitAllowingStateLoss();
        }
    }

    final PurchaseSaveHelper findPurchaseSaveHelper(FragmentManager fragmentManager) {
        return (PurchaseSaveHelper)
                fragmentManager.findFragmentByTag(PURCHASE_SAVE_HELPER);
    }

    /**
     * Save purchase as local draft in AddFragment. Save changes to draft in EditDraftFragment.
     * Do nothing in EditFragment (option so save as draft n/a)
     */
    protected abstract void savePurchaseAsDraft();

    /**
     * Sets new random draft id if not already set and swaps parsefile to bytearray.
     */
    final void pinPurchaseAsDraft() {
        mPurchase.setRandomDraftId();
        mPurchase.swapReceiptParseFileToData();
        mPurchase.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    onParseError(e);
                    return;
                }

                onPinAsDraftSucceeded();
            }
        });
    }

    private void onPinAsDraftSucceeded() {
        mListener.setResultForSnackbar(PurchaseBaseActivity.PURCHASE_SAVED_AS_DRAFT);
        mListener.finishPurchase();
    }

    /**
     * Gets the ParseUsers for a given boolean array. Needed because the usersInvolved (both
     * purchase wide and for each item) are stored in boolean arrays.
     *
     * @param usersInvolvedBoolean
     * @return List with ParseUser objects
     */
    final List<ParseUser> getParseUsersInvolvedFromBoolean(List<Boolean> usersInvolvedBoolean) {
        final List<ParseUser> usersInvolved = new ArrayList<>();
        for (int i = 0, mUsersAvailableParseSize = mUsersAvailableParse.size();
             i < mUsersAvailableParseSize; i++) {
            ParseUser parseUser = mUsersAvailableParse.get(i);
            if (usersInvolvedBoolean.get(i)) {
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

    public interface FragmentInteractionListener {
        void showManualExchangeRateSelectorDialog(String exchangeRate);

        void showDatePickerDialog();

        void showUserPickerDialog(CharSequence[] usersAvailable, boolean[] usersChecked);

        void showStorePickerDialog(String defaultStore);

        void showAccountCreateDialog();

        ParseFile getReceiptParseFile();

        void setReceiptParseFile(ParseFile receiptParseFile);

        void setResultForSnackbar(@PurchaseBaseActivity.PurchaseAction int purchaseAction);

        void finishPurchase();

        void showFab(boolean isSaving);

        void showReceiptFragment();

        void captureImage();

        void progressCircleShow();

        void progressCircleStartFinal();

        void progressCircleHide();
    }

}
