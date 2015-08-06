package ch.giantific.qwittig.utils;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewAnimationUtils;

import com.parse.ParseObject;
import com.parse.ParseUser;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.fraction.Fraction;

import java.util.List;
import java.util.Random;

import ch.giantific.qwittig.data.parse.models.Item;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.User;

import static ch.giantific.qwittig.constants.AppConstants.FAB_CIRCULAR_REVEAL_DELAY;

/**
 * Created by fabio on 05.10.14.
 */
public class Utils {

    private Utils() {
        // class cannot be instantiated
    }

    public static boolean emailIsValid(String email) {
        String pattern = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";

        return !TextUtils.isEmpty(email) && email.matches(pattern);
    }

    public static boolean isPositive(double number) {
        return Double.doubleToRawLongBits(number) >= 0;
    }

    public static boolean isPositive(Fraction number) {
        return number.compareTo(Fraction.ZERO) >= 0;
    }

    public static boolean isPositive(BigFraction number) {
        return number.compareTo(BigFraction.ZERO) >= 0;
    }

    public static float convertDpToPixel(Resources r, float dp) {
        DisplayMetrics metrics = r.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

    /**
     * Returns the screen width in pixels
     *
     * @param context is the context to get the resources
     * @return the screen width in pixels
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    /**
     * Returns a randomly chosen int between 0 and the max value given -1.
     * @param max Maximum value, excluded!
     * @return Random int between 0 and max -1
     */
    public static int getRandomInt(int max) {
        Random generator = new Random();
        return generator.nextInt(max);
    }

    /**
     * Returns the size in pixels of an attribute dimension
     *
     * @param context the context to get the resources from
     * @param attr is the attribute dimension we want to know the size from
     * @return the size in pixels of an attribute dimension
     */
    public static int getThemeAttributeDimensionSize(Context context, int attr) {
        TypedArray typedArray = null;
        try {
            typedArray = context.getTheme().obtainStyledAttributes(new int[] { attr });
            return typedArray.getDimensionPixelSize(0, 0);
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
    }

    /**
     * Reads and returns the tag of a view, checks if its an int and if yes returns the value.
     *
     * @param v
     * @return
     */
    public static int getViewPositionFromTag(View v) {
        int position = 0;

        if (v.getTag() instanceof Integer) {
            position = (Integer) v.getTag();
        }
        return position;
    }

    public static boolean isRunningLollipopAndHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Animator getCircularRevealAnimator(View view) {
        // get the center for the clipping circle
        int cx = view.getWidth() / 2;
        int cy = view.getHeight() / 2;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(view.getWidth(), view.getHeight()) / 2;

        // create the animator for this view (the start radius is zero)
        Animator animator = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        animator.setStartDelay(FAB_CIRCULAR_REVEAL_DELAY);

        return animator;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Animator getCircularHideAnimator(View view) {
        // get the center for the clipping circle
        int cx = view.getWidth() / 2;
        int cy = view.getHeight() / 2;

        // get the initial radius for the clipping circle
        int initialRadius = view.getWidth();

        // create the animator for this view (the start radius is zero) and return it
        return ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);
    }

    public static double calculateMyShare(Purchase purchase) {
        double myShare = 0;
        double exchangeRate = purchase.getExchangeRate();
        for (ParseObject parseObject : purchase.getItems()) {
            Item item = (Item) parseObject;
            List<ParseUser> usersInvolved = item.getUsersInvolved();
            User currentUser = (User) ParseUser.getCurrentUser();
            if (usersInvolved.contains(currentUser)) {
                myShare += (item.getPrice() * exchangeRate / usersInvolved.size());
            }
        }

        return myShare;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&  activeNetwork.isConnectedOrConnecting();
    }
}
