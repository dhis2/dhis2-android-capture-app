package com.dhis2.usescases.teiDashboard.teiProgramList;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityTeiProgramListBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by Cristian on 13/02/2018.
 *
 */

public class TeiProgramListActivity extends ActivityGlobalAbstract implements TeiProgramListContract.View {

    private ActivityTeiProgramListBinding binding;

    @Inject
    TeiProgramListContract.Presenter presenter;
    @Inject
    TeiProgramListAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new TeiProgramListModule()).inject(this);

        super.onCreate(savedInstanceState);
        String trackedEntityId = getIntent().getStringExtra("TEI_UID");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tei_program_list);
        binding.setPresenter(presenter);
        presenter.init(this, trackedEntityId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDettach();
    }

    @Override
    public void setActiveEnrollments(List<EnrollmentModel> enrollments) {
        if (binding.recycler.getAdapter() == null) {
            binding.recycler.setAdapter(adapter);
        }
        adapter.setActiveEnrollments(enrollments);
    }

    @Override
    public void setOtherEnrollments(List<EnrollmentModel> enrollments) {
        if (binding.recycler.getAdapter() == null) {
            binding.recycler.setAdapter(adapter);
        }
        adapter.setOtherEnrollments(enrollments);
    }

    @Override
    public void setPrograms(List<ProgramModel> programs) {
        if (binding.recycler.getAdapter() == null) {
            binding.recycler.setAdapter(adapter);
        }
        adapter.setPrograms(programs);
    }
}