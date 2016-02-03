/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.parse.ParseUser;

import java.util.List;

import ch.giantific.qwittig.BlurTransformation;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.presentation.ui.widgets.CircleDisplay;

/**
 * Contains generic binding adapters.
 */
public class BindingUtils {

    private BindingUtils() {
        // class cannot be instantiated
    }

    @BindingAdapter({"colorScheme"})
    public static void setColorScheme(SwipeRefreshLayout view, int[] colorScheme) {
        view.setColorSchemeColors(colorScheme);
    }

    @BindingAdapter({"bold"})
    public static void setTextBold(TextView view, boolean bold) {
        if (bold) {
            view.setTypeface(null, Typeface.BOLD);
        } else {
            view.setTypeface(null, Typeface.NORMAL);
        }
    }

    @BindingAdapter({"avatarSquare", "fallback"})
    public static void loadAvatarSquare(ImageView view, byte[] avatar, Drawable fallback) {
        final Context context = view.getContext();
        Glide.with(context)
                .load(avatar)
                .error(fallback)
                .into(view);
    }

    @BindingAdapter({"avatar", "fallback"})
    public static void loadAvatar(ImageView view, byte[] avatar, Drawable fallback) {
        glideLoadAvatar(view, avatar, fallback, false);
    }

    @BindingAdapter({"avatarRipple", "fallback"})
    public static void loadAvatarRipple(ImageView view, byte[] avatar, Drawable fallback) {
        glideLoadAvatar(view, avatar, fallback, true);
    }

    private static void glideLoadAvatar(final ImageView view, byte[] avatar, Drawable fallback,
                                        final boolean withRipple) {
        final Context context = view.getContext();
        Glide.with(context)
                .load(avatar)
                .asBitmap()
                .error(fallback)
                .into(new BitmapImageViewTarget(view) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        view.setImageDrawable(AvatarUtils.getRoundedDrawable(context, resource, withRipple));
                    }
                });
    }

    @BindingAdapter({"backdrop"})
    public static void loadAvatarBackdrop(ImageView view, byte[] avatar) {
        final Context context = view.getContext();
        Glide.with(context)
                .load(avatar)
                .bitmapTransform(new BlurTransformation(context))
                .into(view);
    }

    @BindingAdapter({"percentage"})
    public static void setCirclePercentage(CircleDisplay view, float percentage) {
        view.showValue(percentage, 100f, false);
    }

    @BindingAdapter("fadeVisible")
    public static void setFadeVisible(final View view, boolean visible) {
        if (view.getTag() == null) {
            view.setTag(true);
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
        } else {
            view.animate().cancel();

            final int duration = view.getContext().getResources().getInteger(android.R.integer.config_longAnimTime);
            if (visible) {
                view.setVisibility(View.VISIBLE);
                view.setAlpha(0);
                view.animate()
                        .alpha(1)
                        .setDuration(duration)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                view.setAlpha(1);
                            }
                        });
            } else {
                view.animate()
                        .alpha(0)
                        .setDuration(duration)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                view.setAlpha(1);
                                view.setVisibility(View.GONE);
                            }
                        });
            }
        }
    }

    @BindingAdapter({"deadline"})
    public static void setTaskDeadline(TextView view, int daysToDeadline) {
        final Context context = view.getContext();
        final String deadlineString;
        final int color;
        if (daysToDeadline == 0) {
            deadlineString = context.getString(R.string.deadline_today);
            color = R.color.green;
        } else if (daysToDeadline == -1) {
            deadlineString = context.getString(R.string.yesterday);
            color = R.color.red;
        } else if (daysToDeadline < 0) {
            deadlineString = context.getString(R.string.deadline_text_neg, daysToDeadline * -1);
            color = R.color.red;
        } else {
            deadlineString = context.getString(R.string.deadline_text_pos, daysToDeadline);
            color = R.color.green;
        }

        view.setText(deadlineString);
        view.setTextColor(ContextCompat.getColor(context, color));
    }

    @BindingAdapter({"usersInvolved", "currentUser"})
    public static void setTaskUsersInvolved(TextView view, List<ParseUser> usersInvolved,
                                            User currentUser) {
        final Context context = view.getContext();
        final User userResponsible = (User) usersInvolved.get(0);
        String usersInvolvedString = "";
        if (usersInvolved.size() > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(context.getString(R.string.task_users_involved_next)).append(" ");
            for (ParseUser parseUser : usersInvolved) {
                User user = (User) parseUser;
                if (!user.getObjectId().equals(userResponsible.getObjectId())) {
                    stringBuilder.append(user.getNicknameOrMe(context, currentUser)).append(" - ");
                }
            }
            // delete last -
            int length = stringBuilder.length();
            stringBuilder.delete(length - 3, length - 1);
            usersInvolvedString = stringBuilder.toString();
        }

        view.setText(usersInvolvedString);
    }

    @BindingAdapter({"purchaseBackground"})
    public static void setPurchaseBackground(View view, boolean read) {
        final Context context = view.getContext();
        if (read) {
            int[] attrs = new int[]{R.attr.selectableItemBackground};
            TypedArray typedArray = context.obtainStyledAttributes(attrs);
            int backgroundResource = typedArray.getResourceId(0, 0);
            typedArray.recycle();

            view.setBackgroundResource(backgroundResource);
        } else if (Utils.isRunningLollipopAndHigher()) {
            view.setBackground(ContextCompat.getDrawable(context, R.drawable.ripple_white));
        } else {
            view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
        }
    }
}
