package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import static org.dhis2.utils.analytics.AnalyticsConstants.BACK_EVENT;
import static org.dhis2.commons.matomo.Actions.CREATE_EVENT;
import static org.dhis2.commons.matomo.Categories.EVENT_LIST;
import static org.dhis2.commons.matomo.Labels.CLICK;

import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import org.dhis2.R;
import org.dhis2.commons.data.tuples.Pair;
import org.dhis2.commons.prefs.Preference;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.form.data.RulesUtilsProvider;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventFieldMapper;
import org.dhis2.utils.DhisTextUtils;
import org.dhis2.utils.Result;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.commons.matomo.MatomoAnalyticsController;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.event.EventEditableStatus;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class EventInitialPresenter {

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

    private final MatomoAnalyticsController matomoAnalyticsController;

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
        this.programStageId = programStageId;

        view.setAccessDataWrite(
                eventInitialRepository.accessDataWrite(programId).blockingFirst()
        );

        if (eventId != null) {
            compositeDisposable
                    .add(
                            Flowable.zip(
                                            eventInitialRepository.getProgramWithId(programId).toFlowable(BackpressureStrategy.LATEST),
                                            eventInitialRepository.programStageForEvent(eventId),
                                            Pair::create)
                                    .subscribeOn(schedulerProvider.io()).observeOn(schedulerProvider.ui())
                                    .subscribe(septet -> {
                                        this.program = septet.val0();
                                        view.setProgram(septet.val0());
                                        view.setProgramStage(septet.val1());
                                    }, Timber::d));

        } else {
            compositeDisposable.add(
                    eventInitialRepository.getProgramWithId(programId)
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .subscribe(
                                    program -> {
                                        this.program = program;
                                        view.setProgram(program);
                                    },
                                    throwable -> {
                                    }
                            )
            );

            getProgramStages(programId, programStageId);
        }

        if (eventId != null)
            getSectionCompletion();
    }

    @VisibleForTesting
    public String getCurrentOrgUnit(String orgUnitUid) {
        if (preferences.contains(Preference.CURRENT_ORG_UNIT)) {
            return preferences.getString(Preference.CURRENT_ORG_UNIT, null);
        } else return orgUnitUid;
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
                    eventInitialRepository.permanentReferral(
                                    enrollmentUid,
                                    trackedEntityInstanceUid,
                                    program.uid(),
                                    programStageModel,
                                    dueDate,
                                    orgUnitUid,
                                    categoryOptionComboUid,
                                    categoryOptionsUid,
                                    geometry)
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

    public void onDettach() {
        compositeDisposable.clear();
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
                                        new ArrayMap<>(),
                                        new kotlin.Pair<>(false, false)
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
        ruleUtils.applyRuleEffects(true, fieldViewModels, calcResult.items(), null);

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

    public void setChangingCoordinates(boolean changingCoordinates) {
        if (changingCoordinates) {
            preferences.setValue(Preference.EVENT_COORDINATE_CHANGED, true);
        } else {
            preferences.removeValue(Preference.EVENT_COORDINATE_CHANGED);
        }
    }

    public boolean getCompletionPercentageVisibility() {
        return eventInitialRepository.showCompletionPercentage();
    }

    public void onEventCreated() {
        matomoAnalyticsController.trackEvent(EVENT_LIST, CREATE_EVENT, CLICK);
    }

    public boolean isEventEditable() {
        return eventInitialRepository.getEditableStatus().blockingFirst() instanceof EventEditableStatus.Editable;
    }
}
