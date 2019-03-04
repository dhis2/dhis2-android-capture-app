package org.dhis2.usescases.teiDashboard.teiProgramList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.ActivityTeiProgramListBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.main.program.ProgramViewModel;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class TeiProgramListActivity extends ActivityGlobalAbstract implements TeiProgramListContract.TeiProgramListView {

    private ActivityTeiProgramListBinding binding;

    @Inject
    TeiProgramListContract.TeiProgramListPresenter presenter;
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
    public void goToEnrollmentScreen(String enrollmentUid, String programUid) {
        setProgramTheme(presenter.getProgramColor(programUid));
        Intent data = new Intent();
        data.putExtra("GO_TO_ENROLLMENT", enrollmentUid);
        setResult(RESULT_OK, data);

        finish();
    }

    @Override
    public void changeCurrentProgram(String program) {
        if (program != null)
            setProgramTheme(presenter.getProgramColor(program));
        Intent data = new Intent();
        data.putExtra("CHANGE_PROGRAM", program);
        setResult(RESULT_OK, data);

        finish();
    }

    private void setProgramTheme(String color) {
        int programTheme = ColorUtils.getThemeFromColor(color);
        int programColor = ColorUtils.getColorFrom(color,
                ColorUtils.getPrimaryColor(this, ColorUtils.ColorType.PRIMARY));

        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        if (programTheme != -1) {
            prefs.edit().putInt(Constants.PROGRAM_THEME, programTheme).apply();
            binding.toolbar.setBackgroundColor(programColor);
        } else {
            prefs.edit().remove(Constants.PROGRAM_THEME).apply();
            int colorPrimary = getPrimaryColorFromTheme();
            binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, colorPrimary));
        }

        binding.executePendingBindings();
        applyColors();
    }
}