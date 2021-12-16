package org.dhis2.data.forms.dataentry.fields.optionset;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.databinding.ViewDataBinding;
import androidx.databinding.library.baseAdapters.BR;

import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.databinding.FormSpinnerSelectionAccentBinding;
import org.dhis2.databinding.FormSpinnerSelectionBinding;
import org.dhis2.databinding.OptionSetSelectCheckItemBinding;
import org.dhis2.databinding.OptionSetSelectItemBinding;
import org.dhis2.utils.Constants;
import org.dhis2.utils.customviews.CustomDialog;
import org.dhis2.utils.customviews.FieldLayout;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueTypeRenderingType;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import java.util.List;

import static android.text.TextUtils.isEmpty;

public class OptionSetSelectionView extends FieldLayout {
    private ViewDataBinding binding;

    private List<Option> options;
    private ImageView iconView;
    private View descriptionLabel;
    private View delete;
    private OnSelectedOption listener;
    private RadioGroup radioGroup;
    private LinearLayout checkGroup;
    private TextView warningErrorMessage;
    private TextView labelView;
    private ValueTypeRenderingType renderingType;
    private String currentCodeValue;
    private boolean isEditable;
    private List<String> optionsToHide;
    private List<String> optionsToShow;
    private final int DEFAULT_MAX_CHAR_LENGHT = 16;

    public OptionSetSelectionView(Context context) {
        super(context);
        init(context);
    }

    public OptionSetSelectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OptionSetSelectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setLayoutData(boolean isBgTransparent, String renderType) {
        this.isBgTransparent = isBgTransparent;
        if (isBgTransparent)
            binding = FormSpinnerSelectionBinding.inflate(inflater, this, true);
        else
            binding = FormSpinnerSelectionAccentBinding.inflate(inflater, this, true);

        this.labelView = binding.getRoot().findViewById(R.id.label);
        this.iconView = binding.getRoot().findViewById(R.id.renderImage);
        this.descriptionLabel = binding.getRoot().findViewById(R.id.descriptionLabel);
        this.delete = binding.getRoot().findViewById(R.id.delete);
        this.radioGroup = binding.getRoot().findViewById(R.id.radioLayout);
        this.checkGroup = binding.getRoot().findViewById(R.id.checkLayout);
        this.warningErrorMessage = binding.getRoot().findViewById(R.id.warningError);

        if (renderType != null && !renderType.equals(ProgramStageSectionRenderingType.LISTING.name()))
            iconView.setVisibility(View.VISIBLE);

        delete.setOnClickListener(view -> deleteSelectedOption());
    }

    public void deleteSelectedOption() {
        if (listener != null) {
            listener.onOptionsClear();
        }
    }

    public void setOnSelectedOptionListener(OnSelectedOption listener) {
        this.listener = listener;
    }

    public void setObjectStyle(ObjectStyle objectStyle) {
        Bindings.setObjectStyle(iconView, this, objectStyle);
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
        if (delete != null) {
            delete.setEnabled(isEditable);
            delete.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        }

        setEditable(isEditable,
                labelView,
                descriptionLabel,
                delete,
                radioGroup,
                checkGroup
        );
    }

    public void setOptionsToShow(List<String> optionsToHide, List<String> optionsToShow) {
        this.optionsToHide = optionsToHide;
        this.optionsToShow = optionsToShow;
    }

    public void setInitValue(String value, List<Option> options, ValueTypeRenderingType renderingType) {
        this.renderingType = renderingType;
        this.options = options;
        this.currentCodeValue = null;
        if (value != null && value.contains("_os_")) {
            currentCodeValue = value.split("_os_")[1];
        } else {
            currentCodeValue = value;
        }

        switch (renderingType) {
            case VERTICAL_CHECKBOXES:
                checkGroup.setOrientation(LinearLayout.VERTICAL);
                radioGroup.setVisibility(View.GONE);
                checkGroup.setVisibility(View.VISIBLE);
                delete.setVisibility(GONE);
                setCheckOptions();
                break;
            case HORIZONTAL_CHECKBOXES:
                checkGroup.setOrientation(LinearLayout.HORIZONTAL);
                radioGroup.setVisibility(View.GONE);
                checkGroup.setVisibility(View.VISIBLE);
                delete.setVisibility(GONE);
                setCheckOptions();
                break;
            case VERTICAL_RADIOBUTTONS:
                radioGroup.setOrientation(LinearLayout.VERTICAL);
                checkGroup.setVisibility(View.GONE);
                radioGroup.setVisibility(View.VISIBLE);
                delete.setVisibility(value != null ? VISIBLE : GONE);
                setRadioOptions();
                break;
            case HORIZONTAL_RADIOBUTTONS:
                radioGroup.setOrientation(LinearLayout.HORIZONTAL);
                checkGroup.setVisibility(View.GONE);
                radioGroup.setVisibility(View.VISIBLE);
                delete.setVisibility(value != null ? VISIBLE : GONE);
                setRadioOptions();
                break;
        }
    }

    private void setCheckOptions() {
        checkGroup.removeAllViews();
        for (Option option : options) {
            if (canShowOption(option.uid())) {
                OptionSetSelectCheckItemBinding optionBinding = OptionSetSelectCheckItemBinding.inflate(inflater, checkGroup, false);
                optionBinding.setEditable(isEditable);
                optionBinding.setOptionName(option.displayName());
                optionBinding.setVariable(BR.isBgTransparent, isBgTransparent);
                optionBinding.checkBox.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f));
                optionBinding.checkBox.setChecked(currentCodeValue != null && (currentCodeValue.equals(option.displayName()) || currentCodeValue.equals(option.name())));
                optionBinding.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        listener.onSelectedOption(option.code());
                    } else if (currentCodeValue.equals(option.displayName())) {
                        listener.onOptionsClear();
                    }
                });
                checkGroup.addView(optionBinding.getRoot());
            }
        }
        invalidate();
    }

    private void setRadioOptions() {
        radioGroup.removeAllViews();
        for (Option option : options) {
            if (canShowOption(option.uid())) {
                OptionSetSelectItemBinding optionBinding = OptionSetSelectItemBinding.inflate(inflater, radioGroup, false);
                optionBinding.setEditable(isEditable);
                optionBinding.setOptionName(option.displayName());
                optionBinding.setVariable(BR.isBgTransparent, isBgTransparent);
                optionBinding.radio.setLayoutParams(new RadioGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f));
                optionBinding.radio.setChecked(currentCodeValue != null && (currentCodeValue.equals(option.displayName()) || currentCodeValue.equals(option.name())));
                optionBinding.radio.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        listener.onSelectedOption(option.code());
                    }
                });
                radioGroup.addView(optionBinding.getRoot());
            }
        }
        invalidate();
    }

    private boolean canShowOption(String optionUid) {
        boolean inOptionsToShow = optionsToShow.contains(optionUid);
        boolean inOptionsToHide = optionsToHide.contains(optionUid);
        if (!optionsToShow.isEmpty()) {
            return inOptionsToShow;
        } else {
            return !inOptionsToHide;
        }
    }

    public void setWarning(String warning, String error) {
        if (!isEmpty(warning)) {
            warningErrorMessage.setTextColor(ContextCompat.getColor(getContext(), R.color.warning_color));
            warningErrorMessage.setVisibility(View.VISIBLE);
        } else if (!isEmpty(error)) {
            warningErrorMessage.setText(error);
            warningErrorMessage.setTextColor(ContextCompat.getColor(getContext(), R.color.error_color));
            warningErrorMessage.setVisibility(View.VISIBLE);
        } else {
            warningErrorMessage.setVisibility(View.GONE);
        }
    }

    public void setLabel(String label) {
        this.label = label;
        binding.setVariable(BR.label, label);
    }

    public void setDescription(String description) {
        descriptionLabel.setVisibility(label.length() > DEFAULT_MAX_CHAR_LENGHT || description != null ? View.VISIBLE : View.GONE);
        descriptionLabel.setOnClickListener(v ->
                new CustomDialog(
                        getContext(),
                        label,
                        description != null ? description : getContext().getString(R.string.empty_description),
                        getContext().getString(R.string.action_close),
                        null,
                        Constants.DESCRIPTION_DIALOG,
                        null
                ).show());
    }

    public interface OnSelectedOption {
        void onSelectedOption(String optionCode);

        void onOptionsClear();
    }

    public void setViewModel(OptionSetViewModel viewModel) {
        if (binding == null) {
            setLayoutData(viewModel.isBackgroundTransparent(), viewModel.renderType());
        }
        setOnSelectedOptionListener(new OnSelectedOption() {
            @Override
            public void onSelectedOption(String optionCode) {
                viewModel.onOptionSelected(optionCode);
            }

            @Override
            public void onOptionsClear() {
                viewModel.onOptionSelected(null);
            }
        });
        setOptionsToShow(
                viewModel.getOptionsToHide(), viewModel.getOptionsToShow()
        );
        setObjectStyle(viewModel.objectStyle());
        setEditable(viewModel.editable());
        setInitValue(viewModel.value(), viewModel.options(), viewModel.fieldRendering().type());
        setLabel(viewModel.getFormattedLabel());
        String description = viewModel.description();

        if (viewModel.url() != null){
            description = description + "\n" + viewModel.url()  ;
        }

        setDescription(description);
    }
}
