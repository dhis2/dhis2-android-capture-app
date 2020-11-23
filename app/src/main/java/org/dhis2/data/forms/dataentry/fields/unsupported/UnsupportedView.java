package org.dhis2.data.forms.dataentry.fields.unsupported;

import android.content.Context;
import android.util.AttributeSet;

import org.dhis2.databinding.FormUnsupportedBinding;
import org.dhis2.utils.customviews.FieldLayout;

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

    public void setViewModel(UnsupportedViewModel viewModel) {
        setLabel(viewModel.label());
    }
}
