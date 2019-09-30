package org.dhis2.usescases.enrollment

import android.text.TextUtils.isEmpty
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import org.dhis2.R
import org.dhis2.data.forms.dataentry.DataEntryRepository
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel
import org.dhis2.utils.CodeGeneratorImpl
import org.dhis2.utils.Result
import org.dhis2.utils.RulesActionCallbacks
import org.dhis2.utils.RulesUtilsProviderImpl
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceObjectRepository
import org.hisp.dhis.rules.models.RuleActionShowError
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class EnrollmentPresenterImpl(
        val d2: D2,
        private val enrollmentObjectRepository: EnrollmentObjectRepository,
        private val dataEntryRepository: DataEntryRepository,
        private val teiRepository: TrackedEntityInstanceObjectRepository,
        private val programRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program>,
        val formRepository: EnrollmentFormRepository) : EnrollmentContract.Presenter, RulesActionCallbacks {

    private val TAG = "EnrollmentPresenter"
    private lateinit var disposable: CompositeDisposable
    private lateinit var view: EnrollmentContract.View
    private val optionsToHide = ArrayList<String>()
    private val optionsGroupsToHide = ArrayList<String>()
    private val optionsGroupToShow = HashMap<String, ArrayList<String>>()
    private val fieldsFlowable: FlowableProcessor<Boolean> = PublishProcessor.create()
    private var lastFocusItem: String? = null

    override fun init(view: EnrollmentContract.View) {
        this.view = view
        disposable = CompositeDisposable()

        disposable.add(
                teiRepository.get()
                        .flatMap { tei ->
                            d2.trackedEntityModule().trackedEntityTypeAttributes
                                    .byTrackedEntityTypeUid().eq(tei.trackedEntityType()).get()
                                    .map { list ->
                                        list.sortBy { it.sortOrder() }
                                        list.map {
                                            it.trackedEntityAttribute().uid()
                                        }
                                    }
                                    .flatMap {
                                        d2.trackedEntityModule().trackedEntityAttributeValues
                                                .byTrackedEntityInstance().eq(tei.uid())
                                                .byTrackedEntityAttribute().`in`(it)
                                                .get()
                                    }
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { view.displayTeiInfo(it) },
                                { Timber.tag(TAG).e(it) }
                        )
        )

        disposable.add(
                programRepository.get()
                        .map { it.access()?.data()?.write() }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { view.setAccess(it) },
                                { Timber.tag(TAG).e(it) })
        )

        disposable.add(
                enrollmentObjectRepository.get()
                        .map { it.status() }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { view.renderStatus(it!!) },
                                { Timber.tag(TAG).e(it) }
                        )
        )

        disposable.add(
                enrollmentObjectRepository.get()
                        .flatMap { enrollment -> d2.organisationUnitModule().organisationUnits.uid(enrollment.organisationUnit()).get() }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            view.displayOrgUnit(it)
                        }, {
                            Timber.tag(TAG).e(it)
                        }))

        disposable.add(
                programRepository.get()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { view.setDateLabels(it.enrollmentDateLabel(), it.incidentDateLabel()) },
                                { Timber.tag(TAG).e(it) }
                        )
        )

        disposable.add(
                enrollmentObjectRepository.get()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
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
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { view.setUpIncidentDate(it) },
                                { Timber.tag(TAG).e(it) }
                        )
        )

        disposable.add(
                programRepository.get()
                        .flatMap { program ->
                            d2.programModule().programStages
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
                                        it.generatedByEnrollmentDate() == true)
                                    blockEnrollmentDate = true
                                else
                                    blockIncidentDate = true
                            }
                            Pair(blockEnrollmentDate, blockIncidentDate)
                        }.map {
                            if (getProgram().access()?.data()!!.write() == true)
                                it
                            else
                                Pair(first = true, second = true)
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { view.blockDates(it.first, it.second) },
                                { Timber.tag(TAG).e(it) }
                        )
        )


        disposable.add(
                Single.zip(
                        programRepository.get(),
                        enrollmentObjectRepository.get(),
                        BiFunction<Program, Enrollment, Pair<Program, Enrollment>> { program, enrollment -> Pair(program, enrollment) }
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { view.displayEnrollmentCoordinates(it) },
                                { Timber.tag(TAG).e(it) })
        )

        disposable.add(
                teiRepository.get()
                        .flatMap { tei ->
                            d2.trackedEntityModule().trackedEntityTypes.uid(tei.trackedEntityType()).get()
                                    .map { Pair(it, tei) }
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { view.displayTeiCoordinates(it) },
                                { Timber.tag(TAG).e(it) })
        )

        disposable.add(
                view.rowActions().onBackpressureBuffer()
                        .map {
                            if (checkUniqueFilter(it.id(), it.value())) {
                                val saved = saveValue(it.id(), it.value())
                                if (saved)
                                    lastFocusItem = it.id()
                                Pair(saved, false)
                            } else {
                                Pair(first = false, second = true)
                            }
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    if (it.first)
                                        fieldsFlowable.onNext(true)
                                    else if (it.second)
                                        view.showInfoDialog(
                                                view.context.getString(R.string.error),
                                                view.context.getString(R.string.unique_warning)
                                        )

                                },
                                { Timber.tag(TAG).e(it) }
                        )
        )

        disposable.add(
                fieldsFlowable.startWith(true)
                        .switchMap {
                            Flowable.zip<List<FieldViewModel>, Result<RuleEffect>, List<FieldViewModel>>(
                                    dataEntryRepository.list(),
                                    formRepository.calculate(),
                                    BiFunction { fields, result -> applyRuleEffects(fields, result) }
                            )
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            view.showFields(it)
                        }) {
                            Timber.tag(TAG).e(it)
                        }
        )
    }

    override fun finish(enrollmentMode: EnrollmentActivity.EnrollmentMode) {
        when (enrollmentMode) {
            EnrollmentActivity.EnrollmentMode.NEW -> disposable.add(
                    formRepository.autoGenerateEvents()
                            .flatMap { formRepository.useFirstStageDuringRegistration() }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    {
                                        if (!isEmpty(it.second))
                                            view.openEvent(it.second)
                                        else
                                            view.openDashboard(it.first)
                                    },
                                    { Timber.tag(TAG).e(it) }
                            )
            )
            EnrollmentActivity.EnrollmentMode.CHECK -> view.abstractActivity.finish()
        }
    }

    override fun openInitial(eventUid: String): Boolean {
        val event = d2.eventModule().events.uid(eventUid).blockingGet()
        val stage = d2.programModule().programStages.uid(event.programStage()).blockingGet()
        val needsCatCombo = programRepository.blockingGet().categoryCombo() != null &&
                programRepository.blockingGet().categoryCombo()!!.isDefault == false
        val needsCoordinates = stage.featureType() != null && stage.featureType() != FeatureType.NONE

        return needsCatCombo || needsCoordinates
    }

    private fun applyRuleEffects(fields: List<FieldViewModel>, result: Result<RuleEffect>): List<FieldViewModel> {
        if (result.error() != null) {
            Timber.tag(TAG).e(result.error())
            return fields
        }

        optionsToHide.clear()
        optionsGroupsToHide.clear()
        optionsGroupToShow.clear()

        val fieldMap = fields.map { it.uid() to it }.toMap().toMutableMap()

        RulesUtilsProviderImpl(CodeGeneratorImpl())
                .applyRuleEffects(fieldMap, result, this)

        fieldMap.values.forEach {
            if (it is SpinnerViewModel) {
                it.setOptionsToHide(optionsToHide, optionsGroupsToHide)
                if (optionsGroupToShow.keys.contains(it.uid()))
                    it.optionGroupsToShow = optionsGroupToShow[it.uid()]
            }
        }
        return ArrayList(fieldMap.values)
    }

    override fun getEnrollment(): Enrollment {
        return enrollmentObjectRepository.blockingGet()
    }

    override fun getProgram(): Program {
        return programRepository.blockingGet()
    }

    override fun getOrgUnit(): OrganisationUnit {
        return d2.organisationUnitModule().organisationUnits
                .uid(getEnrollment().organisationUnit()).blockingGet()
    }

    override fun updateEnrollmentStatus(newStatus: EnrollmentStatus): Boolean {
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

    override fun updateEnrollmentDate(date: Date?) {
        enrollmentObjectRepository.setEnrollmentDate(date)
        view.setUpEnrollmentDate(date)
    }

    override fun updateIncidentDate(date: Date?) {
        enrollmentObjectRepository.setIncidentDate(date)
        view.setUpIncidentDate(date)
    }

    override fun saveEnrollmentGeometry(geometry: Geometry?) {
        enrollmentObjectRepository.setGeometry(geometry)
    }

    override fun saveTeiGeometry(geometry: Geometry?) {
        teiRepository.setGeometry(geometry)
    }

    override fun deleteAllSavedData() {
        teiRepository.blockingDelete()
    }

    override fun getLastFocusItem(): String? {
        return lastFocusItem
    }

    override fun clearLastFocusItem() {
        lastFocusItem = null
    }

    private fun checkUniqueFilter(uid: String, value: String?): Boolean {
        return if (value != null && valueIsAttribute(uid)) {
            val isUnique = d2.trackedEntityModule().trackedEntityAttributes.uid(uid).blockingGet()!!.unique()
                    ?: false
            val hasValue = !d2.trackedEntityModule().trackedEntityAttributeValues
                    .byTrackedEntityAttribute().eq(uid)
                    .byValue().eq(value).blockingGet().isEmpty()
            if (isUnique) {
                !hasValue
            } else
                true
        } else
            true
    }


    override fun saveValue(uid: String, value: String?): Boolean {
        return if (valueIsAttribute(uid))
            saveAttribute(uid, value)
        else
            saveDataElement(uid, value)
    }

    private fun saveAttribute(uid: String, value: String?): Boolean {
        val valueRepository = d2.trackedEntityModule().trackedEntityAttributeValues
                .value(uid, teiRepository.blockingGet().uid())
        var newValue = value
        if (d2.trackedEntityModule().trackedEntityAttributes.uid(uid).blockingGet().valueType() == ValueType.IMAGE
                && value != null) {
            newValue = getFileResource(value)
        }

        val currentValue = if (valueRepository.blockingExists())
            valueRepository.blockingGet().value() else null
        return if (currentValue != newValue) {
            if (!isEmpty(value))
                valueRepository.blockingSet(newValue)
            else
                valueRepository.blockingDelete()
            true
        } else
            false
    }

    private fun saveDataElement(uid: String, value: String?): Boolean {
        val eventUid = getEventUid(uid)
        var newValue = value
        return if (eventUid != null) {
            val valueRepository = d2.trackedEntityModule().trackedEntityDataValues
                    .value(eventUid, uid)

            if (d2.dataElementModule().dataElements.uid(uid).blockingGet().valueType() == ValueType.IMAGE
                    && value != null) {
                newValue = getFileResource(value)
            }

            val currentValue = if (valueRepository.blockingExists())
                valueRepository.blockingGet().value() else null
            if (currentValue != newValue) {
                if (!isEmpty(value))
                    valueRepository.blockingSet(newValue)
                else
                    valueRepository.blockingDelete()
                true
            } else
                false
        } else
            false
    }

    private fun getFileResource(path: String): String {
        val file = File(path)
        return d2.fileResourceModule().fileResources.blockingAdd(file)
    }

    private fun getEventUid(dataElement: String): String? {
        val events = d2.eventModule().events.byEnrollmentUid().eq(getEnrollment().uid())
                .byStatus().eq(EventStatus.ACTIVE)
                .orderByEventDate(RepositoryScope.OrderByDirection.DESC).blockingGet().map { it.uid() }
        val dataValues = d2.trackedEntityModule().trackedEntityDataValues
                .byDataElement().eq(dataElement)
                .byEvent().`in`(events)
                .blockingGet()

        return if (dataValues != null && !dataValues.isEmpty())
            dataValues[0].event()!!
        else
            null
    }

    private fun valueIsAttribute(uid: String): Boolean {
        return d2.trackedEntityModule().trackedEntityAttributes.uid(uid).blockingExists()
    }

    override fun onDettach() {
        disposable.clear()
    }

    override fun displayMessage(message: String?) {
        view.displayMessage(message)
    }

    override fun setCalculatedValue(calculatedValueVariable: String, value: String) {
        //not used
    }

    override fun setShowError(showError: RuleActionShowError, model: FieldViewModel?) {
        //not used
    }

    override fun unsupportedRuleAction() {
        //not used
    }

    override fun save(uid: String, value: String?) {
        saveValue(uid, value)
    }

    override fun setDisplayKeyValue(label: String, value: String) = Unit

    override fun setHideSection(sectionUid: String) = Unit

    override fun setMessageOnComplete(content: String, canComplete: Boolean) = Unit

    override fun setHideProgramStage(programStageUid: String) = Unit

    override fun setOptionToHide(optionUid: String) {
        optionsToHide.add(optionUid)
    }

    override fun setOptionGroupToHide(optionGroupUid: String, toHide: Boolean, field: String) {
        if (toHide)
            optionsGroupsToHide.add(optionGroupUid)
        else if (!optionsGroupsToHide.contains(optionGroupUid))
        //When combined with show option group the hide option group takes precedence.
            if (optionsGroupToShow[field] != null)
                optionsGroupToShow[field]!!.add(optionGroupUid)
            else
                optionsGroupToShow[field] = ArrayList(optionsGroupsToHide)
    }
}