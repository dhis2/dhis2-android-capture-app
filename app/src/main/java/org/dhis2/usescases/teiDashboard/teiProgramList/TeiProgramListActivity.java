package org.dhis2.usescases.teiDashboard.teiProgramList;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.ActivityTeiProgramListBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.main.program.ProgramViewModel;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by Cristian on 13/02/2018.
 */

public class TeiProgramListActivity extends ActivityGlobalAbstract implements TeiProgramListContract.View {

    private ActivityTeiProgramListBinding binding;

    @Inject
    TeiProgramListContract.Presenter presenter;
    @Inject
    TeiProgramListAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        String trackedEntityId = getIntent().getStringExtra("TEI_UID");
        ((App) getApplicationContext()).userComponent().plus(new TeiProgramListModule(trackedEntityId)).inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tei_program_list);
        binding.setPresenter(presenter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);

    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void setActiveEnrollments(List<EnrollmentViewModel> enrollments) {
        if (binding.recycler.getAdapter() == null) {
            binding.recycler.setAdapter(adapter);
        }
        adapter.setActiveEnrollments(enrollments);
    }

    @Override
    public void setOtherEnrollments(List<EnrollmentViewModel> enrollments) {
        if (binding.recycler.getAdapter() == null) {
            binding.recycler.setAdapter(adapter);
        }
        adapter.setOtherEnrollments(enrollments);
    }

    @Override
    public void setPrograms(List<ProgramViewModel> programs) {
        if (binding.recycler.getAdapter() == null) {
            binding.recycler.setAdapter(adapter);
        }
        adapter.setPrograms(programs);
    }

    @Override
    public void goToEnrollmentScreen(String enrollmentUid) {
        Intent data = new Intent();
        data.putExtra("GO_TO_ENROLLMENT", enrollmentUid);
        setResult(RESULT_OK, data);

        finish();
    }

    @Override
    public void changeCurrentProgram(String program) {
        Intent data = new Intent();
        data.putExtra("CHANGE_PROGRAM", program);
        setResult(RESULT_OK, data);

        finish();
    }
}