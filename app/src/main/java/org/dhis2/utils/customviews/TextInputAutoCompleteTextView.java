package org.dhis2.utils.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

public class TextInputAutoCompleteTextView extends AppCompatAutoCompleteTextView {

    public TextInputAutoCompleteTextView(Context context) {
        super(context);
    }

    public TextInputAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextInputAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        final InputConnection ic = super.onCreateInputConnection(outAttrs);
        if (ic != null) {
            if (outAttrs.hintText == null) {
                // If we don't have a hint and our parent is a TextInputLayout, use it's hint for the
                // EditorInfo. This allows us to display a hint in 'extract mode'.
                final ViewParent parent = getParent();
                if (parent instanceof TextInputLayout) {
                    outAttrs.hintText = ((TextInputLayout) parent).getHint();
                }
            }
            outAttrs.imeOptions = outAttrs.imeOptions | EditorInfo.IME_FLAG_NO_FULLSCREEN;
        }
        return ic;
    }


    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            this.clearFocus();
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
