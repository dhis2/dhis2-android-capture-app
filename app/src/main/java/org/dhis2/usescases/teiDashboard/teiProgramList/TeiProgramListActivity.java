package org.dhis2.usescases.teiDashboard.teiProgramList;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.service.SyncStatusController;
import org.dhis2.databinding.ActivityTeiProgramListBinding;
import org.dhis2.ui.ThemeManager;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.main.program.ProgramViewModel;

import java.util.List;

import javax.inject.Inject;

import kotlin.Unit;

public class TeiProgramListActivity extends ActivityGlobalAbstract implements TeiProgramListContract.View {

    private ActivityTeiProgramListBinding binding;

    @Inject
    TeiProgramListContract.Presenter presenter;
    @Inject
    TeiProgramListAdapter adapter;
    @Inject
    ThemeManager themeManager;
    @Inject
    SyncStatusController syncStatusController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        String trackedEntityId = getIntent().getStringExtra("TEI_UID");
        ((App) getApplicationContext()).userComponent().plus(new TeiProgramListModule(this, trackedEntityId)).inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tei_program_list);
        binding.setPresenter(presenter);

        syncStatusController.observeDownloadProcess().observe(this, syncStatusData -> presenter.refreshData());
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init();
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
    public void goToEnrollmentScreen(String enrollmentUid, String programUid) {
        themeManager.setProgramTheme(programUid);
        updateToolbar(programUid);
        Intent data = new Intent();
        data.putExtra("GO_TO_ENROLLMENT", enrollmentUid);
        data.putExtra("GO_TO_ENROLLMENT_PROGRAM", programUid);
        setResult(RESULT_OK, data);

        finish();
    }

    @Override
    public void changeCurrentProgram(String program, String enrollmentUid) {
        if (program != null) {
            themeManager.setProgramTheme(program);
            updateToolbar(program);
        }
        Intent data = new Intent();
        data.putExtra("CHANGE_PROGRAM", program);
        data.putExtra("CHANGE_PROGRAM_ENROLLMENT", enrollmentUid);
        setResult(RESULT_OK, data);

        finish();
    }

    private void updateToolbar(String programUid) {
        themeManager.getThemePrimaryColor(
                programUid,
                programColor -> {
                    binding.toolbar.setBackgroundColor(programColor);
                    return Unit.INSTANCE;
                },
                themeColorRes -> {
                    binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, themeColorRes));
                    return Unit.INSTANCE;
                });
    }

    @Override
    public void displayBreakGlassError(String teTypeName) {
        displayMessage(getString(R.string.break_glass_error_v2, teTypeName));
    }

    @Override
    public void displayAccessError() {
        displayMessage(getString(R.string.search_access_error));
    }
}