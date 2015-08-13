package ch.giantific.qwittig.data.models;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import java.math.BigDecimal;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.ui.widgets.ListCheckBox;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Created by fabio on 12.10.14.
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

    public View getItemRowView() {
        return mItemRowView;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public BigDecimal getPrice() {
        return mPrice;
    }

    public void setPrice(BigDecimal price) {
        mPrice = price;
    }

    public ItemRow(Context context, View itemRowView, int id, TextInputLayout tilName,
                   TextInputLayout tilPrice, ListCheckBox cbEnabled) {
        mContext = context;
        mItemRowView = itemRowView;
        mTextInputLayoutName = tilName;
        mEditTextName = mTextInputLayoutName.getEditText();
        mTextInputLayoutPrice = tilPrice;
        mEditTextPrice = mTextInputLayoutPrice.getEditText();
        mCheckBoxEnabled = cbEnabled;
        setIds(id);
    }

    public void setIds(int id) {
        mEditTextName.setId(id);
        mEditTextPrice.setId(id + 1000);
        mCheckBoxEnabled.setId(id + 2000);
        mCheckBoxEnabled.setTag(id - 1);
    }


    public String getEditTextName() {
        return mEditTextName.getText().toString().trim();
    }

    public void setEditTextName(String name) {
        mEditTextName.setText(name);
    }

    public BigDecimal getEditTextPrice(String currencyCode) {
        BigDecimal price = BigDecimal.ZERO;
        String priceString = mEditTextPrice.getText().toString();

        if (!TextUtils.isEmpty(priceString)) {
            int maxFractionDigits = MoneyUtils.getMaximumFractionDigits(currencyCode);
            price = MoneyUtils.parsePrice(priceString).setScale(maxFractionDigits,
                    BigDecimal.ROUND_HALF_UP);
        }

        return price;
    }

    public void setEditTextPrice(String price) {
        mEditTextPrice.setText(price);
    }

    public void setCheckBoxChecked(boolean isEnabled) {
        mCheckBoxEnabled.setChecked(isEnabled);
    }

    public boolean[] getUsersChecked() {
        return mCheckBoxEnabled.getUsersChecked();
    }

    public void setUsersChecked(boolean[] usersChecked) {
        mCheckBoxEnabled.setUsersChecked(usersChecked);
    }

    public void updateUsersCheckedAfterPurchaseUserClick(int purchaseUserPosition,
                                                         boolean purchaseUserIsChecked) {
        mCheckBoxEnabled.updateUsersCheckedAfterPurchaseUserClick(purchaseUserPosition, purchaseUserIsChecked);
    }

    public void updateCheckedStatus(int buyerPosition) {
        mCheckBoxEnabled.updateCheckedStatus(buyerPosition);
    }

    public void setCheckBoxColor(List<Boolean> purchaseUsersInvolved) {
        mCheckBoxEnabled.setCheckBoxColor(purchaseUsersInvolved);
    }

    public void setPriceImeOptions(int imeOption) {
        mEditTextPrice.setImeOptions(imeOption);
    }

    public void formatPrice(String currencyCode) {
        String price = mEditTextPrice.getText().toString();
        if (!TextUtils.isEmpty(price)) {
            mEditTextPrice.setText(MoneyUtils.formatPrice(price, currencyCode));
        }
    }

    public boolean setValuesFromEditTexts(boolean acceptEmptyFields, String currencySelected) {
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

    public void requestFocusForName() {
        mEditTextName.requestFocus();
    }
}
