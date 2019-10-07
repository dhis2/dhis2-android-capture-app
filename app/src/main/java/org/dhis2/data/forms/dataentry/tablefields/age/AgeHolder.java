package org.dhis2.data.forms.dataentry.tablefields.age;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.CustomCellViewBinding;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.DialogClickListener;
import org.dhis2.utils.custom_views.AgeView;
import org.dhis2.utils.custom_views.TableFieldDialog;

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
    }

    private void showEditDialog() {

        AgeView ageView = new AgeView(context);
        ageView.setIsBgTransparent(true);

        if (ageViewModel.value() != null && !ageViewModel.value().isEmpty()) {
            ageView.setInitialValue(ageViewModel.value());
        }

        ageView.setAgeChangedListener(ageDate -> {
            String date = ageDate != null ? DateUtils.databaseDateFormat().format(ageDate) : "";
            if (ageViewModel.value() == null || !ageViewModel.value().equals(date))
                processor.onNext(RowAction.create(ageViewModel.uid(), date, ageViewModel.dataElement(),
                        ageViewModel.categoryOptionCombo(), ageViewModel.catCombo(), ageViewModel.row(), ageViewModel.column()));
        });

        new TableFieldDialog(
                context,
                ageViewModel.label(),
                ageViewModel.description(),
                ageView,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        ageView.handleSingleInputs(true);
                    }

                    @Override
                    public void onNegative() {
                    }
                },
                v -> ageView.clearValues()
        ).show();
    }

    @Override
    public void dispose() {
    }
}
