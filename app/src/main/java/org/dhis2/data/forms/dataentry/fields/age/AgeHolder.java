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

    private FormAgeCustomBinding binding;
    private AgeViewModel ageViewModel;

    AgeHolder(FormAgeCustomBinding binding, FlowableProcessor<RowAction> processor, boolean isSearchMode) {
        super(binding);
        this.binding = binding;
        binding.customAgeview.setAgeChangedListener(ageDate -> {
                    if (ageViewModel.value() == null || !Objects.equals(ageViewModel.value(), ageDate == null ? null : DateUtils.Companion.databaseDateFormat().format(ageDate))) {
                        processor.onNext(RowAction.create(ageViewModel.uid(), ageDate == null ? null : DateUtils.Companion.databaseDateFormat().format(ageDate), getAdapterPosition()));
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
        binding.customAgeview.setLabel(label.toString(), ageViewModel.description());
        if (!isEmpty(ageViewModel.value()))
            binding.customAgeview.setInitialValue(ageViewModel.value());
        else
            binding.customAgeview.clearValues();

        if (ageViewModel.warning() != null)
            binding.customAgeview.setWarning(ageViewModel.warning());
        else if (ageViewModel.error() != null)
            binding.customAgeview.setError(ageViewModel.error());
        else
            binding.customAgeview.setError(null);

        binding.customAgeview.setEditable(ageViewModel.editable());

        binding.executePendingBindings();

    }

    @Override
    public void dispose() {
//        disposable.clear();
    }

    @Override
    public void performAction() {
        itemView.setBackground(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.item_selected_bg));
        binding.customAgeview.performOnFocusAction();
    }
}
