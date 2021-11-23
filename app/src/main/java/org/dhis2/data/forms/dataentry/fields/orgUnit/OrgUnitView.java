package org.dhis2.data.forms.dataentry.fields.orgUnit;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputLayout;

import org.dhis2.BR;
import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.databinding.CustomTextViewAccentBinding;
import org.dhis2.databinding.CustomTextViewBinding;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.commons.dialogs.CustomDialog;
import org.dhis2.utils.customviews.FieldLayout;
import org.dhis2.commons.customviews.TextInputAutoCompleteTextView;
import org.dhis2.utils.customviews.orgUnitCascade.OrgUnitCascadeDialog;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.Bindings.ViewExtensionsKt.closeKeyboard;

public class OrgUnitView extends FieldLayout implements OrgUnitCascadeDialog.CascadeOrgUnitCallbacks, View.OnClickListener {
    private ViewDataBinding binding;

    private ImageView iconView;
    private TextInputAutoCompleteTextView editText;
    private TextInputLayout inputLayout;
    private View descriptionLabel;
    private OnDataChanged listener;
    private String value;
    private TextView labelText;
    private final FragmentManager supportFragmentManager = ((FragmentActivity) getContext()).getSupportFragmentManager();
    private OrgUnitViewModel viewModel;

    public OrgUnitView(Context context) {
        super(context);
        init(context);
    }

    public OrgUnitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OrgUnitView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setLayoutData(boolean isBgTransparent, String renderType) {
        this.isBgTransparent = isBgTransparent;
        if (isBgTransparent)
            binding = CustomTextViewBinding.inflate(inflater, this, true);
        else
            binding = CustomTextViewAccentBinding.inflate(inflater, this, true);

        this.editText = binding.getRoot().findViewById(R.id.input_editText);
        this.iconView = binding.getRoot().findViewById(R.id.renderImage);
        this.inputLayout = binding.getRoot().findViewById(R.id.input_layout);
        this.descriptionLabel = binding.getRoot().findViewById(R.id.descriptionLabel);
        this.labelText = binding.getRoot().findViewById(R.id.label);

        if (renderType != null && !renderType.equals(ProgramStageSectionRenderingType.LISTING.name()))
            iconView.setVisibility(View.VISIBLE);


        editText.setFocusable(false);

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                editText.performClick();
            }
        });

        editText.setOnClickListener(this);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        editText.setOnClickListener(l);
    }

    public void setObjectStyle(ObjectStyle objectStyle) {
        Bindings.setObjectStyle(iconView, this, objectStyle);
    }

    public void updateEditable(boolean isEditable) {
        editText.setEnabled(isEditable);
        editText.setFocusable(false);
        editText.setClickable(isEditable);
        editText.setTextColor(
                !isBgTransparent ? ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.ACCENT) :
                        ContextCompat.getColor(getContext(), R.color.textPrimary)
        );
        setEditable(isEditable,
                findViewById(R.id.label),
                inputLayout,
                descriptionLabel);
    }

    public void setValue(String valueUid, String valueName) {
        value = valueUid;
        new Handler().postDelayed(() -> editText.setText(valueName), 100);

    }

    public TextInputAutoCompleteTextView getEditText() {
        return editText;
    }

    public void setWarning(String warning, String error) {
        if (!isEmpty(warning)) {
            inputLayout.setErrorTextAppearance(R.style.warning_appearance);
            inputLayout.setError(warning);
        } else if (!isEmpty(error)) {
            inputLayout.setErrorTextAppearance(R.style.error_appearance);
            inputLayout.setError(error);
        } else
            inputLayout.setError(null);
    }

    public void setLabel(String label) {
        if (inputLayout.getHint() == null || !inputLayout.getHint().toString().equals(label)) {
            this.label = label;
            inputLayout.setHint(this.label);
            binding.setVariable(BR.label, label);
        }

        binding.setVariable(BR.fieldHint, getContext().getString(R.string.choose_ou));
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
    public void textChangedConsumer(String selectedOrgUnitUid, String selectedOrgUnitName) {
        editText.setText(selectedOrgUnitName);
        listener.onDataChanged(selectedOrgUnitUid, selectedOrgUnitName);
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

    @Override
    public void onDialogCancelled() {

    }

    @Override
    public void onClear() {

    }

    @Override
    public void onClick(View v) {
        requestFocus();
        closeKeyboard(v);
        viewModel.onItemClick();

        new OrgUnitCascadeDialog(label, value, new OrgUnitCascadeDialog.CascadeOrgUnitCallbacks() {
            @Override
            public void textChangedConsumer(String selectedOrgUnitUid, String selectedOrgUnitName) {
                listener.onDataChanged(selectedOrgUnitUid, selectedOrgUnitName);
                editText.setText(selectedOrgUnitName);
                editText.setEnabled(true);
            }

            @Override
            public void onDialogCancelled() {
                editText.setEnabled(true);
            }

            @Override
            public void onClear() {
                listener.onDataChanged(null, null);
                editText.setText(null);
                editText.setEnabled(true);
            }
        }, OrgUnitCascadeDialog.OUSelectionType.SEARCH).show(supportFragmentManager, label);
    }

    public void setListener(OnDataChanged listener) {
        this.listener = listener;
    }

    public interface OnDataChanged {
        void onDataChanged(String orgUnitUid, String orgUnitName);
    }

    public void setViewModel(OrgUnitViewModel viewModel) {
        this.viewModel = viewModel;
        if (binding == null) {
            setLayoutData(viewModel.isBackgroundTransparent(), viewModel.renderType());
        }
        setLabel(viewModel.getFormattedLabel());
        setDescription(viewModel.description());
        setWarning(viewModel.warning(), viewModel.error());

        setValue(viewModel.value(), viewModel.displayName());
        getEditText().setText(viewModel.displayName());

        updateEditable(viewModel.editable());
        setListener(viewModel::onDataChange);
    }
}
