package org.dhis2.data.forms.dataentry.fields.spinner;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;
import androidx.databinding.library.baseAdapters.BR;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.textfield.TextInputLayout;

import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.databinding.CustomCellViewBinding;
import org.dhis2.databinding.FormSpinnerAccentBinding;
import org.dhis2.databinding.FormSpinnerBinding;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetTableAdapter;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.customviews.CustomDialog;
import org.dhis2.utils.customviews.FieldLayout;
import org.dhis2.utils.customviews.OptionSetOnClickListener;
import org.dhis2.utils.customviews.OptionSetPopUp;
import org.dhis2.utils.optionset.OptionSetDialog;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.Bindings.ViewExtensionsKt.closeKeyboard;

public class OptionSetView extends FieldLayout implements OptionSetOnClickListener, View.OnClickListener {
    private ViewDataBinding binding;

    private ImageView iconView;
    private TextView editText;
    private TextInputLayout inputLayout;
    private View descriptionLabel;
    private View delete;
    private OnSelectedOption listener;
    private TextView labelText;
    private SpinnerViewModel viewModel;

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
        this.isBgTransparent = isBgTransparent;
        if (isBgTransparent)
            binding = FormSpinnerBinding.inflate(inflater, this, true);
        else
            binding = FormSpinnerAccentBinding.inflate(inflater, this, true);

        this.editText = binding.getRoot().findViewById(R.id.input_editText);
        this.iconView = binding.getRoot().findViewById(R.id.renderImage);
        this.inputLayout = binding.getRoot().findViewById(R.id.input_layout);
        this.descriptionLabel = binding.getRoot().findViewById(R.id.descriptionLabel);
        this.delete = binding.getRoot().findViewById(R.id.delete);
        this.labelText = binding.getRoot().findViewById(R.id.label);

        if (renderType != null && !renderType.equals(ProgramStageSectionRenderingType.LISTING.name()))
            iconView.setVisibility(View.VISIBLE);


        editText.setFocusable(false);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        delete.setOnClickListener(view -> {
            closeKeyboard(this);
            viewModel.onItemClick();
            deleteSelectedOption();
        });

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                editText.performClick();
        });

    }

    public void setCellLayout(ObservableField<DataSetTableAdapter.TableScale> tableScale) {
        binding = DataBindingUtil.inflate(inflater, R.layout.custom_cell_view, this, true);
        isBgTransparent = true;
        ((CustomCellViewBinding) binding).setTableScale(tableScale);
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

    public void deleteSelectedOption() {
        setValueOption(null, null);
    }

    public void setOnSelectedOptionListener(OnSelectedOption listener) {
        this.listener = listener;
    }

    @Override
    public void onSelectOption(Option option) {
        setValueOption(option.displayName(), option.code());
    }

    @Override
    public void dispatchSetActivated(boolean activated) {
        super.dispatchSetActivated(activated);
        if (activated) {
            labelText.setTextColor(ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY));
        } else {
            labelText.setTextColor(ResourcesCompat.getColor(getResources(), R.color.textPrimary, null));
        }
    }

    private void setValueOption(String optionDisplayName, String optionCode) {
        editText.setText(optionDisplayName);
        updateDeleteVisibility(delete);
        listener.onSelectedOption(optionDisplayName, optionCode);
    }

    public void setObjectStyle(ObjectStyle objectStyle) {
        Bindings.setObjectStyle(iconView, this, objectStyle);
    }

    public void updateEditable(boolean isEditable) {
        editText.setEnabled(isEditable);
        editText.setFocusable(false);
        editText.setClickable(isEditable);
        if (delete != null) {
            delete.setEnabled(isEditable);
        }
        editText.setTextColor(
                !isBgTransparent ? ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.ACCENT) :
                        ContextCompat.getColor(getContext(), R.color.textPrimary)
        );
        setEditable(isEditable,
                inputLayout,
                descriptionLabel,
                labelText,
                delete
        );
    }

    public void setValue(String value) {
        if (value != null && value.contains("_os_"))
            value = value.split("_os_")[0];

        if (inputLayout != null) {
            inputLayout.setHintAnimationEnabled(false);
        }
        editText.setText(value);
        if (inputLayout != null) {
            inputLayout.setHintAnimationEnabled(true);
        }

        updateDeleteVisibility(delete);
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

    public void setLabel(String label) {
        if (inputLayout.getHint() == null || !inputLayout.getHint().toString().equals(label)) {
            this.label = label;
            binding.setVariable(BR.label, label);
        }
    }

    public void setDescription(String description) {
        descriptionLabel.setVisibility(description != null ? View.VISIBLE : View.GONE);
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

    @Override
    public void onClick(View v) {
        requestFocus();
        closeKeyboard(v);
        viewModel.onItemClick();
        OptionSetDialog dialog = new OptionSetDialog();
        dialog.create(getContext());
        dialog.setOptionSet(viewModel);

        if (dialog.showDialog()) {
            dialog.setListener(this);
            dialog.setClearListener((view) -> deleteSelectedOption());
            dialog.show(((FragmentActivity) binding.getRoot().getContext()).getSupportFragmentManager(), OptionSetDialog.Companion.getTAG());
        } else {
            dialog.dismiss();
            new OptionSetPopUp(getContext(), v, viewModel,
                    this);
        }
    }

    public interface OnSelectedOption {
        void onSelectedOption(String optionName, String optionCode);
    }

    @Override
    protected boolean hasValue() {
        return editText.getText() != null && !editText.getText().toString().isEmpty();
    }

    @Override
    protected boolean isEditable() {
        return editText.isEnabled();
    }

    public void setBackgroundColor(@ColorInt int color) {
        inputLayout.setBackgroundColor(color);
    }

    public void setViewModel(SpinnerViewModel viewModel) {
        this.viewModel = viewModel;
        if (binding == null) {
            setLayoutData(viewModel.isBackgroundTransparent(), viewModel.renderType());
        }
        binding.setVariable(BR.legend, viewModel.legendValue());
        setOnSelectedOptionListener(viewModel::onOptionSelected);
        updateEditable(viewModel.editable());
        setValue(viewModel.value());
        setWarning(viewModel.warning(), viewModel.error());
        setLabel(viewModel.getFormattedLabel());
        String description = viewModel.description();

        if (viewModel.url() != null){
            description = description + "\n" + viewModel.url()  ;
        }

        setDescription(description);
        setOnClickListener(this);
    }
}
