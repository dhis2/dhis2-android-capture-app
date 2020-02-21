package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.databinding.ItemSectionSelectorBinding;

/**
 * QUADRAM. Created by ppajuelo on 20/11/2018.
 */
public class EventSectionHolder extends RecyclerView.ViewHolder {
    private final ItemSectionSelectorBinding binding;

    public EventSectionHolder(@NonNull ItemSectionSelectorBinding binding) {
        super(binding.root);
        this.binding = binding;
    }

    public void bind(EventSectionModel sectionModel, EventCaptureContract.Presenter presenter) {
        binding.sectionTitle.setText(sectionModel.sectionName());
        binding.setOrder(getAdapterPosition());
        binding.setSectionUid(sectionModel.sectionUid());
        binding.setCurrentSection(presenter.getCurrentSection());
        binding.setPresenter(presenter);

        binding.sectionValues.setText(String.format("%s/%s", sectionModel.numberOfCompletedFields(), sectionModel.numberOfTotalFields()));
    }
}
