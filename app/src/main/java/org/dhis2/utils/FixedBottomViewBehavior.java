package org.dhis2.utils;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

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
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        //making our bottom view depends on ViewPager (or whatever that have appbar_scrolling_view_behavior behavior)
        return dependency instanceof NestedScrollView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        if (ViewCompat.isLaidOut(parent)) {
            //attach our bottom view to the bottom of CoordinatorLayout
            int bottomMargin = ((ViewGroup.MarginLayoutParams) child.getLayoutParams()).bottomMargin;
            int xDelta = 0;
            if (dependency.getTop() > parent.getBottom() - child.getHeight() - 2 * bottomMargin - 150) {
                child.setY(dependency.getTop() + bottomMargin);
                xDelta = dependency.getTop() - parent.getBottom() + child.getWidth() + 2 * bottomMargin +150;
            } else {
                child.setY(parent.getBottom() - child.getHeight() - bottomMargin - 150);
                xDelta = 0;
            }

            child.setX(parent.getRight() - child.getWidth() - bottomMargin + xDelta);


        }
        return false;
    }
}
