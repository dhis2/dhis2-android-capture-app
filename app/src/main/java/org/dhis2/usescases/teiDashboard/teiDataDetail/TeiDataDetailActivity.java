package org.dhis2.usescases.teiDashboard.teiDataDetail;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dhis2.App;
import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.data.forms.FormFragment;
import org.dhis2.databinding.ActivityTeidataDetailBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.map.MapSelectorActivity;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DatePickerUtils;
import org.dhis2.utils.FileResourcesUtil;
import org.dhis2.utils.custom_views.CoordinatesView;
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.enrollment.internal.EnrollmentFields;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class TeiDataDetailActivity extends ActivityGlobalAbstract implements TeiDataDetailContracts.View, CoordinatesView.OnMapPositionClick {
    ActivityTeidataDetailBinding binding;

    @Inject
    TeiDataDetailContracts.Presenter presenter;

    private DashboardProgramModel dashboardProgramModel;
    private EnrollmentStatus enrollmentStatus;
    private PublishSubject<Geometry> onCoordinatesChanged;
    private PublishSubject<Geometry> onTeiCoordinatesChanged;
    private PublishSubject<Unit> onCoordinatesCleared;
    private PublishSubject<Unit> onTeiCoordinatesCleared;
    private CoordinatesView coordinatesViewToUpdate;
    private FeatureType teFeatureType;
    private FeatureType enrollmentFeatureType;

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


        binding.coordinatesView.setIsBgTransparent(true);
        binding.coordinatesView.setMapListener(this);
        binding.coordinatesView.setCurrentLocationListener(this::publishCoordinatesChanged);
        binding.coordinatesView.setLabel(getString(R.string.enrollment_coordinates));

        enrollmentFeatureType = program.getCurrentProgram().featureType();
        binding.coordinatesView.setVisibility(enrollmentFeatureType != FeatureType.NONE ? View.VISIBLE : View.GONE);
        if (enrollmentFeatureType != FeatureType.NONE) {
            binding.coordinatesView.setFeatureType(enrollmentFeatureType);
        }

        if (program.getCurrentEnrollment().geometry() != null)
            binding.coordinatesView.updateLocation(program.getCurrentEnrollment().geometry());


        for (ProgramStage programStage : program.getProgramStages())
            if (programStage.autoGenerateEvent())
                if (programStage.reportDateToUse() != null && programStage.reportDateToUse().equals("enrollmentDate") || programStage.generatedByEnrollmentDate()) {
                    binding.enrollmentDate.setEnabled(false);
                    binding.enrollmentDate.setBackground(null);
                } else {
                    binding.incidentDate.setEnabled(false);
                    binding.incidentDate.setBackground(null);
                }

        presenter.checkTeiCoordinates();

        supportStartPostponedEnterTransition();

        initForm();

    }

    private void initForm() {
     /*   Fragment fragment = FormFragment.newInstance(
                FormViewArguments.createForEnrollment(dashboardProgramModel.getCurrentEnrollment().uid()), true,
                false);
        ((FormFragment) fragment).setSaveButtonTEIDetail(binding.next);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.dataFragment, fragment)
                .commit();*/
    }

    @NonNull
    @Override
    public Observable<Geometry> reportCoordinatesChanged() {
        onCoordinatesChanged = PublishSubject.create();
        return onCoordinatesChanged;
    }

    @NonNull
    @Override
    public Observable<Geometry> teiCoordinatesChanged() {
        onTeiCoordinatesChanged = PublishSubject.create();
        return onTeiCoordinatesChanged;
    }

    @NonNull
    @Override
    public Observable<Unit> reportCoordinatesCleared() {
        onCoordinatesCleared = PublishSubject.create();
        return onCoordinatesCleared;
    }

    @NonNull
    @Override
    public Observable<Unit> teiCoordinatesCleared() {
        onTeiCoordinatesCleared = PublishSubject.create();
        return onTeiCoordinatesCleared;
    }

    @Override
    public Consumer<TrackedEntityType> renderTeiCoordinates() {
        return trackedEntityType -> {
            if (trackedEntityType.featureType() != null) {

                binding.teiCoordinatesView.setIsBgTransparent(true);
                binding.teiCoordinatesView.setMapListener(this);
                binding.teiCoordinatesView.setCurrentLocationListener(this::publishTeiCoodinatesChanged);

                teFeatureType = trackedEntityType.featureType();
                binding.teiCoordinatesView.setVisibility(teFeatureType != FeatureType.NONE ? View.VISIBLE : View.GONE);
                binding.teiCoordinatesView.setLabel(String.format("%s %s", getString(R.string.tei_coordinates), trackedEntityType.name()));
                if (teFeatureType != FeatureType.NONE) {
                    binding.teiCoordinatesView.setFeatureType(teFeatureType);
                }

                if (binding.getDashboardModel().getTei().geometry() != null) {
                    binding.teiCoordinatesView.updateLocation(binding.getDashboardModel().getTei().geometry());
                }

            }
        };
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
        if (requestCode == Constants.RQ_MAP_LOCATION_VIEW && resultCode == RESULT_OK) {
            FeatureType locationType = FeatureType.valueOf(data.getStringExtra(MapSelectorActivity.Companion.getLOCATION_TYPE_EXTRA()));
            String dataExtra = data.getStringExtra(MapSelectorActivity.Companion.getDATA_EXTRA());
            Geometry geometry;
            if (locationType == FeatureType.POINT) {
                Type type = new TypeToken<List<Double>>() {
                }.getType();
                geometry = GeometryHelper.createPointGeometry(new Gson().fromJson(dataExtra, type));
            } else if (locationType == FeatureType.POLYGON) {
                Type type = new TypeToken<List<List<List<Double>>>>() {
                }.getType();
                geometry = GeometryHelper.createPolygonGeometry(new Gson().fromJson(dataExtra, type));
            } else {
                Type type = new TypeToken<List<List<List<List<Double>>>>>() {
                }.getType();
                geometry = GeometryHelper.createMultiPolygonGeometry(new Gson().fromJson(dataExtra, type));
            }
            coordinatesViewToUpdate.updateLocation(geometry);
            if (coordinatesViewToUpdate.getId() == R.id.coordinates_view)
                publishCoordinatesChanged(geometry);
            else
                publishTeiCoodinatesChanged(geometry);
            this.coordinatesViewToUpdate = null;
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

    @Override
    public void onMapPositionClick(CoordinatesView coordinatesView) {
        this.coordinatesViewToUpdate = coordinatesView;
        FeatureType featureType = coordinatesView.getId() == R.id.tei_coordinates_view ? teFeatureType : enrollmentFeatureType;
        startActivityForResult(MapSelectorActivity.Companion.create(getActivity(), featureType, coordinatesView.currentCoordinates()), Constants.RQ_MAP_LOCATION_VIEW);
    }

    private void publishCoordinatesChanged(Geometry geometry) {
        if (onCoordinatesChanged != null && geometry != null) {
            onCoordinatesChanged.onNext(geometry);
        } else if (geometry == null)
            onCoordinatesCleared.onNext(new Unit());
    }

    private void publishTeiCoodinatesChanged(Geometry geometry) {
        if (onTeiCoordinatesChanged != null && geometry != null) {
            onTeiCoordinatesChanged.onNext(geometry);
        } else if (geometry == null)
            onTeiCoordinatesCleared.onNext(new Unit());
    }


}