package org.dhis2.usescases.enrollment

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.flowables.ConnectableFlowable
import io.reactivex.functions.BiFunction
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.Bindings.profilePicturePath
import org.dhis2.R
import org.dhis2.data.forms.dataentry.EnrollmentRepository
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.fields.display.DisplayViewModel
import org.dhis2.data.forms.dataentry.fields.optionset.OptionSetViewModel
import org.dhis2.data.forms.dataentry.fields.section.SectionViewModel
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.utils.DhisTextUtils
import org.dhis2.utils.Result
import org.dhis2.utils.RulesActionCallbacks
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
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceObjectRepository
import org.hisp.dhis.rules.models.RuleActionShowError
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
    private val onRowActionProcessor: FlowableProcessor<RowAction>,
    private val sectionProcessor: Flowable<String>,
    private val matomoAnalyticsController: MatomoAnalyticsController,
    private val formRepository: FormRepository
) : RulesActionCallbacks {

    private var finishing: Boolean = false
    private val disposable = CompositeDisposable()
    private val optionsToHide = HashMap<String, ArrayList<String>>()
    private val optionsGroupsToHide = HashMap<String, ArrayList<String>>()
    private val optionsGroupToShow = HashMap<String, ArrayList<String>>()
    private val fieldsFlowable: FlowableProcessor<Boolean> = PublishProcessor.create()
    private var selectedSection: String = ""
    private var errorFields = mutableMapOf<String, String>()
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

        listenToActions()

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

    @VisibleForTesting
    fun listenToActions() {
        disposable.add(
            onRowActionProcessor
                .onBackpressureBuffer()
                .doOnNext { view.showProgress() }
                .observeOn(schedulerProvider.io())
                .flatMap { rowAction ->
                    Flowable.just(formRepository.processUserAction(rowAction))
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { result ->
                        result.valueStoreResult?.let {
                            when (it) {
                                ValueStoreResult.VALUE_CHANGED -> {
                                    if (shouldShowDateEditionWarning(result.uid)) {
                                        view.showDateEditionWarning()
                                    }
                                    fieldsFlowable.onNext(true)
                                    checkFinishing(true)
                                }
                                ValueStoreResult.VALUE_HAS_NOT_CHANGED -> {
                                    populateList()
                                    view.hideProgress()
                                    checkFinishing(true)
                                }
                                ValueStoreResult.VALUE_NOT_UNIQUE -> {
                                    uniqueFields.add(result.uid)
                                    view.showInfoDialog(
                                        view.context.getString(R.string.error),
                                        view.context.getString(R.string.unique_warning)
                                    )
                                    view.hideProgress()
                                    checkFinishing(false)
                                }
                                ValueStoreResult.UID_IS_NOT_DE_OR_ATTR -> {
                                    Timber.tag(TAG)
                                        .d("${result.uid} is not a data element or attribute")
                                    view.hideProgress()
                                    checkFinishing(false)
                                }
                            }
                        } ?: view.hideProgress()
                    },
                    { Timber.tag(TAG).e(it) }
                )
        )
    }

    private fun checkFinishing(canFinish: Boolean) {
        if (finishing && canFinish) {
            view.performSaveClick()
        }
        finishing = false
    }

    private fun populateList(items: List<FieldUiModel>? = null) {
        view.showFields(formRepository.composeList(items))
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
                if (field.getError()?.isNotEmpty() == true) {
                    errorFields[field.getProgramStageSection() ?: sectionUid] = field.getLabel()
                }
                if (field.isMandatory() && field.getValue().isNullOrEmpty()) {
                    mandatoryFields[field.getLabel()] = field.getProgramStageSection() ?: sectionUid
                    if (showErrors.first) {
                        iterator.set(field.setWarning(mandatoryWarning))
                    }
                }
            }

            if (field !is SectionViewModel && !field.getProgramStageSection().equals(sectionUid)) {
                iterator.remove()
            }
        }
        val sections = finalList.filterIsInstance<SectionViewModel>()

        sections.takeIf { showErrors.first || showErrors.second }?.forEach { section ->
            var errors = 0
            var warnings = 0
            if (showErrors.first) {
                repeat(mandatoryFields.filter { it.value == section.uid() }.size) { warnings++ }
            }
            if (showErrors.second) {
                repeat(errorFields.filter { it.value == section.uid() }.size) { errors++ }
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
        fields.filter { it.getProgramStageSection().equals(sectionUid) && it !is SectionViewModel }
            .forEach {
                total++
                if (!it.getValue().isNullOrEmpty()) {
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
                    BiFunction { fields, result -> applyRuleEffects(fields, result) }
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
                    enrollmentFormRepository.autoGenerateEvents()
                        .flatMap { enrollmentFormRepository.useFirstStageDuringRegistration() }
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                            {
                                if (!DhisTextUtils.isEmpty(it.second)) {
                                    view.openEvent(it.second)
                                } else {
                                    view.openDashboard(it.first)
                                }
                            },
                            { Timber.tag(TAG).e(it) }
                        )
                )
            }
            EnrollmentActivity.EnrollmentMode.CHECK -> view.setResultAndFinish()
        }
    }

    fun updateFields() {
        fieldsFlowable.onNext(true)
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

    private fun applyRuleEffects(
        fields: List<FieldUiModel>,
        result: Result<RuleEffect>
    ): List<FieldUiModel> {
        if (result.error() != null) {
            Timber.tag(TAG).e(result.error())
            return fields
        }

        mandatoryFields.clear()
        errorFields.clear()
        uniqueFields.clear()
        optionsToHide.clear()
        optionsGroupsToHide.clear()
        optionsGroupToShow.clear()

        val fieldMap = fields.map { it.getUid() to it }.toMap().toMutableMap()

        RulesUtilsProviderImpl(d2)
            .applyRuleEffects(fieldMap, result, this)

        val fieldList = ArrayList(fieldMap.values)

        return fieldList.map { fieldViewModel ->
            when (fieldViewModel) {
                is SpinnerViewModel -> {
                    var mappedSpinnerModel = fieldViewModel.setOptionsToHide(
                        optionsToHide[fieldViewModel.uid()] ?: emptyList(),
                        optionsGroupsToHide[fieldViewModel.uid()] ?: emptyList()
                    )
                    if (optionsGroupToShow.keys.contains(fieldViewModel.uid())) {
                        mappedSpinnerModel =
                            fieldViewModel.setOptionGroupsToShow(
                                optionsGroupToShow[fieldViewModel.uid()]
                            )
                    }
                    mappedSpinnerModel
                }
                is OptionSetViewModel -> {
                    var mappedOptionSetModel = fieldViewModel.setOptionsToHide(
                        optionsToHide[fieldViewModel.uid()] ?: emptyList()
                    )
                    if (optionsGroupToShow.keys.contains(fieldViewModel.uid())) {
                        mappedOptionSetModel = fieldViewModel.setOptionsToShow(
                            enrollmentFormRepository.getOptionsFromGroups(
                                optionsGroupToShow[fieldViewModel.uid()] ?: arrayListOf()
                            )
                        )
                    }
                    mappedOptionSetModel
                }
                else -> {
                    fieldViewModel
                }
            }
        }
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
        teiRepository.blockingDelete()
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

    override fun setShowError(showError: RuleActionShowError, model: FieldUiModel?) {
        // not used
    }

    override fun unsupportedRuleAction() {
        // not used
    }

    override fun save(uid: String, value: String?) {
        assignValue(uid, value)
    }

    override fun setMessageOnComplete(content: String, canComplete: Boolean) = Unit

    override fun setHideProgramStage(programStageUid: String) = Unit

    override fun setOptionToHide(optionUid: String, field: String) {
        if (!optionsToHide.containsKey(field)) {
            optionsToHide[field] = arrayListOf(optionUid)
        }
        optionsToHide[field]!!.add(optionUid)
        valueStore.deleteOptionValueIfSelected(field, optionUid)
    }

    override fun setOptionGroupToHide(optionGroupUid: String, toHide: Boolean, field: String) {
        if (toHide) {
            if (!optionsGroupsToHide.containsKey(field)) {
                optionsGroupsToHide[field] = arrayListOf()
            }
            optionsGroupsToHide[field]!!.add(optionGroupUid)
            if (!optionsToHide.containsKey(field)) {
                optionsToHide[field] = arrayListOf()
            }
            optionsToHide[field]!!.addAll(
                enrollmentFormRepository.getOptionsFromGroups(
                    arrayListOf(
                        optionGroupUid
                    )
                )
            )
            valueStore.deleteOptionValueIfSelectedInGroup(field, optionGroupUid, true)
        } else if (!optionsGroupsToHide.containsKey(field) || !optionsGroupsToHide.contains(
            optionGroupUid
        )
        ) {
            if (optionsGroupToShow[field] != null) {
                optionsGroupToShow[field]!!.add(optionGroupUid)
            } else {
                optionsGroupToShow[field] = arrayListOf(optionGroupUid)
            }
            valueStore.deleteOptionValueIfSelectedInGroup(field, optionGroupUid, false)
        }
    }

    private fun assignValue(uid: String, value: String?) {
        try {
            if (d2.dataElementModule().dataElements().uid(uid).blockingExists()) {
                Timber.d("Enrollments rules should not assign values to dataElements")
            } else if (
                d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingExists()
            ) {
                valueStore.save(uid, value).blockingFirst()
            }
        } catch (d2Error: D2Error) {
            Timber.e(d2Error.originalException())
        }
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
                showErrors = Pair(showErrors.first, true)
                view.showErrorFieldsMessage(errorFields.values.toList())
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
}
