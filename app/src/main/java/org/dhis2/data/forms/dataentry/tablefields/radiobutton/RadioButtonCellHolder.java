package org.dhis2.data.forms.dataentry.tablefields.radiobutton;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.CustomCellViewBinding;
import org.dhis2.utils.custom_views.YesNoView;

import io.reactivex.processors.FlowableProcessor;


/**
 * QUADRAM. Created by frodriguez on 18/01/2018.
 */

public class RadioButtonCellHolder extends FormViewHolder {

    private final FlowableProcessor<RowAction> processor;

    final CustomCellViewBinding binding;
    TextView textView;
    RadioButtonViewModel viewModel;
    Context context;

    RadioButtonCellHolder(CustomCellViewBinding binding, FlowableProcessor<RowAction> processor, Context context) {
        super(binding);
        this.context = context;
        textView = binding.inputEditText;
        this.binding = binding;
        this.processor = processor;
    }


    public void update(RadioButtonViewModel checkBoxViewModel, boolean accessDataWrite) {

        this.viewModel = checkBoxViewModel;

        if(checkBoxViewModel.value()!=null && !checkBoxViewModel.value().isEmpty()) {
            if (checkBoxViewModel.value().equals("true"))
                textView.setText(context.getString(R.string.yes));
            else
                textView.setText(context.getString(R.string.no));
        }else{
            textView.setText(null);
        }

        if(!(accessDataWrite && checkBoxViewModel.editable())) {
            textView.setEnabled(false);
        }else
            textView.setEnabled(true);

        if(checkBoxViewModel.mandatory())
            binding.icMandatory.setVisibility(View.VISIBLE);
        else
            binding.icMandatory.setVisibility(View.INVISIBLE);

    }

    public void dispose() {
    }

    @Override
    public void setSelected(SelectionState selectionState) {
        super.setSelected(selectionState);
        if (selectionState == SelectionState.SELECTED && textView.isEnabled()) {
            showEditDialog();
        }
    }
    private void showEditDialog() {

        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        View view = LayoutInflater.from(context).inflate(R.layout.form_yes_no, null);
        YesNoView yesNoView = view.findViewById(R.id.customYesNo);
        yesNoView.setCellLayout();
        yesNoView.setValueType(viewModel.valueType());
        RadioGroup radioGroup = view.findViewById(R.id.radiogroup);
        ImageView clearButton = view.findViewById(R.id.clearSelection);

        if (viewModel.value() != null && Boolean.valueOf(viewModel.value()))
            radioGroup.check(R.id.yes);
        else if (viewModel.value() != null && !viewModel.value().isEmpty())
            radioGroup.check(R.id.no);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RowAction rowAction;
            RadioButton checkedRadioButton = group.findViewById(checkedId);
            switch (checkedId) {
                case R.id.yes:
                    if (checkedRadioButton.isChecked()) {
                        rowAction = RowAction.create(viewModel.uid(), String.valueOf(true), viewModel.dataElement(), viewModel.categoryOptionCombo(), viewModel.catCombo(), viewModel.row(), viewModel.column());
                        break;
                    }
                case R.id.no:
                    if (checkedRadioButton.isChecked()) {
                        rowAction = RowAction.create(viewModel.uid(), String.valueOf(false), viewModel.dataElement(), viewModel.categoryOptionCombo(), viewModel.catCombo(), viewModel.row(), viewModel.column());
                        break;
                    }
                default:
                    rowAction = RowAction.create(viewModel.uid(), null, viewModel.dataElement(), viewModel.categoryOptionCombo(), viewModel.catCombo(), viewModel.row(), viewModel.column());
                    break;
            }
            processor.onNext(rowAction);
            alertDialog.dismiss();
        });

        clearButton.setOnClickListener(view1 -> {
            if (viewModel.editable().booleanValue()) {
                radioGroup.clearCheck();
                alertDialog.dismiss();
            }
        });
        alertDialog.setView(view);

        alertDialog.show();
    }

}
