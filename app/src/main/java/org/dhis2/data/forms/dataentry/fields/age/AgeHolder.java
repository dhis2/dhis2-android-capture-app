package org.dhis2.data.forms.dataentry.fields.age;

import android.graphics.Color;

import androidx.lifecycle.MutableLiveData;

import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormAgeCustomBinding;
import org.dhis2.utils.DateUtils;

import java.util.Objects;

import io.reactivex.processors.FlowableProcessor;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by frodriguez on 20/03/2018.
 */

public class AgeHolder extends FormViewHolder {

    private FormAgeCustomBinding binding;
    private AgeViewModel ageViewModel;

    AgeHolder(FormAgeCustomBinding binding, FlowableProcessor<RowAction> processor, boolean isSearchMode, MutableLiveData<String> currentSelection) {
        super(binding);
        this.binding = binding;
        this.currentUid = currentSelection;

        binding.customAgeview.setAgeChangedListener(ageDate -> {
                    if (ageViewModel.value() == null || !Objects.equals(ageViewModel.value(), ageDate == null ? null : DateUtils.databaseDateFormat().format(ageDate))) {
                        processor.onNext(RowAction.create(ageViewModel.uid(), ageDate == null ? null : DateUtils.uiDateFormat().format(ageDate), getAdapterPosition()));
                        clearBackground(isSearchMode);
                    }
                }
        );

        binding.customAgeview.setActivationListener(() -> setSelectedBackground(isSearchMode));

    }


    public void update(AgeViewModel ageViewModel) {
        this.ageViewModel = ageViewModel;
        fieldUid = ageViewModel.uid();

        descriptionText = ageViewModel.description();
        label = new StringBuilder(ageViewModel.label());
        if (ageViewModel.mandatory())
            label.append("*");
        binding.customAgeview.setLabel(label.toString(), ageViewModel.description());
        if (!isEmpty(ageViewModel.value()))
            binding.customAgeview.setInitialValue(ageViewModel.value());
        else
            binding.customAgeview.clearValues();

        if (ageViewModel.warning() != null)
            binding.customAgeview.setWarning(ageViewModel.warning());
        else if (ageViewModel.error() != null)
            binding.customAgeview.setError(ageViewModel.error());

        binding.customAgeview.setEditable(ageViewModel.editable());

        binding.executePendingBindings();

        initFieldFocus();

    }
}
