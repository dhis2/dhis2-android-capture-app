package org.dhis2.usescases.eventsWithoutRegistration.eventInitial

import androidx.annotation.VisibleForTesting
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.commons.matomo.Actions
import org.dhis2.commons.matomo.Categories
import org.dhis2.commons.matomo.Labels
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.form.data.RulesUtilsProvider
import org.dhis2.form.model.FieldUiModel
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventFieldMapper
import org.dhis2.utils.DhisTextUtils.Companion.isEmpty
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.BACK_EVENT
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.event.EventEditableStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber
import java.util.Date

class EventInitialPresenter(
    private val view: EventInitialContract.View,
    private val ruleUtils: RulesUtilsProvider,
    private val eventInitialRepository: EventInitialRepository,
    private val schedulerProvider: SchedulerProvider,
    private val preferences: PreferenceProvider,
    private val analyticsHelper: AnalyticsHelper,
    private val matomoAnalyticsController: MatomoAnalyticsController,
    private val eventFieldMapper: EventFieldMapper,
) {
    private var eventId: String? = null

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    private var program: Program? = null

    private var programStageId: String? = null

    fun init(
        programId: String?,
        eventId: String?,
        orgInitId: String?,
        programStageId: String?,
    ) {
        this.eventId = eventId
        this.programStageId = programStageId

        view.setAccessDataWrite(
            eventInitialRepository.accessDataWrite(programId).blockingFirst(),
        )

        if (eventId != null) {
            compositeDisposable
                .add(
                    Flowable
                        .zip(
                            eventInitialRepository
                                .getProgramWithId(programId)
                                .toFlowable(BackpressureStrategy.LATEST),
                            eventInitialRepository.programStageForEvent(eventId),
                            { program, programStage -> Pair(program, programStage) },
                        ).subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                            { (program, programStage) ->
                                this.program = program
                                view.setProgram(program)
                                view.setProgramStage(programStage)
                            },
                            { t -> Timber.d(t) },
                        ),
                )
        } else {
            compositeDisposable.add(
                eventInitialRepository
                    .getProgramWithId(programId)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        { program ->
                            this.program = program
                            view.setProgram(program)
                        },
                        Timber::d,
                    ),
            )

            getProgramStages(programId, programStageId)
        }

        if (eventId != null) this.sectionCompletion
    }

    @VisibleForTesting
    fun getCurrentOrgUnit(orgUnitUid: String?): String? =
        if (preferences.contains(Preference.CURRENT_ORG_UNIT)) {
            preferences.getString(Preference.CURRENT_ORG_UNIT, null)
        } else {
            orgUnitUid
        }

    fun onShareClick() {
        view.showQR()
    }

    fun deleteEvent(trackedEntityInstance: String?) {
        if (eventId != null) {
            eventInitialRepository.deleteEvent(eventId, trackedEntityInstance)
            view.showEventWasDeleted()
        } else {
            view.showDeleteEventError()
        }
    }

    val isEnrollmentOpen: Boolean
        get() = eventInitialRepository.isEnrollmentOpen()

    fun getProgramStage(programStageUid: String?) {
        compositeDisposable.add(
            eventInitialRepository
                .programStageWithId(programStageUid)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { programStage -> view.setProgramStage(programStage) },
                    { _ -> view.showProgramStageSelection() },
                ),
        )
    }

    private fun getProgramStages(
        programUid: String?,
        programStageUid: String?,
    ) {
        compositeDisposable.add(
            (
                if (isEmpty(programStageId)) {
                    eventInitialRepository.programStage(programUid)
                } else {
                    eventInitialRepository.programStageWithId(programStageUid)
                }
            ).subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { programStage -> view.setProgramStage(programStage) },
                    { _ -> view.showProgramStageSelection() },
                ),
        )
    }

    fun onBackClick() {
        setChangingCoordinates(false)
        if (eventId != null) {
            analyticsHelper.setEvent(
                BACK_EVENT,
                Labels.Companion.CLICK,
                Actions.Companion.CREATE_EVENT,
            )
        }
        view.back()
    }

    fun createEvent(
        enrollmentUid: String?,
        programStageModel: String,
        date: Date,
        orgUnitUid: String,
        categoryOptionsUid: String,
        geometry: Geometry?,
        trackedEntityInstance: String?,
    ) {
        if (program != null) {
            preferences.setValue(Preference.CURRENT_ORG_UNIT, orgUnitUid)
            compositeDisposable.add(
                eventInitialRepository
                    .createEvent(
                        enrollmentUid,
                        trackedEntityInstance,
                        program!!.uid(),
                        programStageModel,
                        date,
                        orgUnitUid,
                        categoryOptionsUid,
                        geometry,
                    ).subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        { eventUid -> view.onEventCreated(eventUid) },
                        { t -> view.renderError(t.message) },
                    ),
            )
        }
    }

    fun scheduleEvent(
        enrollmentUid: String?,
        programStageModel: String,
        dueDate: Date,
        orgUnitUid: String,
        categoryOptionsUid: String,
        geometry: Geometry?,
    ) {
        if (program != null) {
            preferences.setValue(Preference.CURRENT_ORG_UNIT, orgUnitUid)
            compositeDisposable.add(
                eventInitialRepository
                    .scheduleEvent(
                        enrollmentUid,
                        null,
                        program!!.uid(),
                        programStageModel,
                        dueDate,
                        orgUnitUid,
                        categoryOptionsUid,
                        geometry,
                    ).subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        { eventUid -> view.onEventCreated(eventUid) },
                        { t -> view.renderError(t.message) },
                    ),
            )
        }
    }

    fun onDetach() {
        compositeDisposable.clear()
    }

    private val sectionCompletion: Unit
        get() {
            val fieldsFlowable =
                eventInitialRepository.list()
            val ruleEffectFlowable =
                eventInitialRepository
                    .calculate()
                    .subscribeOn(schedulerProvider.computation())
                    .onErrorReturn { throwable -> Result.failure(Exception(throwable)) }

            // Combining results of two repositories into a single stream.
            val viewModelsFlowable =
                Flowable.zip(
                    fieldsFlowable,
                    ruleEffectFlowable,
                    this::applyEffects,
                )

            compositeDisposable.add(
                eventInitialRepository
                    .eventSections()
                    .flatMap { sectionList ->
                        viewModelsFlowable
                            .map { fields ->
                                eventFieldMapper.map(
                                    fields = fields,
                                    sectionList = sectionList,
                                    currentSection = "",
                                    errors = emptyMap(),
                                    warnings = emptyMap(),
                                    emptyMandatoryFields = emptyMap(),
                                    showErrors = Pair(false, false),
                                )
                            }
                    }.subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        { _ ->
                            view.updatePercentage(eventFieldMapper.completedFieldsPercentage())
                        },
                        Timber::d,
                    ),
            )
        }

    private fun applyEffects(
        viewModels: List<FieldUiModel>,
        calcResult: Result<List<RuleEffect>>,
    ): List<FieldUiModel> {
        if (calcResult.isFailure) {
            Timber.e(calcResult.exceptionOrNull())
            return viewModels
        }

        val fieldViewModels = toMap(viewModels).toMutableMap()
        ruleUtils.applyRuleEffects(
            applyForEvent = true,
            fieldViewModels = fieldViewModels,
            calcResult = calcResult.getOrDefault(emptyList()),
            valueStore = null,
        )

        return ArrayList<FieldUiModel>(fieldViewModels.values)
    }

    fun setChangingCoordinates(changingCoordinates: Boolean) {
        if (changingCoordinates) {
            preferences.setValue(Preference.EVENT_COORDINATE_CHANGED, true)
        } else {
            preferences.removeValue(Preference.EVENT_COORDINATE_CHANGED)
        }
    }

    val completionPercentageVisibility: Boolean
        get() = eventInitialRepository.showCompletionPercentage()

    fun onEventCreated() {
        matomoAnalyticsController.trackEvent(
            Categories.Companion.EVENT_LIST,
            Actions.Companion.CREATE_EVENT,
            Labels.Companion.CLICK,
        )
    }

    val isEventEditable: Boolean
        get() =
            eventInitialRepository
                .getEditableStatus()
                .blockingFirst() is EventEditableStatus.Editable

    companion object {
        private fun toMap(fieldViewModels: List<FieldUiModel>): Map<String, FieldUiModel> {
            val map = LinkedHashMap<String, FieldUiModel>()
            for (fieldViewModel in fieldViewModels) {
                map.put(fieldViewModel.uid, fieldViewModel)
            }
            return map
        }
    }
}
