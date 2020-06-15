package org.dhis2.data.forms.dataentry.tablefields.radiobutton;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.CustomCellViewBinding;
import org.dhis2.utils.DialogClickListener;
import org.dhis2.utils.customviews.TableFieldDialog;
import org.dhis2.utils.customviews.YesNoView;
import org.hisp.dhis.android.core.common.ValueTypeRenderingType;

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

        if (checkBoxViewModel.value() != null && !checkBoxViewModel.value().isEmpty()) {
            if (checkBoxViewModel.value().equals("true"))
                textView.setText(context.getString(R.string.yes));
            else
                textView.setText(context.getString(R.string.no));
        } else {
            textView.setText(null);
        }

        if (!(accessDataWrite && checkBoxViewModel.editable())) {
            textView.setEnabled(false);
        } else
            textView.setEnabled(true);

        if (checkBoxViewModel.mandatory())
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

        YesNoView yesNoView = new YesNoView(context);
        yesNoView.setIsBgTransparent(true);
        yesNoView.setValueType(viewModel.valueType());
        yesNoView.setRendering(ValueTypeRenderingType.DEFAULT);
        yesNoView.getClearButton().setVisibility(View.GONE);

        if (viewModel.value() != null && Boolean.valueOf(viewModel.value()))
            yesNoView.getRadioGroup().check(R.id.yes);
        else if (viewModel.value() != null && !viewModel.value().isEmpty())
            yesNoView.getRadioGroup().check(R.id.no);

        new TableFieldDialog(
                context,
                viewModel.label(),
                viewModel.description(),
                yesNoView,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        RowAction rowAction;
                        switch (yesNoView.getRadioGroup().getCheckedRadioButtonId()) {
                            case R.id.yes:
                                rowAction = RowAction.create(viewModel.uid(), String.valueOf(true), viewModel.dataElement(),
                                        viewModel.categoryOptionCombo(), viewModel.catCombo(), viewModel.row(), viewModel.column());
                                break;
                            case R.id.no:
                                rowAction = RowAction.create(viewModel.uid(), String.valueOf(false), viewModel.dataElement(),
                                        viewModel.categoryOptionCombo(), viewModel.catCombo(), viewModel.row(), viewModel.column());
                                break;
                            default:
                                rowAction = RowAction.create(viewModel.uid(), null, viewModel.dataElement(),
                                        viewModel.categoryOptionCombo(), viewModel.catCombo(), viewModel.row(), viewModel.column());
                                break;
                        }
                        processor.onNext(rowAction);
                    }

                    @Override
                    public void onNegative() {
                    }
                },
                v -> yesNoView.getRadioGroup().clearCheck()
        ).show();
    }

}
