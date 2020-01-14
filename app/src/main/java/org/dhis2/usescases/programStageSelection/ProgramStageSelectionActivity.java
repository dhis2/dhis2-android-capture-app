package org.dhis2.usescases.programStageSelection;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.ActivityProgramStageSelectionBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.ProgramStage;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import static org.dhis2.utils.Constants.ENROLLMENT_UID;
import static org.dhis2.utils.Constants.EVENT_CREATION_TYPE;
import static org.dhis2.utils.Constants.EVENT_PERIOD_TYPE;
import static org.dhis2.utils.Constants.EVENT_REPEATABLE;
import static org.dhis2.utils.Constants.EVENT_SCHEDULE_INTERVAL;
import static org.dhis2.utils.Constants.ORG_UNIT;
import static org.dhis2.utils.Constants.PROGRAM_UID;
import static org.dhis2.utils.Constants.TRACKED_ENTITY_INSTANCE;


/**
 * QUADRAM. Created by ppajuelo on 31/10/2017.
 */

public class ProgramStageSelectionActivity extends ActivityGlobalAbstract implements ProgramStageSelectionContract.View {

    ActivityProgramStageSelectionBinding binding;

    @Inject
    ProgramStageSelectionContract.Presenter presenter;

    ProgramStageSelectionAdapter adapter;
    private String enrollmentId;
    private String programId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        programId = getIntent().getStringExtra("PROGRAM_UID");
        enrollmentId = getIntent().getStringExtra("ENROLLMENT_UID");
        String eventCreationType = getIntent().getStringExtra(EVENT_CREATION_TYPE);
        ((App) getApplicationContext()).userComponent().plus(new ProgramStageSelectionModule(this, programId, enrollmentId, eventCreationType)).inject(this);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_program_stage_selection);
        binding.setPresenter(presenter);
        adapter = new ProgramStageSelectionAdapter(presenter);
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int orientation = Resources.getSystem().getConfiguration().orientation;
        presenter.getProgramStages(programId, enrollmentId); //TODO: enrollment / event path
        int columnCount = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? 3 : 2;
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, columnCount));
    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void setData(List<ProgramStage> programStages) {
        if (programStages != null && !programStages.isEmpty()) {
            adapter.setProgramStages(programStages);
            adapter.notifyDataSetChanged();
        } else {
            // if there are no program stages to select, the event cannot be added
            displayMessage(getString(R.string.program_not_allow_events));
            finish();
        }
    }

    @Override
    public void setResult(String programStageUid, boolean repeatable, @Nullable PeriodType periodType) {
        Intent intent = new Intent(this, EventInitialActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, getIntent().getStringExtra(PROGRAM_UID));
        bundle.putString(TRACKED_ENTITY_INSTANCE, getIntent().getStringExtra(TRACKED_ENTITY_INSTANCE));
        bundle.putString(ORG_UNIT, getIntent().getStringExtra(ORG_UNIT));
        bundle.putString(ENROLLMENT_UID, getIntent().getStringExtra(ENROLLMENT_UID));
        bundle.putString(EVENT_CREATION_TYPE, getIntent().getStringExtra(EVENT_CREATION_TYPE));
        bundle.putBoolean(EVENT_REPEATABLE, repeatable);
        bundle.putSerializable(EVENT_PERIOD_TYPE, periodType);
        bundle.putString(Constants.PROGRAM_STAGE_UID, programStageUid);
        bundle.putInt(EVENT_SCHEDULE_INTERVAL, presenter.getStandardInterval(programStageUid));
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }
}
