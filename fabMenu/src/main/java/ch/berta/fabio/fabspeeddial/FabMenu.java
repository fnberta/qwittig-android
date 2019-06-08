package ch.berta.fabio.fabspeeddial;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides an implementation of the FAB quick dial pattern as described by Google in its Material
 * Design Guidelines. Uses standard {@link FloatingActionButton} from the design support lib to
 * draw the speed dial options.
 */
@CoordinatorLayout.DefaultBehavior(FabMenu.Behavior.class)
public class FabMenu extends FrameLayout implements View.OnClickListener, ProgressArc.Listener,
        CompleteView.Listener {

    private static final String STATE_SUPER = "STATE_SUPER";
    private static final String STATE_MENU_OPEN = "STATE_MENU_OPEN";
    private static final String STATE_PROGRESS_ANIM = "STATE_PROGRESS_ANIM";
    private static final String STATE_PROGRESS_COMPLETE = "STATE_PROGRESS_COMPLETE";
    private static final int VSYNC_RHYTHM = 16;
    private static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final Interpolator FAST_OUT_LINEAR_IN_INTERPOLATOR = new FastOutLinearInInterpolator();
    private int contentPadding;
    private int menuAnimTime;
    private boolean menuAnimating;
    private boolean menuOpen;
    private FloatingActionButton menuFab;
    private CardView menuLabelCard;
    private LinearLayout childContainer;
    private int childMenuRes;
    private Map<FloatingActionButton, MenuItem> fabMenuItemMap;
    private Map<CardView, MenuItem> labelMenuItemMap;
    private FabMenuClickListener fabMenuClickListener;
    private TransitionDrawable background;
    private float childYOffset;
    private ProgressFinalAnimationListener progressFinalAnimationListener;
    private MenuItem clickedMenuItem;

    private ProgressArc progressArc;
    private CompleteView completeView;

    public FabMenu(Context context) {
        this(context, null);
    }

    public FabMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FabMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(@NonNull Context context) {
        final Resources res = getResources();
        if (!Utils.isRunningLollipopAndHigher()) {
            final int maxContentSize = (int) res.getDimension(R.dimen.fab_menu_content_size);
            final int fabSize = res.getDimensionPixelSize(R.dimen.fab_menu_fab_size_normal);
            contentPadding = (fabSize - maxContentSize) / 2;
        }

        childMenuRes = R.menu.fab_menu;
        menuAnimTime = res.getInteger(R.integer.fab_anim_time);
        childYOffset = getResources().getDimensionPixelSize(R.dimen.child_view_anim_y_offset);
        initLayout(context);
        initMenu(context);
    }

    private void initLayout(@NonNull Context context) {
        setClipChildren(false);
        setFocusableInTouchMode(true);
        setOnClickListener(this);

        background = (TransitionDrawable) ContextCompat.getDrawable(context, R.drawable.background);
        setBackground(background);

        LayoutInflater.from(context).inflate(R.layout.main, this, true);
        childContainer = (LinearLayout) findViewById(R.id.ll_items);
        menuFab = (FloatingActionButton) findViewById(R.id.fab_main);
        menuFab.setOnClickListener(this);
        final float fabElevation = ViewCompat.getElevation(menuFab);
        menuLabelCard = (CardView) findViewById(R.id.cv_label_main);
        menuLabelCard.setOnClickListener(this);
        progressArc = (ProgressArc) findViewById(R.id.pa_main);
        progressArc.setListener(this);
        ViewCompat.setElevation(progressArc, fabElevation);
        completeView = (CompleteView) findViewById(R.id.cv_main);
        completeView.setListener(this);
        ViewCompat.setElevation(completeView, fabElevation + 1);
    }

    private void initMenu(@NonNull Context context) {
        final Menu childMenu = new MenuBuilder(context);
        final SupportMenuInflater inflater = new SupportMenuInflater(context);
        inflater.inflate(childMenuRes, childMenu);

        final int size = childMenu.size();
        fabMenuItemMap = new HashMap<>(size);
        labelMenuItemMap = new HashMap<>(size);

        // init label for menu fab
        final MenuItem firstItem = childMenu.getItem(0);
        if (firstItem.isVisible()) {
            fabMenuItemMap.put(menuFab, firstItem);
            labelMenuItemMap.put(menuLabelCard, firstItem);

            final TextView labelText = (TextView) findViewById(R.id.tv_label_main);
            final CharSequence title = firstItem.getTitle();
            if (!TextUtils.isEmpty(title)) {
                labelText.setText(title);
            }
        }

        // init child fabs and labels
        for (int i = 1; i < size; i++) {
            final MenuItem menuItem = childMenu.getItem(i);
            if (menuItem.isVisible()) {
                final LinearLayout childView = getFabFromMenuItem(menuItem);
                childContainer.addView(childView);
            }
        }
    }

    @NonNull
    private LinearLayout getFabFromMenuItem(@NonNull MenuItem menuItem) {
        final LinearLayout childView = (LinearLayout) LayoutInflater.from(getContext())
                .inflate(R.layout.child_fab, this, false);
        final FloatingActionButton fab = (FloatingActionButton) childView.findViewById(R.id.fab_child);
        fabMenuItemMap.put(fab, menuItem);
        final CardView labelCard = (CardView) childView.findViewById(R.id.cv_label);
        labelMenuItemMap.put(labelCard, menuItem);
        final TextView labelText = (TextView) childView.findViewById(R.id.tv_label);

        fab.setImageDrawable(menuItem.getIcon());
        fab.setOnClickListener(this);
        labelCard.setOnClickListener(this);

        final CharSequence title = menuItem.getTitle();
        if (!TextUtils.isEmpty(title)) {
            labelText.setText(title);
        } else {
            childView.removeView(labelCard);
        }
        return childView;
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (!Utils.isRunningLollipopAndHigher()) {
            setFakeShadowOffset();
        }
    }

    private void setFakeShadowOffset() {
        final int startEnd = menuFab.getPaddingStart() - contentPadding;
        final int topBottom = menuFab.getPaddingTop() - contentPadding;
        progressArc.setShadowPadding(startEnd, topBottom);
    }

    @Override
    public void onClick(View view) {
        if (view == menuFab && !menuOpen) {
            if (progressArc.isComplete()) {
                progressArc.resetProgress();
                completeView.hide();
                if (fabMenuClickListener != null) {
                    fabMenuClickListener.onFabCompleteClicked();
                }
            } else if (!progressArc.isAnimationPlaying()) {
                open();
            }

            return;
        }

        if (menuOpen) {
            if (view instanceof FloatingActionButton) {
                clickedMenuItem = fabMenuItemMap.get(view);
            } else if (view instanceof CardView) {
                clickedMenuItem = labelMenuItemMap.get(view);
            }

            close();
        }
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        // close the menu on back button press
        if (menuOpen && event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_UP && event.getRepeatCount() == 0) {
            close();
            return true;
        }

        return super.dispatchKeyEventPreIme(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // don't react to touches if menu is not open
        return menuOpen && super.onTouchEvent(event);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_SUPER, super.onSaveInstanceState());
        bundle.putBoolean(STATE_MENU_OPEN, menuOpen);
        bundle.putBoolean(STATE_PROGRESS_COMPLETE, progressArc.isCompleteOrCompleting());
        bundle.putBoolean(STATE_PROGRESS_ANIM, progressArc.isAnimationPlaying());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;

            menuOpen = bundle.getBoolean(STATE_MENU_OPEN);
            if (menuOpen) {
                open();
            }

            final boolean complete = bundle.getBoolean(STATE_PROGRESS_COMPLETE);
            if (complete) {
                progressArc.setComplete(true);
                completeView.show();
            } else {
                final boolean isAnimating = bundle.getBoolean(STATE_PROGRESS_ANIM);
                if (isAnimating) {
                    progressArc.startProgress();
                }
            }

            state = bundle.getParcelable(STATE_SUPER);
        }

        super.onRestoreInstanceState(state);
    }

    @Override
    public void onArcFinalAnimComplete() {
        completeView.animateIn(progressArc.getScaleDownAnimator());
    }

    @Override
    public void onCompleteViewShown() {
        if (progressFinalAnimationListener != null) {
            progressFinalAnimationListener.onProgressFinalAnimationComplete();
        }
    }

    /**
     * Sets the listener for menu click events.
     *
     * @param fabMenuClickListener the click event listener
     */
    public void setFabMenuClickListener(@NonNull FabMenuClickListener fabMenuClickListener) {
        this.fabMenuClickListener = fabMenuClickListener;
    }

    /**
     * Hides the menu button.
     *
     * @param animate whether to animate the transition
     */
    public void hideMenuButton(boolean animate) {
        if (animate) {
            menuFab.hide();
        } else {
            menuFab.setVisibility(View.GONE);
        }
    }

    /**
     * Shows the menu button.
     *
     * @param animate whether to animate the transition
     */
    public void showMenuButton(boolean animate) {
        if (animate) {
            menuFab.show();
        } else {
            menuFab.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Toggles the visibility of the speed dial options.
     */
    public void toggle() {
        if (menuOpen) {
            close();
        } else {
            open();
        }
    }

    /**
     * Closes the speed dial options.
     */
    public void close() {
        if (menuAnimating) {
            return;
        }

        menuOpen = false;

        final List<Animator> animators = new ArrayList<>(2);
        animators.add(ObjectAnimator.ofFloat(childContainer, "alpha", 0f));
        animators.add(ObjectAnimator.ofFloat(menuLabelCard, "alpha", 0f));

        final AnimatorSet set = new AnimatorSet();
        set.setInterpolator(FAST_OUT_LINEAR_IN_INTERPOLATOR);
        set.setDuration(menuAnimTime);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                menuAnimating = true;
                menuFab.setSelected(false);
                background.reverseTransition(menuAnimTime);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                menuAnimating = false;
                childContainer.setVisibility(View.GONE);
                menuLabelCard.setVisibility(View.GONE);
                if (Utils.isRunningLollipopAndHigher()) {
                    setElevation(0);
                }

                if (fabMenuClickListener != null && clickedMenuItem != null) {
                    fabMenuClickListener.onFabMenuItemClicked(clickedMenuItem);
                    clickedMenuItem = null;
                }
            }
        });
        set.playTogether(animators);
        set.start();
    }

    /**
     * Opens the speed dial options.
     */
    public void open() {
        if (menuAnimating) {
            return;
        }

        menuOpen = true;

        final int count = childContainer.getChildCount();
        final List<Animator> animators = new ArrayList<>(count);
        animators.add(getMenuLabelAnimator());
        for (int i = 0; i < count; i++) {
            // start from last child
            final View child = childContainer.getChildAt(count - 1 - i);

            final View fab = child.findViewById(R.id.fab_child);
            animators.add(getInAnimator(fab, i));

            final View label = child.findViewById(R.id.cv_label);
            animators.add(getInAnimator(label, i));
        }

        final AnimatorSet set = new AnimatorSet();
        set.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
        set.setDuration(menuAnimTime);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                menuAnimating = true;
                menuFab.setSelected(true);

                childContainer.setVisibility(View.VISIBLE);
                childContainer.setAlpha(1f);

                if (Utils.isRunningLollipopAndHigher()) {
                    // make sure we are above everything
                    setElevation(menuFab.getElevation() + 1);
                }
                background.startTransition(menuAnimTime);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                menuAnimating = false;
            }
        });
        set.playTogether(animators);
        set.start();
    }

    private Animator getMenuLabelAnimator() {
        menuLabelCard.setScaleX(0f);
        menuLabelCard.setScaleY(0f);
        menuLabelCard.setAlpha(0f);

        final PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1f);
        final PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1f);
        final PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1f);
        final ObjectAnimator animator =
                ObjectAnimator.ofPropertyValuesHolder(menuLabelCard, scaleX, scaleY, alpha);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                menuLabelCard.setVisibility(View.VISIBLE);
            }
        });

        return animator;
    }

    private Animator getInAnimator(final View view, int position) {
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setAlpha(0f);
        view.setY(0f + childYOffset);

        final PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1f);
        final PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1f);
        final PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1f);
        final PropertyValuesHolder translationY =
                PropertyValuesHolder.ofFloat("translationY", view.getY() - childYOffset);
        final ObjectAnimator animator =
                ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY, alpha, translationY);
        animator.setStartDelay(2 * position * VSYNC_RHYTHM);

        return animator;
    }

    /**
     * Starts the indeterminate spinning progress circle.
     */
    public void startProgress() {
        progressArc.startProgress();
    }

    /**
     * Stops the indeterminate spinning progress circle.
     */
    public void stopProgress() {
        progressArc.stopProgress();
    }

    /**
     * Starts the final animation, i.e. makes the spinning progress circle determinate.
     */
    public void startProgressFinalAnimation() {
        progressArc.startProgressFinalAnimation();
    }

    /**
     * Sets the callback for when the final animation is complete.
     *
     * @param listener the listener that gets called when the final animation is complete
     */
    public void setProgressFinalAnimationListener(@NonNull ProgressFinalAnimationListener listener) {
        progressFinalAnimationListener = listener;
    }

    public void showCompleteView() {
        progressArc.setComplete(true);
        onArcFinalAnimComplete();
    }

    public void hideCompleteView() {
        progressArc.resetProgress();
        completeView.hide();
    }

    /**
     * Provides a {@link CoordinatorLayout.Behavior} that mimics the standard
     * {@link FloatingActionButton} behaviour, i.e. moving out of the way for a {@link Snackbar}.
     * <p/>
     * Code copy pasted from the Google {@link FloatingActionButton} implementation and adapted
     * slightly.
     */
    public static class Behavior extends CoordinatorLayout.Behavior<FabMenu> {

        private ValueAnimator mFabTranslationYAnimator;
        private float mFabTranslationY;

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent,
                                       FabMenu child, View dependency) {
            return dependency instanceof Snackbar.SnackbarLayout;
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, FabMenu child,
                                              View dependency) {
            if (dependency instanceof Snackbar.SnackbarLayout) {
                updateFabTranslationForSnackbar(parent, child, dependency);
            }

            return false;
        }

        private void updateFabTranslationForSnackbar(CoordinatorLayout parent,
                                                     final FabMenu fab, View snackbar) {
            final float targetTransY = getFabTranslationYForSnackbar(parent, fab);
            if (mFabTranslationY == targetTransY) {
                // We're already at (or currently animating to) the target value, return...
                return;
            }

            final float currentTransY = ViewCompat.getTranslationY(fab);

            // Make sure that any current animation is cancelled
            if (mFabTranslationYAnimator != null && mFabTranslationYAnimator.isRunning()) {
                mFabTranslationYAnimator.cancel();
            }

            if (fab.isShown()
                    && Math.abs(currentTransY - targetTransY) > (fab.getHeight() * 0.667f)) {
                // If the FAB will be travelling by more than 2/3 of it's height, let's animate
                // it instead
                if (mFabTranslationYAnimator == null) {
                    mFabTranslationYAnimator = new ValueAnimator();
                    mFabTranslationYAnimator.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
                    mFabTranslationYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            ViewCompat.setTranslationY(fab, animation.getAnimatedFraction());
                        }
                    });
                }
                mFabTranslationYAnimator.setFloatValues(currentTransY, targetTransY);
                mFabTranslationYAnimator.start();
            } else {
                // Now update the translation Y
                ViewCompat.setTranslationY(fab, targetTransY);
            }

            mFabTranslationY = targetTransY;
        }

        private float getFabTranslationYForSnackbar(CoordinatorLayout parent,
                                                    FabMenu fab) {
            float minOffset = 0;
            final List<View> dependencies = parent.getDependencies(fab);
            for (int i = 0, z = dependencies.size(); i < z; i++) {
                final View view = dependencies.get(i);
                if (view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(fab, view)) {
                    minOffset = Math.min(minOffset,
                            ViewCompat.getTranslationY(view) - view.getHeight());
                }
            }

            return minOffset;
        }
    }
}