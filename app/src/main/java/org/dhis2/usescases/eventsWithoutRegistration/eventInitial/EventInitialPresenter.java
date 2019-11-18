package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import static org.dhis2.utils.analytics.AnalyticsConstants.BACK_EVENT;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.CREATE_EVENT;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Sextet;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.eventsWithoutRegistration.eventSummary.EventSummaryRepository;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.EventCreationType;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleActionHideField;
import org.hisp.dhis.rules.models.RuleActionHideSection;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleActionShowWarning;
import org.hisp.dhis.rules.models.RuleEffect;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import rx.exceptions.OnErrorNotImplementedException;
import timber.log.Timber;

/**
 * QUADRAM. Created by Cristian on 01/03/2018.
 */

public class EventInitialPresenter
        implements
        EventInitialContract.Presenter {

    public static final int ACCESS_COARSE_LOCATION_PERMISSION_REQUEST = 101;

    private EventInitialContract.View view;

    private final EventInitialRepository eventInitialRepository;

    private final EventSummaryRepository eventSummaryRepository;

    private final SchedulerProvider schedulerProvider;

    private FusedLocationProviderClient mFusedLocationClient;

    private String eventId;

    private CompositeDisposable compositeDisposable;

    private Program program;

    private CategoryCombo catCombo;

    private String programStageId;

    private List<OrganisationUnit> orgUnits;

    private String programId;

    public EventInitialPresenter(@NonNull EventSummaryRepository eventSummaryRepository,
                                 @NonNull EventInitialRepository eventInitialRepository, @NonNull SchedulerProvider schedulerProvider) {

        this.eventInitialRepository = eventInitialRepository;
        this.eventSummaryRepository = eventSummaryRepository;
        this.schedulerProvider = schedulerProvider;
    }

    @Override
    public void init(EventInitialContract.View mview, String programId, String eventId, String orgInitId,
                     String programStageId) {
        this.view = mview;
        this.eventId = eventId;
        this.programId = programId;
        this.programStageId = programStageId;

        compositeDisposable = new CompositeDisposable();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(view.getContext());

        if (eventId != null) {
            compositeDisposable
                    .add(
                            Flowable
                                    .zip(eventInitialRepository.event(eventId).toFlowable(BackpressureStrategy.LATEST),
                                            eventInitialRepository.getProgramWithId(programId)
                                                    .toFlowable(BackpressureStrategy.LATEST),
                                            eventInitialRepository.catCombo(programId).toFlowable(BackpressureStrategy.LATEST),
                                            eventInitialRepository.programStageForEvent(eventId),
                                            eventInitialRepository.getOptionsFromCatOptionCombo(eventId),
                                            eventInitialRepository.orgUnits(programId).toFlowable(BackpressureStrategy.LATEST),
                                            Sextet::create)
                                    .subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui())
                                    .subscribe(sextet -> {
                                        this.program = sextet.val1();
                                        this.catCombo = sextet.val2();
                                        this.orgUnits = sextet.val5();
                                        view.setProgram(sextet.val1());
                                        view.setProgramStage(sextet.val3());
                                        view.setEvent(sextet.val0());
                                        getCatOptionCombos(catCombo, !sextet.val4().isEmpty() ? sextet.val4() : null);
                                    }, Timber::d));

        } else {
            compositeDisposable
                    .add(
                            Flowable
                                    .zip(
                                            eventInitialRepository.getProgramWithId(programId)
                                                    .toFlowable(BackpressureStrategy.LATEST),
                                            eventInitialRepository.catCombo(programId).toFlowable(BackpressureStrategy.LATEST),
                                            eventInitialRepository.orgUnits(programId).toFlowable(BackpressureStrategy.LATEST),
                                            Trio::create)
                                    .subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui())
                                    .subscribe(trioFlowable -> {
                                        this.program = trioFlowable.val0();
                                        this.catCombo = trioFlowable.val1();
                                        this.orgUnits = trioFlowable.val2();
                                        view.setProgram(trioFlowable.val0());
                                        getCatOptionCombos(catCombo, null);
                                    }, Timber::d));
            getProgramStages(programId, programStageId);
        }

        if (eventId != null)
            getEventSections(eventId);

        if (orgInitId != null) {
            compositeDisposable.add(eventInitialRepository.getOrganisationUnit(orgInitId)
                    .subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui()).subscribe(
                            organisationUnit -> view.setOrgUnit(organisationUnit.uid(), organisationUnit.displayName()),
                            Timber::d));
        }

        compositeDisposable
                .add(eventInitialRepository.accessDataWrite(programId).subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui()).subscribe(view::setAccessDataWrite, Timber::e)

                );
    }

    private void getCatOptionCombos(CategoryCombo categoryCombo, Map<String, CategoryOption> stringCategoryOptionMap) {
        compositeDisposable
                .add(
                        eventInitialRepository.catOptionCombos(categoryCombo.uid()).subscribeOn(schedulerProvider.io())
                                .observeOn(schedulerProvider.ui()).subscribe(categoryOptionCombos -> view
                                        .setCatComboOptions(categoryCombo, categoryOptionCombos, stringCategoryOptionMap),
                                Timber::e));
    }

    @Override
    public void getEventSections(@NonNull String eventId) {
        compositeDisposable
                .add(eventSummaryRepository.programStageSections(eventId).subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui()).subscribe(view::onEventSections, Timber::e));
    }

    @Override
    public List<OrganisationUnit> getOrgUnits() {
        return orgUnits;
    }

    @Override
    public void onShareClick(View mView) {
        view.runSmsSubmission();
    }

    @Override
    public void deleteEvent(String trackedEntityInstance) {
        if (eventId != null) {
            eventInitialRepository.deleteEvent(eventId, trackedEntityInstance);
            view.showEventWasDeleted();
        } else
            view.displayMessage(view.getContext().getString(R.string.delete_event_error));
    }

    @Override
    public boolean isEnrollmentOpen() {
        return eventInitialRepository.isEnrollmentOpen();
    }

    @Override
    public void getStageObjectStyle(String uid) {
        compositeDisposable.add(eventInitialRepository.getObjectStyle(uid).subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(objectStyle -> view.renderObjectStyle(objectStyle), Timber::e));
    }

    @Override
    public void getProgramStage(String programStageUid) {
        compositeDisposable.add(eventInitialRepository.programStageWithId(programStageUid)
                .subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui()).subscribe(
                        programStage -> view.setProgramStage(programStage), throwable -> view.showProgramStageSelection()));
    }

    private void getProgramStages(String programUid, String programStageUid) {

        compositeDisposable
                .add((TextUtils.isEmpty(programStageId) ? eventInitialRepository.programStage(programUid)
                        : eventInitialRepository.programStageWithId(programStageUid)).subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(programStage -> view.setProgramStage(programStage),
                                throwable -> view.showProgramStageSelection()));
    }

    @Override
    public void onBackClick() {
        if (eventId != null)
            view.analyticsHelper().setEvent(BACK_EVENT, CLICK, CREATE_EVENT);
        view.back();
    }

    @Override
    public void createEvent(String enrollmentUid, String programStageModel, Date date, String orgUnitUid,
                            String categoryOptionComboUid, String categoryOptionsUid, Geometry geometry, String trackedEntityInstance) {
        if (program != null)
            compositeDisposable.add(eventInitialRepository
                    .createEvent(enrollmentUid, trackedEntityInstance, view.getContext(), program.uid(), programStageModel,
                            date, orgUnitUid, categoryOptionComboUid, categoryOptionsUid, geometry)
                    .subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui())
                    .subscribe(view::onEventCreated, t -> view.renderError(t.getMessage())));
    }

    @Override
    public void scheduleEventPermanent(String enrollmentUid, String trackedEntityInstanceUid, String programStageModel,
                                       Date dueDate, String orgUnitUid, String categoryOptionComboUid, String categoryOptionsUid, Geometry geometry) {
        if (program != null)
            compositeDisposable.add(eventInitialRepository
                    .scheduleEvent(enrollmentUid, null, view.getContext(), program.uid(), programStageModel, dueDate,
                            orgUnitUid, categoryOptionComboUid, categoryOptionsUid, geometry)
                    .subscribeOn(schedulerProvider.io())
                    /*
                     * .switchMap( //TODO: CHECK THAT SDK ALREADY UPDATES ENROLLMENT AND TEI eventId
                     * -> eventInitialRepository.updateTrackedEntityInstance(eventId,
                     * trackedEntityInstanceUid, orgUnitUid) )
                     */
                    .observeOn(schedulerProvider.ui())
                    .subscribe(view::onEventCreated, t -> view.renderError(t.getMessage())));
    }

    @Override
    public void scheduleEvent(String enrollmentUid, String programStageModel, Date dueDate, String orgUnitUid,
                              String categoryOptionComboUid, String categoryOptionsUid, Geometry geometry) {
        if (program != null)
            compositeDisposable.add(eventInitialRepository
                    .scheduleEvent(enrollmentUid, null, view.getContext(), program.uid(), programStageModel, dueDate,
                            orgUnitUid, categoryOptionComboUid, categoryOptionsUid, geometry)
                    .subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui())
                    .subscribe(view::onEventCreated, t -> view.renderError(t.getMessage())));
    }

    @Override
    public void editEvent(String trackedEntityInstance, String programStageModel, String eventUid, String date,
                          String orgUnitUid, String catComboUid, String catOptionCombo, Geometry geometry) {

        compositeDisposable.add(eventInitialRepository
                .editEvent(trackedEntityInstance, eventUid, date, orgUnitUid, catComboUid, catOptionCombo, geometry)
                .subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui())
                .subscribe((eventModel) -> view.onEventUpdated(eventModel.uid()),
                        error -> view.displayMessage(error.getLocalizedMessage())

                ));
    }

    @Override
    public void onDateClick(@Nullable DatePickerDialog.OnDateSetListener listener) {
        view.showDateDialog(listener);
    }

    @Override
    public void onOrgUnitButtonClick() {
        view.showOrgUnitSelector(orgUnits);
    }

    @Override
    public void onLocationClick() {
        if (ActivityCompat.checkSelfPermission(view.getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(view.getAbstractActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // TODO CRIS: Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                ActivityCompat.requestPermissions(view.getAbstractActivity(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        ACCESS_COARSE_LOCATION_PERMISSION_REQUEST);
            }
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            /*
             * if (location != null)
             * view.setLocation(GeometryHelper.createPointGeometry(location.getLatitude(),
             * location.getLongitude()));
             */
        });
    }

    @Override
    public void onFieldChanged(CharSequence s, int start, int before, int count) {
        view.checkActionButtonVisibility();
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }

    @Override
    public void getSectionCompletion(@Nullable String sectionUid) {
        Flowable<List<FieldViewModel>> fieldsFlowable = eventSummaryRepository.list(sectionUid, eventId);
        Flowable<Result<RuleEffect>> ruleEffectFlowable = eventSummaryRepository.calculate()
                .subscribeOn(schedulerProvider.computation())
                .onErrorReturn(throwable -> Result.failure(new Exception(throwable)));

        // Combining results of two repositories into a single stream.
        Flowable<List<FieldViewModel>> viewModelsFlowable = Flowable.zip(fieldsFlowable, ruleEffectFlowable,
                this::applyEffects);

        compositeDisposable.add(viewModelsFlowable.subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.ui()).subscribe(view.showFields(sectionUid), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                }));
    }

    @NonNull
    private List<FieldViewModel> applyEffects(@NonNull List<FieldViewModel> viewModels,
                                              @NonNull Result<RuleEffect> calcResult) {
        if (calcResult.error() != null) {
            Timber.e(calcResult.error());
            return viewModels;
        }

        Map<String, FieldViewModel> fieldViewModels = toMap(viewModels);
        applyRuleEffects(fieldViewModels, calcResult);

        return new ArrayList<>(fieldViewModels.values());
    }

    @NonNull
    private static Map<String, FieldViewModel> toMap(@NonNull List<FieldViewModel> fieldViewModels) {
        Map<String, FieldViewModel> map = new LinkedHashMap<>();
        for (FieldViewModel fieldViewModel : fieldViewModels) {
            map.put(fieldViewModel.uid(), fieldViewModel);
        }
        return map;
    }

    private void applyRuleEffects(Map<String, FieldViewModel> fieldViewModels, Result<RuleEffect> calcResult) {
        // TODO: APPLY RULE EFFECTS TO ALL MODELS
        view.setHideSection(null);
        for (RuleEffect ruleEffect : calcResult.items()) {
            RuleAction ruleAction = ruleEffect.ruleAction();
            if (ruleAction instanceof RuleActionShowWarning) {
                RuleActionShowWarning showWarning = (RuleActionShowWarning) ruleAction;
                FieldViewModel model = fieldViewModels.get(showWarning.field());

                if (model instanceof EditTextViewModel) {
                    fieldViewModels.put(showWarning.field(),
                            ((EditTextViewModel) model).withWarning(showWarning.content()));
                }
            } else if (ruleAction instanceof RuleActionShowError) {
                RuleActionShowError showError = (RuleActionShowError) ruleAction;
                FieldViewModel model = fieldViewModels.get(showError.field());

                if (model instanceof EditTextViewModel) {
                    fieldViewModels.put(showError.field(),
                            ((EditTextViewModel) model).withError(showError.content()));
                }
            } else if (ruleAction instanceof RuleActionHideField) {
                RuleActionHideField hideField = (RuleActionHideField) ruleAction;
                fieldViewModels.remove(hideField.field());
            } else if (ruleAction instanceof RuleActionHideSection) {
                RuleActionHideSection hideSection = (RuleActionHideSection) ruleAction;
                view.setHideSection(hideSection.programStageSection());
            }
        }
    }

    @Override
    public String getCatOptionCombo(List<CategoryOptionCombo> categoryOptionCombos, List<CategoryOption> values) {
        return eventInitialRepository.getCategoryOptionCombo(catCombo.uid(), UidsHelper.getUidsList(values));
    }

    @Override
    public Date getStageLastDate(String programStageUid, String enrollmentUid) {
        return eventInitialRepository.getStageLastDate(programStageUid, enrollmentUid);
    }

    @Override
    public void getEventOrgUnit(String ouUid) {
        compositeDisposable.add(eventInitialRepository.getOrganisationUnit(ouUid)
                .subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui())
                .subscribe(orgUnit -> view.setOrgUnit(orgUnit.uid(), orgUnit.displayName()), Timber::e));
    }

    @Override
    public void initOrgunit(Date selectedDate) {
        compositeDisposable.add(eventInitialRepository
                .filteredOrgUnits(DateUtils.databaseDateFormat().format(selectedDate), programId, null)
                .subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui()).subscribe(orgUnits -> {
                    if (orgUnits.size() == 1 && (view.eventcreateionType() == EventCreationType.ADDNEW
                            || view.eventcreateionType() == EventCreationType.DEFAULT))
                        view.setInitialOrgUnit(orgUnits.get(0));
                    else
                        view.setInitialOrgUnit(null);
                }, throwable -> view.renderError(throwable.getMessage())));
    }
}
