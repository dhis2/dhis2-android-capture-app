package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ItemSectionSelectorBinding;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 20/11/2018.
 */
public class SectionSelectorAdapter extends RecyclerView.Adapter<EventSectionHolder> {
    private final EventCaptureContract.Presenter presenter;
    List<Pair<FormSectionViewModel, List<FieldViewModel>>> items;
    private String currentSection;
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
        eventSectionHolder.bind(currentSection, items.get(position), presenter);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void swapData(String currentSection, Pair<FormSectionViewModel, List<FieldViewModel>> update) {

        if (!this.items.contains(update)) {
            this.items.add(update);
            this.currentSection = currentSection;
            notifyDataSetChanged();

            float cont = 0;
            for (FieldViewModel fieldViewModel : update.val1())
                if (!isEmpty(fieldViewModel.value()))
                    cont++;
            percentage = percentage +(cont) / update.val1().size();
            percentageFlowable.onNext(percentage);


        } else if (!currentSection.equals(this.currentSection)) {
            this.currentSection = currentSection;
            notifyDataSetChanged();
        }
    }

    public FlowableProcessor<Float> completionPercentage() {
        return percentageFlowable;
    }
}
