package org.dhis2.data.forms.dataentry.fields.spinner;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.Constants;
import org.dhis2.utils.custom_views.OptionSetDialog;
import org.dhis2.utils.custom_views.OptionSetOnClickListener;
import org.dhis2.utils.custom_views.OptionSetPopUp;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;

import java.util.Map;

import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentActivity;
import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 07/11/2017.
 */

public class SpinnerHolder extends FormViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, OptionSetOnClickListener {

    private final FlowableProcessor<RowAction> processor;
    private final FlowableProcessor<Trio<String, String, Integer>> processorOptionSet;
    private final ImageView iconView;
    private final TextInputEditText editText;
    private final TextInputLayout inputLayout;
    private final View descriptionLabel;
    private final View delete;
    private final boolean isSearchMode;

    private SpinnerViewModel viewModel;
    private int numberOfOptions = 0;
    private Map<String, OptionModel> options;

    SpinnerHolder(ViewDataBinding mBinding, FlowableProcessor<RowAction> processor, FlowableProcessor<Trio<String, String, Integer>> processorOptionSet, String renderType, boolean isSearchMode) {
        super(mBinding);
        this.editText = mBinding.getRoot().findViewById(R.id.input_editText);
        this.iconView = mBinding.getRoot().findViewById(R.id.renderImage);
        this.inputLayout = mBinding.getRoot().findViewById(R.id.input_layout);
        this.descriptionLabel = mBinding.getRoot().findViewById(R.id.descriptionLabel);
        this.delete = mBinding.getRoot().findViewById(R.id.delete);
        this.processor = processor;
        this.processorOptionSet = processorOptionSet;
        this.isSearchMode = isSearchMode;

        if (renderType != null && !renderType.equals(ProgramStageSectionRenderingType.LISTING.name()))
            iconView.setVisibility(View.VISIBLE);

        editText.setOnClickListener(this);

        delete.setOnClickListener(view -> deleteSelectedOption());

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                editText.performClick();
        });
    }

    private void deleteSelectedOption() {
        setValueOption(null, null);
        delete.setVisibility(View.GONE);
    }

    public void update(SpinnerViewModel viewModel) {
        this.viewModel = viewModel;

        numberOfOptions = viewModel.numberOfOptions();
        Bindings.setObjectStyle(iconView, itemView, viewModel.objectStyle());
        editText.setEnabled(viewModel.editable());
        editText.setFocusable(false);
        editText.setClickable(viewModel.editable());

        String value = viewModel.value();
        if (value != null && value.contains("_os_"))
            value = value.split("_os_")[0];

        editText.setText(value);

        if (editText.getText() != null && !editText.getText().toString().isEmpty()) {
            delete.setVisibility(View.VISIBLE);
        }

        if (!isEmpty(viewModel.warning())) {
            inputLayout.setErrorTextAppearance(R.style.warning_appearance);
            inputLayout.setError(viewModel.warning());
        } else if (!isEmpty(viewModel.error())) {
            inputLayout.setErrorTextAppearance(R.style.error_appearance);
            inputLayout.setError(viewModel.error());
        } else
            inputLayout.setError(null);

        if (inputLayout.getHint() == null || !inputLayout.getHint().toString().equals(viewModel.label())) {
            label = new StringBuilder(viewModel.label());
            if (viewModel.mandatory())
                label.append("*");
            inputLayout.setHint(label);
        }

        descriptionText = viewModel.description();

        descriptionLabel.setVisibility(label.length() > 16 || descriptionText != null ? View.VISIBLE : View.GONE);

    }

    public void dispose() {
    }

    @Override
    public void onClick(View v) {
        closeKeyboard(v);
        if (numberOfOptions > itemView.getContext().getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE).getInt(Constants.OPTION_SET_DIALOG_THRESHOLD, 15)) {
            OptionSetDialog dialog = OptionSetDialog.newInstance();
            dialog
                    .setProcessor(processorOptionSet)
                    .setOptionSetUid(viewModel)
                    .setOnClick(this)
                    .setCancelListener(view -> dialog.dismiss())
                    .setClearListener(view -> {
                                processor.onNext(
                                        RowAction.create(viewModel.uid(), null));
                                viewModel.withValue(null);
                                editText.setText(null);
                                dialog.dismiss();
                            }
                    ).show(((FragmentActivity) binding.getRoot().getContext()).getSupportFragmentManager(), null);
        } else {
            OptionSetPopUp.getInstance()
                    .setOptionSetUid(viewModel)
                    .setProcessor(processorOptionSet)
                    .setOnClick(this)
                    .show(itemView.getContext(), v);
        }
    }

    @Override
    public void onSelectOption(OptionModel option) {
        setValueOption(option.displayName(), option.code());
        OptionSetDialog.newInstance().dismiss();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (OptionSetPopUp.getInstance().getOptions() != null && item.getTitle() != null) {
            OptionModel selectedOption = OptionSetPopUp.getInstance().getOptions().get(item.getTitle().toString());
            if (selectedOption != null) {
                setValueOption(selectedOption.displayName(), selectedOption.code());
            }
            OptionSetPopUp.getInstance().dismiss();
        }
        return false;
    }

    private void setValueOption(String optionDisplayName, String optionCode) {

        editText.setText(optionDisplayName);

        if (optionDisplayName != null && !optionDisplayName.isEmpty()) {
            delete.setVisibility(View.VISIBLE);
        } else {
            delete.setVisibility(View.GONE);
        }

        processor.onNext(
                RowAction.create(viewModel.uid(), isSearchMode ? optionDisplayName + "_os_" + optionCode : optionCode, true)
        );
        viewModel.withValue(isSearchMode ? optionDisplayName : optionCode);
      /*  View nextView;
        if ((nextView = editText.focusSearch(View.FOCUS_DOWN)) != null)
            nextView.requestFocus();*/
    }
}
