package org.dhis2.data.forms.dataentry.tablefields.age;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.CustomCellViewBinding;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.custom_views.AgeView;

import java.text.ParseException;
import java.util.Date;

import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by frodriguez on 20/03/2018.
 */

public class AgeHolder extends FormViewHolder {

    private final FlowableProcessor<RowAction> processor;

    CustomCellViewBinding binding;
    TextView textView;
    AgeViewModel ageViewModel;
    Context context;

    AgeHolder(CustomCellViewBinding binding, FlowableProcessor<RowAction> processor, Context context) {
        super(binding);
        this.binding = binding;
        this.context = context;
        this.processor = processor;
        textView = binding.inputEditText;
    }


    public void update(AgeViewModel ageViewModel, boolean accessDataWrite) {

        this.ageViewModel = ageViewModel;

        if (!isEmpty(ageViewModel.value())) {
            try {
                Date date = DateUtils.databaseDateFormat().parse(ageViewModel.value());
                textView.setText(DateUtils.uiDateFormat().format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if(ageViewModel.mandatory())
            binding.icMandatory.setVisibility(View.VISIBLE);
        else
            binding.icMandatory.setVisibility(View.INVISIBLE);

        if(!(accessDataWrite && ageViewModel.editable())) {
            textView.setEnabled(false);
        }else
            textView.setEnabled(true);

        binding.executePendingBindings();

    }

    @Override
    public void setSelected(SelectionState selectionState) {
        super.setSelected(selectionState);
        if (selectionState == SelectionState.SELECTED && textView.isEnabled()) {
            showEditDialog();
        }
    }

    private void showEditDialog() {

        View view = LayoutInflater.from(context).inflate(R.layout.form_age_custom, null);
        AgeView ageView = view.findViewById(R.id.custom_ageview);

        AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.CustomDialog)
                .setPositiveButton(R.string.action_accept, (dialog, which) -> {
                    ageView.onFocusChange(ageView.findViewById(R.id.input_days), false);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.clear, (dialog, which) -> processor.onNext(
                        RowAction.create(ageViewModel.uid(), null, ageViewModel.dataElement(), ageViewModel.categoryOptionCombo(), ageViewModel.catCombo(), ageViewModel.row(), ageViewModel.column())))
                .create();

        ageView.setIsBgTransparent(true);
        if(ageViewModel.value() != null && !ageViewModel.value().isEmpty())
            ageView.setInitialValue(ageViewModel.value());

        ageView.setLabel(ageViewModel.label(), ageViewModel.description());

        ageView.setAgeChangedListener(ageDate -> {
                    if (ageViewModel.value() == null || !ageViewModel.value().equals(DateUtils.databaseDateFormat().format(ageDate)))
                        processor.onNext(RowAction.create(ageViewModel.uid(), DateUtils.databaseDateFormat().format(ageDate), ageViewModel.dataElement(), ageViewModel.categoryOptionCombo(), ageViewModel.catCombo(), ageViewModel.row(), ageViewModel.column()));
        });
        alertDialog.setView(view);

        alertDialog.show();
    }

    @Override
    public void dispose() {
    }
}
