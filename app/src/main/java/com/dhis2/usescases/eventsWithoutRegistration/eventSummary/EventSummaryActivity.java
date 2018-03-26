package com.dhis2.usescases.eventsWithoutRegistration.eventSummary;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.data.forms.FormSectionViewModel;
import com.dhis2.databinding.ActivityEventSummaryBinding;
import com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.utils.CustomViews.ProgressBarAnimation;

import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by Cristian on 01/03/2018.
 *
 */

public class EventSummaryActivity extends ActivityGlobalAbstract implements EventSummaryContract.View, ProgressBarAnimation.OnUpdate {

    private static final int PROGRESS_TIME = 2000;

    public static final String EVENT_ID = "event_id";
    public static final String PROGRAM_ID = "program_id";

    @Inject
    EventSummaryContract.Presenter presenter;
    private ActivityEventSummaryBinding binding;
    private int completionPercent = 44;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new EventSummaryModule()).inject(this);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_summary);
        binding.setPresenter(presenter);
        initProgressBar();

        if (getIntent().getExtras() != null) {
            String eventId = getIntent().getExtras().getString(EVENT_ID);
            String programId = getIntent().getExtras().getString(PROGRAM_ID);
            presenter.init(this, programId, eventId);
        }
        else {
            finish();
        }
    }

    @Override
    public void setProgram(ProgramModel program) {
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
        for (FormSectionViewModel formSectionViewModel : formSectionViewModels){
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View inflatedLayout = inflater.inflate(R.layout.event_section_row, null, false);
            ((TextView) inflatedLayout.findViewById(R.id.section_title)).setText(formSectionViewModel.label());
            binding.eventSectionRows.addView(inflatedLayout);
        }
    }
}