package org.dhis2.usescases.teiDashboard

import com.google.gson.reflect.TypeToken
import dhis2.org.analytics.charts.Charts
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function
import org.dhis2.bindings.profilePicturePath
import org.dhis2.commons.data.tuples.Pair
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.ui.MetadataIconData
import org.dhis2.utils.DateUtils
import org.dhis2.utils.ValueUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper.getUidsList
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.relationship.RelationshipType
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import timber.log.Timber

class DashboardRepositoryImpl(
    private val d2: D2,
    private val charts: Charts,
    private val teiUid: String,
    private val programUid: String?,
    private val enrollmentUid: String?,
    private val teiAttributesProvider: TeiAttributesProvider,
    private val preferenceProvider: PreferenceProvider,
    private val metadataIconProvider: MetadataIconProvider,
) : DashboardRepository {
    override fun getTeiHeader(): String? {
        return d2.trackedEntityModule().trackedEntitySearch()
            .byProgram().eq(programUid)
            .uid(teiUid).blockingGet()?.header
    }

    override fun getTeiProfilePath(): String? {
        val tei = d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingGet()
        return tei?.profilePicturePath(d2, programUid)
    }

    override fun getProgramStages(programStages: String): Observable<List<ProgramStage>> {
        return d2.programModule().programStages().byProgramUid().eq(programUid).get().toObservable()
    }

    override fun getEnrollment(): Observable<Enrollment> {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get().map { it }
            .toObservable()
    }

    override fun getTEIEnrollmentEvents(
        programUid: String?,
        teiUid: String,
    ): Observable<List<Event>> {
        return d2.eventModule().events().byEnrollmentUid().eq(enrollmentUid)
            .byDeleted().isFalse
            .orderByTimeline(RepositoryScope.OrderByDirection.ASC)
            .get().toFlowable().flatMapIterable { events: List<Event>? -> events }
            .map { event: Event ->
                var event = event
                if (java.lang.Boolean.FALSE
                    == d2.programModule().programs().uid(programUid).blockingGet()!!
                        .ignoreOverdueEvents()
                ) if (event.status() == EventStatus.SCHEDULE &&
                    event.dueDate()!!
                        .before(DateUtils.getInstance().today)
                ) {
                    event = updateState(event, EventStatus.OVERDUE)
                }
                event
            }.toList()
            .toObservable()
    }

    override fun getEnrollmentEventsWithDisplay(
        programUid: String?,
        teiUid: String,
    ): Observable<List<Event>> {
        return d2.eventModule().events().byEnrollmentUid().eq(enrollmentUid).get()
            .toObservable()
            .map { events: List<Event> ->
                val finalEvents: MutableList<Event> =
                    ArrayList()
                for (event in events) {
                    if (d2.programModule().programStages().uid(event.programStage()).blockingGet()!!
                            .displayGenerateEventBox()!!
                    ) {
                        finalEvents.add(event)
                    }
                }
                finalEvents
            }
    }

    override fun getTEIAttributeValues(
        programUid: String?,
        teiUid: String,
    ): Observable<List<TrackedEntityAttributeValue>> {
        return if (programUid != null) {
            teiAttributesProvider.getValuesFromProgramTrackedEntityAttributesByProgram(
                programUid,
                teiUid,
            )
                .map<List<TrackedEntityAttributeValue>> { attributesValues: List<TrackedEntityAttributeValue> ->
                    val formattedValues: MutableList<TrackedEntityAttributeValue> =
                        java.util.ArrayList()
                    for (attributeValue in attributesValues) {
                        if (attributeValue.value() != null) {
                            val attribute =
                                d2.trackedEntityModule().trackedEntityAttributes()
                                    .uid(attributeValue.trackedEntityAttribute()).blockingGet()
                            if (attribute!!.valueType() != ValueType.IMAGE) {
                                formattedValues.add(
                                    ValueUtils.transform(
                                        d2,
                                        attributeValue,
                                        attribute!!.valueType(),
                                        if (attribute!!.optionSet() != null) {
                                            attribute!!.optionSet()!!
                                                .uid()
                                        } else {
                                            null
                                        },
                                    ),
                                )
                            }
                        } else {
                            formattedValues.add(
                                TrackedEntityAttributeValue.builder()
                                    .trackedEntityAttribute(attributeValue.trackedEntityAttribute())
                                    .trackedEntityInstance(teiUid)
                                    .value("")
                                    .build(),
                            )
                        }
                    }
                    formattedValues
                }.toObservable()
        } else {
            val teType =
                d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingGet()!!
                    .trackedEntityType()
            val attributeValues: MutableList<TrackedEntityAttributeValue> = java.util.ArrayList()
            for (attributeValue in teiAttributesProvider.getValuesFromTrackedEntityTypeAttributes(
                teType,
                teiUid,
            )) {
                val attribute = d2.trackedEntityModule().trackedEntityAttributes()
                    .uid(attributeValue.trackedEntityAttribute()).blockingGet()
                if (attribute!!.valueType() != ValueType.IMAGE && attributeValue.value() != null) {
                    attributeValues.add(
                        ValueUtils.transform(
                            d2,
                            attributeValue,
                            attribute.valueType(),
                            if (attribute.optionSet() != null) {
                                attribute.optionSet()!!
                                    .uid()
                            } else {
                                null
                            },
                        ),
                    )
                }
            }
            if (attributeValues.isEmpty()) {
                for (attributeValue in teiAttributesProvider.getValuesFromProgramTrackedEntityAttributes(
                    teType,
                    teiUid,
                )) {
                    val attribute = d2.trackedEntityModule().trackedEntityAttributes()
                        .uid(attributeValue.trackedEntityAttribute()).blockingGet()
                    attributeValues.add(
                        ValueUtils.transform(
                            d2,
                            attributeValue,
                            attribute!!.valueType(),
                            if (attribute.optionSet() != null) {
                                attribute.optionSet()!!
                                    .uid()
                            } else {
                                null
                            },
                        ),
                    )
                }
            }
            Observable.just(attributeValues)
        }
    }

    override fun setFollowUp(enrollmentUid: String?): Boolean {
        val followUp = (
            java.lang.Boolean.TRUE
                == d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()!!
                    .followUp()
            )
        return try {
            d2.enrollmentModule().enrollments().uid(enrollmentUid).setFollowUp(!followUp)
            !followUp
        } catch (d2Error: D2Error) {
            Timber.e(d2Error)
            followUp
        }
    }

    override fun updateState(event: Event?, newStatus: EventStatus): Event {
        try {
            d2.eventModule().events().uid(event?.uid()).setStatus(newStatus)
        } catch (d2Error: D2Error) {
            Timber.e(d2Error)
        }
        return d2.eventModule().events().uid(event?.uid()).blockingGet()!!
    }

    override fun completeEnrollment(enrollmentUid: String): Flowable<Enrollment> {
        return Flowable.fromCallable {
            d2.enrollmentModule().enrollments().uid(enrollmentUid)
                .setStatus(EnrollmentStatus.COMPLETED)
            d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()
        }
    }

    override fun displayGenerateEvent(eventUid: String?): Observable<ProgramStage> {
        return d2.eventModule().events().uid(eventUid).get()
            .map<String> { obj: Event -> obj.programStage() }
            .flatMap { stageUid: String ->
                d2.programModule().programStages().uid(stageUid).get()
            }.map { it }.toObservable()
    }

    override fun relationshipsForTeiType(teType: String): Observable<List<Pair<RelationshipType?, String>>> {
        return d2.systemInfoModule().systemInfo().get().toObservable()
            .map<String?>(Function { obj: SystemInfo -> obj.version() })
            .flatMap { version: String? ->
                if (version == "2.29") {
                    return@flatMap d2.relationshipModule().relationshipTypes()
                        .get().toObservable()
                        .flatMapIterable<RelationshipType?> { list: List<RelationshipType?>? -> list }
                        .map<Pair<RelationshipType?, String>> { relationshipType: RelationshipType? ->
                            Pair.create<RelationshipType?, String>(
                                relationshipType!!,
                                teType,
                            )
                        }.toList().toObservable()
                } else {
                    return@flatMap d2.relationshipModule()
                        .relationshipTypes().withConstraints().get()
                        .map<List<Pair<RelationshipType?, String>>> { relationshipTypes: List<RelationshipType> ->
                            val relTypeList: MutableList<Pair<RelationshipType?, String>> =
                                java.util.ArrayList()
                            for (relationshipType in relationshipTypes) {
                                if (relationshipType.fromConstraint() != null && relationshipType.fromConstraint()!!
                                        .trackedEntityType() != null && relationshipType.fromConstraint()!!
                                        .trackedEntityType()!!.uid() == teType
                                ) {
                                    if (relationshipType.toConstraint() != null && relationshipType.toConstraint()!!
                                            .trackedEntityType() != null
                                    ) {
                                        relTypeList.add(
                                            Pair.create(
                                                relationshipType,
                                                relationshipType.toConstraint()!!
                                                    .trackedEntityType()!!.uid(),
                                            ),
                                        )
                                    }
                                } else if (relationshipType.bidirectional()!! && relationshipType.toConstraint() != null && relationshipType.toConstraint()!!
                                        .trackedEntityType() != null && relationshipType.toConstraint()!!
                                        .trackedEntityType()!!
                                        .uid() == teType
                                ) {
                                    if (relationshipType.fromConstraint() != null && relationshipType.fromConstraint()!!
                                            .trackedEntityType() != null
                                    ) {
                                        relTypeList.add(
                                            Pair.create(
                                                relationshipType,
                                                relationshipType.fromConstraint()!!
                                                    .trackedEntityType()!!.uid(),
                                            ),
                                        )
                                    }
                                }
                            }
                            relTypeList.toList()
                        }.toObservable()
                }
            }
    }

    override fun catOptionCombo(catComboUid: String?): CategoryOptionCombo {
        return d2.categoryModule().categoryOptionCombos().uid(catComboUid).blockingGet()!!
    }

    override fun getTrackedEntityInstance(teiUid: String): Observable<TrackedEntityInstance> {
        return Observable.fromCallable {
            d2.trackedEntityModule().trackedEntityInstances().byUid().eq(teiUid).one()
                .blockingGet()
        }
    }

    override fun getProgramTrackedEntityAttributes(programUid: String?): Observable<List<ProgramTrackedEntityAttribute>> {
        return if (programUid != null) {
            d2.programModule().programTrackedEntityAttributes().byProgram().eq(programUid)
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC).get().toObservable()
        } else {
            Observable.fromCallable {
                d2.trackedEntityModule().trackedEntityAttributes()
                    .byDisplayInListNoProgram().eq(true).blockingGet()
            }.map { trackedEntityAttributes: List<TrackedEntityAttribute> ->
                val programs =
                    d2.programModule().programs().blockingGet()
                val teaUids = getUidsList(trackedEntityAttributes)
                val programTrackedEntityAttributes: MutableList<ProgramTrackedEntityAttribute> =
                    java.util.ArrayList()
                for (program in programs) {
                    val attributeList =
                        d2.programModule()
                            .programTrackedEntityAttributes().byProgram().eq(program.uid())
                            .orderBySortOrder(RepositoryScope.OrderByDirection.ASC).blockingGet()
                    for (pteattr in attributeList) {
                        if (teaUids.contains(pteattr.uid())) {
                            programTrackedEntityAttributes.add(
                                pteattr,
                            )
                        }
                    }
                }
                programTrackedEntityAttributes
            }
        }
    }

    override fun getTeiOrgUnits(
        teiUid: String,
        programUid: String?,
    ): Observable<List<OrganisationUnit>> {
        var enrollmentRepo = d2.enrollmentModule().enrollments().byTrackedEntityInstance()
            .eq(teiUid)
        if (programUid != null) {
            enrollmentRepo = enrollmentRepo.byProgram().eq(programUid)
        }

        return enrollmentRepo.get().toObservable().map { enrollments: List<Enrollment> ->
            val orgUnitIds: MutableList<String> =
                java.util.ArrayList()
            for (enrollment in enrollments) {
                enrollment.organisationUnit()?.let { orgUnitIds.add(it) }
            }
            d2.organisationUnitModule().organisationUnits().byUid().`in`(orgUnitIds.toList())
                .blockingGet()
        }
    }

    override fun getDashboardModel(): DashboardModel {
        return if (programUid == null) {
            DashboardTEIModel(
                getTEIEnrollments(teiUid).blockingFirst(),
                getTrackedEntityInstance(teiUid).blockingFirst(),
                getTEIAttributeValues(null, teiUid).blockingFirst(),
                getTeiActivePrograms(teiUid, true).blockingFirst(),
                getTeiOrgUnits(teiUid, null).blockingFirst(),
                getTeiHeader(),
                getTeiProfilePath(),
            )
        } else {
            DashboardEnrollmentModel(
                getEnrollment().blockingFirst(),
                getProgramStages(programUid).blockingFirst(),
                getTEIEnrollmentEvents(programUid, teiUid).blockingFirst(),
                getTrackedEntityInstance(teiUid).blockingFirst(),
                getAttributesMap(programUid, teiUid).blockingFirst(),
                getTEIAttributeValues(programUid, teiUid).blockingFirst(),
                getTeiActivePrograms(teiUid, false).blockingFirst(),
                getTeiOrgUnits(teiUid, programUid).blockingFirst(),
                getTeiHeader(),
                getTeiProfilePath(),
            )
        }
    }

    override fun getTeiActivePrograms(
        teiUid: String,
        showOnlyActive: Boolean,
    ): Observable<List<kotlin.Pair<Program, MetadataIconData>>> {
        val enrollmentRepo = d2.enrollmentModule().enrollments().byTrackedEntityInstance()
            .eq(teiUid).byDeleted().eq(false)
        if (showOnlyActive) enrollmentRepo.byStatus().eq(EnrollmentStatus.ACTIVE)

        return enrollmentRepo.get().toObservable()
            .flatMapIterable { enrollments: List<Enrollment>? -> enrollments }
            .map { obj: Enrollment -> obj.program() }
            .toList().toObservable()
            .map { programUids ->
                d2.programModule().programs().byUid()
                    .`in`(programUids.filterNotNull()).blockingGet().map {
                        Pair(it, metadataIconProvider(it.style()))
                    }
            }
    }

    override fun getTEIEnrollments(teiUid: String): Observable<List<Enrollment>> {
        return d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(teiUid).byDeleted()
            .eq(false).get().toObservable()
    }

    override fun saveCatOption(eventUid: String?, catOptionComboUid: String?) {
        try {
            d2.eventModule().events().uid(eventUid).setAttributeOptionComboUid(catOptionComboUid)
        } catch (d2Error: D2Error) {
            Timber.e(d2Error)
        }
    }

    override fun checkIfDeleteTeiIsPossible(): Boolean {
        val local = d2.trackedEntityModule()
            .trackedEntityInstances()
            .uid(teiUid)
            .blockingGet()
            ?.state() == State.TO_POST
        val hasAuthority = d2.userModule()
            .authorities()
            .byName().eq("F_TEI_CASCADE_DELETE")
            .one().blockingExists()

        return local || hasAuthority
    }

    override fun deleteTei(): Single<Boolean> {
        return d2.trackedEntityModule()
            .trackedEntityInstances()
            .uid(teiUid)
            .delete()
            .andThen(Single.fromCallable { true })
    }

    override fun checkIfDeleteEnrollmentIsPossible(enrollmentUid: String): Boolean {
        val local = d2.enrollmentModule()
            .enrollments()
            .uid(enrollmentUid)
            .blockingGet()!!.state() == State.TO_POST
        val hasAuthority = d2.userModule()
            .authorities()
            .byName().eq("F_ENROLLMENT_CASCADE_DELETE")
            .one().blockingExists()

        return local || hasAuthority
    }

    override fun deleteEnrollment(enrollmentUid: String): Single<Boolean> {
        return Single.fromCallable {
            val enrollmentObjectRepository =
                d2.enrollmentModule()
                    .enrollments().uid(enrollmentUid)
            enrollmentObjectRepository.setStatus(
                enrollmentObjectRepository.blockingGet()!!.status()!!,
            )
            enrollmentObjectRepository.blockingDelete()
            !d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(teiUid)
                .byDeleted().isFalse
                .byStatus().eq(EnrollmentStatus.ACTIVE).blockingGet().isEmpty()
        }
    }

    override fun getNoteCount(): Single<Int> {
        return d2.enrollmentModule().enrollments()
            .withNotes()
            .uid(enrollmentUid)
            .get()
            .map(Function { enrollment: Enrollment -> if (enrollment.notes() != null) enrollment.notes()!!.size else 0 })
    }

    override fun getEnrollmentStatus(enrollmentUid: String?): EnrollmentStatus? {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()!!.status()
    }

    override fun updateEnrollmentStatus(
        enrollmentUid: String,
        status: EnrollmentStatus,
    ): Observable<StatusChangeResultCode> {
        return try {
            if (d2.programModule().programs().uid(programUid).blockingGet()!!.access().data()
                    .write()
            ) {
                if (reopenCheck(status)) {
                    d2.enrollmentModule().enrollments().uid(enrollmentUid).setStatus(status)
                    Observable.just(StatusChangeResultCode.CHANGED)
                } else {
                    Observable.just(StatusChangeResultCode.ACTIVE_EXIST)
                }
            } else {
                Observable.just(StatusChangeResultCode.WRITE_PERMISSION_FAIL)
            }
        } catch (error: D2Error) {
            Observable.just(StatusChangeResultCode.FAILED)
        }
    }

    private fun reopenCheck(status: EnrollmentStatus): Boolean {
        return status != EnrollmentStatus.ACTIVE || d2.enrollmentModule().enrollments()
            .byProgram().eq(programUid)
            .byTrackedEntityInstance().eq(teiUid)
            .byStatus().eq(EnrollmentStatus.ACTIVE)
            .blockingIsEmpty()
    }

    override fun programHasRelationships(): Boolean {
        return if (programUid != null) {
            val teiTypeUid = d2.programModule().programs()
                .uid(programUid)
                .blockingGet()
                ?.trackedEntityType()
                ?.uid()
            teiTypeUid?.let { relationshipsForTeiType(it) }!!.blockingFirst().isNotEmpty()
        } else {
            false
        }
    }

    override fun programHasAnalytics(): Boolean {
        return if (programUid != null) {
            val enrollmentScopeRulesUids = d2.programModule().programRules()
                .byProgramUid().eq(programUid)
                .byProgramStageUid().isNull
                .blockingGetUids()
            val hasDisplayRuleActions = !d2.programModule().programRuleActions()
                .byProgramRuleUid().`in`(enrollmentScopeRulesUids)
                .byProgramRuleActionType()
                .`in`(ProgramRuleActionType.DISPLAYKEYVALUEPAIR, ProgramRuleActionType.DISPLAYTEXT)
                .blockingIsEmpty()
            val hasProgramIndicator =
                !d2.programModule().programIndicators().byProgramUid().eq(programUid)
                    .blockingIsEmpty()
            val hasCharts =
                enrollmentUid?.let { charts.geEnrollmentCharts(enrollmentUid).isNotEmpty() }
                    ?: false
            hasDisplayRuleActions || hasProgramIndicator || hasCharts
        } else {
            false
        }
    }

    override fun getGrouping(): Boolean {
        return getGroupingOptions().getOrDefault(programUid, true)
    }

    override fun setGrouping(groupEvent: Boolean) {
        val groups = getGroupingOptions()
        programUid?.let { groups[programUid] = groupEvent }
        preferenceProvider.saveAsJson<Map<String, Boolean>>(
            Preference.GROUPING,
            groups,
        )
    }

    private fun getGroupingOptions(): HashMap<String, Boolean> {
        val typeToken: TypeToken<HashMap<String, Boolean>> =
            object : TypeToken<HashMap<String, Boolean>>() {}
        val grouping = preferenceProvider.getObjectFromJson(
            Preference.GROUPING,
            typeToken,
            HashMap(),
        )
        return grouping
    }

    override fun getTETypeName(): String? {
        return getTrackedEntityInstance(teiUid).flatMap { tei: TrackedEntityInstance ->
            d2.trackedEntityModule().trackedEntityTypes()
                .uid(tei.trackedEntityType())
                .get()
                .toObservable()
        }.blockingFirst()?.displayName()
    }

    override fun getAttributesMap(
        programUid: String,
        teiUid: String,
    ): Observable<List<kotlin.Pair<TrackedEntityAttribute, TrackedEntityAttributeValue>>> {
        return teiAttributesProvider.getProgramTrackedEntityAttributesByProgram(programUid, teiUid)
            .toObservable()
            .flatMapIterable { list: List<kotlin.Pair<TrackedEntityAttribute?, TrackedEntityAttributeValue?>>? -> list }
            .map { (attribute, attributeValue): kotlin.Pair<TrackedEntityAttribute?, TrackedEntityAttributeValue?> ->
                val formattedAttributeValue: TrackedEntityAttributeValue =
                    if (attributeValue != null && attribute!!.valueType() != ValueType.IMAGE) {
                        ValueUtils.transform(
                            d2,
                            attributeValue,
                            attribute.valueType(),
                            if (attribute.optionSet() != null) {
                                attribute.optionSet()!!
                                    .uid()
                            } else {
                                null
                            },
                        )
                    } else {
                        TrackedEntityAttributeValue.builder()
                            .trackedEntityAttribute(attribute!!.uid())
                            .trackedEntityInstance(teiUid)
                            .value("")
                            .build()
                    }
                Pair(
                    attribute,
                    formattedAttributeValue,
                )
            }.toList().toObservable()
    }
}
