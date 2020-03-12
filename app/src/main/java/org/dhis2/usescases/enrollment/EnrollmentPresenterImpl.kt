package org.dhis2.usescases.enrollment

import android.annotation.SuppressLint
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.flowables.ConnectableFlowable
import io.reactivex.functions.BiFunction
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.Bindings.profilePicturePath
import org.dhis2.Bindings.toDate
import org.dhis2.R
import org.dhis2.data.forms.dataentry.DataEntryRepository
import org.dhis2.data.forms.dataentry.EnrollmentRepository
import org.dhis2.data.forms.dataentry.StoreResult
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.ValueStoreImpl
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.option_set.OptionSetViewModel
import org.dhis2.data.forms.dataentry.fields.section.SectionViewModel
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.utils.DhisTextUtils
import org.dhis2.utils.Result
import org.dhis2.utils.RulesActionCallbacks
import org.dhis2.utils.RulesUtilsProviderImpl
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.DELETE_AND_BACK
import org.dhis2.utils.analytics.SAVE_ENROLL
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
import java.util.concurrent.TimeUnit
import kotlin.collections.set

private const val TAG = "EnrollmentPresenter"

class EnrollmentPresenterImpl(
    val view: EnrollmentView,
    val d2: D2,
    private val enrollmentObjectRepository: EnrollmentObjectRepository,
    private val dataEntryRepository: DataEntryRepository,
    private val teiRepository: TrackedEntityInstanceObjectRepository,
    private val programRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program>,
    private val schedulerProvider: SchedulerProvider,
    val formRepository: EnrollmentFormRepository,
    private val valueStore: ValueStore,
    private val analyticsHelper: AnalyticsHelper
) : RulesActionCallbacks {

    private val disposable = CompositeDisposable()
    private val optionsToHide = HashMap<String, ArrayList<String>>()
    private val optionsGroupsToHide = HashMap<String, ArrayList<String>>()
    private val optionsGroupToShow = HashMap<String, ArrayList<String>>()
    private val fieldsFlowable: FlowableProcessor<Boolean> = PublishProcessor.create()
    private var lastFocusItem: String? = null
    private var selectedSection: String = ""
    private var errorFields = mutableMapOf<String, String>()
    private var mandatoryFields = mutableMapOf<String, String>()
    private var uniqueFields = mutableMapOf<String, String>()
    private val backButtonProcessor: FlowableProcessor<Boolean> = PublishProcessor.create()

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
                                    .byTrackedEntityAttribute().eq(it.trackedEntityAttribute()?.uid())
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

        disposable.add(
            view.rowActions().onBackpressureBuffer()
                .flatMap { rowAction ->
                    when (rowAction.id()) {
                        EnrollmentRepository.ENROLLMENT_DATE_UID -> {
                            enrollmentObjectRepository.setEnrollmentDate(rowAction.value()?.toDate())
                            Flowable.just(
                                StoreResult(
                                    "",
                                    ValueStoreImpl.ValueStoreResult.VALUE_CHANGED
                                )
                            )
                        }
                        EnrollmentRepository.INCIDENT_DATE_UID -> {
                            enrollmentObjectRepository.setIncidentDate(rowAction.value()?.toDate())
                            Flowable.just(
                                StoreResult(
                                    "",
                                    ValueStoreImpl.ValueStoreResult.VALUE_CHANGED
                                )
                            )
                        }
                        EnrollmentRepository.ORG_UNIT_UID -> {
                            Flowable.just(
                                StoreResult(
                                    "",
                                    ValueStoreImpl.ValueStoreResult.VALUE_CHANGED
                                )
                            )
                        }
                        EnrollmentRepository.TEI_COORDINATES_UID -> {
                            val geometry = rowAction.extraData()?.let {
                                Geometry.builder()
                                    .coordinates(rowAction.value())
                                    .type(FeatureType.valueOf(it))
                                    .build()
                            }
                            saveTeiGeometry(geometry)
                            Flowable.just(
                                StoreResult(
                                    "",
                                    ValueStoreImpl.ValueStoreResult.VALUE_CHANGED
                                )
                            )
                        }
                        EnrollmentRepository.ENROLLMENT_COORDINATES_UID -> {
                            val geometry = rowAction.extraData()?.let {
                                Geometry.builder()
                                    .coordinates(rowAction.value())
                                    .type(FeatureType.valueOf(it))
                                    .build()
                            }
                            saveEnrollmentGeometry(geometry)
                            Flowable.just(
                                StoreResult(
                                    "",
                                    ValueStoreImpl.ValueStoreResult.VALUE_CHANGED
                                )
                            )
                        }
                        else -> valueStore.save(rowAction.id(), rowAction.value())
                    }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        when (it.valueStoreResult) {
                            ValueStoreImpl.ValueStoreResult.VALUE_CHANGED -> {
                                lastFocusItem = it.uid
                                fieldsFlowable.onNext(true)
                            }
                            ValueStoreImpl.ValueStoreResult.VALUE_HAS_NOT_CHANGED -> {
                                /*Do nothing*/
                            }
                            ValueStoreImpl.ValueStoreResult.VALUE_NOT_UNIQUE -> {
                                view.showInfoDialog(
                                    view.context.getString(R.string.error),
                                    view.context.getString(R.string.unique_warning)
                                )
                            }
                            ValueStoreImpl.ValueStoreResult.UID_IS_NOT_DE_OR_ATTR ->
                                Timber.tag(TAG).d("${it.uid} is not a data element or attribute")
                        }
                    },
                    { Timber.tag(TAG).e(it) }
                )
        )

        val fields = getFieldFlowable()

        disposable.add(
            dataEntryRepository.enrollmentSectionUids()
                .flatMap { sectionList ->
                    view.sectionFlowable().startWith(sectionList[0])
                        .map { setCurrentSection(it) }
                        .switchMap { section ->
                            fields.map { fieldList ->
                                return@map setFieldsToShow(section, fieldList)
                            }
                        }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({
                    view.showFields(it)
                    view.setSaveButtonVisible(true)
                    view.setSelectedSection(selectedSection)
                }) {
                    Timber.tag(TAG).e(it)
                }
        )

        disposable.add(
            view.sectionFlowable()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.io())
                .subscribe(
                    { fieldsFlowable.onNext(true) },
                    { Timber.tag(TAG).e(it) }
                )
        )

        fields.connect()
    }

    fun setCurrentSection(sectionUid: String): String {
        if (sectionUid == selectedSection) {
            this.selectedSection = ""
        } else {
            this.selectedSection = sectionUid
        }
        return selectedSection
    }

    fun setFieldsToShow(section: String, fieldList: List<FieldViewModel>): List<FieldViewModel> {
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
                    .setOpen(field.uid() == section)
                    .setCompletedFields(values)
                    .setTotalFields(totals)
                iterator.set(sectionViewModel)
            }

            if (field !is SectionViewModel) {
                val isUnique =
                    d2.trackedEntityModule().trackedEntityAttributes().uid(field.uid()).blockingGet()?.unique() ?: false
                var uniqueValueAlreadyExist: Boolean
                if (isUnique && field.value()!=null) {
                    uniqueValueAlreadyExist = d2.trackedEntityModule().trackedEntityAttributeValues()
                        .byTrackedEntityAttribute().eq(field.uid())
                        .byValue().eq(field.value()).blockingGet().size > 1
                    if(uniqueValueAlreadyExist){
                        uniqueFields[field.uid()] = field.label()
                    }
                }
                if (field.error()?.isNotEmpty() == true) {
                    errorFields[field.programStageSection() ?: section] = field.label()
                }
                if (field.mandatory() && field.value().isNullOrEmpty()) {
                    mandatoryFields[field.programStageSection() ?: section] = field.label()
                }
            }

            if (field !is SectionViewModel && !field.programStageSection().equals(
                    section
                )
            ) {
                iterator.remove()
            }
        }
        return finalList
    }

    fun getValueCount(fields: List<FieldViewModel>, sectionUid: String): Pair<Int, Int> {
        var total = 0
        var values = 0
        fields.filter { it.programStageSection().equals(sectionUid) && it !is SectionViewModel }
            .forEach {
                total++
                if (!it.value().isNullOrEmpty()) {
                    values++
                }
            }
        return Pair(values, total)
    }

    fun getFieldFlowable(): ConnectableFlowable<List<FieldViewModel>> {
        return fieldsFlowable.startWith(true)
            .observeOn(schedulerProvider.io())
            .flatMap {
                Flowable.zip<List<FieldViewModel>, Result<RuleEffect>, List<FieldViewModel>>(
                    dataEntryRepository.list(),
                    formRepository.calculate(),
                    BiFunction { fields, result -> applyRuleEffects(fields, result) }
                )
            }.publish()
    }

    fun subscribeToBackButton() {
        disposable.add(backButtonProcessor
            .doOnNext { view.requestFocus() }
            .debounce(1, TimeUnit.SECONDS, schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                { view.performSaveClick() },
                { t -> Timber.e(t) }
            )
        )
    }

    fun finish(enrollmentMode: EnrollmentActivity.EnrollmentMode) {
        when (enrollmentMode) {
            EnrollmentActivity.EnrollmentMode.NEW -> disposable.add(
                formRepository.autoGenerateEvents()
                    .flatMap { formRepository.useFirstStageDuringRegistration() }
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
                d2.categoryModule().categoryCombos().uid(catComboUid).blockingGet().isDefault == false
        val needsCoordinates =
            stage.featureType() != null && stage.featureType() != FeatureType.NONE

        return needsCatCombo || needsCoordinates
    }

    private fun applyRuleEffects(
        fields: List<FieldViewModel>,
        result: Result<RuleEffect>
    ): List<FieldViewModel> {
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

        val fieldMap = fields.map { it.uid() to it }.toMap().toMutableMap()

        RulesUtilsProviderImpl()
            .applyRuleEffects(fieldMap, result, this)

        fieldMap.values.forEach {
            if (it is SpinnerViewModel) {
                it.setOptionsToHide(
                    optionsToHide[it.uid()] ?: emptyList(),
                    optionsGroupsToHide[it.uid()] ?: emptyList()
                )
                if (optionsGroupToShow.keys.contains(it.uid())) {
                    it.optionGroupsToShow = optionsGroupToShow[it.uid()]
                }
            }
            if (it is OptionSetViewModel) {
                it.optionsToHide = optionsToHide[it.uid()]
                if (optionsGroupToShow.keys.contains(it.uid())) {
                    it.optionsToShow = formRepository.getOptionsFromGroups(
                        optionsGroupToShow[it.uid()] ?: arrayListOf()
                    )
                }
            }
        }

        return ArrayList(fieldMap.values)
    }

    fun getEnrollment(): Enrollment {
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

    fun getLastFocusItem(): String? {
        return lastFocusItem
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

    override fun setCalculatedValue(calculatedValueVariable: String, value: String) {
        // not used
    }

    override fun setShowError(showError: RuleActionShowError, model: FieldViewModel?) {
        // not used
    }

    override fun unsupportedRuleAction() {
        // not used
    }

    override fun save(uid: String, value: String?) {
        assignValue(uid, value)
    }

    override fun setDisplayKeyValue(label: String, value: String) = Unit

    override fun setHideSection(sectionUid: String) = Unit

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
                formRepository.getOptionsFromGroups(
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
                view.showMissingMandatoryFieldsMessage(mandatoryFields)
                false
            }
            this.errorFields.isNotEmpty() -> {
                view.showErrorFieldsMessage(errorFields.values.toList())
                false
            }
            else -> {
                analyticsHelper.setEvent(SAVE_ENROLL, CLICK, SAVE_ENROLL)
                true
            }
        }
    }
}
