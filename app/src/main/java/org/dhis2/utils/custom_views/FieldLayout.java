package org.dhis2.utils.custom_views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import org.dhis2.utils.ColorUtils;

import androidx.annotation.Nullable;

/**
 * QUADRAM. Created by ppajuelo on 29/01/2019.
 */
public abstract class FieldLayout extends RelativeLayout {

    protected boolean isBgTransparent;
    protected LayoutInflater inflater;
    protected String label;


    public FieldLayout(Context context) {
        super(context);
    }

    public FieldLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FieldLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(Context context) {
        setFocusable(true);
        setClickable(true);
        setFocusableInTouchMode(true);
        inflater = LayoutInflater.from(context);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

    }

    public void nextFocus(View view) {
        View nextView;
        if ((nextView = view.focusSearch(FOCUS_DOWN)) != null)
            nextView.requestFocus();
    }
}
