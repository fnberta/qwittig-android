/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.fraction.Fraction;

import java.util.List;
import java.util.Random;

/**
 * Provides useful generic utility methods.
 */
public class Utils {

    private Utils() {
        // class cannot be instantiated
    }

    /**
     * Returns whether the passed string is a valid email address or not.
     *
     * @param email the email address to test
     * @return whether the email address is valid or not
     */
    public static boolean emailIsValid(@NonNull String email) {
        String pattern = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";

        return !TextUtils.isEmpty(email) && email.matches(pattern);
    }

    /**
     * Returns whether the passed number is positive, i.e. bigger or equal to zero.
     *
     * @param number the number to test
     * @return whether the number is positive or not
     */
    public static boolean isPositive(double number) {
        return Double.doubleToRawLongBits(number) >= 0;
    }

    /**
     * Returns whether the passed number is positive, i.e. bigger or equal to zero.
     *
     * @param number the number to test
     * @return whether the number is positive or not
     */
    public static boolean isPositive(@NonNull Fraction number) {
        return number.compareTo(Fraction.ZERO) >= 0;
    }

    /**
     * Returns whether the passed number is positive, i.e. bigger or equal to zero.
     *
     * @param number the number to test
     * @return whether the number is positive or not
     */
    public static boolean isPositive(@NonNull BigFraction number) {
        return number.compareTo(BigFraction.ZERO) >= 0;
    }

    /**
     * Returns the passed DP value in pixels.
     *
     * @param r  the resources to use to get the display's density
     * @param dp the dp value to convert
     * @return the dp value in pixels
     */
    public static float convertDpToPixel(@NonNull Resources r, float dp) {
        DisplayMetrics metrics = r.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

    /**
     * Returns the screen width in pixels.
     *
     * @param context the context to use to get the resources
     * @return the screen width in pixels
     */
    public static int getScreenWidth(@NonNull Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    /**
     * Returns a randomly chosen int between 0 and the max value given -1.
     *
     * @param max the maximum value, excluded
     * @return Random int between 0 and max-1
     */
    public static int getRandomInt(int max) {
        Random generator = new Random();
        return generator.nextInt(max);
    }

    /**
     * Returns the size in pixels of an attribute dimension.
     *
     * @param context the context to use to get the resources from
     * @param attr    the attribute dimension we want to know the size from
     * @return the size in pixels of the attribute dimension
     */
    public static int getThemeAttributeDimensionSize(@NonNull Context context, int attr) {
        TypedArray typedArray = null;
        try {
            typedArray = context.getTheme().obtainStyledAttributes(new int[]{attr});
            return typedArray.getDimensionPixelSize(0, 0);
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
    }

    /**
     * Returns the value of a view's tag if it is an int.
     *
     * @param v the view to read tag from
     * @return the view's tag value or 0 if it is not an int
     */
    public static int getViewPositionFromTag(@NonNull View v) {
        int value = 0;

        if (v.getTag() instanceof Integer) {
            value = (Integer) v.getTag();
        }
        return value;
    }

    /**
     * Returns whether the running Android version is lollipop and higher or an older version.
     *
     * @return whether the running Android version is lollipop and higher or an older version
     */
    public static boolean isRunningLollipopAndHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Returns whether there is an active network connection or not.
     * <p/>
     * Note that an active network connection does not guarantee that there is a connection to the
     * internet.
     *
     * @param context the context to use to get the {@link ConnectivityManager}
     * @return whether there is an active network connection or not
     */
    public static boolean isConnected(@NonNull Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Returns the number of boolean values passed in.
     *
     * @param values the booleans to count
     * @return the number of boolean values
     */
    public static int countTrue(@NonNull boolean... values) {
        int count = 0;
        for (boolean value : values) {
            if (value) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the last entry in a list, returns null if the list is empty.
     *
     * @param list the list to get the last entry from
     * @param <T>  the type of the objects in the list
     * @return the last entry in the list or null if the list is empty
     */
    @Nullable
    public static <T> T getLastInList(@NonNull List<T> list) {
        try {
            return list.get(list.size() - 1);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Checks that all given permissions have been granted by verifying that each entry in the
     * given array is of the value {@link PackageManager#PERMISSION_GRANTED}.
     *
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     */
    public static boolean verifyPermissions(@NonNull int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
