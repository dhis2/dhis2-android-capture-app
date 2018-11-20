package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.databinding.ObservableBoolean;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ItemSectionSelectorBinding;

import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 20/11/2018.
 */
public class EventSectionHolder extends RecyclerView.ViewHolder {
    private final ItemSectionSelectorBinding binding;
    private final ObservableBoolean isCurrentSection = new ObservableBoolean(false);

    public EventSectionHolder(@NonNull ItemSectionSelectorBinding binding) {
        super(binding.root);
        this.binding = binding;
    }

    public void bind(String currentSection, Pair<FormSectionViewModel, List<FieldViewModel>> pair, EventCaptureContract.Presenter presenter) {
        binding.sectionTitle.setText(pair.val0().label());
        binding.setOrder(getAdapterPosition());
        binding.setIsCurrentSection(isCurrentSection);
        isCurrentSection.set(currentSection.equals(pair.val0().sectionUid()));
        binding.setPresenter(presenter);

        int completedValues = 0;
        for (FieldViewModel fieldViewModel : pair.val1())
            if (!TextUtils.isEmpty(fieldViewModel.value()))
                completedValues++;
        binding.sectionValues.setText(String.format("%s/%s", completedValues, pair.val1().size()));
    }
}
