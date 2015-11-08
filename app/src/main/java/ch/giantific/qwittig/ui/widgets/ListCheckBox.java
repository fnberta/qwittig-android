/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.CheckBox;

import java.util.Arrays;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.utils.Utils;

/**
 * Provides a check box that allows to save a list of users involved and changes its checked status
 * and color according to whether this list differs from a purchases' list of users involved.
 * <p/>
 * Subclass of {@link CheckBox}.
 */
public class ListCheckBox extends CheckBox {

    private static final String STATE_SUPER = "STATE_SUPER";
    private static final String STATE_USERS_CHECKED = "STATE_USERS_CHECKED";
    private static final String STATE_IS_SPECIAL = "STATE_IS_SPECIAL";
    @Nullable
    private boolean[] mUsersChecked;
    private boolean mIsSpecial;

    public ListCheckBox(Context context) {
        super(context);
    }

    public ListCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Nullable
    public boolean[] getUsersChecked() {
        return mUsersChecked;
    }

    public void setUsersChecked(@Nullable boolean[] usersChecked) {
        mUsersChecked = usersChecked;
    }

    /**
     * Updates the usersInvolved for the item where the checkbox was clicked.
     *
     * @param buyerPosition         the position of the buyer
     * @param purchaseUsersInvolved list with purchase wide usersInvolved
     */
    public void updateUsersCheckedAfterCheckedChange(int buyerPosition,
                                                     @NonNull boolean[] purchaseUsersInvolved) {
        if (isChecked()) {
            boolean onlyBuyerIsChecked = true;
            int purchaseUsersInvolvedSize = purchaseUsersInvolved.length;
            for (int i = 0; i < purchaseUsersInvolvedSize; i++) {
                if (purchaseUsersInvolved[i] && i != buyerPosition) {
                    onlyBuyerIsChecked = false;
                }
            }

            if (onlyBuyerIsChecked) {
                checkAllUsers(purchaseUsersInvolvedSize);
            } else {
                mUsersChecked = Arrays.copyOf(purchaseUsersInvolved, purchaseUsersInvolvedSize);
            }
        } else {
            unCheckAllUsersExceptBuyer(buyerPosition);
        }
    }


    /**
     * Marks all users as checked
     */
    private void checkAllUsers(int numberOfUsers) {
        if (mUsersChecked == null) {
            mUsersChecked = new boolean[numberOfUsers];
        }

        for (int i = 0, mUsersCheckedLength = mUsersChecked.length; i < mUsersCheckedLength; i++) {
            mUsersChecked[i] = true;
        }
    }

    /**
     * Un-checks all users except the buyer of the purchase
     *
     * @param buyerPosition the position of the buyer
     */
    private void unCheckAllUsersExceptBuyer(int buyerPosition) {
        for (int i = 0, mUsersCheckedLength = mUsersChecked != null ? mUsersChecked.length : 0;
             i < mUsersCheckedLength; i++) {
            mUsersChecked[i] = i == buyerPosition;
        }
    }

    /**
     * Checks if the given user is selected/unselected and changes it accordingly
     *
     * @param purchaseUserPosition  the position of the user that was clicked in the purchase wide
     *                              selection
     * @param purchaseUserIsChecked whether the user is now checked or unchecked
     */
    public void updateUsersCheckedAfterPurchaseUserClick(int purchaseUserPosition,
                                                         boolean purchaseUserIsChecked) {
        if (mUsersChecked == null) {
            return;
        }

        if (purchaseUserIsChecked) {
            if (!mUsersChecked[purchaseUserPosition]) {
                mUsersChecked[purchaseUserPosition] = true;
            }
        } else {
            if (mUsersChecked[purchaseUserPosition]) {
                mUsersChecked[purchaseUserPosition] = false;
            }
        }
    }

    /**
     * Updates the checked status, depending on how many users are enabled. Sets to unchecked if
     * only the buyer is enabled.
     *
     * @param buyerPosition position of the buyer
     */
    public void updateCheckedStatus(int buyerPosition) {
        if (mUsersChecked == null) {
            return;
        }

        boolean onlyBuyerIsCheckedInPurchase = true;
        for (int i = 0, usersCheckedLength = mUsersChecked.length; i < usersCheckedLength; i++) {
            if (mUsersChecked[i]) {
                if (i != buyerPosition) {
                    onlyBuyerIsCheckedInPurchase = false;
                }
            }
        }
        if (onlyBuyerIsCheckedInPurchase) {
            setChecked(false);
        } else {
            setChecked(true);
        }
    }

    /**
     * Checks whether the users checked for the checkbox item are equal to the purchase wide
     * users involved. If yes, sets the color to normal, if no sets it to special (red).
     * If checkbox is unchecked, sets it to normal in any case.
     */
    public void setCheckBoxColor(@NonNull boolean[] purchaseUsersInvolved) {
        final boolean newIsSpecial = isChecked() && !Arrays.equals(mUsersChecked, purchaseUsersInvolved);
        if (mIsSpecial != newIsSpecial) {
            mIsSpecial = newIsSpecial;

            if (Utils.isRunningLollipopAndHigher()) {
                setButtonTintList();
            }
        }
    }

    /**
     * Sets the button color tint list depending on whether the users checked differ from the
     * purchase wide or not.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setButtonTintList() {
        if (mIsSpecial) {
            setButtonTintList(ContextCompat.getColorStateList(getContext(), R.color.checkbox_color_red));
        } else {
            setButtonTintList(ContextCompat.getColorStateList(getContext(), R.color.checkbox_color_accent));
        }

        // TODO: implement for Android <5.0
    }

    @NonNull
    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();

        bundle.putParcelable(STATE_SUPER, super.onSaveInstanceState());
        bundle.putBooleanArray(STATE_USERS_CHECKED, mUsersChecked);
        bundle.putBoolean(STATE_IS_SPECIAL, mIsSpecial);

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            mUsersChecked = bundle.getBooleanArray(STATE_USERS_CHECKED);
            mIsSpecial = bundle.getBoolean(STATE_IS_SPECIAL);
            if (mIsSpecial) {
                setButtonTintList();
            }
            state = bundle.getParcelable(STATE_SUPER);
        }

        super.onRestoreInstanceState(state);
    }
}
