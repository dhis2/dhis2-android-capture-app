package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.app.DatePickerDialog;
import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.dhis2.R;
import org.dhis2.commons.prefs.Preference;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Sextet;
import org.dhis2.data.tuples.Trio;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventFieldMapper;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.DhisTextUtils;
import org.dhis2.utils.EventCreationType;
import org.dhis2.utils.Result;
import org.dhis2.utils.RulesUtilsProvider;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import kotlin.Pair;
import timber.log.Timber;

import static org.dhis2.utils.analytics.AnalyticsConstants.BACK_EVENT;
import static org.dhis2.utils.analytics.matomo.Actions.CREATE_EVENT;
import static org.dhis2.utils.analytics.matomo.Categories.EVENT_LIST;
import static org.dhis2.utils.analytics.matomo.Labels.CLICK;

public class EventInitialPresenter {

    public static final int ACCESS_LOCATION_PERMISSION_REQUEST = 101;
    private final PreferenceProvider preferences;
    private final AnalyticsHelper analyticsHelper;

    private final EventInitialContract.View view;

    private final EventInitialRepository eventInitialRepository;

    private final RulesUtilsProvider ruleUtils;

    private final SchedulerProvider schedulerProvider;
    private final EventFieldMapper eventFieldMapper;

    private String eventId;

    public CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Program program;

    private String programStageId;

    private List<OrganisationUnit> orgUnits;

    private String programId;

    private MatomoAnalyticsController matomoAnalyticsController;

    public EventInitialPresenter(@NonNull EventInitialContract.View view,
                                 @NonNull RulesUtilsProvider ruleUtils,
                                 @NonNull EventInitialRepository eventInitialRepository,
                                 @NonNull SchedulerProvider schedulerProvider,
                                 @NonNull PreferenceProvider preferenceProvider,
                                 @NonNull AnalyticsHelper analyticsHelper,
                                 @NonNull MatomoAnalyticsController matomoAnalyticsController,
                                 @NonNull EventFieldMapper eventFieldMapper) {

        this.view = view;
        this.eventInitialRepository = eventInitialRepository;
        this.ruleUtils = ruleUtils;
        this.schedulerProvider = schedulerProvider;
        this.preferences = preferenceProvider;
        this.analyticsHelper = analyticsHelper;
        this.matomoAnalyticsController = matomoAnalyticsController;
        this.eventFieldMapper = eventFieldMapper;
    }

    public void init(String programId,
                     String eventId,
                     String orgInitId,
                     String programStageId) {
        this.eventId = eventId;
        this.programId = programId;
        this.programStageId = programStageId;

        view.setAccessDataWrite(
                eventInitialRepository.accessDataWrite(programId).blockingFirst()
        );

        compositeDisposable.add(
                eventInitialRepository.getGeometryModel(programId, null)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::setGeometryModel,
                                throwable -> {
                                    Timber.d(throwable);
                                    view.displayFeatureTypeError();
                                }
                        ));

        if (eventId != null) {
            compositeDisposable
                    .add(
                            Flowable.zip(
                                    eventInitialRepository.event(eventId).toFlowable(BackpressureStrategy.LATEST),
                                    eventInitialRepository.getProgramWithId(programId).toFlowable(BackpressureStrategy.LATEST),
                                    eventInitialRepository.catCombo(programId).toFlowable(BackpressureStrategy.LATEST),
                                    eventInitialRepository.programStageForEvent(eventId),
                                    eventInitialRepository.getOptionsFromCatOptionCombo(eventId),
                                    eventInitialRepository.orgUnits(programId).toFlowable(BackpressureStrategy.LATEST),
                                    Sextet::create)
                                    .subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui())
                                    .subscribe(sextet -> {
                                        this.program = sextet.val1();
                                        this.orgUnits = sextet.val5();
                                        view.setProgram(sextet.val1());
                                        view.setProgramStage(sextet.val3());
                                        view.setEvent(sextet.val0());
                                        getCatOptionCombos(sextet.val2(), !sextet.val4().isEmpty() ? sextet.val4() : null);
                                    }, Timber::d));

        } else {
            compositeDisposable
                    .add(
                            Flowable.zip(
                                    eventInitialRepository.getProgramWithId(programId).toFlowable(BackpressureStrategy.LATEST),
                                    eventInitialRepository.catCombo(programId).toFlowable(BackpressureStrategy.LATEST),
                                    eventInitialRepository.orgUnits(programId).toFlowable(BackpressureStrategy.LATEST),
                                    Trio::create)
                                    .subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui())
                                    .subscribe(trioFlowable -> {
                                        this.program = trioFlowable.val0();
                                        this.orgUnits = trioFlowable.val2();
                                        view.setProgram(trioFlowable.val0());
                                        getCatOptionCombos(trioFlowable.val1(), null);
                                    }, Timber::d));
            getProgramStages(programId, programStageId);
        }

        if (eventId != null)
            getSectionCompletion();

        if (getCurrentOrgUnit(orgInitId) != null) {
            compositeDisposable.add(
                    eventInitialRepository.getOrganisationUnit(getCurrentOrgUnit(orgInitId))
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .subscribe(
                                    organisationUnit -> view.setOrgUnit(organisationUnit.uid(), organisationUnit.displayName()),
                                    Timber::d
                            )
            );
        }
    }

    @VisibleForTesting
    public String getCurrentOrgUnit(String orgUnitUid) {
        if (preferences.contains(Preference.CURRENT_ORG_UNIT)) {
            return preferences.getString(Preference.CURRENT_ORG_UNIT, null);
        } else return orgUnitUid;
    }

    private void getCatOptionCombos(CategoryCombo categoryCombo, Map<String, CategoryOption> stringCategoryOptionMap) {
        compositeDisposable.add(
                eventInitialRepository.catOptionCombos(categoryCombo.uid())
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                categoryOptionCombos -> view.setCatComboOptions(categoryCombo, categoryOptionCombos, stringCategoryOptionMap),
                                Timber::e
                        )
        );
    }

    public void onShareClick() {
        view.showQR();
    }

    public void deleteEvent(String trackedEntityInstance) {
        if (eventId != null) {
            eventInitialRepository.deleteEvent(eventId, trackedEntityInstance);
            view.showEventWasDeleted();
        } else
            view.displayMessage(view.getContext().getString(R.string.delete_event_error));
    }

    public boolean isEnrollmentOpen() {
        return eventInitialRepository.isEnrollmentOpen();
    }

    public void getStageObjectStyle(String uid) {
        compositeDisposable.add(
                eventInitialRepository.getObjectStyle(uid)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                objectStyle -> view.renderObjectStyle(objectStyle),
                                Timber::e
                        )
        );
    }

    public void getProgramStage(String programStageUid) {
        compositeDisposable.add(
                eventInitialRepository.programStageWithId(programStageUid)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                programStage -> view.setProgramStage(programStage),
                                throwable -> view.showProgramStageSelection()
                        )
        );
    }

    private void getProgramStages(String programUid, String programStageUid) {
        compositeDisposable.add(
                (DhisTextUtils.Companion.isEmpty(programStageId) ? eventInitialRepository.programStage(programUid)
                        : eventInitialRepository.programStageWithId(programStageUid))
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                programStage -> view.setProgramStage(programStage),
                                throwable -> view.showProgramStageSelection()
                        )
        );
    }

    public void onBackClick() {
        setChangingCoordinates(false);
        if (eventId != null)
            analyticsHelper.setEvent(BACK_EVENT, CLICK, CREATE_EVENT);
        view.back();
    }

    public void createEvent(String enrollmentUid, String programStageModel, Date date, String orgUnitUid,
                            String categoryOptionComboUid, String categoryOptionsUid, Geometry geometry, String trackedEntityInstance) {
        if (program != null) {
            preferences.setValue(Preference.CURRENT_ORG_UNIT, orgUnitUid);
            compositeDisposable.add(
                    eventInitialRepository.createEvent(enrollmentUid, trackedEntityInstance, program.uid(), programStageModel, date, orgUnitUid, categoryOptionComboUid, categoryOptionsUid, geometry)
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .subscribe(
                                    view::onEventCreated,
                                    t -> view.renderError(t.getMessage())
                            )
            );
        }
    }

    public void scheduleEventPermanent(String enrollmentUid, String trackedEntityInstanceUid, String programStageModel,
                                       Date dueDate, String orgUnitUid, String categoryOptionComboUid, String categoryOptionsUid, Geometry geometry) {
        if (program != null) {
            preferences.setValue(Preference.CURRENT_ORG_UNIT, orgUnitUid);
            compositeDisposable.add(
                    eventInitialRepository.scheduleEvent(enrollmentUid, null, program.uid(), programStageModel, dueDate, orgUnitUid, categoryOptionComboUid, categoryOptionsUid, geometry)
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .subscribe(
                                    view::onEventCreated,
                                    t -> view.renderError(t.getMessage())
                            )
            );
        }
    }

    public void scheduleEvent(String enrollmentUid, String programStageModel, Date dueDate, String orgUnitUid,
                              String categoryOptionComboUid, String categoryOptionsUid, Geometry geometry) {
        if (program != null) {
            preferences.setValue(Preference.CURRENT_ORG_UNIT, orgUnitUid);
            compositeDisposable.add(
                    eventInitialRepository.scheduleEvent(enrollmentUid, null, program.uid(), programStageModel, dueDate, orgUnitUid, categoryOptionComboUid, categoryOptionsUid, geometry)
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .subscribe(
                                    view::onEventCreated,
                                    t -> view.renderError(t.getMessage())
                            )
            );
        }
    }

    public void editEvent(String trackedEntityInstance, String programStageModel, String eventUid, String date,
                          String orgUnitUid, String catComboUid, String catOptionCombo, Geometry geometry) {

        compositeDisposable.add(
                eventInitialRepository.editEvent(trackedEntityInstance, eventUid, date, orgUnitUid, catComboUid, catOptionCombo, geometry)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                eventModel -> view.onEventUpdated(eventModel.uid()),
                                error -> view.displayMessage(error.getLocalizedMessage())
                        )
        );
    }

    public void onDateClick(@Nullable DatePickerDialog.OnDateSetListener listener) {
        view.showDateDialog(listener);
    }

    public void onOrgUnitButtonClick() {
        view.showOrgUnitSelector(orgUnits);
    }

    public void onFieldChanged(CharSequence s, int start, int before, int count) {
        view.checkActionButtonVisibility();
    }

    public void onDettach() {
        compositeDisposable.clear();
    }

    public void displayMessage(String message) {
        view.displayMessage(message);
    }

    private void getSectionCompletion() {
        Flowable<List<FieldUiModel>> fieldsFlowable = eventInitialRepository.list();
        Flowable<Result<RuleEffect>> ruleEffectFlowable = eventInitialRepository.calculate()
                .subscribeOn(schedulerProvider.computation())
                .onErrorReturn(throwable -> Result.failure(new Exception(throwable)));

        // Combining results of two repositories into a single stream.
        Flowable<List<FieldUiModel>> viewModelsFlowable = Flowable.zip(fieldsFlowable, ruleEffectFlowable,
                this::applyEffects);

        compositeDisposable.add(
                eventInitialRepository.eventSections()
                        .flatMap(sectionList -> viewModelsFlowable
                                .map(fields -> eventFieldMapper.map(
                                        fields,
                                        sectionList,
                                        "",
                                        new ArrayMap<>(),
                                        new ArrayMap<>(),
                                        new Pair<>(false, false)
                                )))
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                sectionsAndFields -> view.updatePercentage(
                                        eventFieldMapper.completedFieldsPercentage()),
                                Timber::d
                        ));
    }

    @NonNull
    private List<FieldUiModel> applyEffects(@NonNull List<FieldUiModel> viewModels,
                                            @NonNull Result<RuleEffect> calcResult) {
        if (calcResult.error() != null) {
            Timber.e(calcResult.error());
            return viewModels;
        }

        Map<String, FieldUiModel> fieldViewModels = toMap(viewModels);
        ruleUtils.applyRuleEffects(true, fieldViewModels, calcResult, null, options -> new ArrayList<>());

        return new ArrayList<>(fieldViewModels.values());
    }

    @NonNull
    private static Map<String, FieldUiModel> toMap(@NonNull List<FieldUiModel> fieldViewModels) {
        Map<String, FieldUiModel> map = new LinkedHashMap<>();
        for (FieldUiModel fieldViewModel : fieldViewModels) {
            map.put(fieldViewModel.getUid(), fieldViewModel);
        }
        return map;
    }

    public String getCatOptionCombo(String catComboUid, List<CategoryOptionCombo> categoryOptionCombos, List<CategoryOption> values) {
        return eventInitialRepository.getCategoryOptionCombo(catComboUid, UidsHelper.getUidsList(values));
    }

    public Date getStageLastDate(String programStageUid, String enrollmentUid) {
        return eventInitialRepository.getStageLastDate(programStageUid, enrollmentUid);
    }

    public void getEventOrgUnit(String ouUid) {
        compositeDisposable.add(
                eventInitialRepository.getOrganisationUnit(ouUid)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                orgUnit -> view.setOrgUnit(orgUnit.uid(), orgUnit.displayName()),
                                Timber::e
                        )
        );
    }

    public void initOrgunit(Date selectedDate) {
        compositeDisposable.add(eventInitialRepository
                .filteredOrgUnits(DateUtils.databaseDateFormat().format(selectedDate), programId, null)
                .flatMap(filteredOrgUnits -> {
                    if (orgUnits.size() > 1) {
                        orgUnits = filteredOrgUnits;
                    }
                    if (getCurrentOrgUnit(null) != null) {
                        String prevOrgUnitUid = getCurrentOrgUnit(null);
                        for (OrganisationUnit ou : orgUnits) {
                            if (ou.uid().equals(prevOrgUnitUid)) {
                                return Observable.just(ou);
                            }
                        }
                        return Observable.error(new NullPointerException("Orgunit is null"));

                    } else if (orgUnits.size() == 1 && (view.eventcreateionType() == EventCreationType.ADDNEW
                            || view.eventcreateionType() == EventCreationType.DEFAULT)) {
                        return Observable.just(orgUnits.get(0));
                    } else {
                        return Observable.error(new NullPointerException("No org units available"));
                    }
                })
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        orgUnit -> view.setInitialOrgUnit(orgUnit),
                        throwable -> view.setInitialOrgUnit(null)
                )
        );
    }

    public CategoryOption getCatOption(String selectedOption) {
        return eventInitialRepository.getCatOption(selectedOption);
    }

    public int catOptionSize(String uid) {
        return eventInitialRepository.getCatOptionSize(uid);
    }

    public void setChangingCoordinates(boolean changingCoordinates) {
        if (changingCoordinates) {
            preferences.setValue(Preference.EVENT_COORDINATE_CHANGED, true);
        } else {
            preferences.removeValue(Preference.EVENT_COORDINATE_CHANGED);
        }
    }

    public List<CategoryOption> getCatOptions(String categoryUid) {
        return eventInitialRepository.getCategoryOptions(categoryUid);
    }

    public boolean getCompletionPercentageVisibility() {
        return eventInitialRepository.showCompletionPercentage();
    }

    public void onEventCreated() {
        matomoAnalyticsController.trackEvent(EVENT_LIST, CREATE_EVENT, CLICK);
    }
}
