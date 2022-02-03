package org.dhis2.data.forms.dataentry.tablefields.age;

import static android.text.TextUtils.isEmpty;

import android.content.Context;
import android.view.View;

import org.dhis2.Bindings.StringExtensionsKt;
import org.dhis2.commons.dialogs.DialogClickListener;
import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.CustomCellViewBinding;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.customviews.TableFieldDialog;

import java.util.Date;

import io.reactivex.processors.FlowableProcessor;

public class AgeHolder extends FormViewHolder {

    private final FlowableProcessor<RowAction> processor;

    CustomCellViewBinding binding;
    AgeViewModel ageViewModel;
    Context context;
    String date;


    AgeHolder(CustomCellViewBinding binding, FlowableProcessor<RowAction> processor, Context context) {
        super(binding);
        this.binding = binding;
        this.context = context;
        this.processor = processor;
        textView = binding.inputEditText;
    }


    public void update(AgeViewModel ageViewModel, boolean accessDataWrite) {
        super.update(ageViewModel);
        this.accessDataWrite = accessDataWrite;

        this.ageViewModel = ageViewModel;

        if (!isEmpty(ageViewModel.value())) {
            try {
                Date date = StringExtensionsKt.toDate(ageViewModel.value());
                textView.setText(DateUtils.uiDateFormat().format(date));
            } catch (Exception e) {
                textView.setError(e.getMessage());
            }
        } else
            textView.setText(null);

        if (ageViewModel.mandatory())
            binding.icMandatory.setVisibility(View.VISIBLE);
        else
            binding.icMandatory.setVisibility(View.INVISIBLE);

        if (!(accessDataWrite && ageViewModel.editable())) {
            textView.setEnabled(false);
        } else
            textView.setEnabled(true);

        binding.executePendingBindings();

    }

    @Override
    public void setSelected(SelectionState selectionState) {
        super.setSelected(selectionState);
        if (selectionState == SelectionState.SELECTED && textView.isEnabled()) {
            showEditDialog();
        }
        setBackground();
    }

    private void showEditDialog() {

        AgeView ageView = new AgeView(context);
        ageView.setIsBgTransparent();

        if (ageViewModel.value() != null && !ageViewModel.value().isEmpty()) {
            ageView.setInitialValue(ageViewModel.value());
        }

        ageView.setAgeChangedListener(ageDate -> date = ageDate != null ? DateUtils.oldUiDateFormat().format(ageDate) : "");

        new TableFieldDialog(
                context,
                ageViewModel.label(),
                ageViewModel.description(),
                ageView,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        if (ageViewModel.value() == null || !ageViewModel.value().equals(date))
                            processor.onNext(RowAction.create(ageViewModel.uid(), date, ageViewModel.dataElement(),
                                    ageViewModel.categoryOptionCombo(), ageViewModel.catCombo(), ageViewModel.row(), ageViewModel.column()));
                    }

                    @Override
                    public void onNegative() {
                    }
                },
                null
        ).show();
    }

    @Override
    public void dispose() {
    }
}
