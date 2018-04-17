package com.dhis2.usescases.programStageSelection;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityProgramStageSelectionBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;

import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.List;

import javax.inject.Inject;

import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.PROGRAM_STAGE_UID;

/**
 * Created by ppajuelo on 31/10/2017.
 *
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
        String programId = getIntent().getStringExtra("PROGRAM_UID");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_program_stage_selection);
        binding.setPresenter(presenter);
        presenter.getProgramStages(programId, this);
        int columnCount = getResources().getBoolean(R.bool.is_tablet) ? 3 : 2;
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, columnCount));
        adapter = new ProgramStageSelectionAdapter(presenter);
        binding.recyclerView.setAdapter(adapter);
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
    public void setResult(String programStageUid) {
        Intent data = new Intent();
        data.putExtra(PROGRAM_STAGE_UID, programStageUid);
        setResult(RESULT_OK, data);
        finish();
    }
}
