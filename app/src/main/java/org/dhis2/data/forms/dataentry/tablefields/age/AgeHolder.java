package org.dhis2.data.forms.dataentry.tablefields.age;

import android.support.v4.content.ContextCompat;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.FormAgeCustomBinding;
import org.dhis2.utils.DateUtils;

import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by frodriguez on 20/03/2018.
 */

public class AgeHolder extends FormViewHolder {

    FormAgeCustomBinding binding;
    AgeViewModel ageViewModel;

    AgeHolder(FormAgeCustomBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        this.binding = binding;
        binding.customAgeview.setAgeChangedListener(ageDate -> {
                    if (ageViewModel.value() == null || !ageViewModel.value().equals(DateUtils.databaseDateFormat().format(ageDate)))
                        processor.onNext(RowAction.create(ageViewModel.uid(), DateUtils.databaseDateFormat().format(ageDate), ageViewModel.dataElement(), ageViewModel.listCategoryOption(), ageViewModel.row(), ageViewModel.column()));
                }
        );
    }


    public void update(AgeViewModel ageViewModel, boolean accessDataWrite) {
//        model.onNext(viewModel);
        this.ageViewModel = ageViewModel;

        descriptionText = ageViewModel.description();
        label = new StringBuilder(ageViewModel.label());
        if (ageViewModel.mandatory())
            label.append("*");
        binding.customAgeview.setLabel(label.toString(),ageViewModel.description());
        if (!isEmpty(ageViewModel.value())) {
            binding.customAgeview.setInitialValue(ageViewModel.value());
        }

        if (ageViewModel.warning() != null)
            binding.customAgeview.setWarningOrError(ageViewModel.warning());
        else if (ageViewModel.error() != null)
            binding.customAgeview.setWarningOrError(ageViewModel.error());
        else
            binding.customAgeview.setWarningOrError(null);

        if (!ageViewModel.editable()) {
            binding.customAgeview.setEnabled(false);
            binding.customAgeview.setBackgroundColor(ContextCompat.getColor(binding.customAgeview.getContext(), R.color.bg_black_e6e));
        } else {
            binding.customAgeview.setEnabled(true);
            binding.customAgeview.setBackgroundColor(ContextCompat.getColor(binding.customAgeview.getContext(), R.color.white));
        }

        binding.customAgeview.setEditable(accessDataWrite);

        binding.executePendingBindings();

    }

    @Override
    public void dispose() {
//        disposable.clear();
    }
}
