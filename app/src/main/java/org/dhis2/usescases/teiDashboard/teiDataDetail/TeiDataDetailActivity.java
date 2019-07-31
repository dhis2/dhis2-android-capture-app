package org.dhis2.usescases.teiDashboard.teiDataDetail;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.widget.DatePicker;

import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import org.dhis2.App;
import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.data.forms.FormFragment;
import org.dhis2.data.forms.FormViewArguments;
import org.dhis2.databinding.ActivityTeidataDetailBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.map.MapSelectorActivity;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DatePickerUtils;
import org.dhis2.utils.FileResourcesUtil;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.enrollment.internal.EnrollmentFields;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.Calendar;
import java.util.Date;
import java.io.File;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class TeiDataDetailActivity extends ActivityGlobalAbstract implements TeiDataDetailContracts.View {
    ActivityTeidataDetailBinding binding;

    @Inject
    TeiDataDetailContracts.Presenter presenter;

    private DashboardProgramModel dashboardProgramModel;
    private EnrollmentStatus enrollmentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new TeiDataDetailModule(getIntent().getStringExtra("ENROLLMENT_UID"))).inject(this);

        supportPostponeEnterTransition();
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_teidata_detail);
        binding.setPresenter(presenter);

        init(getIntent().getStringExtra("TEI_UID"), getIntent().getStringExtra("PROGRAM_UID"), getIntent().getStringExtra("ENROLLMENT_UID"));

        binding.programLockLayout.setOnClickListener(this::showScheduleContentOptions);
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    private void showScheduleContentOptions(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.deactivate:
                    presenter.onDeactivate(dashboardProgramModel);
                    break;
                case R.id.complete:
                    presenter.onComplete(dashboardProgramModel);
                    break;
                case R.id.activate:
                    presenter.onActivate(dashboardProgramModel);
                    break;
                case R.id.reOpen:
                    presenter.onReOpen(dashboardProgramModel);
                    break;
                default:
                    break;
            }
            return false;
        });// to implement on click event on items of menu
        MenuInflater inflater = popup.getMenuInflater();
        int menuId = 0;
        if (enrollmentStatus == EnrollmentStatus.ACTIVE) {
            menuId = R.menu.tei_detail_options_active;
        } else if (enrollmentStatus == EnrollmentStatus.CANCELLED) {
            menuId = R.menu.tei_detail_options_cancelled;
        } else if (enrollmentStatus == EnrollmentStatus.COMPLETED) {
            menuId = R.menu.tei_detail_options_completed;
        }
        if (menuId != 0) {
            inflater.inflate(menuId, popup.getMenu());
            popup.show();
        }
    }


    @Override
    public void init(String teiUid, String programUid, String enrollmentUid) {
        presenter.init(this, teiUid, programUid, enrollmentUid);
    }

    @Override
    public void setData(DashboardProgramModel program) {
        this.dashboardProgramModel = program;
        this.enrollmentStatus = program.getCurrentEnrollment().status();
        binding.setDashboardModel(program);
        binding.setProgram(program.getCurrentProgram());
        binding.setEnrollmentStatus(program.getCurrentEnrollment().status());
        binding.setEnrollmentDate(dashboardProgramModel.getCurrentEnrollment().enrollmentDate());
        binding.setIncidentDate(dashboardProgramModel.getCurrentEnrollment().incidentDate());
        binding.executePendingBindings();

        if (program.getCurrentProgram().captureCoordinates()) {
            binding.coordinatesLayout.setVisibility(View.VISIBLE);
            binding.location1.setOnClickListener(v -> presenter.onLocationClick());
            binding.location2.setOnClickListener(v -> presenter.onLocation2Click());
        }

        for(ProgramStageModel programStage: program.getProgramStages())
            if(programStage.autoGenerateEvent())
                if(programStage.reportDateToUse() != null && programStage.reportDateToUse().equals(EnrollmentFields.ENROLLMENT_DATE) || programStage.generatedByEnrollmentDate()) {
                    binding.enrollmentDate.setEnabled(false);
                    binding.enrollmentDate.setBackground(null);
                }else {
                    binding.incidentDate.setEnabled(false);
                    binding.incidentDate.setBackground(null);
                }

        supportStartPostponedEnterTransition();


        initForm();

    }

    private void initForm() {
        Fragment fragment = FormFragment.newInstance(
                FormViewArguments.createForEnrollment(dashboardProgramModel.getCurrentEnrollment().uid()), true,
                false);
        ((FormFragment) fragment).setSaveButtonTEIDetail(binding.next);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.dataFragment, fragment)
                .commit();
    }


    @Override
    public Consumer<EnrollmentStatus> handleStatus() {
        return enrollmentStatus -> {
            this.enrollmentStatus = enrollmentStatus;
            Bindings.setEnrolmentIcon(binding.programLock, enrollmentStatus);
            Bindings.setEnrolmentText(binding.programLockText, enrollmentStatus);
            binding.setEnrollmentStatus(enrollmentStatus);
            binding.executePendingBindings();
            initForm();
        };
    }

    @Override
    public void setLocation(double latitude, double longitude) {
        binding.lat.setText(String.format(Locale.US, "%.5f", latitude));
        binding.lon.setText(String.format(Locale.US, "%.5f", longitude));
    }

    @Override
    public void showTeiImage(String fileName) {
        File file = FileResourcesUtil.getFileForAttribute(this, fileName);
        Glide.with(this)
                .load(file)
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .placeholder(R.drawable.photo_temp_gray)
                .error(R.drawable.photo_temp_gray)
                .transition(withCrossFade())
                .transform(new CircleCrop())
                .into(binding.teiImage);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        if (getSupportFragmentManager().getFragments().get(0) instanceof FormFragment)
            ((FormFragment) getSupportFragmentManager().getFragments().get(0)).onBackPressed(false);
        else
            finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.RQ_MAP_LOCATION && resultCode == RESULT_OK) {
            String savedLat = data.getStringExtra(MapSelectorActivity.LATITUDE);
            String savedLon = data.getStringExtra(MapSelectorActivity.LONGITUDE);
            setLocation(Double.valueOf(savedLat), Double.valueOf(savedLon));
            presenter.saveLocation(Double.valueOf(savedLat), Double.valueOf(savedLon));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void showCustomIncidentCalendar(Date date) {
        DatePickerUtils.getDatePickerDialog(
                this,
                dashboardProgramModel.getCurrentProgram().incidentDateLabel(),
                date,
                false,
                new DatePickerUtils.OnDatePickerClickListener() {
                    @Override
                    public void onNegativeClick() {
                        Date date = new Date();
                        presenter.updateIncidentDate(date);
                        binding.setIncidentDate(date);
                    }

                    @Override
                    public void onPositiveClick(DatePicker datePicker) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                        presenter.updateIncidentDate(calendar.getTime());
                        binding.setIncidentDate(calendar.getTime());
                    }
                }).show();
    }

    @Override
    public void showCustomEnrollmentCalendar(Date date) {
        DatePickerUtils.getDatePickerDialog(
                this,
                dashboardProgramModel.getCurrentProgram().enrollmentDateLabel(),
                date,
                false,
                new DatePickerUtils.OnDatePickerClickListener() {
                    @Override
                    public void onNegativeClick() {
                        Date date = new Date();
                        presenter.updateEnrollmentDate(date);
                        binding.setEnrollmentDate(date);
                    }

                    @Override
                    public void onPositiveClick(DatePicker datePicker) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                        presenter.updateEnrollmentDate(calendar.getTime());
                        binding.setEnrollmentDate(calendar.getTime());
                    }
                }).show();
    }

}