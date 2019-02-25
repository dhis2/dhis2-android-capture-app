package com.tbuonomo.viewpagerdotsindicator;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public abstract class FrameLayoutIndicator extends FrameLayout {

    protected List<ImageView> strokeDots;
    protected View dotIndicatorView;
    protected ViewPager viewPager;
    protected boolean dotsClickable;
    protected LinearLayout strokeDotsLinearLayout;
    protected int dotsCornerRadius;
    protected int dotsStrokeColor;
    protected int dotIndicatorColor;
    protected int dotsStrokeWidth;
    protected ViewPager.OnPageChangeListener pageChangedListener;

    public FrameLayoutIndicator(@NonNull Context context) {
        super(context);
    }

    public FrameLayoutIndicator(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FrameLayoutIndicator(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    protected void addStrokeDots(int count) {
        for (int i = 0; i < count; i++) {
            ViewGroup dot = buildDot(true);
            final int finalI = i;
            dot.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dotsClickable && viewPager != null && viewPager.getAdapter() != null && finalI < viewPager.getAdapter().getCount()) {
                        viewPager.setCurrentItem(finalI, true);
                    }
                }
            });

            strokeDots.add((ImageView) dot.findViewById(R.id.spring_dot));
            strokeDotsLinearLayout.addView(dot);
        }
    }

    protected void setUpDotBackground(boolean stroke, View dotView) {
        GradientDrawable dotBackground = (GradientDrawable) dotView.getBackground();
        if (stroke) {
            dotBackground.setStroke(dotsStrokeWidth, dotsStrokeColor);
        } else {
            dotBackground.setColor(dotIndicatorColor);
        }
        dotBackground.setCornerRadius(dotsCornerRadius);
    }

    protected void removeDots(int count) {
        for (int i = 0; i < count; i++) {
            strokeDotsLinearLayout.removeViewAt(strokeDotsLinearLayout.getChildCount() - 1);
            strokeDots.remove(strokeDots.size() - 1);
        }
    }

    protected int dpToPx(int dp) {
        return (int) (getContext().getResources().getDisplayMetrics().density * dp);
    }

    //*********************************************************
    // Users Methods
    //*********************************************************
    protected void setUpDotsAnimators() {
        if (viewPager != null && viewPager.getAdapter() != null && viewPager.getAdapter().getCount() > 0) {
            if (pageChangedListener != null) {
                viewPager.removeOnPageChangeListener(pageChangedListener);
            }
            setUpOnPageChangedListener();
            viewPager.addOnPageChangeListener(pageChangedListener);
        }
    }

    protected void setUpViewPager() {
        if (viewPager.getAdapter() != null) {
            viewPager.getAdapter().registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    refreshDots();
                }
            });
        }
    }

    /**
     * Set the indicator dot color.
     *
     * @param color the color fo the indicator dot.
     */
    public void setDotIndicatorColor(int color) {
        if (dotIndicatorView != null) {
            dotIndicatorColor = color;
            setUpDotBackground(false, dotIndicatorView);
        }
    }

    /**
     * Set the stroke indicator dots color.
     *
     * @param color the color fo the stroke indicator dots.
     */
    public void setStrokeDotsIndicatorColor(int color) {
        if (strokeDots != null && !strokeDots.isEmpty()) {
            dotsStrokeColor = color;
            for (ImageView v : strokeDots) {
                setUpDotBackground(true, v);
            }
        }
    }

    /**
     * Determine if the stroke dots are clickable to go the a page directly.
     *
     * @param dotsClickable true if dots are clickables.
     */
    public void setDotsClickable(boolean dotsClickable) {
        this.dotsClickable = dotsClickable;
    }

    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
        setUpViewPager();
        refreshDots();
    }

    public abstract ViewGroup buildDot(boolean stroke);

    public abstract void setUpOnPageChangedListener();

    public abstract void refreshDots();
}
