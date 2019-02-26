package org.dhis2.utils.custom_views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.textfield.TextInputEditText;

import org.dhis2.BR;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.datetime.OnDateSelected;

import java.util.Date;

import androidx.databinding.ViewDataBinding;

public abstract class GlobalDateView extends FieldLayout implements View.OnClickListener {

    protected ViewDataBinding binding;
    protected String label;
    protected Date date;
    protected TextInputEditText editText;
    protected OnDateSelected listener;

    public GlobalDateView(Context context) {
        super(context);
    }

    public GlobalDateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GlobalDateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void performOnFocusAction() {
        editText.performClick();
    }

    public void setUpEditText() {
        editText = findViewById(R.id.inputEditText);
        editText.setFocusable(false); //Makes editText not editable
        editText.setClickable(true);//  but clickable
        editText.setOnFocusChangeListener(this::onFocusChanged);
        editText.setOnClickListener(this);
    }

    public void setIsBgTransparent(boolean isBgTransparent) {
        this.isBgTransparent = isBgTransparent;
        setLayout();
    }

    public void setLabel(String label) {
        this.label = label;
        binding.setVariable(BR.label, label);
        binding.executePendingBindings();
    }

    public void setDescription(String description) {
        binding.setVariable(BR.description, description);
        binding.executePendingBindings();
    }


    public void onFocusChanged(View view, boolean b) {
        if (b)
            onClick(view);
    }

    @Override
    public void onClick(View view) {
        onClick();
    }

    public void setWarningOrError(String msg) {
        editText.setError(msg);
    }

    public void setDateListener(OnDateSelected listener) {
        this.listener = listener;
    }

    public void setEditable(Boolean editable) {
        editText.setEnabled(editable);
    }

    public abstract void onClick();

    public abstract void setLayout();
}
