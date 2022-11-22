package org.dhis2.data.forms.dataentry.tablefields.radiobutton;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.dhis2.R;
import org.dhis2.utils.customviews.FieldLayout;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeRenderingType;


public class YesNoView extends FieldLayout {

    private ViewDataBinding binding;
    private boolean hasValue;
    private ConstraintLayout checksLayout;
    private RadioGroup radioGroup;
    private RadioButton yes;
    private RadioButton no;
    private MaterialCheckBox checkYes;
    private MaterialCheckBox checkNo;
    private boolean changingChecks;

    private TextView labelView;
    private View clearButton;
    private LinearLayout checkGroup;
    private SwitchMaterial yesOnlyToggle;
    private RadioGroup.OnCheckedChangeListener radioClickListener;
    private CompoundButton.OnCheckedChangeListener checkBoxClickListener;
    private CompoundButton.OnCheckedChangeListener toggleListener;
    private OnValueChanged valueListener;
    private RadioButtonViewModel viewModel;

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

    public void init(Context context) {
        super.init(context);
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
        if (valueType == ValueType.TRUE_ONLY) {
            no.setVisibility(View.GONE);
            checkNo.setVisibility(View.GONE);
        } else {
            no.setVisibility(View.VISIBLE);
            checkNo.setVisibility(View.VISIBLE);
        }
    }

    public void setRendering(ValueTypeRenderingType rendering) {
        switch (rendering) {
            case HORIZONTAL_CHECKBOXES:
                checksLayout.setVisibility(View.VISIBLE);
                radioGroup.setVisibility(View.GONE);
                checkGroup.setVisibility(View.VISIBLE);
                yesOnlyToggle.setVisibility(View.GONE);
                clearButton.setVisibility(View.GONE);
                checkGroup.setOrientation(LinearLayout.HORIZONTAL);
                radioGroup.setOnCheckedChangeListener(null);
                checkYes.setOnCheckedChangeListener(checkBoxClickListener);
                checkNo.setOnCheckedChangeListener(checkBoxClickListener);
                yesOnlyToggle.setOnCheckedChangeListener(null);
                break;
            case VERTICAL_CHECKBOXES:
                checksLayout.setVisibility(View.VISIBLE);
                radioGroup.setVisibility(View.GONE);
                checkGroup.setVisibility(View.VISIBLE);
                yesOnlyToggle.setVisibility(View.GONE);
                clearButton.setVisibility(View.GONE);
                checkGroup.setOrientation(LinearLayout.VERTICAL);
                radioGroup.setOnCheckedChangeListener(null);
                checkYes.setOnCheckedChangeListener(checkBoxClickListener);
                checkNo.setOnCheckedChangeListener(checkBoxClickListener);
                yesOnlyToggle.setOnCheckedChangeListener(null);
                break;
            case VERTICAL_RADIOBUTTONS:
                checksLayout.setVisibility(View.VISIBLE);
                radioGroup.setVisibility(View.VISIBLE);
                checkGroup.setVisibility(View.GONE);
                yesOnlyToggle.setVisibility(View.GONE);
                clearButton.setVisibility(View.VISIBLE);
                radioGroup.setOrientation(LinearLayout.VERTICAL);
                radioGroup.setOnCheckedChangeListener(radioClickListener);
                checkYes.setOnCheckedChangeListener(null);
                checkNo.setOnCheckedChangeListener(null);
                yesOnlyToggle.setOnCheckedChangeListener(null);
                break;
            case TOGGLE:
                if (valueType == ValueType.TRUE_ONLY) {
                    checksLayout.setVisibility(View.GONE);
                    radioGroup.setVisibility(View.GONE);
                    checkGroup.setVisibility(View.GONE);
                    yesOnlyToggle.setVisibility(View.VISIBLE);
                    clearButton.setVisibility(View.GONE);
                    radioGroup.setOnCheckedChangeListener(null);
                    yesOnlyToggle.setOnCheckedChangeListener(toggleListener);
                } else {
                    checksLayout.setVisibility(View.VISIBLE);
                    radioGroup.setVisibility(View.VISIBLE);
                    checkGroup.setVisibility(View.GONE);
                    yesOnlyToggle.setVisibility(View.GONE);
                    clearButton.setVisibility(View.VISIBLE);
                    radioGroup.setOrientation(LinearLayout.HORIZONTAL);
                    radioGroup.setOnCheckedChangeListener(radioClickListener);
                    yesOnlyToggle.setOnCheckedChangeListener(null);
                }
                checkYes.setOnCheckedChangeListener(null);
                checkNo.setOnCheckedChangeListener(null);
                break;
            default:
            case HORIZONTAL_RADIOBUTTONS:
                checksLayout.setVisibility(View.VISIBLE);
                radioGroup.setVisibility(View.VISIBLE);
                checkGroup.setVisibility(View.GONE);
                yesOnlyToggle.setVisibility(View.GONE);
                clearButton.setVisibility(View.VISIBLE);
                radioGroup.setOrientation(LinearLayout.HORIZONTAL);
                radioGroup.setOnCheckedChangeListener(radioClickListener);
                checkYes.setOnCheckedChangeListener(null);
                checkNo.setOnCheckedChangeListener(null);
                yesOnlyToggle.setOnCheckedChangeListener(null);
                break;
        }
    }

    public void setIsBgTransparent(boolean isBgTransparent) {
        this.isBgTransparent = isBgTransparent;
        setLayout();
    }

    private void setLayout() {
        if (isBgTransparent)
            binding = DataBindingUtil.inflate(inflater, R.layout.yes_no_view_primary, this, true);
        else
            binding = DataBindingUtil.inflate(inflater, R.layout.yes_no_view, this, true);

        checksLayout = findViewById(R.id.checkLayouts);
        radioGroup = findViewById(R.id.radiogroup);
        clearButton = findViewById(R.id.clearSelection);
        yes = findViewById(R.id.yes);
        no = findViewById(R.id.no);
        checkGroup = findViewById(R.id.checkGroup);
        checkYes = findViewById(R.id.checkYes);
        checkNo = findViewById(R.id.checkNo);
        yesOnlyToggle = findViewById(R.id.yesOnlyToggle);

        labelView = findViewById(R.id.label);

        clearButton.setOnClickListener(v -> {
            if (valueListener != null) {
                valueListener.onClearValue();
            }
        });

        radioClickListener = (group, checkedId) -> {
            if (valueListener != null) {
                switch (checkedId) {
                    case R.id.yes:
                        valueListener.onValueChanged(true);
                        break;
                    case R.id.no:
                        valueListener.onValueChanged(false);
                        break;
                    default:
                        valueListener.onClearValue();
                        break;
                }
            }
        };

        checkBoxClickListener = (buttonView, isChecked) -> {
            changingChecks = false;
            switch (buttonView.getId()) {
                case R.id.checkYes:
                    if (isChecked && checkNo.isChecked()) {
                        changingChecks = true;
                        checkNo.setChecked(false);
                    }
                    break;
                case R.id.checkNo:
                    if (isChecked && checkYes.isChecked()) {
                        changingChecks = true;
                        checkYes.setChecked(false);
                    }
                    break;
            }
            if (!changingChecks && valueListener != null) {
                boolean anyCheck = checkYes.isChecked() || checkNo.isChecked();
                if (anyCheck) {
                    valueListener.onValueChanged(checkYes.isChecked());
                } else {
                    valueListener.onClearValue();
                }
            }
        };

        toggleListener = (buttonView, isChecked) -> {
            if (valueListener != null) {
                if (isChecked) {
                    valueListener.onValueChanged(true);
                } else {
                    valueListener.onClearValue();
                }
            }
        };

    }

    public RadioGroup getRadioGroup() {
        return radioGroup;
    }

    public View getClearButton() {
        return clearButton;
    }

    public interface OnValueChanged {
        void onValueChanged(boolean isActive);

        void onClearValue();
    }

    @Override
    protected boolean hasValue() {
        return hasValue;
    }

    @Override
    protected boolean isEditable() {
        return clearButton.isEnabled();
    }
}
