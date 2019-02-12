package org.dhis2.data.forms.dataentry.fields.age;

import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormAgeCustomBinding;
import org.dhis2.utils.DateUtils;

import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by frodriguez on 20/03/2018.
 */

public class AgeHolder extends FormViewHolder {

    private FormAgeCustomBinding formAgeCustomBinding;
    private AgeViewModel ageViewModel;

    AgeHolder(FormAgeCustomBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        this.formAgeCustomBinding = binding;
        binding.customAgeview.setAgeChangedListener(ageDate -> {
                    if (ageViewModel.value() == null || !ageViewModel.value().equals(DateUtils.databaseDateFormat().format(ageDate)))
                        processor.onNext(RowAction.create(ageViewModel.uid(), DateUtils.databaseDateFormat().format(ageDate)));
                }
        );
    }

    public void update(AgeViewModel ageViewModel) {
        this.ageViewModel = ageViewModel;

        descriptionText = ageViewModel.description();
        label = new StringBuilder(ageViewModel.label());
        if (ageViewModel.mandatory())
            label.append("*");
        formAgeCustomBinding.customAgeview.setLabel(label.toString(), ageViewModel.description());
        if (!isEmpty(ageViewModel.value())) {
            formAgeCustomBinding.customAgeview.setInitialValue(ageViewModel.value());
        }

        if (ageViewModel.warning() != null)
            formAgeCustomBinding.customAgeview.setWarningOrError(ageViewModel.warning());
        else if (ageViewModel.error() != null)
            formAgeCustomBinding.customAgeview.setWarningOrError(ageViewModel.error());
        else
            formAgeCustomBinding.customAgeview.setWarningOrError(null);

        formAgeCustomBinding.customAgeview.setEditable(ageViewModel.editable());

        formAgeCustomBinding.executePendingBindings();

    }

    @Override
    public void dispose() {
        // do nothing
    }
}
