package com.dhis2.usescases.eventsWithoutRegistration.eventSummary;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
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
    private int completionPercent = 44;
    private int totalFields;

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
        initProgressBar();
    }

    @Override
    public void setProgram(@NonNull ProgramModel program) {
        binding.setName(program.displayName());
    }

    private void initProgressBar(){
        //TODO CRIS: GET REAL PERCENTAGE HERE
        completionPercent = 44;
        ProgressBarAnimation gainAnim = new ProgressBarAnimation(binding.progressGains, 0, completionPercent, false, this);
        gainAnim.setDuration(PROGRESS_TIME);
        binding.progressGains.setAnimation(gainAnim);
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
        // TODO CRIS: REPLACE X WITH NUMBER OF COMPLETED FIELDS + UPDATE PROGRESS CIRCLE + UPDATE GREEN PROGRESS ON EACH ROW
        View sectionView = sections.get(sectionUid);
        if (sectionView != null) {
            totalFields = totalFields + updates.size();
            String completionText = "X/" + updates.size();
            ((TextView) sectionView.findViewById(R.id.section_percent)).setText(completionText);
        }

        binding.summaryHeader.setText(String.format(getString(R.string.event_summary_header), "X", String.valueOf(totalFields)));
        binding.summarySectionsHeader.setText(String.format(getString(R.string.event_summary_sections_header), "X", String.valueOf(totalFields)));
    }
}