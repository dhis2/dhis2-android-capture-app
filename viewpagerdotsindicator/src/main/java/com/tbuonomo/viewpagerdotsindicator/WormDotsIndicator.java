package com.tbuonomo.viewpagerdotsindicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.viewpager.widget.ViewPager;

import static android.widget.LinearLayout.HORIZONTAL;
import static com.tbuonomo.viewpagerdotsindicator.UiUtils.getThemePrimaryColor;

public class WormDotsIndicator extends FrameLayoutIndicator {

    private View dotIndicatorLayout;

    // Attributes
    private int dotsSize;
    private int dotsSpacing;
    private int horizontalMargin;
    private SpringAnimation dotIndicatorXSpring;
    private SpringAnimation dotIndicatorWidthSpring;

    public WormDotsIndicator(Context context) {
        this(context, null);
    }

    public WormDotsIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WormDotsIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        strokeDots = new ArrayList<>();
        strokeDotsLinearLayout = new LinearLayout(context);
        LayoutParams linearParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        horizontalMargin = dpToPx(24);
        linearParams.setMargins(horizontalMargin, 0, horizontalMargin, 0);
        strokeDotsLinearLayout.setLayoutParams(linearParams);
        strokeDotsLinearLayout.setOrientation(HORIZONTAL);
        addView(strokeDotsLinearLayout);

        dotsSize = dpToPx(16); // 16dp
        dotsSpacing = dpToPx(4); // 4dp
        dotsStrokeWidth = dpToPx(2); // 2dp
        dotsCornerRadius = dotsSize / 2;
        dotIndicatorColor = getThemePrimaryColor(context);
        dotsStrokeColor = dotIndicatorColor;
        dotsClickable = true;

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.WormDotsIndicator);

            // Dots attributes
            dotIndicatorColor = a.getColor(R.styleable.WormDotsIndicator_dotsColor, dotIndicatorColor);
            dotsStrokeColor = a.getColor(R.styleable.WormDotsIndicator_dotsStrokeColor, dotIndicatorColor);
            dotsSize = (int) a.getDimension(R.styleable.WormDotsIndicator_dotsSize, dotsSize);
            dotsSpacing = (int) a.getDimension(R.styleable.WormDotsIndicator_dotsSpacing, dotsSpacing);
            dotsCornerRadius = (int) a.getDimension(R.styleable.WormDotsIndicator_dotsCornerRadius, dotsSize / 2f);

            // Spring dots attributes
            dotsStrokeWidth = (int) a.getDimension(R.styleable.WormDotsIndicator_dotsStrokeWidth, dotsStrokeWidth);

            a.recycle();
        }

        if (isInEditMode()) {
            addStrokeDots(5);
            addView(buildDot(false));
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        refreshDots();
    }

    public void refreshDots() {
        if (dotIndicatorLayout == null) {
            setUpDotIndicator();
        }

        if (viewPager != null && viewPager.getAdapter() != null) {
            // Check if we need to refresh the strokeDots count
            if (strokeDots.size() < viewPager.getAdapter().getCount()) {
                addStrokeDots(viewPager.getAdapter().getCount() - strokeDots.size());
            } else if (strokeDots.size() > viewPager.getAdapter().getCount()) {
                removeDots(strokeDots.size() - viewPager.getAdapter().getCount());
            }
            setUpDotsAnimators();
        } else {
            Log.e(WormDotsIndicator.class.getSimpleName(), "You have to set an adapter to the view pager before !");
        }
    }

    private void setUpDotIndicator() {
        dotIndicatorLayout = buildDot(false);
        dotIndicatorView = dotIndicatorLayout.findViewById(R.id.worm_dot);
        addView(dotIndicatorLayout);
        dotIndicatorXSpring = new SpringAnimation(dotIndicatorLayout, SpringAnimation.TRANSLATION_X);
        SpringForce springForceX = new SpringForce(0);
        springForceX.setDampingRatio(1f);
        springForceX.setStiffness(300);
        dotIndicatorXSpring.setSpring(springForceX);

        FloatPropertyCompat floatPropertyCompat = new FloatPropertyCompat("DotsWidth") {
            @Override
            public float getValue(Object object) {
                return dotIndicatorView.getLayoutParams().width;
            }

            @Override
            public void setValue(Object object, float value) {
                ViewGroup.LayoutParams params = dotIndicatorView.getLayoutParams();
                params.width = (int) value;
                dotIndicatorView.requestLayout();
            }
        };
        dotIndicatorWidthSpring = new SpringAnimation(dotIndicatorLayout, floatPropertyCompat);
        SpringForce springForceWidth = new SpringForce(0);
        springForceWidth.setDampingRatio(1f);
        springForceWidth.setStiffness(300);
        dotIndicatorWidthSpring.setSpring(springForceWidth);
    }

    public ViewGroup buildDot(boolean stroke) {
        ViewGroup dot = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.worm_dot_layout, this, false);
        View dotImageView = dot.findViewById(R.id.worm_dot);
        dotImageView.setBackground(
                ContextCompat.getDrawable(getContext(), stroke ? R.drawable.worm_dot_stroke_background : R.drawable.worm_dot_background));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dotImageView.getLayoutParams();
        params.width = params.height = dotsSize;
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

        params.setMargins(dotsSpacing, 0, dotsSpacing, 0);

        setUpDotBackground(stroke, dotImageView);
        return dot;
    }

    public void setUpOnPageChangedListener() {
        pageChangedListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int stepX = dotsSize + dotsSpacing * 2;
                float xFinalPosition;
                float widthFinalPosition;

                if (positionOffset >= 0 && positionOffset < 0.1f) {
                    xFinalPosition = (float) horizontalMargin + position * stepX;
                    widthFinalPosition = dotsSize;
                } else if (positionOffset >= 0.1f && positionOffset <= 0.9f) {
                    xFinalPosition = (float) horizontalMargin + position * stepX;
                    widthFinalPosition = (float) dotsSize + stepX;
                } else {
                    xFinalPosition = (float) horizontalMargin + (position + 1) * stepX;
                    widthFinalPosition = dotsSize;
                }

                if (dotIndicatorXSpring.getSpring().getFinalPosition() != xFinalPosition) {
                    dotIndicatorXSpring.getSpring().setFinalPosition(xFinalPosition);
                }

                if (dotIndicatorWidthSpring.getSpring().getFinalPosition() != widthFinalPosition) {
                    dotIndicatorWidthSpring.getSpring().setFinalPosition(widthFinalPosition);
                }

                if (!dotIndicatorXSpring.isRunning()) {
                    dotIndicatorXSpring.start();
                }

                if (!dotIndicatorWidthSpring.isRunning()) {
                    dotIndicatorWidthSpring.start();
                }
            }

            @Override
            public void onPageSelected(int position) {
                // do nothing
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // do nothing
            }
        };
    }
}
