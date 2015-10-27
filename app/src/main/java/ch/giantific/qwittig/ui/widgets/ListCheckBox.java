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
    @Nullable
    private boolean[] mUsersChecked;

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
                setUsersChecked(purchaseUsersInvolved);
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
        boolean[] usersChecked = getUsersChecked();
        if (usersChecked == null) {
            return;
        }

        if (purchaseUserIsChecked) {
            if (!usersChecked[purchaseUserPosition]) {
                usersChecked[purchaseUserPosition] = true;
            }
        } else if (usersChecked[purchaseUserPosition]) {
            usersChecked[purchaseUserPosition] = false;
        }

        setUsersChecked(usersChecked);
    }

    /**
     * Updates the checked status, depending on how many users are enabled. Sets to unchecked if
     * only the buyer is enabled.
     *
     * @param buyerPosition position of the buyer
     */
    public void updateCheckedStatus(int buyerPosition) {
        boolean[] usersChecked = getUsersChecked();
        if (usersChecked == null) {
            return;
        }

        boolean onlyBuyerIsCheckedInPurchase = true;
        for (int i = 0, usersCheckedLength = usersChecked.length; i < usersCheckedLength; i++) {
            if (usersChecked[i]) {
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
        boolean[] usersChecked = getUsersChecked();
        boolean isSpecial = false;

        if (isChecked() && !Arrays.equals(usersChecked, purchaseUsersInvolved)) {
            isSpecial = true;
        }

        if (Utils.isRunningLollipopAndHigher()) {
            setListStatus(isSpecial);
        }
    }

    /**
     * Sets the color depending on whether the users checked differ from the purchase wide or not.
     *
     * @param isSpecial whether the users checked differ from the purchase wide or not
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setListStatus(boolean isSpecial) {
        if (isSpecial) {
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

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            mUsersChecked = bundle.getBooleanArray(STATE_USERS_CHECKED);
            state = bundle.getParcelable(STATE_SUPER);
        }

        super.onRestoreInstanceState(state);
    }
}
