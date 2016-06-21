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
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.PieData;

import ch.berta.fabio.fabprogress.FabProgress;
import ch.berta.fabio.fabspeeddial.FabMenu;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;
import ch.giantific.qwittig.presentation.purchases.details.widgets.CircleDisplay;
import ch.giantific.qwittig.presentation.navdrawer.BlurTransformation;
import ch.giantific.qwittig.presentation.settings.profile.AvatarLoadListener;
import ch.giantific.qwittig.presentation.stats.widgets.BarChart;
import ch.giantific.qwittig.presentation.stats.widgets.PieChart;

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
    public static void loadAvatarSquare(ImageView view, String avatarUrl, Drawable fallback) {
        final Context context = view.getContext();
        Glide.with(context)
                .load(avatarUrl)
                .error(fallback)
                .into(view);
    }

    @BindingAdapter({"avatarSquare", "fallback", "listener"})
    public static void loadAvatarSquare(ImageView view, String avatarUrl,
                                        Drawable fallback, final AvatarLoadListener listener) {
        final Context context = view.getContext();
        Glide.with(context)
                .load(avatarUrl)
                .error(fallback)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        listener.onAvatarLoaded();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        listener.onAvatarLoaded();
                        return false;
                    }
                })
                .into(view);
    }

    @BindingAdapter({"avatar", "fallback"})
    public static void loadAvatar(ImageView view, String avatarUrl, Drawable fallback) {
        glideLoadAvatar(view, avatarUrl, fallback, false);
    }

    @BindingAdapter({"avatarRipple", "fallback"})
    public static void loadAvatarRipple(ImageView view, String avatarUrl, Drawable fallback) {
        glideLoadAvatar(view, avatarUrl, fallback, true);
    }

    private static void glideLoadAvatar(final ImageView view, String avatarUrl, Drawable fallback,
                                        final boolean withRipple) {
        final Context context = view.getContext();
        Glide.with(context)
                .load(avatarUrl)
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
    public static void loadAvatarBackdrop(ImageView view, String avatarUrl) {
        final Context context = view.getContext();
        Glide.with(context)
                .load(avatarUrl)
                .bitmapTransform(new BlurTransformation(context))
                .into(view);
    }

    @BindingAdapter({"receiptImage", "loadListener"})
    public static void loadReceiptImage(ImageView view, String receiptUri, final LoadingViewModel listener) {
        final Context context = view.getContext();
        Glide.with(context)
                .load(receiptUri)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        listener.setLoading(false);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        listener.setLoading(false);
                        return false;
                    }
                })
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

    @BindingAdapter({"drawableStartBounds"})
    public static void setDrawableStartWithIntrinsicBounds(TextView view, int drawableRes) {
        final Drawable[] drawables = view.getCompoundDrawablesRelative();
        final Drawable drawable = ContextCompat.getDrawable(view.getContext(), drawableRes);
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, drawables[1], drawables[2], drawables[3]);
    }

    @BindingAdapter({"purchaseBackground"})
    public static void setPurchaseBackground(View view, boolean read) {
        final Context context = view.getContext();
        if (read) {
            final int[] attrs = new int[]{R.attr.selectableItemBackground};
            final TypedArray typedArray = context.obtainStyledAttributes(attrs);
            final int backgroundResource = typedArray.getResourceId(0, 0);
            typedArray.recycle();

            view.setBackgroundResource(backgroundResource);
        } else if (Utils.isRunningLollipopAndHigher()) {
            view.setBackground(ContextCompat.getDrawable(context, R.drawable.ripple_white));
        } else {
            view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
        }
    }

    @BindingAdapter({"progress", "animStop"})
    public static void setFabProgressAnim(FabProgress view, boolean start, boolean animStop) {
        if (start) {
            view.startProgress();
        } else if (animStop) {
            view.startProgressFinalAnimation();
        } else {
            view.stopProgress();
        }
    }

    @BindingAdapter({"progress", "animStop"})
    public static void setFabMenuProgressAnim(FabMenu view, boolean start, boolean animStop) {
        if (start) {
            view.startProgress();
        } else if (animStop) {
            view.startProgressFinalAnimation();
        } else {
            view.stopProgress();
        }
    }

    @BindingAdapter({"contentLoading"})
    public static void setContentLoadingProgressBarLoading(ContentLoadingProgressBar view,
                                                           boolean loading) {
        if (loading) {
            view.show();
        } else {
            view.hide();
        }
    }

    @BindingAdapter({"barData", "dataEmpty"})
    public static void setBarChartData(BarChart view, BarData barData, boolean dataEmpty) {
        if (barData != null) {
            view.setData(barData);
            if (!view.isEmpty() && !dataEmpty) {
                view.animateY(BarChart.ANIMATION_Y_TIME);
            }
        }
    }

    @BindingAdapter({"pieData", "dataEmpty"})
    public static void setPieChartData(PieChart view, PieData pieData, boolean dataEmpty) {
        if (pieData != null) {
            view.setData(pieData);
            if (!view.isEmpty() && !dataEmpty) {
                view.animateY(PieChart.ANIMATION_Y_TIME);
            }
        }
    }

    @BindingAdapter({"pieData"})
    public static void setPieChartRowData(PieChart view, PieData pieData) {
        if (pieData != null) {
            view.setData(pieData);
            if (!view.isEmpty()) {
                view.animateY(PieChart.ANIMATION_Y_TIME);
            }
        }
    }

    @BindingAdapter({"taskTimeFrame"})
    public static void setTaskTimeFrame(TextView view, int timeFrameRes) {
        if (timeFrameRes > 0) {
            view.setText(timeFrameRes);
        }
    }
}
