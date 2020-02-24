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
import org.dhis2.data.forms.dataentry.fields.section.SectionViewModel
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.utils.DhisTextUtils
import org.dhis2.utils.Result
import org.dhis2.utils.RulesActionCallbacks
import org.dhis2.utils.RulesUtilsProviderImpl
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
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceObjectRepository
import org.hisp.dhis.rules.models.RuleActionShowError
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber
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
    private val valueStore: ValueStore
) : RulesActionCallbacks {

    private lateinit var disposable: CompositeDisposable
    private val optionsToHide = ArrayList<String>()
    private val optionsGroupsToHide = ArrayList<String>()
    private val optionsGroupToShow = HashMap<String, ArrayList<String>>()
    private val fieldsFlowable: FlowableProcessor<Boolean> = PublishProcessor.create()
    private var lastFocusItem: String? = null

    fun init() {
        disposable = CompositeDisposable()

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
                        }
                    },
                    { Timber.tag(TAG).e(it) }
                )
        )

        val fields = getFieldFlowable()

        disposable.add(
            dataEntryRepository.enrollmentSectionUids()
                .flatMap { sectionList ->
                    view.sectionFlowable().startWith(sectionList[0]).distinctUntilChanged()
                        .switchMap { section ->
                            fields.map { fieldList ->
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
                                    } else if (!field.programStageSection().equals(section)) {
                                        iterator.remove()
                                    }
                                }
                                finalList
                            }
                        }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({
                    view.showFields(it)
                    view.setSaveButtonVisible(true)
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
            EnrollmentActivity.EnrollmentMode.CHECK -> view.abstractActivity.finish()
        }
    }

    fun updateFields() {
        fieldsFlowable.onNext(true)
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

        optionsToHide.clear()
        optionsGroupsToHide.clear()
        optionsGroupToShow.clear()

        val fieldMap = fields.map { it.uid() to it }.toMap().toMutableMap()

        RulesUtilsProviderImpl()
            .applyRuleEffects(fieldMap, result, this)

        fieldMap.values.forEach {
            if (it is SpinnerViewModel) {
                it.setOptionsToHide(optionsToHide, optionsGroupsToHide)
                if (optionsGroupToShow.keys.contains(it.uid())) {
                    it.optionGroupsToShow = optionsGroupToShow[it.uid()]
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

    override fun setOptionToHide(optionUid: String) {
        optionsToHide.add(optionUid)
    }

    override fun setOptionGroupToHide(optionGroupUid: String, toHide: Boolean, field: String) {
        if (toHide) {
            optionsGroupsToHide.add(optionGroupUid)
        } else if (!optionsGroupsToHide.contains(optionGroupUid)) {
            // When combined with show option group the hide option group takes precedence.
            if (optionsGroupToShow[field] != null) {
                optionsGroupToShow[field]!!.add(optionGroupUid)
            } else {
                optionsGroupToShow[field] = ArrayList(optionsGroupsToHide)
            }
        }
    }

    private fun assignValue(uid: String, value: String?) {
        try {
            if (d2.dataElementModule().dataElements().uid(uid).blockingExists()) {
                // TODO: CHECK THIS: Enrollments rules should not assign values to dataElements
            } else if (
                d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingExists()
            ) {
                valueStore.save(uid, value).blockingFirst()
            }
        } catch (d2Error: D2Error) {
            Timber.e(d2Error.originalException())
        }
    }

    fun dataIntegrityCheck(emptyMandatoryFields: List<String>, errorFields: List<String>): Boolean {
        return when {
            emptyMandatoryFields.isNotEmpty() -> {
                view.showMissingMandatoryFieldsMessage(emptyMandatoryFields)
                false
            }
            errorFields.isNotEmpty() -> {
                view.showErrorFieldsMessage(errorFields)
                false
            }
            else -> true
        }
    }
}
