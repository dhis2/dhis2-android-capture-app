package org.dhis2.utils;

import android.content.Context;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

/**
 * QUADRAM. Created by ppajuelo on 19/04/2018.
 */

public class FixedBottomViewBehavior extends CoordinatorLayout.Behavior<View> {

    public FixedBottomViewBehavior() {
    }

    public FixedBottomViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(@NotNull CoordinatorLayout parent, @NotNull View child, @NotNull View dependency) {
        //making our bottom view depends on ViewPager (or whatever that have appbar_scrolling_view_behavior behavior)
        return dependency instanceof NestedScrollView;
    }

    @Override
    public boolean onDependentViewChanged(@NotNull CoordinatorLayout parent, @NotNull View child, @NotNull View dependency) {
        if (ViewCompat.isLaidOut(parent)) {
            //attach our bottom view to the bottom of CoordinatorLayout
            int bottomMargin = ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).bottomMargin;
            float xDelta = 0f;
            if (dependency.getTop() > parent.getBottom() - child.getHeight() - 2f * bottomMargin - 150f) {
                child.setY(dependency.getTop() + (float) bottomMargin);
                xDelta = dependency.getTop() - parent.getBottom() + child.getWidth() + 2f * bottomMargin + 150f;
            } else {
                child.setY(parent.getBottom() - child.getHeight() - bottomMargin - 150f);
            }

            child.setX(parent.getRight() - child.getWidth() - bottomMargin + xDelta);


        }
        return false;
    }
}
