package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.jakewharton.rxbinding2.view.RxView;

import org.dhis2.App;
import org.dhis2.Bindings.DoubleExtensionsKt;
import org.dhis2.R;
import org.dhis2.commons.dialogs.CustomDialog;
import org.dhis2.commons.dialogs.DialogClickListener;
import org.dhis2.commons.dialogs.calendarpicker.CalendarPicker;
import org.dhis2.commons.dialogs.calendarpicker.OnDatePickerListener;
import org.dhis2.commons.popupmenu.AppMenuHelper;
import org.dhis2.commons.prefs.Preference;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.data.dhislogic.DhisPeriodUtils;
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel;
import org.dhis2.data.forms.dataentry.fields.unsupported.UnsupportedViewModel;
import org.dhis2.data.location.LocationProvider;
import org.dhis2.databinding.ActivityEventInitialBinding;
import org.dhis2.databinding.CategorySelectorBinding;
import org.dhis2.form.data.GeometryController;
import org.dhis2.form.data.GeometryParserImpl;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.uicomponents.map.views.MapSelectorActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.qrCodes.eventsworegistration.QrEventsWORegistrationActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.EventCreationType;
import org.dhis2.utils.EventMode;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.analytics.AnalyticsConstants;
import org.dhis2.utils.category.CategoryDialog;
import org.dhis2.utils.customviews.CatOptionPopUp;
import org.dhis2.utils.customviews.OrgUnitDialog;
import org.dhis2.utils.customviews.PeriodDialog;
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import kotlin.Unit;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialPresenter.ACCESS_LOCATION_PERMISSION_REQUEST;
import static org.dhis2.utils.Constants.ENROLLMENT_UID;
import static org.dhis2.utils.Constants.EVENT_CREATION_TYPE;
import static org.dhis2.utils.Constants.EVENT_PERIOD_TYPE;
import static org.dhis2.utils.Constants.ONE_TIME;
import static org.dhis2.utils.Constants.ORG_UNIT;
import static org.dhis2.utils.Constants.PERMANENT;
import static org.dhis2.utils.Constants.PROGRAM_UID;
import static org.dhis2.utils.Constants.RQ_MAP_LOCATION_VIEW;
import static org.dhis2.utils.Constants.TRACKED_ENTITY_INSTANCE;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.CREATE_EVENT;
import static org.dhis2.utils.analytics.AnalyticsConstants.DELETE_EVENT;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;

public class EventInitialActivity extends ActivityGlobalAbstract implements EventInitialContract.View,
        DatePickerDialog.OnDateSetListener {

    @Inject
    EventInitialPresenter presenter;

    @Inject
    PreferenceProvider preferences;

    @Inject
    DhisPeriodUtils periodUtils;

    @Inject
    LocationProvider locationProvider;

    private Event eventModel;

    private ActivityEventInitialBinding binding;

    //Bundle variables
    private String programUid;
    private String eventUid;
    private EventCreationType eventCreationType;
    private String getTrackedEntityInstance;
    private String enrollmentUid;
    private String selectedOrgUnit;
    private PeriodType periodType;
    private String programStageUid;
    private EnrollmentStatus enrollmentStatus;
    private int eventScheduleInterval;

    private String selectedDateString;
    private Date selectedDate;
    private Date selectedOrgUnitOpeningDate;
    private Date selectedOrgUnitClosedDate;
    private ProgramStage programStage;

    private String tempCreate;
    private boolean fixedOrgUnit;
    private String catOptionComboUid;
    private CategoryCombo catCombo;
    private Map<String, CategoryOption> selectedCatOption = new HashMap<>();
    private OrgUnitDialog orgUnitDialog;
    private Program program;
    private ArrayList<String> sectionsToHide;
    private Boolean accessData;

    private CompositeDisposable disposable = new CompositeDisposable();
    private Geometry newGeometry;
    private CoordinateViewModel currentGeometryModel;
    private GeometryController geometryController = new GeometryController(new GeometryParserImpl());

    public static Bundle getBundle(String programUid, String eventUid, String eventCreationType,
                                   String teiUid, PeriodType eventPeriodType, String orgUnit, String stageUid,
                                   String enrollmentUid, int eventScheduleInterval, EnrollmentStatus enrollmentStatus) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.PROGRAM_UID, programUid);
        bundle.putString(Constants.EVENT_UID, eventUid);
        bundle.putString(Constants.EVENT_CREATION_TYPE, eventCreationType);
        bundle.putString(Constants.TRACKED_ENTITY_INSTANCE, teiUid);
        bundle.putString(Constants.ENROLLMENT_UID, enrollmentUid);
        bundle.putString(Constants.ORG_UNIT, orgUnit);
        bundle.putSerializable(Constants.EVENT_PERIOD_TYPE, eventPeriodType);
        bundle.putString(Constants.PROGRAM_STAGE_UID, stageUid);
        bundle.putInt(Constants.EVENT_SCHEDULE_INTERVAL, eventScheduleInterval);
        bundle.putSerializable(Constants.ENROLLMENT_STATUS, enrollmentStatus);
        return bundle;
    }

    private void initVariables() {
        programUid = getIntent().getStringExtra(PROGRAM_UID);
        eventUid = getIntent().getStringExtra(Constants.EVENT_UID);
        eventCreationType = getIntent().getStringExtra(EVENT_CREATION_TYPE) != null ?
                EventCreationType.valueOf(getIntent().getStringExtra(EVENT_CREATION_TYPE)) :
                EventCreationType.DEFAULT;
        getTrackedEntityInstance = getIntent().getStringExtra(TRACKED_ENTITY_INSTANCE);
        enrollmentUid = getIntent().getStringExtra(ENROLLMENT_UID);
        selectedOrgUnit = getIntent().getStringExtra(ORG_UNIT);
        periodType = (PeriodType) getIntent().getSerializableExtra(EVENT_PERIOD_TYPE);
        programStageUid = getIntent().getStringExtra(Constants.PROGRAM_STAGE_UID);
        enrollmentStatus = (EnrollmentStatus) getIntent().getSerializableExtra(Constants.ENROLLMENT_STATUS);
        eventScheduleInterval = getIntent().getIntExtra(Constants.EVENT_SCHEDULE_INTERVAL, 0);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        initVariables();
        ((App) getApplicationContext()).userComponent().plus(
                new EventInitialModule(this,
                        eventUid,
                        programStageUid,
                        getContext())
        ).inject(this);
        setScreenName(this.getLocalClassName());
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_initial);
        binding.setPresenter(presenter);

        initProgressBar();

        setUpScreenByCreationType();

        initActionButton();
        binding.actionButton.setEnabled(true);
        presenter.init(programUid, eventUid, selectedOrgUnit, programStageUid);
    }

    private void initActionButton() {
        disposable.add(RxView.clicks(binding.actionButton)
                .debounce(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(v -> {
                            binding.actionButton.setEnabled(false);
                            String programStageModelUid = programStage == null ? "" : programStage.uid();
                            if (eventUid == null) { // This is a new Event
                                presenter.onEventCreated();
                                analyticsHelper().setEvent(CREATE_EVENT, AnalyticsConstants.DATA_CREATION, CREATE_EVENT);
                                if (eventCreationType == EventCreationType.REFERAL && tempCreate.equals(PERMANENT)) {
                                    presenter.scheduleEventPermanent(
                                            enrollmentUid,
                                            getTrackedEntityInstance,
                                            programStageModelUid,
                                            selectedDate,
                                            selectedOrgUnit,
                                            null,
                                            catOptionComboUid,
                                            newGeometry
                                    );
                                } else if (eventCreationType == EventCreationType.SCHEDULE || eventCreationType == EventCreationType.REFERAL) {
                                    presenter.scheduleEvent(
                                            enrollmentUid,
                                            programStageModelUid,
                                            selectedDate,
                                            selectedOrgUnit,
                                            null,
                                            catOptionComboUid,
                                            newGeometry
                                    );
                                } else {
                                    presenter.createEvent(
                                            enrollmentUid,
                                            programStageModelUid,
                                            selectedDate,
                                            selectedOrgUnit,
                                            null,
                                            catOptionComboUid,
                                            newGeometry,
                                            getTrackedEntityInstance);
                                }
                            } else {
                                presenter.editEvent(getTrackedEntityInstance,
                                        programStageModelUid,
                                        eventUid,
                                        DateUtils.databaseDateFormat().format(selectedDate), selectedOrgUnit, null,
                                        catOptionComboUid,
                                        newGeometry
                                );
                            }
                        },
                        Timber::e));
    }

    private void setUpScreenByCreationType() {

        if (eventCreationType == EventCreationType.REFERAL) {
            binding.temp.setVisibility(View.VISIBLE);
            binding.oneTime.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    tempCreate = ONE_TIME;
                }
                checkActionButtonVisibility();
            });
            binding.permanent.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    tempCreate = PERMANENT;
                }
                checkActionButtonVisibility();
            });
        } else {
            binding.temp.setVisibility(View.GONE);
        }

        if (eventCreationType == EventCreationType.SCHEDULE) {
            fixedOrgUnit = true;
            binding.orgUnitLayout.setVisibility(View.GONE);
        } else {
            fixedOrgUnit = false;
            binding.orgUnitLayout.setVisibility(View.VISIBLE);
            binding.orgUnit.setOnClickListener(v -> {
                if (!fixedOrgUnit)
                    presenter.onOrgUnitButtonClick();
            });
        }

        if (eventUid == null) {
            binding.actionButton.setText(R.string.next);
            binding.editionLayout.setVisibility(View.GONE);
        } else {
            fixedOrgUnit = true;
            binding.orgUnitLayout.setEnabled(false);
            binding.actionButton.setText(R.string.update);
        }

    }

    @Override
    protected void onDestroy() {
        presenter.onDettach();
        disposable.dispose();
        super.onDestroy();
    }

    private void initProgressBar() {
        if (eventUid != null && presenter.getCompletionPercentageVisibility()) {
            binding.completion.setVisibility(View.VISIBLE);
        } else {
            binding.completion.setVisibility(View.GONE);
        }
    }

    @Override
    public void checkActionButtonVisibility() {
        if (eventUid == null) {
            if (isFormCompleted())
                binding.actionButton.setVisibility(View.VISIBLE); //If creating a new event, show only if minimun data is completed
            else
                binding.actionButton.setVisibility(View.GONE);

        } else {
            if (eventModel != null) {
                if (eventModel.status() == EventStatus.OVERDUE && enrollmentStatus == EnrollmentStatus.CANCELLED)
                    binding.actionButton.setVisibility(View.GONE);
            } else
                binding.actionButton.setVisibility(View.VISIBLE); //Show actionButton always for already created events
        }
    }

    private boolean isFormCompleted() {

        if (!catComboIsDefaultOrNull())
            return isCompleted(selectedDateString) &&
                    isCompleted(selectedOrgUnit) &&
                    isSelectedDateBetweenOpeningAndClosedDates() &&
                    catCombo != null && catCombo.categories() != null && selectedCatOption.size() == catCombo.categories().size() &&
                    ((eventCreationType != EventCreationType.REFERAL) || (eventCreationType == EventCreationType.REFERAL && tempCreate != null));
        else
            return isCompleted(selectedDateString) &&
                    isCompleted(selectedOrgUnit) &&
                    isSelectedDateBetweenOpeningAndClosedDates() &&
                    ((eventCreationType != EventCreationType.REFERAL) || (eventCreationType == EventCreationType.REFERAL && tempCreate != null));
    }

    private boolean isSelectedDateBetweenOpeningAndClosedDates() {
        if (selectedDate == null)
            return false;
        boolean isAfterOpening = selectedOrgUnitOpeningDate == null || selectedDate.after(selectedOrgUnitOpeningDate);
        boolean isBeforeClosed = selectedOrgUnitClosedDate == null || selectedDate.before(selectedOrgUnitClosedDate);
        return isAfterOpening && isBeforeClosed;

    }

    private boolean isCompleted(String field) {
        return field != null && !field.isEmpty();
    }

    @Override
    public void setProgram(@NonNull Program program) {
        this.program = program;

        setUpActivityTitle();

        if (eventModel == null) {
            Calendar now = DateUtils.getInstance().getCalendar();
            if (periodType == null) {

                if (eventCreationType != EventCreationType.SCHEDULE)
                    selectedDate = now.getTime();
                else {
                    now.setTime(presenter.getStageLastDate(programStageUid, enrollmentUid));
                    int minDateFromStart = presenter.getMinDateByProgramStage(programStageUid);
                    if (minDateFromStart > 0) {
                        now.add(Calendar.DAY_OF_YEAR, minDateFromStart);
                    }
                    selectedDate = DateUtils.getInstance().getNextPeriod(null, now.getTime(), 0);
                }

                selectedDateString = DateUtils.uiDateFormat().format(selectedDate);

            } else {
                now.setTime(DateUtils.getInstance().getNextPeriod(periodType, now.getTime(), eventCreationType != EventCreationType.SCHEDULE ? 0 : 1));
                selectedDate = now.getTime();
                selectedDateString = periodUtils.getPeriodUIString(periodType, selectedDate, Locale.getDefault());
            }

            binding.date.setText(selectedDateString);
            if (selectedOrgUnit == null && eventUid == null)
                presenter.initOrgunit(selectedDate);

        } else {
            if (!isEmpty(eventModel.enrollment()) && eventCreationType != EventCreationType.ADDNEW) {
                binding.orgUnit.setEnabled(false);
                binding.orgUnitLayout.setVisibility(View.GONE);
            }
        }

        binding.date.setOnClickListener(view -> {
            if (periodType == null)
                presenter.onDateClick(EventInitialActivity.this);
            else {
                Date minDate = DateUtils.getInstance().expDate(null, program.expiryDays(), periodType);
                Date lastPeriodDate = DateUtils.getInstance().getNextPeriod(periodType, minDate, -1, true);

                if (lastPeriodDate.after(DateUtils.getInstance().getNextPeriod(program.expiryPeriodType(), minDate, 0)))
                    minDate = DateUtils.getInstance().getNextPeriod(periodType, lastPeriodDate, 0);

                new PeriodDialog()
                        .setPeriod(periodType)
                        .setMinDate(minDate)
                        .setMaxDate(eventCreationType.equals(EventCreationType.ADDNEW) || eventCreationType.equals(EventCreationType.DEFAULT) ? DateUtils.getInstance().getToday() : null)
                        .setPossitiveListener(selectedDate -> {
                            this.selectedDate = selectedDate;
                            binding.date.setText(periodUtils.getPeriodUIString(periodType, selectedDate, Locale.getDefault()));
                            binding.date.clearFocus();
                            if (!fixedOrgUnit) {
                                presenter.initOrgunit(selectedDate);
                            }
                        })
                        .show(getSupportFragmentManager(), PeriodDialog.class.getSimpleName());
            }
        });

        if (eventModel != null &&
                (DateUtils.getInstance().isEventExpired(eventModel.eventDate(),
                        eventModel.completedDate(), eventModel.status(),
                        program.completeEventsExpiryDays(),
                        program.expiryPeriodType(),
                        program.expiryDays()) || eventModel.status() == EventStatus.COMPLETED || eventModel.status() == EventStatus.SKIPPED)) {
            binding.date.setEnabled(false);
            for (int i = 0; i < binding.catComboLayout.getChildCount(); i++)
                binding.catComboLayout.getChildAt(i).findViewById(R.id.cat_combo).setEnabled(false);
            binding.orgUnit.setEnabled(false);
            binding.temp.setEnabled(false);
            if (presenter.isEventEditable()) {
                binding.actionButton.setText(getString(R.string.action_close));
            } else {
                binding.actionButton.setText(getString(R.string.check_event));
            }
            binding.executePendingBindings();

        }

    }

    private void setUpActivityTitle() {
        String activityTitle;
        if (eventCreationType == EventCreationType.REFERAL) {
            activityTitle = program.displayName() + " - " + getString(R.string.referral);
        } else {
            if (eventModel != null && !isEmpty(eventModel.enrollment()) && eventCreationType != EventCreationType.ADDNEW) {
                binding.orgUnit.setEnabled(false);
                binding.orgUnitLayout.setVisibility(View.GONE);
            }

            activityTitle = eventUid == null ? program.displayName() + " - " + getString(R.string.new_event) : program.displayName();
        }
        binding.setName(activityTitle);
    }

    @Override
    public void setEvent(Event event) {

        catOptionComboUid = event.attributeOptionCombo();

        if (event.eventDate() != null) {
            selectedDate = event.eventDate();
            binding.date.setText(DateUtils.uiDateFormat().format(selectedDate));
        }

        eventModel = event;

        presenter.getEventOrgUnit(event.organisationUnit());
    }

    @Override
    public void onEventCreated(String eventUid) {
        showToast(getString(R.string.event_created));
        if (eventCreationType != EventCreationType.SCHEDULE && eventCreationType != EventCreationType.REFERAL) {
            startFormActivity(eventUid, true);
        } else {
            finish();
        }
    }

    @Override
    public void onEventUpdated(String eventUid) {
        startFormActivity(eventUid, false);
    }

    private void startFormActivity(String eventUid, boolean isNew) {
        Intent intent = new Intent(this, EventCaptureActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        intent.putExtras(EventCaptureActivity.getActivityBundle(eventUid, programUid, isNew ? EventMode.NEW : EventMode.CHECK));
        startActivity(intent);
        finish();
    }

    @Override
    public void setProgramStage(ProgramStage programStage) {
        this.programStage = programStage;
        binding.setProgramStage(programStage);

        if (periodType == null)
            periodType = programStage.periodType();

        if (eventCreationType == EventCreationType.SCHEDULE)
            binding.dateLayout.setHint(programStage.dueDateLabel() != null ? programStage.dueDateLabel() : getString(R.string.due_date));
        else if (programStage.executionDateLabel() != null)
            binding.dateLayout.setHint(programStage.executionDateLabel());
        else
            binding.dateLayout.setHint(getString(R.string.event_date));

        if (eventCreationType == EventCreationType.SCHEDULE && programStage.hideDueDate()) {
            binding.dateLayout.setVisibility(View.GONE);

            Calendar now = DateUtils.getInstance().getCalendar();
            if (periodType == null) {
                now.add(Calendar.DAY_OF_YEAR, eventScheduleInterval);
                selectedDate = DateUtils.getInstance().getNextPeriod(null, now.getTime(), 0);
                selectedDateString = DateUtils.uiDateFormat().format(selectedDate);
            } else {
                now.setTime(DateUtils.getInstance().getNextPeriod(periodType, now.getTime(), eventCreationType != EventCreationType.SCHEDULE ? 0 : 1));
                selectedDate = now.getTime();
                selectedDateString = periodUtils.getPeriodUIString(periodType, selectedDate, Locale.getDefault());
            }
        }
        presenter.getStageObjectStyle(this.programStage.uid());
    }

    @Override
    public void setCatComboOptions(CategoryCombo catCombo, List<CategoryOptionCombo> categoryOptionCombos, Map<String, CategoryOption> stringCategoryOptionMap) {

        runOnUiThread(() -> {
            this.catCombo = catCombo;
            if (stringCategoryOptionMap != null)
                selectedCatOption = stringCategoryOptionMap;

            binding.catComboLayout.removeAllViews();

            if (!catCombo.isDefault() && catCombo.categories() != null)
                for (Category category : catCombo.categories()) {
                    CategorySelectorBinding catSelectorBinding = CategorySelectorBinding.inflate(LayoutInflater.from(this));
                    catSelectorBinding.catCombLayout.setHint(category.displayName());
                    catSelectorBinding.catCombo.setOnClickListener(view -> {
                                if (presenter.catOptionSize(category.uid()) > CategoryDialog.DEFAULT_COUNT_LIMIT) {
                                    showCategoryDialog(category, categoryOptionCombos, catSelectorBinding);
                                } else {
                                    showCategoryPopUp(category, categoryOptionCombos, catSelectorBinding);
                                }
                            }
                    );

                    if (stringCategoryOptionMap != null && stringCategoryOptionMap.get(category.uid()) != null)
                        catSelectorBinding.catCombo.setText(stringCategoryOptionMap.get(category.uid()).displayName());
                    catSelectorBinding.getRoot().setEnabled(accessData);
                    binding.catComboLayout.addView(catSelectorBinding.getRoot());
                }
            else if (catCombo.isDefault())
                catOptionComboUid = categoryOptionCombos.get(0).uid();

            checkActionButtonVisibility();
        });
    }

    private void showCategoryDialog(Category category, List<CategoryOptionCombo> categoryOptionCombos, CategorySelectorBinding catSelectorBinding) {
        new CategoryDialog(
                CategoryDialog.Type.CATEGORY_OPTIONS,
                category.uid(),
                true,
                selectedDate,
                selectedOption -> {
                    CategoryOption categoryOption = presenter.getCatOption(selectedOption);
                    selectedCatOption.put(category.uid(), categoryOption);
                    catSelectorBinding.catCombo.setText(categoryOption.displayName());
                    if (selectedCatOption.size() == catCombo.categories().size()) {
                        catOptionComboUid = presenter.getCatOptionCombo(catCombo.uid(), categoryOptionCombos, new ArrayList<>(selectedCatOption.values()));
                        checkActionButtonVisibility();
                    }
                    return null;
                }
        ).show(getSupportFragmentManager(),
                CategoryDialog.Companion.getTAG());
    }

    private void showCategoryPopUp(Category category, List<CategoryOptionCombo> categoryOptionCombos, CategorySelectorBinding catSelectorBinding) {
        new CatOptionPopUp(
                this,
                catSelectorBinding.getRoot(),
                category.displayName(),
                presenter.getCatOptions(category.uid()),
                true,
                selectedDate,
                categoryOption -> {
                    if (categoryOption != null)
                        selectedCatOption.put(category.uid(), categoryOption);
                    else
                        selectedCatOption.remove(category.uid());
                    catSelectorBinding.catCombo.setText(categoryOption != null ? categoryOption.displayName() : null);
                    if (selectedCatOption.size() == catCombo.categories().size()) {
                        catOptionComboUid = presenter.getCatOptionCombo(catCombo.uid(), categoryOptionCombos, new ArrayList<>(selectedCatOption.values()));
                        checkActionButtonVisibility();
                    }
                    return Unit.INSTANCE;
                }
        ).show();
    }

    @Override
    public void showDateDialog(DatePickerDialog.OnDateSetListener listener) {
        showCalendar(listener);
    }

    private void showCalendar(DatePickerDialog.OnDateSetListener listener) {
        int scheduleInterval = 0;
        Date minDate = null;
        Date maxDate = null;
        boolean allowedFutureDates = false;
        if (eventCreationType == EventCreationType.SCHEDULE) {
            scheduleInterval = eventScheduleInterval;
            allowedFutureDates = true;
        }

        if (program.expiryPeriodType() != null) {
            minDate = DateUtils.getInstance().expDate(null, program.expiryDays() == null ? 0 : program.expiryDays(), program.expiryPeriodType());
        }
        switch (eventCreationType) {
            case ADDNEW:
            case DEFAULT:
                maxDate = new Date(System.currentTimeMillis() - 1000);
                break;
            case REFERAL:
            case SCHEDULE:
                break;
        }

        CalendarPicker dialog = new CalendarPicker(getContext());
        dialog.setInitialDate(selectedDate);
        dialog.setMinDate(minDate);
        dialog.setMaxDate(maxDate);
        dialog.setScheduleInterval(scheduleInterval);
        dialog.isFutureDatesAllowed(allowedFutureDates);
        dialog.setListener(
                new OnDatePickerListener() {
                    @Override
                    public void onNegativeClick() {
                    }

                    @Override
                    public void onPositiveClick(DatePicker datePicker) {
                        onDateSet(datePicker, datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                    }
                }
        );
        dialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        selectedDate = c.getTime();
        selectedDateString = periodUtils.getPeriodUIString(periodType, selectedDate, Locale.getDefault());
        binding.date.setText(selectedDateString);
        binding.date.clearFocus();
        if (!fixedOrgUnit) {
            presenter.initOrgunit(selectedDate);
        }
    }

    @Override
    public void updatePercentage(float primaryValue) {
        binding.completion.setCompletionPercentage(primaryValue);
    }

    @Override
    public void showProgramStageSelection() {
        presenter.getProgramStage(programStageUid);
    }

    @Override
    public void setHideSection(String sectionUid) {
        if (sectionsToHide == null || sectionUid == null)
            sectionsToHide = new ArrayList<>();

        if (sectionUid != null && !sectionsToHide.contains(sectionUid))
            sectionsToHide.add(sectionUid);
    }

    @Override
    public void renderObjectStyle(ObjectStyle data) {
        int color = ColorUtils.getColorFrom(data.color(),
                ColorUtils.getPrimaryColor(this, ColorUtils.ColorType.PRIMARY_LIGHT));
        binding.programStageIcon.setBackground(
                ColorUtils.tintDrawableWithColor(
                        binding.programStageIcon.getBackground(),
                        color
                )
        );
        binding.programStageIcon.setImageResource(
                new ResourceManager(this).getObjectStyleDrawableResource(
                        data.icon(),
                        R.drawable.ic_default_outline
                )
        );
        binding.programStageIcon.setColorFilter(ColorUtils.getContrastColor(color));

    }

    @Override
    public EventCreationType eventcreateionType() {
        return eventCreationType;
    }

    @Override
    public void setInitialOrgUnit(OrganisationUnit organisationUnit) {
        if (organisationUnit != null) {
            this.selectedOrgUnit = organisationUnit.uid();
            binding.orgUnit.setText(organisationUnit.displayName());
            if (eventCreationType != EventCreationType.DEFAULT) {
                binding.orgUnit.setEnabled(eventUid == null);
            }
        } else
            binding.orgUnit.setText("");
    }

    private int calculateCompletedFields(@NonNull List<FieldUiModel> updates) {
        int total = 0;
        for (FieldUiModel fieldViewModel : updates) {
            if (fieldViewModel.getValue() != null && !fieldViewModel.getValue().isEmpty())
                total++;
        }
        return total;
    }

    private int calculateUnsupportedFields(@NonNull List<FieldUiModel> updates) {
        int total = 0;
        for (FieldUiModel fieldViewModel : updates) {
            if (fieldViewModel instanceof UnsupportedViewModel)
                total++;
        }
        return total;
    }

    @Override
    public void setOrgUnit(String orgUnitId, String orgUnitName) {
        preferences.setValue(Preference.CURRENT_ORG_UNIT, orgUnitId);
        this.selectedOrgUnit = orgUnitId;
        binding.orgUnit.setText(orgUnitName);
    }

    @Override
    public void showNoOrgUnits() {
        renderError(getString(R.string.no_org_units));
        selectedDateString = null;
        selectedDate = null;
        binding.date.setText("");
        binding.executePendingBindings();
        checkActionButtonVisibility();
    }

    @Override
    public void setAccessDataWrite(Boolean canWrite) {
        this.accessData = canWrite;
        if (!canWrite || !presenter.isEnrollmentOpen()) {
            binding.date.setEnabled(false);
            binding.date.setClickable(false);
            binding.orgUnit.setEnabled(false);
            for (int i = 0; i < binding.catComboLayout.getChildCount(); i++)
                binding.catComboLayout.getChildAt(i).findViewById(R.id.cat_combo).setEnabled(false);
            binding.actionButton.setText(getString(R.string.action_close));
            binding.executePendingBindings();
        }
    }

    @Override
    public void showOrgUnitSelector(List<OrganisationUnit> orgUnits) {
        if (orgUnits != null && !orgUnits.isEmpty()) {

            Iterator<OrganisationUnit> iterator = orgUnits.iterator();
            while (iterator.hasNext()) {
                OrganisationUnit organisationUnit = iterator.next();
                if (organisationUnit.openingDate() != null && organisationUnit.openingDate().after(selectedDate)
                        || organisationUnit.closedDate() != null && organisationUnit.closedDate().before(selectedDate))
                    iterator.remove();
            }

            orgUnitDialog = OrgUnitDialog.getInstace()
                    .setTitle(!binding.orgUnit.getText().toString().isEmpty() ? binding.orgUnit.getText().toString() : getString(R.string.org_unit))
                    .setMultiSelection(false)
                    .setOrgUnits(orgUnits)
                    .setProgram(programUid)
                    .setPossitiveListener(data -> {
                        setOrgUnit(orgUnitDialog.getSelectedOrgUnit(), orgUnitDialog.getSelectedOrgUnitName());
                        orgUnitDialog.dismiss();
                    })
                    .setNegativeListener(data -> orgUnitDialog.dismiss())
                    .setNodeClickListener((node, value) -> {
                        if (!node.getChildren().isEmpty())
                            node.setExpanded(node.isExpanded());
                    });

            if (!orgUnitDialog.isAdded())
                orgUnitDialog.show(getSupportFragmentManager(), "ORG_UNIT_DIALOG");
        } else {
            showNoOrgUnits();
        }
    }

    @Override
    public void showQR() {
        Intent intent = new Intent(EventInitialActivity.this, QrEventsWORegistrationActivity.class);
        intent.putExtra(Constants.EVENT_UID, eventUid);
        startActivity(intent);
    }

    private boolean catComboIsDefaultOrNull() {
        return (catCombo == null || catCombo.isDefault());
    }

    @Override
    public void setTutorial() {

        new Handler().postDelayed(() -> {
            SparseBooleanArray stepConditions = new SparseBooleanArray();
            stepConditions.put(0, eventUid == null);
            HelpManager.getInstance().show(getActivity(), HelpManager.TutorialName.EVENT_INITIAL, stepConditions);
        }, 500);
    }

    @Override
    public void showMoreOptions(View view) {
        new AppMenuHelper.Builder().menu(this, R.menu.event_menu).anchor(view)
                .onMenuInflated(popupMenu -> {
                    popupMenu.getMenu().findItem(R.id.menu_delete).setVisible(accessData && presenter.isEnrollmentOpen());
                    popupMenu.getMenu().findItem(R.id.menu_share).setVisible(eventUid != null);
                    return Unit.INSTANCE;
                })
                .onMenuItemClicked(itemId -> {
                    switch (itemId) {
                        case R.id.showHelp:
                            analyticsHelper().setEvent(SHOW_HELP, CLICK, SHOW_HELP);
                            setTutorial();
                            break;
                        case R.id.menu_delete:
                            confirmDeleteEvent();
                            break;
                        case R.id.menu_share:
                            presenter.onShareClick();
                            break;
                        default:
                            break;
                    }
                    return false;
                })
                .build()
                .show();
    }

    public void confirmDeleteEvent() {
        new CustomDialog(
                this,
                getString(R.string.delete_event),
                getString(R.string.confirm_delete_event),
                getString(R.string.delete),
                getString(R.string.cancel),
                0,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        analyticsHelper().setEvent(DELETE_EVENT, CLICK, DELETE_EVENT);
                        presenter.deleteEvent(getTrackedEntityInstance);
                    }

                    @Override
                    public void onNegative() {
                        // dismiss
                    }
                }
        ).show();
    }

    @Override
    public void showEventWasDeleted() {
        showToast(getString(R.string.event_was_deleted));
        finish();
    }

    @Override
    public void setGeometryModel(CoordinateViewModel geometryModel) {
        setGeometryCallback(geometryModel);
        binding.geometry.setItem(geometryModel);
    }

    private void setGeometryCallback(CoordinateViewModel geometryModel) {
        currentGeometryModel = geometryModel;
        geometryModel.setCallback(geometryController.getCoordinatesCallback(
                value -> {
                    presenter.setChangingCoordinates(true);
                    setNewGeometry(value);
                    setGeometryModel((CoordinateViewModel) geometryModel.withValue(value));
                    return Unit.INSTANCE;
                },
                fieldUid -> {
                    requestLocation();
                    return Unit.INSTANCE;
                },
                (fieldUid, featureType, initialCoordinates) -> {
                    startActivityForResult(
                            MapSelectorActivity.Companion.create(EventInitialActivity.this,
                                    FeatureType.valueOfFeatureType(featureType),
                                    initialCoordinates),
                            RQ_MAP_LOCATION_VIEW);
                    return Unit.INSTANCE;
                }
        ));
    }

    @Override
    public void setNewGeometry(String value) {
        if (value != null) {
            this.newGeometry = Geometry.builder()
                    .coordinates(value)
                    .type(programStage.featureType())
                    .build();
        } else {
            this.newGeometry = null;
        }
    }

    @Override
    public void displayFeatureTypeError() {
        displayMessage(getString(R.string.coordinate_feature_type_error));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == RQ_MAP_LOCATION_VIEW && data != null && data.getExtras() != null) {
            FeatureType locationType = FeatureType.valueOf(data.getStringExtra(MapSelectorActivity.LOCATION_TYPE_EXTRA));
            String dataExtra = data.getStringExtra(MapSelectorActivity.DATA_EXTRA);
            Geometry geometry = geometryController.generateLocationFromCoordinates(locationType, dataExtra);
            currentGeometryModel.onCurrentLocationClick(geometry);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        if (requestCode == ACCESS_LOCATION_PERMISSION_REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation();
        }
    }

    private void requestLocation() {
        locationProvider.getLastKnownLocation(
                location -> {
                    double longitude = DoubleExtensionsKt.truncate(location.getLongitude());
                    double latitude = DoubleExtensionsKt.truncate(location.getLatitude());
                    Geometry geometry = GeometryHelper.createPointGeometry(longitude, latitude);
                    currentGeometryModel.onCurrentLocationClick(geometry);
                    return Unit.INSTANCE;
                },
                () -> {
                    ActivityCompat.requestPermissions((ActivityGlobalAbstract) getContext(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            ACCESS_LOCATION_PERMISSION_REQUEST);
                    return Unit.INSTANCE;
                },
                () -> {
                    displayMessage(getString(R.string.enable_location_message));
                    return Unit.INSTANCE;
                });
    }

    @Override
    public void setEditionStatus(String reason) {
        binding.editionReason.setText(reason);
    }

    @Override
    public void hideEditionStatus() {
        binding.editionReason.setVisibility(View.GONE);
    }
}