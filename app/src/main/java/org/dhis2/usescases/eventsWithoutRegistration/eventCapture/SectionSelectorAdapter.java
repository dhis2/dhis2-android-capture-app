package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemSectionSelectorBinding;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

/**
 * QUADRAM. Created by ppajuelo on 20/11/2018.
 */
public class SectionSelectorAdapter extends RecyclerView.Adapter<EventSectionHolder> {
    private final EventCaptureContract.Presenter presenter;
    private List<EventSectionModel> items;
    private float percentage;
    private FlowableProcessor<Float> percentageFlowable;

    public SectionSelectorAdapter(EventCaptureContract.Presenter presenter) {
        this.presenter = presenter;
        this.items = new ArrayList<>();
        percentage = 0;
        percentageFlowable = PublishProcessor.create();
        percentageFlowable.onNext(0f);
    }

    @NonNull
    @Override
    public EventSectionHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ItemSectionSelectorBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_section_selector, viewGroup, false);
        return new EventSectionHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventSectionHolder eventSectionHolder, int position) {
        eventSectionHolder.bind(items.get(position), presenter);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void swapData(String currentSection, List<EventSectionModel> update) {

        this.items.clear();
        this.items.addAll(update);
        notifyDataSetChanged();

        percentageFlowable.onNext(calculateCompletionPercentage());

    }

    public FlowableProcessor<Float> completionPercentage() {
        return percentageFlowable;
    }

    private float calculateCompletionPercentage() {
        float wValues = 0f;
        float totals = 0f;
        for (EventSectionModel sectionModel : items) {
            wValues += (float) sectionModel.numberOfCompletedFields();
            totals += (float) sectionModel.numberOfTotalFields();
        }
        if (totals == 0){
            return  100;
        }
        percentage = wValues / totals;
        return percentage;
    }
}
