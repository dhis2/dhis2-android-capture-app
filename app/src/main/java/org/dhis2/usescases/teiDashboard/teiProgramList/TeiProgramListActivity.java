package org.dhis2.usescases.teiDashboard.teiProgramList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.ActivityTeiProgramListBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.main.program.ProgramViewModel;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;

import java.util.List;

import javax.inject.Inject;

/**
 * QUADRAM. Created by Cristian on 13/02/2018.
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
    public void goToEnrollmentScreen(String enrollmentUid, String programUid) {
        SetProgramTheme(presenter.getProgramColor(programUid));
        Intent data = new Intent();
        data.putExtra("GO_TO_ENROLLMENT", enrollmentUid);
        data.putExtra("GO_TO_ENROLLMENT_PROGRAM", programUid);
        setResult(RESULT_OK, data);

        finish();
    }

    @Override
    public void changeCurrentProgram(String program) {
        if (program != null)
            SetProgramTheme(presenter.getProgramColor(program));
        Intent data = new Intent();
        data.putExtra("CHANGE_PROGRAM", program);
        setResult(RESULT_OK, data);

        finish();
    }

    private void SetProgramTheme(String color) {
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
            int colorPrimary;
            switch (prefs.getInt(Constants.THEME, R.style.AppTheme)) {
                case R.style.AppTheme:
                    colorPrimary = R.color.colorPrimary;
                    break;
                case R.style.RedTheme:
                    colorPrimary = R.color.colorPrimaryRed;
                    break;
                case R.style.OrangeTheme:
                    colorPrimary = R.color.colorPrimaryOrange;
                    break;
                case R.style.GreenTheme:
                    colorPrimary = R.color.colorPrimaryGreen;
                    break;
                default:
                    colorPrimary = R.color.colorPrimary;
                    break;
            }
            binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, colorPrimary));
        }

        binding.executePendingBindings();
        setTheme(prefs.getInt(Constants.PROGRAM_THEME, prefs.getInt(Constants.THEME, R.style.AppTheme)));

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            TypedValue typedValue = new TypedValue();
            TypedArray a = obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimaryDark});
            int colorToReturn = a.getColor(0, 0);
            a.recycle();
            window.setStatusBarColor(colorToReturn);
        }
    }
}