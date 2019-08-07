package org.dhis2.utils.custom_views;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputLayout;

import org.dhis2.BR;
import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.databinding.CustomTextViewAccentBinding;
import org.dhis2.databinding.CustomTextViewBinding;
import org.dhis2.utils.custom_views.orgUnitCascade.OrgUnitCascadeDialog;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import static android.text.TextUtils.isEmpty;

public class OrgUnitView extends FieldLayout implements OrgUnitCascadeDialog.CascadeOrgUnitCallbacks {
    private ViewDataBinding binding;

    private ImageView iconView;
    private TextInputAutoCompleteTextView editText;
    private TextInputLayout inputLayout;
    private View descriptionLabel;
    private OnDataChanged listener;
    private String value;
    private FragmentManager fm;

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
        if (isBgTransparent)
            binding = CustomTextViewBinding.inflate(inflater, this, true);
        else
            binding = CustomTextViewAccentBinding.inflate(inflater, this, true);

        this.editText = binding.getRoot().findViewById(R.id.input_editText);
        this.iconView = binding.getRoot().findViewById(R.id.renderImage);
        this.inputLayout = binding.getRoot().findViewById(R.id.input_layout);
        this.descriptionLabel = binding.getRoot().findViewById(R.id.descriptionLabel);

        if (renderType != null && !renderType.equals(ProgramStageSectionRenderingType.LISTING.name()))
            iconView.setVisibility(View.VISIBLE);


        editText.setFocusable(false);

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                activate();
                editText.performClick();
            }
        });

        editText.setOnClickListener(v -> new OrgUnitCascadeDialog(label, value, new OrgUnitCascadeDialog.CascadeOrgUnitCallbacks() {
            @Override
            public void textChangedConsumer(String selectedOrgUnitUid, String selectedOrgUnitName) {
                listener.onDataChanged(selectedOrgUnitUid);
                editText.setText(selectedOrgUnitName);
                editText.setEnabled(true);
            }

            @Override
            public void onDialogCancelled() {
                editText.setEnabled(true);
            }

            @Override
            public void onClear() {
                listener.onDataChanged(null);
                editText.setText(null);
                editText.setEnabled(true);
            }
        }, OrgUnitCascadeDialog.OUSelectionType.SEARCH).show(fm, label));
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

    public void setLabel(String label, boolean mandatory) {
        if (inputLayout.getHint() == null || !inputLayout.getHint().toString().equals(label)) {
            StringBuilder labelBuilder = new StringBuilder(label);
            if (mandatory)
                labelBuilder.append("*");
            this.label = labelBuilder.toString();
            inputLayout.setHint(this.label);
            binding.setVariable(BR.label, this.label);
        }
    }

    public void setDescription(String description) {
        descriptionLabel.setVisibility(label.length() > 16 || description != null ? View.VISIBLE : View.GONE);
    }

    @Override
    public void textChangedConsumer(String selectedOrgUnitUid, String selectedOrgUnitName) {
        editText.setText(selectedOrgUnitName);
        listener.onDataChanged(selectedOrgUnitUid);
    }

    @Override
    public void onDialogCancelled() {

    }

    @Override
    public void onClear() {

    }

    public void setListener(OnDataChanged listener) {
        this.listener = listener;
    }

    public void setFragmentManager(FragmentManager fm) {
        this.fm = fm;
    }

    public interface OnDataChanged {
        void onDataChanged(String orgUnitUid);
    }
}
