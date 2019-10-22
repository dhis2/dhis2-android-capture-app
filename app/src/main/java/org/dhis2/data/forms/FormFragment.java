package org.dhis2.data.forms;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.rxbinding2.view.RxView;

import org.dhis2.App;
import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryFragment;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.section.viewmodels.date.DatePickerDialogFragment;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.map.MapSelectorActivity;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DialogClickListener;
import org.dhis2.utils.customviews.CategoryComboDialog;
import org.dhis2.utils.customviews.CoordinatesView;
import org.dhis2.utils.customviews.CustomDialog;
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;
import org.hisp.dhis.rules.models.RuleActionErrorOnCompletion;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleActionWarningOnCompletion;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.DELETE_AND_BACK;


public class FormFragment extends FragmentGlobalAbstract implements FormView, CoordinatesView.OnMapPositionClick, CoordinatesView.OnCurrentLocationClick {
    private static final String FORM_VIEW_ARGUMENTS = "formViewArguments";
    private static final String FORM_VIEW_ACTIONBAR = "formViewActionbar";
    private static final String IS_ENROLLMENT = "isEnrollment";
    private static final String FORM_VIEW_TABLAYOUT = "formViewTablayout";
    private static final int RC_GO_BACK = 101;

    private View nextButton;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;

    @Inject
    FormPresenter formPresenter;

    private FormSectionAdapter formSectionAdapter;
    private PublishSubject<String> onReportDateChanged;
    private PublishSubject<String> onIncidentDateChanged;
    private PublishSubject<Geometry> onCoordinatesChanged;
    private PublishSubject<Geometry> onTeiCoordinatesChanged;
    private PublishSubject<Unit> onCoordinatesCleared;

    private TextInputLayout reportDateLayout;
    private TextInputEditText reportDate;
    private PublishSubject<ReportStatus> undoObservable;
    private PublishSubject<EnrollmentStatus> undoSaveObservable;
    private CoordinatorLayout coordinatorLayout;
    private TextInputLayout incidentDateLayout;
    private TextInputEditText incidentDate;

    private CoordinatesView coordinatesViewToUpdate;
    private CoordinatesView coordinatesView;
    private CoordinatesView teiCoordinatesView;

    private boolean isEnrollment;
    private Trio<String, String, String> enrollmentTrio;

    private String messageOnComplete = "";
    private LinearLayout dateLayout;
    private View datesLayout;
    private static final int RQ_EVENT = 9876;
    private RuleActionErrorOnCompletion errorOnCompletion;
    private RuleActionShowError showError;
    private String programUid;
    private String teiUid;
    private Date openingDate;
    private Date closingDate;
    private boolean mandatoryDelete = true;

    private ProgressBar progressBar;
    private View saveButton;
    private DataEntryFragment enrollmentFragment;
    private boolean needInitial;
    private String programStageUid;
    private String enrollmentUid;
    private FeatureType teFeatureType;
    private FeatureType enrollmentFeatureType;

    public View getDatesLayout() {
        return datesLayout;
    }

    public FormFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(@NonNull FormViewArguments formViewArguments, boolean isEnrollment, boolean showActionBar) {
        FormFragment fragment = new FormFragment();
        Bundle args = new Bundle();
        args.putParcelable(FORM_VIEW_ARGUMENTS, formViewArguments);
        args.putBoolean(IS_ENROLLMENT, isEnrollment);
        args.putBoolean(FORM_VIEW_ACTIONBAR, showActionBar);
        boolean showTabLayout = false;
        if (showActionBar)
            showTabLayout = true;
        args.putBoolean(FORM_VIEW_TABLAYOUT, showTabLayout);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment newInstance(@NonNull FormViewArguments formViewArguments, boolean isEnrollment, boolean showActionBar, boolean showTabLayout) {
        FormFragment fragment = new FormFragment();
        Bundle args = new Bundle();
        args.putParcelable(FORM_VIEW_ARGUMENTS, formViewArguments);
        args.putBoolean(IS_ENROLLMENT, isEnrollment);
        args.putBoolean(FORM_VIEW_ACTIONBAR, showActionBar);
        args.putBoolean(FORM_VIEW_TABLAYOUT, showTabLayout);
        fragment.setArguments(args);
        return fragment;
    }

    public void setSaveButtonTEIDetail(View view) {
        this.saveButton = view;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dateLayout = view.findViewById(R.id.date_layout);
        nextButton = view.findViewById(R.id.next);
        viewPager = view.findViewById(R.id.viewpager_dataentry);
        tabLayout = view.findViewById(R.id.tablayout_data_entry);
        toolbar = view.findViewById(R.id.toolbar);
        View appBarLayout = view.findViewById(R.id.appbarlayout_data_entry);
        datesLayout = view.findViewById(R.id.data_entry_dates);
        reportDate = view.findViewById(R.id.report_date);
        reportDateLayout = view.findViewById(R.id.report_date_layout);
        incidentDateLayout = view.findViewById(R.id.incident_date_layout);
        incidentDate = view.findViewById(R.id.incident_date_text);
        coordinatorLayout = view.findViewById(R.id.coordinatorlayout_form);
        progressBar = view.findViewById(R.id.progress);
        Bindings.setProgressColor(progressBar, R.color.colorPrimary);
        formSectionAdapter = new FormSectionAdapter(getChildFragmentManager());
        viewPager.setAdapter(formSectionAdapter);
        tabLayout.setupWithViewPager(viewPager);

        if (getArguments() != null && getArguments().getBoolean(FORM_VIEW_ACTIONBAR))
            setupActionBar();
        else if (getArguments() != null && getArguments().getBoolean(FORM_VIEW_TABLAYOUT)) {
            toolbar.setVisibility(View.GONE);
            datesLayout.setVisibility(View.GONE);
            nextButton.setVisibility(View.GONE);
            tabLayout.setVisibility(View.VISIBLE);
        } else {
            appBarLayout.setVisibility(View.GONE);
            nextButton.setVisibility(View.GONE);
        }
        coordinatesView = view.findViewById(R.id.coordinates_view);
        teiCoordinatesView = view.findViewById(R.id.tei_coordinates_view);

        if (isEnrollment) {
            coordinatesView.setIsBgTransparent(false);
            coordinatesView.setMapListener(this);
            coordinatesView.setCurrentLocationListener(this);
            coordinatesView.setLabel(getString(R.string.enrollment_coordinates));
            teiCoordinatesView.setIsBgTransparent(false);
            teiCoordinatesView.setMapListener(this);
            teiCoordinatesView.setCurrentLocationListener(this);
        }
        setupActionBar();

        formPresenter.onAttach(this);
        if (saveButton != null)
            formPresenter.initializeSaveObservable();
    }

    private void setupActionBar() {
        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setHomeButtonEnabled(true);
            }
        }
    }

    @SuppressLint("RxDefaultScheduler")
    @NonNull
    @Override
    public Observable<ReportStatus> eventStatusChanged() {
        undoObservable = PublishSubject.create();
        return undoObservable.mergeWith(RxView.clicks(nextButton).map(o -> getReportStatusFromButton())).debounce(500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onNext(ReportStatus reportStatus) {
        if (viewPager.getAdapter() != null && viewPager.getCurrentItem() < viewPager.getAdapter().getCount() - 1) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);

            if (viewPager.getCurrentItem() == viewPager.getAdapter().getCount() - 1) {
                ((Button) nextButton).setText(getString(R.string.save));
            } else
                ((Button) nextButton).setText(getString(R.string.next));

        } else {
            formPresenter.checkMandatoryFields();
        }
    }

    @NonNull
    @Override
    public Observable<String> reportDateChanged() {
        onReportDateChanged = PublishSubject.create();
        return onReportDateChanged;
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
    public Observable<Unit> reportCoordinatesCleared(){
        onCoordinatesCleared = PublishSubject.create();
        return onCoordinatesCleared;
    }

    @NonNull
    @Override
    public Observable<String> incidentDateChanged() {
        onIncidentDateChanged = PublishSubject.create();
        return onIncidentDateChanged;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (getArguments() != null && getActivity() != null) {
            FormViewArguments arguments = getArguments().getParcelable(FORM_VIEW_ARGUMENTS);
            if (arguments != null) {
                ((App) getActivity().getApplicationContext())
                        .createFormComponent(new FormModule(arguments))
                        .inject(this);
            }

            this.isEnrollment = getArguments().getBoolean(IS_ENROLLMENT);
            if (isEnrollment)
                enrollmentUid = arguments.uid();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        formPresenter.onDetach();
        super.onDestroy();
    }

    @NonNull
    @Override
    public Consumer<List<FormSectionViewModel>> renderSectionViewModels() {
        return sectionViewModels -> {
            int currentPostion = -1;
            if (formSectionAdapter.getCount() > 0 && formSectionAdapter.areDifferentSections(sectionViewModels)) {
                currentPostion = viewPager.getCurrentItem();
                for (Fragment fragment : getChildFragmentManager().getFragments()) {
                    if (fragment instanceof DataEntryFragment) {
                        continue;
                    } else if (fragment != null) {
                        getChildFragmentManager().beginTransaction().remove(fragment).commit();
                    }
                }
                formSectionAdapter = new FormSectionAdapter(getChildFragmentManager());
                viewPager.setAdapter(formSectionAdapter);
            }
            formSectionAdapter.swapData(sectionViewModels);
            tabLayout.setupWithViewPager(viewPager);
            if (currentPostion != -1)
                viewPager.setCurrentItem(currentPostion, false);
            if (sectionViewModels.isEmpty()) {
                Timber.d("Show empty state");
                // TODO: Show empty state
            }
            if (viewPager.getAdapter() != null && viewPager.getCurrentItem() == viewPager.getAdapter().getCount() - 1) {
                ((Button) nextButton).setText(getString(R.string.save));
            }
            if (!getChildFragmentManager().getFragments().isEmpty())
                enrollmentFragment = ((DataEntryFragment) getChildFragmentManager().getFragments().get(0));
        };
    }

    @NonNull
    @Override
    public Consumer<Pair<Program, String>> renderReportDate() {
        return programAndDate -> {
            reportDate.setText(programAndDate.val1());
            reportDateLayout.setHint(programAndDate.val0().enrollmentDateLabel());
        };
    }

    @NonNull
    @Override
    public Consumer<Pair<Program, String>> renderIncidentDate() {
        return programAndDate -> {
            incidentDateLayout.setHint(programAndDate.val0().incidentDateLabel());
            incidentDateLayout.setVisibility(View.VISIBLE);
            incidentDate.setText(programAndDate.val1());
        };
    }

    @NonNull
    @Override
    public Consumer<FeatureType> renderCaptureCoordinates() {
        return featureType -> {
            coordinatesView.setVisibility(featureType != FeatureType.NONE ? View.VISIBLE : View.GONE);
            enrollmentFeatureType = featureType;
            if (featureType != FeatureType.NONE) {
                coordinatesView.setFeatureType(featureType);
            }
        };
    }


    @Override
    public Consumer<TrackedEntityType> renderTeiCoordinates() {
        return trackedEntityType -> {
            if(trackedEntityType.featureType() != null) {
                teFeatureType = trackedEntityType.featureType();
                teiCoordinatesView.setVisibility(teFeatureType != FeatureType.NONE ? View.VISIBLE : View.GONE);
                teiCoordinatesView.setLabel(String.format("%s %s", getString(R.string.tei_coordinates), trackedEntityType.name()));
                if (teFeatureType != FeatureType.NONE) {
                    teiCoordinatesView.setFeatureType(teFeatureType);
                }
            }
        };
    }

    @Override
    public void setMinMaxDates(Date openingDate, Date closingDate) {
        this.openingDate = openingDate;
        this.closingDate = closingDate;
    }

    @NonNull
    @Override
    public Consumer<String> renderTitle() {
        return title -> {
            if (isEnrollment)
                toolbar.setTitle(String.format(getString(R.string.enroll_in), title));
            else
                toolbar.setTitle(title);
        };
    }

    @NonNull
    @Override
    public Consumer<ReportStatus> renderStatus() {
        return eventStatus -> nextButton.setActivated(true);
    }

    @NonNull
    @Override
    public Consumer<Trio<String, String, String>> finishEnrollment() {
        return trio -> {
            enrollmentTrio = trio;
            progressBar.setVisibility(View.VISIBLE);
            formPresenter.checkMandatoryFields();
            if (trio.val2() != null && !trio.val2().isEmpty())
                formPresenter.getNeedInitial(trio.val2());
        };
    }

    @Override
    public void setNeedInitial(boolean needInitial, String programStageUid) {
        this.needInitial = needInitial;
        this.programStageUid = programStageUid;
    }


    @Override
    public void renderStatusChangeSnackBar(@NonNull ReportStatus reportStatus) {
        String snackBarMessage = reportStatus == ReportStatus.COMPLETED ? getString(R.string.completed) : getString(R.string.activated);

        Snackbar.make(coordinatorLayout, snackBarMessage, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.revert), v1 -> {
                    if (undoObservable == null) {
                        return;
                    }
                    if (reportStatus == ReportStatus.COMPLETED) {
                        undoObservable.onNext(ReportStatus.ACTIVE);
                    } else {
                        undoObservable.onNext(ReportStatus.COMPLETED);
                    }
                })
                .show();
    }


    private ReportStatus getReportStatusFromButton() {
        datesLayout.requestFocus();
        return nextButton.isActivated() ? ReportStatus.ACTIVE : ReportStatus.COMPLETED;
    }

    @Override
    public void initReportDatePicker(boolean reportAllowFutureDates, boolean incidentAllowFutureDates) {
        reportDate.setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                DatePickerDialogFragment dialog = DatePickerDialogFragment.create(reportAllowFutureDates);
                dialog.setOpeningClosingDates(openingDate, closingDate);
                dialog.show(getFragmentManager());
                dialog.setFormattedOnDateSetListener(publishReportDateChange());
            }
        });

        incidentDate.setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                DatePickerDialogFragment dialog = DatePickerDialogFragment.create(incidentAllowFutureDates);
                dialog.setOpeningClosingDates(openingDate, closingDate);
                dialog.show(getFragmentManager());
                dialog.setFormattedOnDateSetListener(publishIncidentDateChange());
            }
        });

        reportDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (getFragmentManager() != null) {
                    DatePickerDialogFragment dialog = DatePickerDialogFragment.create(reportAllowFutureDates);
                    dialog.setOpeningClosingDates(openingDate, closingDate);
                    dialog.show(getFragmentManager());
                    dialog.setFormattedOnDateSetListener(publishReportDateChange());
                }
            }
        });

        incidentDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (getFragmentManager() != null) {
                    DatePickerDialogFragment dialog = DatePickerDialogFragment.create(incidentAllowFutureDates);
                    dialog.setOpeningClosingDates(openingDate, closingDate);
                    dialog.show(getFragmentManager());
                    dialog.setFormattedOnDateSetListener(publishIncidentDateChange());
                }
            }
        });
    }

    @NonNull
    private DatePickerDialogFragment.FormattedOnDateSetListener publishReportDateChange() {

        return new DatePickerDialogFragment.FormattedOnDateSetListener() {
            @Override
            public void onDateSet(@NonNull Date date) {
                if (onReportDateChanged != null) {
                    onReportDateChanged.onNext(BaseIdentifiableObject.DATE_FORMAT.format(date));
                }
            }

            @Override
            public void onClearDate() {
                onReportDateChanged.onNext(BaseIdentifiableObject.DATE_FORMAT.format(new Date()));
            }
        };
    }

    @NonNull
    private DatePickerDialogFragment.FormattedOnDateSetListener publishIncidentDateChange() {

        return new DatePickerDialogFragment.FormattedOnDateSetListener() {
            @Override
            public void onDateSet(@NonNull Date date) {
                if (onIncidentDateChanged != null) {
                    onIncidentDateChanged.onNext(BaseIdentifiableObject.DATE_FORMAT.format(date));
                }
            }

            @Override
            public void onClearDate() {
                onIncidentDateChanged.onNext(BaseIdentifiableObject.DATE_FORMAT.format(new Date()));
            }
        };

    }

    @Override
    public void onMapPositionClick(CoordinatesView coordinatesView) {
        if (getActivity() != null && isAdded()) {
            this.coordinatesViewToUpdate = coordinatesView;
            FeatureType featureType = coordinatesView.getId() == R.id.tei_coordinates_view ? teFeatureType : enrollmentFeatureType;
            startActivityForResult(MapSelectorActivity.Companion.create(getActivity(), featureType,coordinatesView.currentCoordinates()), Constants.RQ_MAP_LOCATION_VIEW);
        }
    }

    @Override
    public void onCurrentLocationClick(Geometry geometry) {
        if (coordinatesViewToUpdate.getId() == R.id.coordinates_view)
            publishCoordinatesChanged(geometry);
        else
            publishTeiCoodinatesChanged(geometry);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.RQ_MAP_LOCATION_VIEW:
                if (data != null) {
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

                    this.coordinatesViewToUpdate = null;
                }
                break;
            case RQ_EVENT:
                if (data != null)
                    openDashboard(data.getStringExtra(Constants.EVENT_UID));
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void openDashboard(@Nullable String eventUid) {
        Bundle bundle = new Bundle();
        bundle.putString("PROGRAM_UID", programUid);
        bundle.putString("TEI_UID", teiUid);
        if (eventUid != null)
            bundle.putString(Constants.EVENT_UID, eventUid);
        startActivity(TeiDashboardMobileActivity.class, bundle, false, false, null);
        if (getActivity() != null && isAdded()) {
            getActivity().finish();
        }
    }

    private void publishCoordinatesChanged(Geometry geometry) {
        if (onCoordinatesChanged != null && geometry != null) {
            onCoordinatesChanged.onNext(geometry);
        }
    }

    private void publishTeiCoodinatesChanged(Geometry geometry) {
        if (onTeiCoordinatesChanged != null && geometry != null) {
            onTeiCoordinatesChanged.onNext(geometry);
        }
    }

    @Override
    public void messageOnComplete(String content, boolean canComplete) {
        this.messageOnComplete = content;
    }

    @Override
    public void hideDates() {
        this.dateLayout.setVisibility(View.GONE);
    }

    @Override
    public void isMandatoryFieldsRequired(List<FieldViewModel> viewModels) {
        boolean mandatoryRequired = false;
        for (FieldViewModel viewModel : viewModels) {
            if (viewModel.mandatory() && isEmpty(viewModel.value()))
                mandatoryRequired = true;
        }
        if (!mandatoryRequired) {
            if (enrollmentTrio != null) {
                if (!enrollmentTrio.val2().isEmpty()) { //val0 is teiUid uid, val1 is programUid, val2 is event uid
                    this.programUid = enrollmentTrio.val1();
                    this.teiUid = enrollmentTrio.val0();
                    if (needInitial) {
                        Bundle bundle = EventInitialActivity.getBundle(
                                programUid,
                                enrollmentTrio.val2(),
                                null,
                                enrollmentTrio.val0(),
                                null,
                                formPresenter.getEnrollmentOu(enrollmentUid),
                                programStageUid,
                                enrollmentUid,
                                0,
                                EnrollmentStatus.ACTIVE);
                        Intent eventInitialIntent = new Intent(getAbstracContext(), EventInitialActivity.class);
                        eventInitialIntent.putExtras(bundle);
                      /*  eventInitialIntent.putExtra(Constants.PROGRAM_UID, programUid);
                        eventInitialIntent.putExtra(Constants.EVENT_UID, enrollmentTrio.val2());
                        eventInitialIntent.putExtra(Constants.PROGRAM_STAGE_UID, programStageUid);*/
                        startActivityForResult(eventInitialIntent, RQ_EVENT);
                    } else {
                        Intent eventCreationIntent = new Intent(getAbstracContext(), EventCaptureActivity.class);
                        eventCreationIntent.putExtras(EventCaptureActivity.getActivityBundle(enrollmentTrio.val2(), enrollmentTrio.val1()));
                        eventCreationIntent.putExtra(Constants.TRACKED_ENTITY_INSTANCE, enrollmentTrio.val0());
                        startActivityForResult(eventCreationIntent, RQ_EVENT);
                    }
                } else if (!enrollmentFragment.checkErrors()) { //val0 is program uid, val1 is trackedEntityInstance, val2 is empty
                    this.programUid = enrollmentTrio.val1();
                    this.teiUid = enrollmentTrio.val0();
                    openDashboard(null);
                } else {
                    progressBar.setVisibility(View.GONE);
                    showErrorsDialog();
                }
            } else {
                checkAction();
            }
        } else {
            progressBar.setVisibility(View.GONE);
            showMandatoryFieldsDialog();
        }
    }

    private void checkAction() {
        if (isAdded() && getContext() != null) {
            getActivity().finish(); //TODO: ASK IF USER WANTS TO DELETE RECORD
/*
            CustomDialog dialog = new CustomDialog(
                    getContext(),
                    getString(R.string.warning_error_on_complete_title),
                    messageOnComplete,
                    getString(R.string.button_ok),
                    getString(R.string.cancel),
                    1001,
                    new DialogClickListener() {
                        @Override
                        public void onPositive() {
                            if (canComplete && getActivity() != null && isAdded())
                                getActivity().finish();
                        }

                        @Override
                        public void onNegative() {
                            // do nothing
                            progressBar.setVisibility(View.GONE);
                        }
                    });
            if (isAdded() && !isEmpty(messageOnComplete) && !dialog.isShowing())
                dialog.show();
            else if (isAdded() && getActivity() != null) {
                getActivity().finish();
            }*/
        }
    }

    private void showErrorsDialog() {
        new CustomDialog(
                getAbstracContext(),
                getAbstracContext().getString(R.string.error_fields_title),
                String.format(getString(R.string.error_fields), enrollmentFragment.getErrorFields()),
                getAbstracContext().getString(R.string.button_ok),
                null,
                RC_GO_BACK,
                null)
                .show();
    }

    @Override
    public void showMandatoryFieldsDialog() {
        String description;
        String buttonAccept;
        if (mandatoryDelete) {
            description = isEnrollment ? getAbstracContext().getString(R.string.missing_mandatory_fields_text) :
                    getAbstracContext().getString(R.string.missing_mandatory_fields_events);
            buttonAccept = isEnrollment ? getAbstracContext().getString(R.string.missing_mandatory_fields_go_back) :
                    getAbstracContext().getString(R.string.button_ok);
        } else {
            description = getAbstracContext().getString(R.string.missing_mandatory_fields_text_not_delete);
            buttonAccept = getAbstracContext().getString(R.string.action_accept);
        }
        new CustomDialog(
                getAbstracContext(),
                getAbstracContext().getString(R.string.missing_mandatory_fields_title),
                description,
                buttonAccept,
                getAbstracContext().getString(R.string.cancel),
                RC_GO_BACK,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        if (mandatoryDelete)
                            if (isEnrollment)
                                deleteAllSavedDataAndGoBack();
                            else if (getActivity() != null && isAdded()) {
                                getActivity().finish();
                            }
                    }

                    @Override
                    public void onNegative() {
                        // do nothing
                    }
                })
                .show();
    }

    private void deleteAllSavedDataAndGoBack() {
        analyticsHelper().setEvent(DELETE_AND_BACK, CLICK, DELETE_AND_BACK);
        formPresenter.deleteCascade();
    }

    @Override
    public void onAllSavedDataDeleted() {
        if (getActivity() != null)
            getActivity().finish();
    }

    @Override
    public void onBackPressed() {
        String description = isEnrollment ? getAbstracContext().getString(R.string.delete_go_back) :
                getAbstracContext().getString(R.string.missing_mandatory_fields_events);
        String buttonAccept = isEnrollment ? getAbstracContext().getString(R.string.missing_mandatory_fields_go_back) :
                getAbstracContext().getString(R.string.button_ok);
        new CustomDialog(
                getAbstracContext(),
                getAbstracContext().getString(R.string.title_delete_go_back),
                description,
                getAbstracContext().getString(R.string.cancel),
                buttonAccept,
                RC_GO_BACK,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        // do nothing
                    }

                    @Override
                    public void onNegative() {
                        if (mandatoryDelete)
                            if (isEnrollment)
                                deleteAllSavedDataAndGoBack();
                            else if (getActivity() != null && isAdded()) {
                                getActivity().finish();
                            }
                    }
                })
                .show();
    }

    public Observable<EnrollmentStatus> onObservableBackPressed() {
        undoSaveObservable = PublishSubject.create();
        return undoSaveObservable.mergeWith(RxView.clicks(saveButton).map(o -> {
            mandatoryDelete = false;
            return getEnrollmentStatusFromButton();
        }).debounce(500, TimeUnit.MILLISECONDS));
    }

    private EnrollmentStatus getEnrollmentStatusFromButton() {
        datesLayout.requestFocus();
        return saveButton.isActivated() ? EnrollmentStatus.ACTIVE : EnrollmentStatus.COMPLETED;
    }

    public void onBackPressed(boolean delete) {
        this.mandatoryDelete = delete;
        formPresenter.checkMandatoryFields();
    }

    public RuleActionErrorOnCompletion hasErrorOnComple() {
        return errorOnCompletion;
    }

    public RuleActionShowError hasError() {
        return showError;
    }

    public String getMessageOnComplete() {
        return messageOnComplete;
    }

    @Override
    public void setErrorOnCompletion(RuleActionErrorOnCompletion errorOnCompletion) {
        this.errorOnCompletion = errorOnCompletion;
    }

    @Override
    public void setWarningOnCompletion(RuleActionWarningOnCompletion warningOnCompletion) {
        // do nothing
    }

    @Override
    public void setShowError(RuleActionShowError showError) {
        this.showError = showError;
    }

    @Override
    public void showCatComboDialog(CategoryCombo categoryCombo, List<CategoryOptionCombo> categoryOptionCombos) {
        new CategoryComboDialog(getAbstracContext(), categoryCombo, categoryOptionCombos, 123, selectedOption -> formPresenter.saveCategoryOption(selectedOption)).show();
    }
}