package org.dhis2.usescases.enrollment

import android.annotation.SuppressLint
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.flowables.ConnectableFlowable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.Bindings.profilePicturePath
import org.dhis2.R
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.schedulers.defaultSubscribe
import org.dhis2.data.forms.dataentry.EnrollmentRepository
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.fields.display.DisplayViewModel
import org.dhis2.data.forms.dataentry.fields.section.SectionViewModel
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.utils.Result
import org.dhis2.utils.RulesUtilsProviderConfigurationError
import org.dhis2.utils.RulesUtilsProviderImpl
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.DELETE_AND_BACK
import org.dhis2.utils.analytics.SAVE_ENROLL
import org.dhis2.utils.analytics.matomo.Actions.Companion.CREATE_TEI
import org.dhis2.utils.analytics.matomo.Categories.Companion.TRACKER_LIST
import org.dhis2.utils.analytics.matomo.Labels.Companion.CLICK
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceObjectRepository
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber

private const val TAG = "EnrollmentPresenter"

class EnrollmentPresenterImpl(
    val view: EnrollmentView,
    val d2: D2,
    private val enrollmentObjectRepository: EnrollmentObjectRepository,
    private val dataEntryRepository: EnrollmentRepository,
    private val teiRepository: TrackedEntityInstanceObjectRepository,
    private val programRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program>,
    private val schedulerProvider: SchedulerProvider,
    private val enrollmentFormRepository: EnrollmentFormRepository,
    private val valueStore: ValueStore,
    private val analyticsHelper: AnalyticsHelper,
    private val mandatoryWarning: String,
    private val sectionProcessor: Flowable<String>,
    private val matomoAnalyticsController: MatomoAnalyticsController
) {

    private var configurationErrors: List<RulesUtilsProviderConfigurationError> = listOf()
    private var showConfigurationError = true
    private var finishing: Boolean = false
    private val disposable = CompositeDisposable()
    private val fieldsFlowable: FlowableProcessor<Boolean> = PublishProcessor.create()
    private var selectedSection: String = ""
    private var errorFields = mutableMapOf<String, String>()
    private var warningFields = mutableMapOf<String, String>()
    private var mandatoryFields = mutableMapOf<String, String>()
    private var uniqueFields = mutableListOf<String>()
    private val backButtonProcessor: FlowableProcessor<Boolean> = PublishProcessor.create()
    private var showErrors: Pair<Boolean, Boolean> = Pair(first = false, second = false)
    private var hasShownIncidentDateEditionWarning = false
    private var hasShownEnrollmentDateEditionWarning = false

    fun init() {
        view.setSaveButtonVisible(false)

        disposable.add(
            teiRepository.get()
                .flatMap { tei ->
                    d2.trackedEntityModule().trackedEntityTypeAttributes()
                        .byTrackedEntityTypeUid().eq(tei.trackedEntityType()).get()
                        .map { list ->
                            val attrList = list.filter {
                                d2.trackedEntityModule().trackedEntityAttributes()
                                    .uid(it.trackedEntityAttribute()?.uid())
                                    .blockingGet().valueType() != ValueType.IMAGE
                            }.sortedBy {
                                it.sortOrder()
                            }.map {
                                d2.trackedEntityModule().trackedEntityAttributeValues()
                                    .byTrackedEntityInstance().eq(tei.uid())
                                    .byTrackedEntityAttribute().eq(
                                        it.trackedEntityAttribute()?.uid()
                                    )
                                    .one()
                                    .blockingGet()?.value() ?: ""
                            }
                            val icon =
                                tei.profilePicturePath(d2, programRepository.blockingGet().uid())
                            Pair(attrList, icon)
                        }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { mainAttributes ->
                        view.displayTeiInfo(mainAttributes.first, mainAttributes.second)
                    },
                    { Timber.tag(TAG).e(it) }
                )
        )

        disposable.add(
            programRepository.get()
                .map { it.access()?.data()?.write() }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.setAccess(it) },
                    { Timber.tag(TAG).e(it) }
                )
        )

        disposable.add(
            enrollmentObjectRepository.get()
                .map { it.status() }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.renderStatus(it!!) },
                    { Timber.tag(TAG).e(it) }
                )
        )

        val fields = getFieldFlowable()

        disposable.add(
            dataEntryRepository.enrollmentSectionUids()
                .flatMap { sectionList ->
                    sectionProcessor.startWith(sectionList[0])
                        .map { setCurrentSection(it) }
                        .doOnNext { view.showProgress() }
                        .switchMap { section ->
                            fields.map { fieldList ->
                                return@map setFieldsToShow(section, fieldList)
                            }
                        }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({
                    populateList(it)
                    view.setSaveButtonVisible(true)
                    view.hideProgress()
                    if (configurationErrors.isNotEmpty() && showConfigurationError) {
                        view.displayConfigurationErrors(configurationErrors)
                    }
                }) {
                    Timber.tag(TAG).e(it)
                }
        )

        disposable.add(
            sectionProcessor
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.io())
                .subscribe(
                    { fieldsFlowable.onNext(true) },
                    { Timber.tag(TAG).e(it) }
                )
        )

        fields.connect()
    }

    private fun checkFinishing() {
        if (finishing) {
            view.performSaveClick()
        }
        finishing = false
    }

    private fun populateList(items: List<FieldUiModel>? = null) {
        view.showFields(items)
    }

    private fun setCurrentSection(sectionUid: String): String {
        if (sectionUid == selectedSection) {
            this.selectedSection = ""
        } else {
            this.selectedSection = sectionUid
        }
        return selectedSection
    }

    private fun shouldShowDateEditionWarning(uid: String): Boolean {
        return if (uid == EnrollmentRepository.ENROLLMENT_DATE_UID &&
            dataEntryRepository.hasEventsGeneratedByEnrollmentDate() &&
            !hasShownEnrollmentDateEditionWarning
        ) {
            hasShownEnrollmentDateEditionWarning = true
            true
        } else if (uid == EnrollmentRepository.INCIDENT_DATE_UID &&
            dataEntryRepository.hasEventsGeneratedByIncidentDate() &&
            !hasShownIncidentDateEditionWarning
        ) {
            hasShownIncidentDateEditionWarning = true
            true
        } else {
            false
        }
    }

    fun setFieldsToShow(sectionUid: String, fieldList: List<FieldUiModel>): List<FieldUiModel> {
        val finalList = fieldList.toMutableList()
        val iterator = finalList.listIterator()
        while (iterator.hasNext()) {
            val field = iterator.next()
            if (field is SectionViewModel) {
                var sectionViewModel: SectionViewModel = field
                val (values, totals) = getValueCount(
                    fieldList,
                    sectionViewModel.uid()
                )
                sectionViewModel = sectionViewModel
                    .setOpen(field.uid() == sectionUid)
                    .setCompletedFields(values)
                    .setTotalFields(totals)
                iterator.set(sectionViewModel)
            }

            if (field !is SectionViewModel && field !is DisplayViewModel) {
                if (field.mandatory && field.value.isNullOrEmpty()) {
                    mandatoryFields[field.label] = field.programStageSection ?: sectionUid
                    if (showErrors.first) {
                        iterator.set(field.setWarning(mandatoryWarning))
                    }
                }
            }

            if (field !is SectionViewModel && !field.programStageSection.equals(sectionUid)) {
                iterator.remove()
            }
        }
        val sections = finalList.filterIsInstance<SectionViewModel>()

        sections.takeIf { showErrors.first || showErrors.second }?.forEach { section ->
            var errors = 0
            var warnings = 0
            if (showErrors.first) {
                repeat(
                    warningFields.filter { warning ->
                        fieldList.firstOrNull { field ->
                            field.uid == warning.key && field.programStageSection == section.uid()
                        } != null
                    }.size +
                        mandatoryFields.filter { it.value == section.uid() }.size
                ) { warnings++ }
            }
            if (showErrors.second) {
                repeat(
                    errorFields.filter { error ->
                        fieldList.firstOrNull { field ->
                            field.uid == error.key && field.programStageSection == section.uid()
                        } != null
                    }.size
                ) { errors++ }
            }
            finalList[finalList.indexOf(section)] = section.withErrorsAndWarnings(
                if (errors != 0) {
                    errors
                } else {
                    null
                },
                if (warnings != 0) {
                    warnings
                } else {
                    null
                }
            )
        }
        return finalList
    }

    private fun getValueCount(fields: List<FieldUiModel>, sectionUid: String): Pair<Int, Int> {
        var total = 0
        var values = 0
        fields.filter { it.programStageSection.equals(sectionUid) && it !is SectionViewModel }
            .forEach {
                total++
                if (!it.value.isNullOrEmpty()) {
                    values++
                }
            }
        return Pair(values, total)
    }

    private fun getFieldFlowable(): ConnectableFlowable<List<FieldUiModel>> {
        return fieldsFlowable.startWith(true)
            .observeOn(schedulerProvider.io())
            .flatMap {
                Flowable.zip<List<FieldUiModel>, Result<RuleEffect>, List<FieldUiModel>>(
                    dataEntryRepository.list(),
                    enrollmentFormRepository.calculate(),
                    { fields, result -> applyRuleEffects(fields, result) }
                )
            }.publish()
    }

    fun subscribeToBackButton() {
        disposable.add(
            backButtonProcessor
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.performSaveClick() },
                    { t -> Timber.e(t) }
                )
        )
    }

    fun finish(enrollmentMode: EnrollmentActivity.EnrollmentMode) {
        when (enrollmentMode) {
            EnrollmentActivity.EnrollmentMode.NEW -> {
                matomoAnalyticsController.trackEvent(TRACKER_LIST, CREATE_TEI, CLICK)
                disposable.add(
                    enrollmentFormRepository.generateEvents()
                        .defaultSubscribe(
                            schedulerProvider,
                            {
                                it.second?.let { eventUid ->
                                    view.openEvent(eventUid)
                                } ?: view.openDashboard(it.first)
                            },
                            { Timber.tag(TAG).e(it) }
                        )
                )
            }
            EnrollmentActivity.EnrollmentMode.CHECK -> view.setResultAndFinish()
        }
    }

    fun updateFields(action: RowAction? = null) {
        action?.let {
            if (shouldShowDateEditionWarning(it.id)) {
                view.showDateEditionWarning()
            }
        }

        fieldsFlowable.onNext(true)
        checkFinishing()
    }

    fun backIsClicked() {
        backButtonProcessor.onNext(true)
    }

    fun openInitial(eventUid: String): Boolean {
        val catComboUid = getProgram().categoryComboUid()
        val event = d2.eventModule().events().uid(eventUid).blockingGet()
        val stage = d2.programModule().programStages().uid(event.programStage()).blockingGet()
        val needsCatCombo = programRepository.blockingGet().categoryComboUid() != null &&
            d2.categoryModule().categoryCombos().uid(catComboUid)
            .blockingGet().isDefault == false
        val needsCoordinates =
            stage.featureType() != null && stage.featureType() != FeatureType.NONE

        return needsCatCombo || needsCoordinates
    }

    fun applyRuleEffects(
        fields: List<FieldUiModel>,
        result: Result<RuleEffect>
    ): List<FieldUiModel> {
        if (result.error() != null) {
            Timber.tag(TAG).e(result.error())
            return fields
        }

        mandatoryFields.clear()
        errorFields.clear()
        warningFields.clear()
        uniqueFields.clear()

        val fieldMap = fields.map { it.uid to it }.toMap().toMutableMap()
        RulesUtilsProviderImpl(d2).applyRuleEffects(
            false,
            fieldMap,
            result,
            valueStore
        ).apply {
            this@EnrollmentPresenterImpl.configurationErrors = configurationErrors
            errorFields = errorMap().toMutableMap()
            warningFields = warningMap().toMutableMap()
        }

        return ArrayList(fieldMap.values)
    }

    fun getEnrollment(): Enrollment? {
        return enrollmentObjectRepository.blockingGet()
    }

    fun getProgram(): Program {
        return programRepository.blockingGet()
    }

    fun updateEnrollmentStatus(newStatus: EnrollmentStatus): Boolean {
        return try {
            if (getProgram().access()?.data()?.write() == true) {
                enrollmentObjectRepository.setStatus(newStatus)
                view.renderStatus(newStatus)
                true
            } else {
                view.displayMessage(null)
                false
            }
        } catch (error: D2Error) {
            false
        }
    }

    fun hasAccess() = getProgram().access()?.data()?.write() ?: false

    fun saveEnrollmentGeometry(geometry: Geometry?) {
        enrollmentObjectRepository.setGeometry(geometry)
    }

    fun saveTeiGeometry(geometry: Geometry?) {
        teiRepository.setGeometry(geometry)
    }

    fun deleteAllSavedData() {
        if (teiRepository.blockingGet().syncState() == State.TO_POST) {
            teiRepository.blockingDelete()
        } else {
            enrollmentObjectRepository.blockingDelete()
        }
        analyticsHelper.setEvent(DELETE_AND_BACK, CLICK, DELETE_AND_BACK)
    }

    @SuppressLint("CheckResult")
    fun saveFile(attributeUid: String, value: String?) {
        valueStore.save(attributeUid, value).blockingFirst()
    }

    fun onDettach() {
        disposable.clear()
    }

    fun displayMessage(message: String?) {
        view.displayMessage(message)
    }

    fun dataIntegrityCheck(): Boolean {
        return when {
            uniqueFields.isNotEmpty() -> {
                view.showInfoDialog(
                    view.context.getString(R.string.error),
                    view.context.getString(R.string.unique_coincidence_found)
                )
                false
            }
            mandatoryFields.isNotEmpty() -> {
                showErrors = Pair(true, showErrors.second)
                fieldsFlowable.onNext(true)
                view.showMissingMandatoryFieldsMessage(mandatoryFields)
                false
            }
            this.errorFields.isNotEmpty() -> {
                showErrors = Pair(showErrors.first || warningFields.isNotEmpty(), true)
                fieldsFlowable.onNext(true)
                view.showErrorFieldsMessage(errorFields.values.toList())
                false
            }
            warningFields.isNotEmpty() -> {
                showErrors = Pair(true, showErrors.second)
                fieldsFlowable.onNext(true)
                view.showWarningFieldsMessage(warningFields.values.toList())
                false
            }
            else -> {
                analyticsHelper.setEvent(SAVE_ENROLL, CLICK, SAVE_ENROLL)
                true
            }
        }
    }

    fun onTeiImageHeaderClick() {
        val picturePath = enrollmentFormRepository.getProfilePicture()
        if (picturePath.isNotEmpty()) {
            view.displayTeiPicture(picturePath)
        }
    }

    fun setFinishing() {
        finishing = true
    }

    fun disableConfErrorMessage() {
        showConfigurationError = false
    }

    fun getEventStage(eventUid: String) =
        enrollmentFormRepository.getProgramStageUidFromEvent(eventUid)
}
