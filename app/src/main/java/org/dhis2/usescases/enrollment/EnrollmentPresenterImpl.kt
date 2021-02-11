package org.dhis2.usescases.enrollment

import android.annotation.SuppressLint
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.flowables.ConnectableFlowable
import io.reactivex.functions.BiFunction
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import kotlin.collections.set
import org.dhis2.Bindings.profilePicturePath
import org.dhis2.Bindings.toDate
import org.dhis2.R
import org.dhis2.data.forms.dataentry.EnrollmentRepository
import org.dhis2.data.forms.dataentry.StoreResult
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.ValueStoreImpl
import org.dhis2.data.forms.dataentry.fields.ActionType
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.RowAction
import org.dhis2.data.forms.dataentry.fields.display.DisplayViewModel
import org.dhis2.data.forms.dataentry.fields.optionset.OptionSetViewModel
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

private const val TAG = "EnrollmentPresenter"

class EnrollmentPresenterImpl(
    val view: EnrollmentView,
    val d2: D2,
    private val enrollmentObjectRepository: EnrollmentObjectRepository,
    private val dataEntryRepository: EnrollmentRepository,
    private val teiRepository: TrackedEntityInstanceObjectRepository,
    private val programRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program>,
    private val schedulerProvider: SchedulerProvider,
    val formRepository: EnrollmentFormRepository,
    private val valueStore: ValueStore,
    private val analyticsHelper: AnalyticsHelper,
    private val mandatoryWarning: String,
    private val onRowActionProcessor: FlowableProcessor<RowAction>,
    private val sectionProcessor: Flowable<String>
) : RulesActionCallbacks {

    private val disposable = CompositeDisposable()
    private val optionsToHide = HashMap<String, ArrayList<String>>()
    private val optionsGroupsToHide = HashMap<String, ArrayList<String>>()
    private val optionsGroupToShow = HashMap<String, ArrayList<String>>()
    private val fieldsFlowable: FlowableProcessor<Boolean> = PublishProcessor.create()
    private var selectedSection: String = ""
    private var errorFields = mutableMapOf<String, String>()
    private var mandatoryFields = mutableMapOf<String, String>()
    private var uniqueFields = mutableMapOf<String, String>()
    private val backButtonProcessor: FlowableProcessor<Boolean> = PublishProcessor.create()
    private var showErrors: Pair<Boolean, Boolean> = Pair(first = false, second = false)
    private var hasShownIncidentDateEditionWarning = false
    private var hasShownEnrollmentDateEditionWarning = false
    private var focusedItem: RowAction? = null
    private var itemList: List<FieldViewModel>? = null
    private val itemsWithError = mutableListOf<RowAction>()

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

        disposable.add(
            onRowActionProcessor
                .onBackpressureBuffer()
                .doOnNext { view.showProgress() }
                .observeOn(schedulerProvider.io())
                .flatMap { rowAction ->

                    when (rowAction.type) {
                        ActionType.ON_SAVE -> {
                            if (rowAction.id == focusedItem?.id &&
                                focusedItem?.type == ActionType.ON_FOCUS
                            ) {
                                focusedItem = null
                            }
                            if (rowAction.error != null) {
                                if (itemsWithError.find { it.id == rowAction.id } == null) {
                                    itemsWithError.add(rowAction)
                                }
                                Flowable.just(
                                    StoreResult(
                                        rowAction.id,
                                        ValueStoreImpl.ValueStoreResult.VALUE_HAS_NOT_CHANGED
                                    )
                                )
                            } else {
                                itemsWithError.find { it.id == rowAction.id }?.let {
                                    itemsWithError.remove(it)
                                }

                                when (rowAction.id) {
                                    EnrollmentRepository.ENROLLMENT_DATE_UID -> {
                                        enrollmentObjectRepository.setEnrollmentDate(
                                            rowAction.value?.toDate()
                                        )
                                        Flowable.just(
                                            StoreResult(
                                                EnrollmentRepository.ENROLLMENT_DATE_UID,
                                                ValueStoreImpl.ValueStoreResult.VALUE_CHANGED
                                            )
                                        )
                                    }
                                    EnrollmentRepository.INCIDENT_DATE_UID -> {
                                        enrollmentObjectRepository.setIncidentDate(
                                            rowAction.value?.toDate()
                                        )
                                        Flowable.just(
                                            StoreResult(
                                                EnrollmentRepository.INCIDENT_DATE_UID,
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
                                        val geometry = rowAction.extraData?.let {
                                            Geometry.builder()
                                                .coordinates(rowAction.value)
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
                                        val geometry = rowAction.extraData?.let {
                                            Geometry.builder()
                                                .coordinates(rowAction.value)
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
                                    else -> valueStore.save(rowAction.id, rowAction.value)
                                }
                            }
                        }
                        ActionType.ON_FOCUS, ActionType.ON_NEXT -> {
                            this.focusedItem = rowAction

                            Flowable.just(
                                StoreResult(
                                    rowAction.id,
                                    ValueStoreImpl.ValueStoreResult.VALUE_HAS_NOT_CHANGED
                                )
                            )
                        }

                        ActionType.ON_TEXT_CHANGE -> {
                            itemList?.let { list ->
                                list.find { item ->
                                    item.uid() == rowAction.id
                                }?.let { item ->
                                    itemList = list.updated(
                                        list.indexOf(item),
                                        item.withValue(rowAction.value).withEditMode(true)
                                    )
                                }
                            }

                            Flowable.just(StoreResult(rowAction.id))
                        }
                    }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { result ->
                        result.valueStoreResult?.let {
                            when (it) {
                                ValueStoreImpl.ValueStoreResult.VALUE_CHANGED -> {
                                    if (shouldShowDateEditionWarning(result.uid)) {
                                        view.showDateEditionWarning()
                                    }
                                    fieldsFlowable.onNext(true)
                                }
                                ValueStoreImpl.ValueStoreResult.VALUE_HAS_NOT_CHANGED -> {
                                    composeList()
                                    view.hideProgress()
                                }
                                ValueStoreImpl.ValueStoreResult.VALUE_NOT_UNIQUE -> {
                                    view.showInfoDialog(
                                        view.context.getString(R.string.error),
                                        view.context.getString(R.string.unique_warning)
                                    )
                                    view.hideProgress()
                                }
                                ValueStoreImpl.ValueStoreResult.UID_IS_NOT_DE_OR_ATTR -> {
                                    Timber.tag(TAG)
                                        .d("${result.uid} is not a data element or attribute")
                                    view.hideProgress()
                                }
                            }
                        } ?: view.hideProgress()
                    },
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
                    itemList = it
                    composeList()
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

    private fun getNextItem(currentItemUid: String): String? {
        return itemList?.let { list ->
            val oldItem = list.find { it.uid() == currentItemUid }
            val pos = list.indexOf(oldItem)
            if (pos < list.size - 1) {
                return list[pos + 1].getUid()
            }
            return null
        }
    }

    private fun composeList() = itemList?.let {
        val listWithErrors = mergeListWithErrorFields(it, itemsWithError)
        view.showFields(setFocusedItem(listWithErrors))
    }

    private fun mergeListWithErrorFields(
        list: List<FieldViewModel>,
        fieldsWithError: MutableList<RowAction>
    ): List<FieldViewModel> {
        return list.map { item ->
            fieldsWithError.find { it.id == item.uid() }?.let { action ->
                item.withValue(action.value).withError(action.error)
            } ?: item
        }
    }

    private fun setFocusedItem(list: List<FieldViewModel>) = focusedItem?.let {
        val uid = if (it.type == ActionType.ON_NEXT) {
            getNextItem(it.id)
        } else {
            it.id
        }

        list.find { item ->
            item.uid() == uid
        }?.let { item ->
            list.updated(list.indexOf(item), item.withFocus(true))
        } ?: list
    } ?: list

    fun <E> Iterable<E>.updated(index: Int, elem: E): List<E> =
        mapIndexed { i, existing -> if (i == index) elem else existing }

    fun setCurrentSection(sectionUid: String): String {
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

            if (field !is SectionViewModel && field !is DisplayViewModel) {
                val isUnique =
                    d2.trackedEntityModule().trackedEntityAttributes()
                        .uid(field.uid()).blockingGet()?.unique() ?: false
                var uniqueValueAlreadyExist: Boolean
                if (isUnique && field.value() != null) {
                    uniqueValueAlreadyExist =
                        d2.trackedEntityModule()
                        .trackedEntityAttributeValues()
                        .byTrackedEntityAttribute()
                        .eq(field.uid())
                        .byValue().eq(field.value())
                        .blockingGet().size > 1
                    if (uniqueValueAlreadyExist) {
                        uniqueFields[field.uid()] = field.label()
                    }
                }
                if (field.error()?.isNotEmpty() == true) {
                    errorFields[field.programStageSection() ?: section] = field.label()
                }
                if (field.mandatory() && field.value().isNullOrEmpty()) {
                    mandatoryFields[field.label()] = field.programStageSection() ?: section
                    if (showErrors.first) {
                        iterator.set(field.withWarning(mandatoryWarning))
                    }
                }
            }

            if (field !is SectionViewModel && !field.programStageSection().equals(section)) {
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

    private fun getFieldFlowable(): ConnectableFlowable<List<FieldViewModel>> {
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
            d2.categoryModule().categoryCombos().uid(catComboUid)
            .blockingGet().isDefault == false
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

        RulesUtilsProviderImpl(d2)
            .applyRuleEffects(fieldMap, result, this)

        fieldMap.values.forEachIndexed { index, fieldViewModel ->
            if (fieldViewModel is SpinnerViewModel) {
                fieldViewModel.setOptionsToHide(
                    optionsToHide[fieldViewModel.uid()] ?: emptyList(),
                    optionsGroupsToHide[fieldViewModel.uid()] ?: emptyList()
                )
                if (optionsGroupToShow.keys.contains(fieldViewModel.uid())) {
                    fieldViewModel.optionGroupsToShow = optionsGroupToShow[fieldViewModel.uid()]
                }
            }
            if (fieldViewModel is OptionSetViewModel) {
                fieldViewModel.optionsToHide = optionsToHide[fieldViewModel.uid()]
                if (optionsGroupToShow.keys.contains(fieldViewModel.uid())) {
                    fieldViewModel.optionsToShow = formRepository.getOptionsFromGroups(
                        optionsGroupToShow[fieldViewModel.uid()] ?: arrayListOf()
                    )
                }
            }
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
        val picturePath = formRepository.getProfilePicture()
        if (picturePath.isNotEmpty()) {
            view.displayTeiPicture(picturePath)
        }
    }
}
