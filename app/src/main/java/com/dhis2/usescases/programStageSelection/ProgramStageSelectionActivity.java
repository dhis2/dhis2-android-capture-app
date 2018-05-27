package com.dhis2.usescases.programStageSelection;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityProgramStageSelectionBinding;
import com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import com.dhis2.usescases.general.ActivityGlobalAbstract;

import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.List;

import javax.inject.Inject;

import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.ENROLLMENT_UID;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.EVENT_CREATION_TYPE;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.EVENT_PERIOD_TYPE;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.EVENT_REPEATABLE;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.NEW_EVENT;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.PROGRAM_STAGE_UID;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.PROGRAM_UID;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.TRACKED_ENTITY_INSTANCE;

/**
 * Created by ppajuelo on 31/10/2017.
 */

public class ProgramStageSelectionActivity extends ActivityGlobalAbstract implements ProgramStageSelectionContract.View {

    ActivityProgramStageSelectionBinding binding;

    @Inject
    ProgramStageSelectionContract.Presenter presenter;

    ProgramStageSelectionAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new ProgramStageSelectionModule()).inject(this);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_program_stage_selection);
        binding.setPresenter(presenter);
        int columnCount = getResources().getBoolean(R.bool.is_tablet) ? 3 : 2;
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, columnCount));
        adapter = new ProgramStageSelectionAdapter(presenter);
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String programId = getIntent().getStringExtra("PROGRAM_UID");
        String enrollmenId = getIntent().getStringExtra("ENROLLMENT_UID");

        presenter.getProgramStages(programId, enrollmenId, this); //TODO: enrollment / event path

    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void setData(List<ProgramStageModel> programStageModels) {
        adapter.setProgramStageModels(programStageModels);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void setResult(String programStageUid, boolean repeatable, @Nullable PeriodType periodType) {
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, getIntent().getStringExtra("PROGRAM_UID"));
        bundle.putString(TRACKED_ENTITY_INSTANCE, getIntent().getStringExtra("TRACKED_ENTITY_INSTANCE"));
        bundle.putString("ORG_UNIT", getIntent().getStringExtra("ORG_UNIT"));
        bundle.putString(ENROLLMENT_UID, getIntent().getStringExtra("ENROLLMENT_UID"));
        bundle.putBoolean(NEW_EVENT, getIntent().getBooleanExtra("NEW_EVENT", true));
        bundle.putString(EVENT_CREATION_TYPE, getIntent().getStringExtra("EVENT_CREATION_TYPE"));
        bundle.putBoolean(EVENT_REPEATABLE, repeatable);
        bundle.putSerializable(EVENT_PERIOD_TYPE, periodType);
        bundle.putString(PROGRAM_STAGE_UID, programStageUid);

        startActivity(EventInitialActivity.class, bundle, true, false, null);
    }
}
