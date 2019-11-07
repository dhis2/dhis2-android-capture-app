package org.dhis2.utils.customviews;

import android.content.Context;
import android.util.AttributeSet;

import org.dhis2.databinding.FormUnsupportedBinding;

public class UnsupportedView extends FieldLayout {

    private FormUnsupportedBinding binding;

    public UnsupportedView(Context context) {
        super(context);
        init(context);
        setLayout();
    }

    public UnsupportedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        setLayout();
    }

    public UnsupportedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        setLayout();
    }

    private void setLayout() {
        binding = FormUnsupportedBinding.inflate(inflater, this, true);
    }

    public void setLabel(String label) {
        binding.formButton.setText(label);
    }

}
