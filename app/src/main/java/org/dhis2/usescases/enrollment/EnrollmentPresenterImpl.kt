package org.dhis2.usescases.enrollment

import android.annotation.SuppressLint
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import java.util.Date
import kotlin.collections.set
import org.dhis2.R
import org.dhis2.data.forms.dataentry.DataEntryRepository
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.ValueStoreImpl
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
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
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceObjectRepository
import org.hisp.dhis.rules.models.RuleActionShowError
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber
import java.util.concurrent.TimeUnit

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

    private val TAG = "EnrollmentPresenter"
    private lateinit var disposable: CompositeDisposable
    private val optionsToHide = ArrayList<String>()
    private val optionsGroupsToHide = ArrayList<String>()
    private val optionsGroupToShow = HashMap<String, ArrayList<String>>()
    private val fieldsFlowable: FlowableProcessor<Boolean> = PublishProcessor.create()
    private var lastFocusItem: String? = null
    private val backButtonProcessor: FlowableProcessor<Boolean> = PublishProcessor.create()

    fun init() {
        disposable = CompositeDisposable()

        view.hideSaveButton()
        view.showAdjustingForm()

        disposable.add(
            teiRepository.get()
                .flatMap { tei ->
                    d2.trackedEntityModule().trackedEntityTypeAttributes()
                        .byTrackedEntityTypeUid().eq(tei.trackedEntityType()).get()
                        .map { list ->
                            list.sortBy { it.sortOrder() }
                            list.map {
                                it.trackedEntityAttribute()?.uid()
                            }
                        }
                        .flatMap {
                            d2.trackedEntityModule().trackedEntityAttributeValues()
                                .byTrackedEntityInstance().eq(tei.uid())
                                .byTrackedEntityAttribute().`in`(it)
                                .get()
                        }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.displayTeiInfo(it) },
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
            enrollmentObjectRepository.get()
                .flatMap { enrollment ->
                    d2.organisationUnitModule().organisationUnits().uid(
                        enrollment.organisationUnit()
                    ).get()
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        view.displayOrgUnit(it)
                    },
                    {
                        Timber.tag(TAG).e(it)
                    }
                )
        )

        disposable.add(
            programRepository.get()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.setDateLabels(it.enrollmentDateLabel(), it.incidentDateLabel()) },
                    { Timber.tag(TAG).e(it) }
                )
        )

        disposable.add(
            enrollmentObjectRepository.get()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.setUpEnrollmentDate(it.enrollmentDate()) },
                    { Timber.tag(TAG).e(it) }
                )
        )

        disposable.add(
            enrollmentObjectRepository.get()
                .flatMap { enrollment ->
                    programRepository.get()
                        .filter { it.displayIncidentDate() ?: false }
                        .map {
                            enrollment.incidentDate()
                        }.toSingle()
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.setUpIncidentDate(it) },
                    { Timber.tag(TAG).e(it) }
                )
        )

        disposable.add(
            programRepository.get()
                .flatMap { program ->
                    d2.programModule().programStages()
                        .byProgramUid().eq(program.uid())
                        .byAutoGenerateEvent().isTrue
                        .get()
                }
                .map { stages ->
                    var blockEnrollmentDate = false
                    var blockIncidentDate = false
                    stages.forEach {
                        if (it.reportDateToUse() != null &&
                            it.reportDateToUse().equals("enrollmentDate") ||
                            it.generatedByEnrollmentDate() == true
                        ) {
                            blockEnrollmentDate = true
                        } else {
                            blockIncidentDate = true
                        }
                    }
                    Pair(blockEnrollmentDate, blockIncidentDate)
                }.map {
                    if (getProgram().access()?.data()!!.write() == true) {
                        it
                    } else {
                        Pair(first = true, second = true)
                    }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.blockDates(it.first, it.second) },
                    { Timber.tag(TAG).e(it) }
                )
        )

        disposable.add(
            Single.zip(
                programRepository.get(),
                enrollmentObjectRepository.get(),
                BiFunction<Program, Enrollment, Pair<Program, Enrollment>> { program, enrollment ->
                    Pair(program, enrollment)
                }
            )
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.displayEnrollmentCoordinates(it) },
                    { Timber.tag(TAG).e(it) }
                )
        )

        disposable.add(
            teiRepository.get()
                .flatMap { tei ->
                    d2.trackedEntityModule().trackedEntityTypes().uid(tei.trackedEntityType()).get()
                        .map { Pair(it, tei) }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.displayTeiCoordinates(it) },
                    { Timber.tag(TAG).e(it) }
                )
        )

        disposable.add(
            view.rowActions().onBackpressureBuffer()
                .flatMap { rowAction ->
                    valueStore.save(rowAction.id(), rowAction.value())
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

        disposable.add(
            fieldsFlowable.startWith(true)
                .observeOn(schedulerProvider.io())
                .switchMap {
                    Flowable.zip<List<FieldViewModel>, Result<RuleEffect>, List<FieldViewModel>>(
                        dataEntryRepository.list(),
                        formRepository.calculate(),
                        BiFunction { fields, result -> applyRuleEffects(fields, result) }
                    )
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({
                    view.showFields(it)
                    view.showSaveButton()
                    view.hideAdjustingForm()
                }) {
                    Timber.tag(TAG).e(it)
                    view.hideAdjustingForm()
                }
        )
    }

    fun subscribeToBackButton(){
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
            EnrollmentActivity.EnrollmentMode.CHECK -> view.abstractActivity.finish()
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

    fun getOrgUnit(): OrganisationUnit {
        return d2.organisationUnitModule().organisationUnits()
            .uid(getEnrollment().organisationUnit()).blockingGet()
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

    fun updateEnrollmentDate(date: Date?) {
        enrollmentObjectRepository.setEnrollmentDate(date)
        view.setUpEnrollmentDate(date)
    }

    fun updateIncidentDate(date: Date?) {
        enrollmentObjectRepository.setIncidentDate(date)
        view.setUpIncidentDate(date)
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

    fun dataIntegrityCheck(mandatoryOk: Boolean, hasError: Boolean): Boolean {
        return if (!mandatoryOk) {
            view.showMissingMandatoryFieldsMessage()
            false
        } else if (hasError) {
            view.showErrorFieldsMessage()
            false
        } else {
            true
        }
    }
}
