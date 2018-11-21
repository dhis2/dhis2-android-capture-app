package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.databinding.ObservableBoolean;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import org.dhis2.databinding.ItemSectionSelectorBinding;

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

    public void bind(String currentSection, EventSectionModel sectionModel, EventCaptureContract.Presenter presenter) {
        binding.sectionTitle.setText(sectionModel.sectionName());
        binding.setOrder(getAdapterPosition());
        binding.setIsCurrentSection(isCurrentSection);
        isCurrentSection.set(currentSection.equals(sectionModel.sectionUid()));
        binding.setPresenter(presenter);

        binding.sectionValues.setText(String.format("%s/%s", sectionModel.numberOfCompletedFields(), sectionModel.numberOfTotalFields()));
    }
}
