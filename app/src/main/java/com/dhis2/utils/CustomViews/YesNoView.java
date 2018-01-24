package com.dhis2.utils.CustomViews;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.dhis2.R;
import com.dhis2.databinding.YesNoViewBinding;

import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;


/**
 * Created by frodriguez on 1/24/2018.
 */

public class YesNoView extends RelativeLayout implements RadioGroup.OnCheckedChangeListener {

    private YesNoViewBinding binding;

    private RadioGroup radioGroup;
    private RadioButton yes;
    private RadioButton no;
    private RadioButton no_value;

    public YesNoView(Context context) {
        super(context);
        init(context);
    }

    public YesNoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public YesNoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = YesNoViewBinding.inflate(inflater, this, true);
        radioGroup = findViewById(R.id.radiogroup);
        yes  = findViewById(R.id.yes);
        no = findViewById(R.id.no);
        no_value = findViewById(R.id.no_value);
        radioGroup.setOnCheckedChangeListener(this);
    }

    public void setAttribute(TrackedEntityAttributeModel attribute){
        binding.setAttribute(attribute);
        if(attribute.valueType() == ValueType.TRUE_ONLY)
            no.setVisibility(View.GONE);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {

    }
}
