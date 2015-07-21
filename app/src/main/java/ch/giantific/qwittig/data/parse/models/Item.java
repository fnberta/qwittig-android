package ch.giantific.qwittig.data.parse.models;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Created by fabio on 12.10.14.
 */
@ParseClassName("Item")
public class Item extends ParseObject {

    public static final String CLASS = "Item";
    public static final String NAME = "name";
    public static final String PRICE = "price";
    public static final String USERS_INVOLVED = "usersInvolved";

    private Context mContext;
    private TextInputLayout mTextInputLayoutName;
    private EditText mEditTextName;
    private TextInputLayout mTextInputLayoutPrice;
    private EditText mEditTextPrice;
    private CheckBox mCheckBoxEnabled;

    public String getName() {
        return getString(NAME);
    }

    public void setName(String name) {
        put(NAME, name);
    }

    public double getPrice() {
        return getDouble(PRICE);
    }

    public void setPrice(BigDecimal finalPrice) {
        put(PRICE, finalPrice);
    }

    public List<ParseUser> getUsersInvolved() {
        return getList(USERS_INVOLVED);
    }

    public void setUsersInvolved(List<ParseUser> usersInvolved) {
        put(USERS_INVOLVED, usersInvolved);
    }

    public Item() {
        // A default constructor is required.
    }

    public Item(Context context, int id, TextInputLayout tilName, TextInputLayout tilPrice,
                CheckBox cbEnabled) {
        mContext = context;
        mTextInputLayoutName = tilName;
        mEditTextName = mTextInputLayoutName.getEditText();
        mTextInputLayoutPrice = tilPrice;
        mEditTextPrice = mTextInputLayoutPrice.getEditText();
        mCheckBoxEnabled = cbEnabled;
        setIds(id);
        setAccessRights(getCurrentGroup());
    }

    public void setIds(int id) {
        mEditTextName.setId(id);
        mEditTextPrice.setId(id + 1000);
        mCheckBoxEnabled.setId(id + 2000);
        mCheckBoxEnabled.setTag(id - 1);
    }

    private void setAccessRights(ParseObject group) {
        ParseACL acl = ParseUtils.getDefaultAcl(group);
        setACL(acl);
    }

    private ParseObject getCurrentGroup() {
        User currentUser = (User) ParseUser.getCurrentUser();
        return currentUser.getCurrentGroup();
    }

    public List<String> getUsersInvolvedIds() {
        List<String> listIds = new ArrayList<>();
        List<ParseUser> list = getUsersInvolved();
        for (ParseUser user : list) {
            listIds.add(user.getObjectId());
        }
        return listIds;
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

    public boolean isCheckBoxChecked() {
        return mCheckBoxEnabled.isChecked();
    }

    public void setCheckBoxChecked(boolean isEnabled) {
        mCheckBoxEnabled.setChecked(isEnabled);
    }

    public void setCheckBoxColor(boolean isSpecial) {
        if (isSpecial) {
            if (Utils.isRunningLollipopAndHigher()) {
                setCheckBoxTintList(mContext.getResources()
                        .getColorStateList(R.color.checkbox_color_red));
            }
        } else {
            if (Utils.isRunningLollipopAndHigher()) {
                setCheckBoxTintList(mContext.getResources()
                        .getColorStateList(R.color.checkbox_color_accent));
            }
        }
        // TODO: implement for android versions <5.0
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setCheckBoxTintList(ColorStateList colorStateList) {
        mCheckBoxEnabled.setButtonTintList(colorStateList);
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
        }

        // check if price is empty
        BigDecimal price = BigDecimal.ZERO;
        if (!acceptEmptyFields && TextUtils.isEmpty(priceString)) {
            mTextInputLayoutPrice.setError(mContext.getString(R.string.error_item_price));
            isComplete = false;
        } else {
            price = MoneyUtils.parsePrice(priceString);
            int maxFractionDigits = MoneyUtils.getMaximumFractionDigits(currencySelected);
            price = price.setScale(maxFractionDigits, BigDecimal.ROUND_HALF_UP);
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
