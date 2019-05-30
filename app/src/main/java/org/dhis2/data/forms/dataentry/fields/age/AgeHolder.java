package org.dhis2.data.forms.dataentry.fields.age;

import android.graphics.Color;

import androidx.appcompat.content.res.AppCompatResources;

import org.dhis2.R;
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

    private FormAgeCustomBinding formAgeCustomBinding;
    private AgeViewModel ageViewModel;

    AgeHolder(FormAgeCustomBinding binding, FlowableProcessor<RowAction> processor, boolean isSearchMode) {
        super(binding);
        this.formAgeCustomBinding = binding;
        binding.customAgeview.setAgeChangedListener(ageDate -> {
                    if (ageViewModel.value() == null || !Objects.equals(ageViewModel.value(), ageDate == null ? null : DateUtils.databaseDateFormat().format(ageDate))) {
                        processor.onNext(RowAction.create(ageViewModel.uid(), ageDate == null ? null : DateUtils.databaseDateFormat().format(ageDate), getAdapterPosition()));
                        if (!isSearchMode)
                            itemView.setBackgroundColor(Color.WHITE);
                    }
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
            formAgeCustomBinding.customAgeview.setWarning(ageViewModel.warning());
        else if (ageViewModel.error() != null)
            formAgeCustomBinding.customAgeview.setError(ageViewModel.error());
        else
            formAgeCustomBinding.customAgeview.setError(null);

        formAgeCustomBinding.customAgeview.setEditable(ageViewModel.editable());

        formAgeCustomBinding.executePendingBindings();

    }

    @Override
    public void dispose() {
        // unused
    }

    @Override
    public void performAction() {
        itemView.setBackground(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.item_selected_bg));
        formAgeCustomBinding.customAgeview.performOnFocusAction();
    }
}
