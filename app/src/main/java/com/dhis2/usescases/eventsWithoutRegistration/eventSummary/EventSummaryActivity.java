package com.dhis2.usescases.eventsWithoutRegistration.eventSummary;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.data.forms.FormSectionViewModel;
import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.dhis2.databinding.ActivityEventSummaryBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.utils.CustomViews.ProgressBarAnimation;

import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

/**
 * Created by Cristian on 01/03/2018.
 *
 */

public class EventSummaryActivity extends ActivityGlobalAbstract implements EventSummaryContract.View, ProgressBarAnimation.OnUpdate {

    private static final int PROGRESS_TIME = 2000;

    public static final String EVENT_ID = "event_id";
    public static final String PROGRAM_ID = "program_id";

    private Map<String, View> sections = new HashMap<>();

    @Inject
    EventSummaryContract.Presenter presenter;
    private ActivityEventSummaryBinding binding;
    private int completionPercent;
    private int totalFields;
    private int totalCompletedFields;
    private int fieldsToCompleteBeforeClosing;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(EVENT_ID) && getIntent().getExtras().containsKey(PROGRAM_ID)
                && getIntent().getExtras().getString(EVENT_ID) != null && getIntent().getExtras().getString(PROGRAM_ID) != null) {
            String eventId = getIntent().getExtras().getString(EVENT_ID);
            String programId = getIntent().getExtras().getString(PROGRAM_ID);
            ((App) getApplicationContext()).userComponent().plus(new EventSummaryModule(this, eventId)).inject(this);
            presenter.init(this, programId, eventId);
        }
        else {
            finish();
        }

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_summary);
        binding.setPresenter(presenter);

        binding.actionButton.setOnClickListener(v -> finish());
    }

    @Override
    public void setProgram(@NonNull ProgramModel program) {
        binding.setName(program.displayName());
    }

    @Override
    public void onUpdate(boolean lost, float interpolatedTime) {
        int progress = (int)(completionPercent * interpolatedTime);
        String text = String.valueOf(progress) + "%";
        binding.progress.setText(text);
    }

    @Override
    public void onEventSections(List<FormSectionViewModel> formSectionViewModels) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (FormSectionViewModel formSectionViewModel : formSectionViewModels){
            View inflatedLayout = inflater.inflate(R.layout.event_section_row, null, false);
            ((TextView) inflatedLayout.findViewById(R.id.section_title)).setText(formSectionViewModel.label());
            binding.eventSectionRows.addView(inflatedLayout);
            sections.put(formSectionViewModel.sectionUid(), inflatedLayout);
            presenter.getSectionCompletion(formSectionViewModel.sectionUid());
        }
    }

    @NonNull
    @Override
    public Consumer<List<FieldViewModel>> showFields(String sectionUid) {
        return fields -> swap(fields, sectionUid);
    }

    void swap(@NonNull List<FieldViewModel> updates, String sectionUid) {
        View sectionView = sections.get(sectionUid);
        if (sectionView != null) {
            int completedSectionFields = calculateCompletedFields(updates);
            int totalSectionFields = updates.size();
            totalFields = totalFields + totalSectionFields;
            totalCompletedFields = totalCompletedFields + completedSectionFields;
            fieldsToCompleteBeforeClosing = fieldsToCompleteBeforeClosing + calculateMandatoryUnansweredFields(updates);
            String completionText = completedSectionFields + "/" + totalSectionFields;
            ((TextView) sectionView.findViewById(R.id.section_percent)).setText(completionText);
            sectionView.findViewById(R.id.completed_progress)
                    .setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, totalSectionFields - completedSectionFields));
            sectionView.findViewById(R.id.empty_progress)
                    .setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, completedSectionFields));
        }

        binding.summaryHeader.setText(String.format(getString(R.string.event_summary_header), String.valueOf(totalCompletedFields), String.valueOf(totalFields)));
        binding.summarySectionsHeader.setText(String.format(getString(R.string.event_summary_sections_header), String.valueOf(totalCompletedFields), String.valueOf(totalFields)));
        float completionPerone = (float) totalCompletedFields / (float) totalFields;
        completionPercent = (int) (completionPerone * 100);
        ProgressBarAnimation gainAnim = new ProgressBarAnimation(binding.progressGains, 0, completionPercent, false, this);
        gainAnim.setDuration(PROGRESS_TIME);
        binding.progressGains.startAnimation(gainAnim);
        checkButton();
    }

    private void checkButton(){
        binding.actionButton.setEnabled(fieldsToCompleteBeforeClosing <= 0);
    }

    private int calculateCompletedFields(@NonNull List<FieldViewModel> updates){
        int total = 0;
        for (FieldViewModel fieldViewModel : updates){
            if (fieldViewModel.value() != null && !fieldViewModel.value().isEmpty())
                total++;
        }
        return total;
    }

    private int calculateMandatoryUnansweredFields(@NonNull List<FieldViewModel> updates){
        int total = 0;
        for (FieldViewModel fieldViewModel : updates){
            if ((fieldViewModel.value() == null || fieldViewModel.value().isEmpty()) && fieldViewModel.mandatory())
                total++;
        }
        return total;
    }
}