package ch.giantific.qwittig.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
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
import android.widget.CheckBox;
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

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.ItemUsersChecked;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Config;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.Item;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.adapter.PurchaseAddUsersInvolvedRecyclerAdapter;
import ch.giantific.qwittig.ui.widgets.SwipeDismissTouchListener;
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

    private static final String STATE_ROW_COUNT = "row_count";
    private static final String STATE_STORE_SELECTED = "state_store_selected";
    private static final String STATE_DATE_SELECTED = "state_date_selected";
    private static final String STATE_PURCHASE_USERS_INVOLVED = "state_purchase_users_involved";
    private static final String STATE_ITEMS_USERS_INVOLVED = "state_items_users_involved";
    private static final String STATE_CURRENCY_SELECTED = "state_currency_selected";
    private static final String STATE_IS_SAVING = "state_is_saving";

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
    ArrayList<ItemUsersChecked> mItemsUsersChecked;
    Group mCurrentGroup;
    String mCurrencySelected;
    String mCurrentGroupCurrency;
    User mCurrentUser;
    boolean mIsSaving;
    Button mButtonAddRow;
    private View mViewDate;
    private View mViewStore;
    private List<View> mItemRows = new ArrayList<>();
    private TextView mTextViewPickDate;
    private Spinner mSpinnerCurrency;
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

        if (savedInstanceState != null) {
            mItemRowCount = savedInstanceState.getInt(STATE_ROW_COUNT);
            mDateSelected = DateUtils.parseLongToDate(savedInstanceState.getLong(STATE_DATE_SELECTED));
            mPurchaseUsersInvolved = Booleans.asList(savedInstanceState
                    .getBooleanArray(STATE_PURCHASE_USERS_INVOLVED));
            mItemsUsersChecked = savedInstanceState
                    .getParcelableArrayList(STATE_ITEMS_USERS_INVOLVED);
            mCurrencySelected = savedInstanceState.getString(STATE_CURRENCY_SELECTED);
            mStoreSelected = savedInstanceState.getString(STATE_STORE_SELECTED);
            mIsSaving = savedInstanceState.getBoolean(STATE_IS_SAVING);
        } else {
            mItemRowCount = 1;
            mDateSelected = new Date();
            mPurchaseUsersInvolved = new ArrayList<>();
            mItemsUsersChecked = new ArrayList<>();
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
        outState.putParcelableArrayList(STATE_ITEMS_USERS_INVOLVED, mItemsUsersChecked);
        outState.putString(STATE_CURRENCY_SELECTED, mCurrencySelected);
        outState.putBoolean(STATE_IS_SAVING, mIsSaving);
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
                updateUsersInvolved(true);
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

        if (savedInstanceState != null) {
            revealFab();
        }

        setupRows();
    }

    void revealFab() {
        mListener.showFab(true);
    }

    /**
     * Sets the currency spinner to the position of the specified currency code.
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
        for (ParseObject parseObject : mItems) {
            Item item = (Item) parseObject;
            item.formatPrice(currencyCode);
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

    @CallSuper
    void setupRows() {
        mItems.clear();
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
    final ParseObject addNewItemRow(int idCounter) {
        View itemRow = getActivity().getLayoutInflater()
                .inflate(R.layout.row_add_purchase, mLayoutTotalItemRow, false);
        itemRow.setTag(idCounter - 1); // tag will be used in the ClickListener to get the position of the row, -1 because List index starts at 0
        mItemRows.add(itemRow);

        TextInputLayout tilItemName = (TextInputLayout) itemRow.findViewById(R.id.til_item_name);
        tilItemName.requestFocus(); // somehow needed on Android 5.0+, otherwise etItemPrice gets focused

        TextInputLayout tilItemPrice = (TextInputLayout) itemRow.findViewById(R.id.til_item_price);
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

        CheckBox cbEnabled = (CheckBox) itemRow.findViewById(R.id.cb_item_enabled);
        cbEnabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedItemPosition = Utils.getViewPositionFromTag(v);

                updateUsersInvolved(((CheckBox) v).isChecked());
                setCheckBoxColor(mSelectedItemPosition);
                updateTotalAndMyShareValues();
            }
        });
        cbEnabled.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mSelectedItemPosition = Utils.getViewPositionFromTag(v);

                ItemUsersChecked itemUsersChecked = mItemsUsersChecked.get(mSelectedItemPosition);
                boolean[] usersChecked = itemUsersChecked.getUsersChecked();
                mListener.showUserPickerDialog(mUsersAvailableArray, usersChecked);

                return true;
            }
        });

        // set a default value for the usersInvolved, this would fail for the first row in AddFragment
        // because the user lists are not yet setup. Hence we call another method in AddFragment to set
        // a default value for the first row when the user lists are ready. On recreation,
        // mItemsUsersChecked will already be filled with values, hence no new values will be added
        // (size() will be bigger than idCounter)
        if (mItemsUsersChecked.size() < idCounter && !mPurchaseUsersInvolved.isEmpty()) {
            mItemsUsersChecked.add(new ItemUsersChecked(Booleans.toArray(mPurchaseUsersInvolved)));
        }

        final Item item = new Item(getActivity(), idCounter, tilItemName, tilItemPrice, cbEnabled);
        mItems.add(item);

        itemRow.setOnClickListener(null); // SwipeDismissTouchListener doesn't work without an OnClickListener
        itemRow.setOnTouchListener(new SwipeDismissTouchListener(itemRow, null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        int position = Utils.getViewPositionFromTag(view);

                        mLayoutTotalItemRow.removeView(view);
                        mItems.remove(position);
                        mItemRowCount--;
                        mItemRows.remove(position);
                        mItemsUsersChecked.remove(position);
                        resetIdsAndTags();
                        updatePurchaseUsersInvolved();
                        updateTotalAndMyShareValues();
                    }
                }));

        mLayoutTotalItemRow.addView(itemRow);

        return item;
    }

    /**
     * Updates the usersInvolved for the item where the checkbox was clicked.
     *
     * @param isChecked whether the checkbox is checked or not
     */
    private void updateUsersInvolved(boolean isChecked) {
        ItemUsersChecked itemUsersChecked = mItemsUsersChecked.get(mSelectedItemPosition);
        int buyerPosition = mUsersAvailableParse.indexOf(ParseUser.getCurrentUser());

        if (isChecked) {
            boolean onlyBuyerIsChecked = true;
            for (int i = 0, mPurchaseUsersInvolvedSize = mPurchaseUsersInvolved.size(); i < mPurchaseUsersInvolvedSize; i++) {
                if (mPurchaseUsersInvolved.get(i)) {
                    if (i != buyerPosition) {
                        onlyBuyerIsChecked = false;
                    }

                }
            }
            if (!onlyBuyerIsChecked) {
                boolean[] usersChecked = Booleans.toArray(mPurchaseUsersInvolved);
                itemUsersChecked.setUsersChecked(usersChecked);
            } else {
                itemUsersChecked.checkAll();
            }
        } else {
            itemUsersChecked.checkAllExceptBuyer(buyerPosition);
        }

        updatePurchaseUsersInvolved();
    }

    /**
     * Checks for a checked checkbox whether the usersChecked for an item are equal to the purchase
     * wide usersInvolved. If yes, set color to normal, if no set it to special.
     * If checkbox is unchecked, set it to normal in any case.
     *
     * @param position the position of the item with the checkbox
     */
    private void setCheckBoxColor(int position) {
        Item item = (Item) mItems.get(position);
        boolean isChecked = item.isCheckBoxChecked();

        ItemUsersChecked itemUsersChecked = mItemsUsersChecked.get(position);
        List<Boolean> usersChecked = Booleans.asList(itemUsersChecked.getUsersChecked());

        if (isChecked) {
            if (!usersChecked.equals(mPurchaseUsersInvolved)) {
                item.setCheckBoxColor(true);
            } else {
                item.setCheckBoxColor(false);
            }
        } else {
            item.setCheckBoxColor(false);
        }
    }

    /**
     * Renumbers ids and tags after a row was dismissed. Needed for proper recreation and correct
     * determination of itemRow position
     */
    private void resetIdsAndTags() {
        for (int i = 0; i < mItems.size(); i++) {
            Item item = (Item) mItems.get(i);
            item.setIds(i + 1);

            View itemRow = mItemRows.get(i);
            itemRow.setTag(i);
        }
    }

    private void updateTotalAndMyShareValues() {
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal myShare = BigDecimal.ZERO;

        for (int i = 0, mItemsSize = mItems.size(); i < mItemsSize; i++) {
            ParseObject parseObject = mItems.get(i);
            Item item = (Item) parseObject;
            BigDecimal finalPrice = item.getEditTextPrice(mCurrencySelected);

            // update total price
            totalPrice = totalPrice.add(finalPrice);

            // update my share
            boolean[] itemUsersChecked = mItemsUsersChecked.get(i).getUsersChecked();
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
        for (ParseObject parseObject : mItems) {
            Item item = (Item) parseObject;
            item.setPriceImeOptions(EditorInfo.IME_ACTION_NEXT);
        }
        Item lastItem = (Item) Iterables.getLast(mItems);
        lastItem.setPriceImeOptions(EditorInfo.IME_ACTION_DONE);
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
        MessageUtils.showToast(getActivity(), getString(R.string.toast_min_one_user));
    }

    /**
     * Checks for each item if the clicked user is selected/unselected and changes it accordingly
     *
     * @param position
     * @param userIsEnabled
     */
    private void updateItemsUsersChecked(int position, boolean userIsEnabled) {
        for (int i = 0, mItemsUsersCheckedSize = mItemsUsersChecked.size(); i < mItemsUsersCheckedSize; i++) {
            ItemUsersChecked itemUsersChecked = mItemsUsersChecked.get(i);
            boolean[] usersChecked = itemUsersChecked.getUsersChecked();

            if (userIsEnabled) {
                if (!usersChecked[position]) {
                    usersChecked[position] = true;
                }
            } else {
                if (usersChecked[position]) {
                    usersChecked[position] = false;
                }
            }
            itemUsersChecked.setUsersChecked(usersChecked);

            updateCheckedStatus(i);
            setCheckBoxColor(i);
        }
    }

    /**
     * Updates the checked status of a checkbox, depending on how many users are enabled
     *
     * @param position index of the checkbox
     */
    private void updateCheckedStatus(int position) {
        Item item = (Item) mItems.get(position);
        ItemUsersChecked itemUsersChecked = mItemsUsersChecked.get(position);
        boolean[] usersChecked = itemUsersChecked.getUsersChecked();
        int buyerPosition = mUsersAvailableParse.indexOf(ParseUser.getCurrentUser());

        boolean onlyBuyerIsChecked = true;
        for (int i = 0, usersCheckedLength = usersChecked.length; i < usersCheckedLength; i++) {
            if (usersChecked[i]) {
                if (i != buyerPosition) {
                    onlyBuyerIsChecked = false;
                }
            }
        }
        if (onlyBuyerIsChecked) {
            item.setCheckBoxChecked(false);
        } else {
            item.setCheckBoxChecked(true);
        }
    }

    /**
     * Gets called from the activity when the dialog for setting the usersInvolved for an item is
     * closed. Sets the new values in the class wide mItemsUsersChecked
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
        ItemUsersChecked itemUsersChecked = mItemsUsersChecked.get(mSelectedItemPosition);
        itemUsersChecked.setUsersChecked(Booleans.toArray(usersChecked));

        updateCheckedStatus(mSelectedItemPosition);
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

            for (ItemUsersChecked itemUsersChecked : mItemsUsersChecked) {
                boolean[] usersCheckedArray = itemUsersChecked.getUsersChecked();

                if (usersCheckedArray[i]) {
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
        for (int i = 0, mItemsSize = mItems.size(); i < mItemsSize; i++) {
            setCheckBoxColor(i);
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
        if (mItems.size() < 1) {
            MessageUtils.showToast(getActivity(), getString(R.string.toast_min_one_item));
            return false;
        }

        mTotalPrice = 0;
        boolean itemsAreComplete = true;

        for (int i = 0, mItemsSize = mItems.size(); i < mItemsSize; i++) {
            ParseObject itemParse = mItems.get(i);
            Item item = (Item) itemParse;
            boolean itemIsComplete = item.setValuesFromEditTexts(acceptEmptyFields, mCurrencySelected);

            if (itemIsComplete) {
                // set usersInvolved for the item
                ItemUsersChecked itemUsersChecked = mItemsUsersChecked.get(i);
                boolean[] usersChecked = itemUsersChecked.getUsersChecked();
                List<ParseUser> usersInvolved = getParseUsersInvolvedFromBoolean(Booleans.asList(usersChecked));
                item.setUsersInvolved(usersInvolved);

                // add item price to purchase totalPrice
                mTotalPrice += item.getPrice();
            } else {
                // If the item is not complete, we don't want the purchase to be added yet.
                itemsAreComplete = false;
            }
        }

        return itemsAreComplete;
    }

    /**
     * AddFragment creates a new purchase and EditFragment updates the original one
     */
    protected abstract void setPurchase();

    /**
     * Saves purchase in Parse database.
     */
    final void savePurchaseInParse() {
        convertPrices(true);

        mPurchase.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    convertPrices(false);
                    onParseError(e);
                    return;
                }

                onSaveSucceeded();
            }
        });
    }

    private void convertPrices(boolean toGroupCurrency) {
        double exchangeRate = mPurchase.getExchangeRate();
        if (exchangeRate == 1) {
            return;
        }

        List<ParseObject> items = mPurchase.getItems();
        for (ParseObject parseObject : items) {
            Item item = (Item) parseObject;
            item.convertPrice(exchangeRate, toGroupCurrency);
        }

        mPurchase.convertTotalPrice(toGroupCurrency);
    }

    @CallSuper
    void onParseError(ParseException e) {
        mIsSaving = false;
        mListener.progressCircleHide();
        ParseErrorHandler.handleParseError(getActivity(), e);
        showErrorSnackbar(ParseErrorHandler.getErrorMessage(getActivity(), e));
    }

    void showErrorSnackbar(String message) {
        MessageUtils.showBasicSnackbar(mButtonAddRow, message);
    }

    /**
     * Pin purchase in AddFragment, finish in EditFragment and unpin/repin in EditDraftFragment
     */
    protected abstract void onSaveSucceeded();

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
        pinPurchase(true);
    }

    /**
     * Pins purchase to local datastore.
     */
    final void pinPurchase(final boolean asDraft) {
        if (asDraft) {
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
        } else {
            mPurchase.pinInBackground(Purchase.PIN_LABEL + mCurrentGroup.getObjectId(), new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        onParseError(e);
                        return;
                    }

                    onPinSucceeded();
                }
            });
        }
    }

    void onPinSucceeded() {
        mIsSaving = false;
        mListener.setResultForSnackbar(PurchaseBaseActivity.PURCHASE_SAVED);
        mListener.progressCircleStartFinal();
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
        void showDatePickerDialog();

        void showUserPickerDialog(CharSequence[] usersAvailable, boolean[] usersChecked);

        void showStorePickerDialog(String defaultStore);

        void showAccountCreateDialog();

        ParseFile getReceiptParseFile();

        void setReceiptParseFile(ParseFile receiptParseFile);

        void setResultForSnackbar(@PurchaseBaseActivity.PurchaseAction int purchaseAction);

        void finishPurchase();

        void showFab(boolean needsLayoutListener);

        void showReceiptFragment();

        void captureImage();

        void progressCircleShow();

        void progressCircleStartFinal();

        void progressCircleHide();
    }

}
