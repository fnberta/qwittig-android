/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.models;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.parse.ParseUser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.ui.widgets.ListCheckBox;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Represents a row of a purchase item. It includes the view containing the values for the item.
 */
public class ItemRow {

    private Context mContext;
    private View mItemRowView;
    private TextInputLayout mTextInputLayoutName;
    private EditText mEditTextName;
    private TextInputLayout mTextInputLayoutPrice;
    private EditText mEditTextPrice;
    private ListCheckBox mCheckBoxEnabled;
    private String mName;
    private BigDecimal mPrice;

    public ItemRow(@NonNull Context context, @NonNull View itemRowView, int id,
                   @NonNull TextInputLayout tilName, @NonNull TextInputLayout tilPrice,
                   @NonNull ListCheckBox cbEnabled) {
        mContext = context.getApplicationContext();
        mItemRowView = itemRowView;
        mTextInputLayoutName = tilName;
        mEditTextName = mTextInputLayoutName.getEditText();
        mTextInputLayoutPrice = tilPrice;
        mEditTextPrice = mTextInputLayoutPrice.getEditText();
        mCheckBoxEnabled = cbEnabled;
        setIds(id);
    }

    public String getName() {
        return mName;
    }

    public void setName(@NonNull String name) {
        mName = name;
    }

    public BigDecimal getPrice() {
        return mPrice;
    }

    public void setPrice(@NonNull BigDecimal price) {
        mPrice = price;
    }

    /**
     * Sets the ides of the views that contain user input.
     *
     * @param id the base id to use
     */
    public void setIds(int id) {
        mEditTextName.setId(id);
        mEditTextPrice.setId(id + 1000);
        mCheckBoxEnabled.setId(id + 2000);
        mCheckBoxEnabled.setTag(id - 1);
    }

    /**
     * Sets the view tag of the item row view.
     *
     * @param tag the tag to set
     */
    public void setViewTag(int tag) {
        mItemRowView.setTag(tag);
    }

    /**
     * Returns a string of the user input data of the name EditText.
     *
     * @return a string with the user input name
     */
    @NonNull
    public String getEditTextName() {
        return mEditTextName.getText().toString().trim();
    }


    /**
     * Returns a {@link BigDecimal} of the user input data of the price EditText.
     *
     * @param currencyCode the currency code to use to round the value
     * @return a {@link BigDecimal} of the user input price
     */
    public BigDecimal getEditTextPrice(@NonNull String currencyCode) {
        BigDecimal price = BigDecimal.ZERO;
        String priceString = mEditTextPrice.getText().toString();

        if (!TextUtils.isEmpty(priceString)) {
            int maxFractionDigits = MoneyUtils.getMaximumFractionDigits(currencyCode);
            price = MoneyUtils.parsePrice(priceString).setScale(maxFractionDigits,
                    BigDecimal.ROUND_HALF_UP);
        }

        return price;
    }

    public void setCheckBoxChecked(boolean isEnabled) {
        mCheckBoxEnabled.setChecked(isEnabled);
    }

    public boolean[] getUsersChecked() {
        return mCheckBoxEnabled.getUsersChecked();
    }

    public void setUsersChecked(@NonNull boolean[] usersChecked) {
        mCheckBoxEnabled.setUsersChecked(usersChecked);
    }

    public void updateUsersCheckedAfterPurchaseUserClick(int purchaseUserPosition,
                                                         boolean purchaseUserIsChecked) {
        mCheckBoxEnabled.updateUsersCheckedAfterPurchaseUserClick(purchaseUserPosition, purchaseUserIsChecked);
    }

    public void updateCheckedStatus(int buyerPosition) {
        mCheckBoxEnabled.updateCheckedStatus(buyerPosition);
    }

    public void setCheckBoxColor(@NonNull boolean[] purchaseUsersInvolved) {
        mCheckBoxEnabled.setCheckBoxColor(purchaseUsersInvolved);
    }

    public void setPriceImeOptions(int imeOption) {
        mEditTextPrice.setImeOptions(imeOption);
    }

    /**
     * Sets the name and price of the item row.
     *
     * @param name  the name to set
     * @param price the price to set
     */
    public void fillValues(String name, String price) {
        setFloatingAnimationEnabled(false);
        mEditTextName.setText(name);
        mEditTextPrice.setText(price);
    }

    private void setFloatingAnimationEnabled(boolean enable) {
        mTextInputLayoutName.setHintAnimationEnabled(enable);
        mTextInputLayoutPrice.setHintAnimationEnabled(enable);
    }

    /**
     * Formats the value of the price EditText with currency style
     *
     * @param currencyCode the currency code to use for the formatting
     */
    public void formatPrice(@NonNull String currencyCode) {
        String price = mEditTextPrice.getText().toString();
        if (!TextUtils.isEmpty(price)) {
            mEditTextPrice.setText(MoneyUtils.formatPrice(price, currencyCode));
        }
    }

    /**
     * Sets the values from the EditTexts to the global fields and returns <code>true</code> if
     * they are not empty and <code>false</code> if the are.
     *
     * @param acceptEmptyFields whether to accept empty fields or not
     * @param currencySelected  the currency code of the selected currency
     * @return whether the name and price are both non empty or not
     */
    public boolean setValuesFromEditTexts(boolean acceptEmptyFields, @NonNull String currencySelected) {
        boolean isComplete = true;

        String name = getEditTextName();
        String priceString = mEditTextPrice.getText().toString();

        // check if name is empty
        if (!acceptEmptyFields && TextUtils.isEmpty(name)) {
            mTextInputLayoutName.setError(mContext.getString(R.string.error_item_name));
            isComplete = false;
        } else {
            mTextInputLayoutName.setErrorEnabled(false);
        }

        // check if price is empty
        BigDecimal price = BigDecimal.ZERO;
        if (!acceptEmptyFields && TextUtils.isEmpty(priceString)) {
            mTextInputLayoutPrice.setError(mContext.getString(R.string.error_item_price));
            isComplete = false;
        } else {
            mTextInputLayoutPrice.setErrorEnabled(false);
            int maxFractionDigits = MoneyUtils.getMaximumFractionDigits(currencySelected);
            price = MoneyUtils.parsePrice(priceString)
                    .setScale(maxFractionDigits, BigDecimal.ROUND_HALF_UP);
        }

        // if name and price fields are not empty, send data to parse.com
        if (isComplete) {
            setName(name);
            setPrice(price);
        }

        return isComplete;
    }

    /**
     * Requests the focus for the name edit text.
     */
    public void requestFocusForName() {
        mEditTextName.requestFocus();
    }

    /**
     * Returns the object ids of the users checked in this row
     *
     * @param usersAvailable the parse users available
     * @return the object ids of the users checked in this row
     */
    @NonNull
    public List<String> getParseUsersInvolvedIds(@NonNull List<ParseUser> usersAvailable) {
        final List<String> usersInvolved = new ArrayList<>();
        boolean[] usersChecked = getUsersChecked();
        for (int i = 0, usersSize = usersAvailable.size(); i < usersSize; i++) {
            User user = (User) usersAvailable.get(i);
            if (usersChecked[i]) {
                usersInvolved.add(user.getObjectId());
            }
        }
        return usersInvolved;
    }
}
