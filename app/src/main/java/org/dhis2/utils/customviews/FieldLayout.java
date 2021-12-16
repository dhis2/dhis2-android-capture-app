package org.dhis2.utils.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import org.hisp.dhis.android.core.common.ValueType;

/**
 * QUADRAM. Created by ppajuelo on 29/01/2019.
 */
public abstract class FieldLayout extends RelativeLayout {

    protected boolean isBgTransparent;
    protected LayoutInflater inflater;
    protected String label;
    protected ValueType valueType;


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

    public void nextFocus(View view) {
        View nextView;
        if ((nextView = view.focusSearch(FOCUS_DOWN)) != null)
            nextView.requestFocus();
    }

    public void setEditable(boolean editable, View... views) {
        for (View view : views) {
            if(view!=null) {
                view.setAlpha(editable ? 1 : 0.5f);
            }
        }
    }

    protected void updateDeleteVisibility(View clearButton) {
        if (clearButton != null) {
            if(hasValue() && isEditable()){
                clearButton.setVisibility(View.VISIBLE);
            }else{
                clearButton.setVisibility(View.GONE);
            }
        }
    }

    protected boolean hasValue(){
        return false;
    }

    protected boolean isEditable(){
        return false;
    }
}

