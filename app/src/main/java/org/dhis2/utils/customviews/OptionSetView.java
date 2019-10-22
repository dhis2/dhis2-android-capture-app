package org.dhis2.utils.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;

import com.google.android.material.textfield.TextInputLayout;

import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.databinding.CustomCellViewBinding;
import org.dhis2.databinding.FormSpinnerAccentBinding;
import org.dhis2.databinding.FormSpinnerBinding;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetTableAdapter;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import static android.text.TextUtils.isEmpty;

public class OptionSetView extends FieldLayout implements OptionSetOnClickListener {
    private ViewDataBinding binding;

    private ImageView iconView;
    private TextView editText;
    private TextInputLayout inputLayout;
    private View descriptionLabel;
    private View delete;
    private OnSelectedOption listener;
    private int numberOfOptions = 0;

    public OptionSetView(Context context) {
        super(context);
        init(context);
    }

    public OptionSetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OptionSetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setLayoutData(boolean isBgTransparent, String renderType) {
        if (isBgTransparent)
            binding = FormSpinnerBinding.inflate(inflater, this, true);
        else
            binding = FormSpinnerAccentBinding.inflate(inflater, this, true);

        this.editText = binding.getRoot().findViewById(R.id.input_editText);
        this.iconView = binding.getRoot().findViewById(R.id.renderImage);
        this.inputLayout = binding.getRoot().findViewById(R.id.input_layout);
        this.descriptionLabel = binding.getRoot().findViewById(R.id.descriptionLabel);
        this.delete = binding.getRoot().findViewById(R.id.delete);

        if (renderType != null && !renderType.equals(ProgramStageSectionRenderingType.LISTING.name()))
            iconView.setVisibility(View.VISIBLE);


        editText.setFocusable(false);

        delete.setOnClickListener(view -> deleteSelectedOption());

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                editText.performClick();
        });

    }
    public void setCellLayout(ObservableField<DataSetTableAdapter.TableScale> tableScale){
        binding = DataBindingUtil.inflate(inflater, R.layout.custom_cell_view, this, true);
        ((CustomCellViewBinding)binding).setTableScale(tableScale);
        editText = findViewById(R.id.inputEditText);
        editText.setFocusable(false); //Makes editText not editable
        editText.setClickable(true);//  but clickable

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                editText.performClick();
        });
    }


    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        editText.setOnClickListener(l);
    }

    public void setNumberOfOptions(int numberOfOptions) {
        this.numberOfOptions = numberOfOptions;
    }

    public void deleteSelectedOption() {
        setValueOption(null, null);
        if(delete!=null)
            delete.setVisibility(View.GONE);
    }

    public void setOnSelectedOptionListener(OnSelectedOption listener) {
        this.listener = listener;
    }

    @Override
    public void onSelectOption(Option option) {
        setValueOption(option.displayName(), option.code());
    }

    private void setValueOption(String optionDisplayName, String optionCode) {

        editText.setText(optionDisplayName);

        if(delete!=null) {
            if (optionDisplayName != null && !optionDisplayName.isEmpty()) {
                delete.setVisibility(View.VISIBLE);
            } else {
                delete.setVisibility(View.GONE);
            }
        }

        listener.onSelectedOption(optionDisplayName, optionCode);

    }

    public void setObjectStyle(ObjectStyle objectStyle) {
        Bindings.setObjectStyle(iconView, this, objectStyle);
    }

    public void updateEditable(boolean isEditable) {
        editText.setEnabled(isEditable);
        editText.setFocusable(false);
        editText.setClickable(isEditable);
    }

    public void setValue(String value) {
        if (value != null && value.contains("_os_"))
            value = value.split("_os_")[0];

        editText.setText(value);

        if (delete!=null && editText.getText() != null && !editText.getText().toString().isEmpty()) {
            delete.setVisibility(View.VISIBLE);
        }
    }

    public void setWarning(String warning, String error) {
        if (!isEmpty(warning)) {
            inputLayout.setErrorTextAppearance(R.style.warning_appearance);
            inputLayout.setError(warning);
        } else if (!isEmpty(error)) {
            inputLayout.setErrorTextAppearance(R.style.error_appearance);
            inputLayout.setError(error);
            editText.setText("");
        } else
            inputLayout.setError(null);
    }

    public void setLabel(String label, boolean mandatory) {
        if (inputLayout.getHint() == null || !inputLayout.getHint().toString().equals(label)) {
            StringBuilder labelBuilder = new StringBuilder(label);
            if (mandatory)
                labelBuilder.append("*");
            this.label = labelBuilder.toString();
            inputLayout.setHint(this.label);
        }
    }

    public void setDescription(String description) {
        descriptionLabel.setVisibility(label.length() > 16 || description != null ? View.VISIBLE : View.GONE);
    }

    public boolean openOptionDialog() {
        return numberOfOptions > getContext().getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE).getInt(Constants.OPTION_SET_DIALOG_THRESHOLD, 15);
    }

    public interface OnSelectedOption {
        void onSelectedOption(String optionName, String optionCode);
    }
}
