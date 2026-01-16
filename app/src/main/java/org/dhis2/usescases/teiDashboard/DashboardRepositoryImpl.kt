package org.dhis2.usescases.teiDashboard

import com.google.gson.reflect.TypeToken
import dhis2.org.analytics.charts.Charts
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function
import org.dhis2.bindings.profilePicturePath
import org.dhis2.commons.data.ProgramConfigurationRepository
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.mobile.commons.model.MetadataIconData
import org.dhis2.utils.ValueUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper.getUidsList
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentAccess
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
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
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
    private val programConfigurationRepository: ProgramConfigurationRepository,
    private val featureConfigRepository: FeatureConfigRepository,
) : DashboardRepository {
    override fun getTeiHeader(): String? =
        d2
            .trackedEntityModule()
            .trackedEntitySearch()
            .byProgram()
            .eq(programUid)
            .uid(teiUid)
            .blockingGet()
            ?.header

    override fun getTeiProfilePath(): String? {
        val tei =
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .uid(teiUid)
                .blockingGet()
        return tei?.profilePicturePath(d2, programUid)
    }

    override fun getProgramStages(programStages: String): Observable<List<ProgramStage>> =
        d2
            .programModule()
            .programStages()
            .byProgramUid()
            .eq(programUid)
            .get()
            .toObservable()

    override fun getEnrollment(): Observable<Enrollment> =
        d2
            .enrollmentModule()
            .enrollments()
            .uid(enrollmentUid)
            .get()
            .map { it }
            .toObservable()

    override fun getEnrollmentEventsWithDisplay(
        programUid: String?,
        teiUid: String,
    ): Observable<List<Event>> =
        d2
            .eventModule()
            .events()
            .byEnrollmentUid()
            .eq(enrollmentUid)
            .get()
            .toObservable()
            .map { events: List<Event> ->
                val finalEvents: MutableList<Event> =
                    ArrayList()
                for (event in events) {
                    if (d2
                            .programModule()
                            .programStages()
                            .uid(event.programStage())
                            .blockingGet()!!
                            .displayGenerateEventBox()!!
                    ) {
                        finalEvents.add(event)
                    }
                }
                finalEvents
            }

    override fun getTEIAttributeValues(
        programUid: String?,
        teiUid: String,
    ): Observable<List<TrackedEntityAttributeValue>> =
        if (programUid != null) {
            teiAttributesProvider
                .getValuesFromProgramTrackedEntityAttributesByProgram(
                    programUid,
                    teiUid,
                ).map<List<TrackedEntityAttributeValue>> { attributesValues: List<TrackedEntityAttributeValue> ->
                    val formattedValues = formatProgramAttributeValues(attributesValues)
                    formattedValues
                }.toObservable()
        } else {
            val teType =
                d2
                    .trackedEntityModule()
                    .trackedEntityInstances()
                    .uid(teiUid)
                    .blockingGet()!!
                    .trackedEntityType()

            val attributeValues =
                mapTeiTypeAttributeValues(
                    teiAttributesProvider.getValuesFromTrackedEntityTypeAttributes(
                        teType,
                        teiUid,
                    ),
                )
            if (attributeValues.isEmpty()) {
                formatProgramAttributeValuesByTrackedEntity(
                    attributeValues,
                    teiAttributesProvider.getValuesFromProgramTrackedEntityAttributes(
                        teType,
                        teiUid,
                    ),
                )
            }
            Observable.just(attributeValues)
        }

    private fun formatProgramAttributeValues(list: List<TrackedEntityAttributeValue>): MutableList<TrackedEntityAttributeValue> {
        val formattedValues: MutableList<TrackedEntityAttributeValue> = mutableListOf()
        for (attributeValue in list) {
            if (attributeValue.value() != null) {
                val attribute =
                    d2
                        .trackedEntityModule()
                        .trackedEntityAttributes()
                        .uid(attributeValue.trackedEntityAttribute())
                        .blockingGet()
                if (attribute!!.valueType() != ValueType.IMAGE) {
                    formattedValues.add(
                        ValueUtils.transform(
                            d2,
                            attributeValue,
                            attribute.valueType(),
                            if (attribute.optionSet() != null) {
                                attribute
                                    .optionSet()!!
                                    .uid()
                            } else {
                                null
                            },
                        ),
                    )
                }
            } else {
                formattedValues.add(
                    TrackedEntityAttributeValue
                        .builder()
                        .trackedEntityAttribute(attributeValue.trackedEntityAttribute())
                        .trackedEntityInstance(teiUid)
                        .value("")
                        .build(),
                )
            }
        }
        return formattedValues
    }

    private fun formatProgramAttributeValuesByTrackedEntity(
        formattedList: MutableList<TrackedEntityAttributeValue>,
        list: List<TrackedEntityAttributeValue>,
    ): MutableList<TrackedEntityAttributeValue> {
        for (attributeValue in list) {
            val attribute =
                d2
                    .trackedEntityModule()
                    .trackedEntityAttributes()
                    .uid(attributeValue.trackedEntityAttribute())
                    .blockingGet()
            formattedList.add(
                ValueUtils.transform(
                    d2,
                    attributeValue,
                    attribute!!.valueType(),
                    if (attribute.optionSet() != null) {
                        attribute
                            .optionSet()!!
                            .uid()
                    } else {
                        null
                    },
                ),
            )
        }
        return formattedList
    }

    private fun mapTeiTypeAttributeValues(list: List<TrackedEntityAttributeValue>): MutableList<TrackedEntityAttributeValue> {
        val attributeValues: MutableList<TrackedEntityAttributeValue> = mutableListOf()
        for (attributeValue in list) {
            val attribute =
                d2
                    .trackedEntityModule()
                    .trackedEntityAttributes()
                    .uid(attributeValue.trackedEntityAttribute())
                    .blockingGet()
            if (attribute!!.valueType() != ValueType.IMAGE && attributeValue.value() != null) {
                attributeValues.add(
                    ValueUtils.transform(
                        d2,
                        attributeValue,
                        attribute.valueType(),
                        if (attribute.optionSet() != null) {
                            attribute
                                .optionSet()!!
                                .uid()
                        } else {
                            null
                        },
                    ),
                )
            }
        }

        return attributeValues
    }

    private fun mapRelationShipTypes(
        list: List<RelationshipType>,
        teType: String,
    ): MutableList<Pair<RelationshipType?, String>> {
        val relTypeList: MutableList<Pair<RelationshipType?, String>> =
            java.util.ArrayList()
        for (relationshipType in list) {
            if (relationshipType.fromConstraint() != null &&
                relationshipType
                    .fromConstraint()!!
                    .trackedEntityType() != null &&
                relationshipType
                    .fromConstraint()!!
                    .trackedEntityType()!!
                    .uid() == teType
            ) {
                if (relationshipType.toConstraint() != null &&
                    relationshipType
                        .toConstraint()!!
                        .trackedEntityType() != null
                ) {
                    relTypeList.add(
                        Pair(
                            relationshipType,
                            relationshipType
                                .toConstraint()!!
                                .trackedEntityType()!!
                                .uid(),
                        ),
                    )
                }
            } else if (relationshipType.bidirectional()!! &&
                relationshipType.toConstraint() != null &&
                relationshipType
                    .toConstraint()!!
                    .trackedEntityType() != null &&
                relationshipType
                    .toConstraint()!!
                    .trackedEntityType()!!
                    .uid() == teType &&
                relationshipType.fromConstraint() != null &&
                relationshipType.fromConstraint()!!.trackedEntityType() != null
            ) {
                relTypeList.add(
                    Pair(
                        relationshipType,
                        relationshipType
                            .fromConstraint()!!
                            .trackedEntityType()!!
                            .uid(),
                    ),
                )
            }
        }
        return relTypeList
    }

    override fun setFollowUp(enrollmentUid: String?): Boolean {
        val followUp = (
            java.lang.Boolean.TRUE
                ==
                d2
                    .enrollmentModule()
                    .enrollments()
                    .uid(enrollmentUid)
                    .blockingGet()!!
                    .followUp()
        )
        return try {
            d2
                .enrollmentModule()
                .enrollments()
                .uid(enrollmentUid)
                .setFollowUp(!followUp)
            !followUp
        } catch (d2Error: D2Error) {
            Timber.e(d2Error)
            followUp
        }
    }

    override fun updateState(
        event: Event?,
        newStatus: EventStatus,
    ): Event {
        try {
            d2
                .eventModule()
                .events()
                .uid(event?.uid())
                .setStatus(newStatus)
        } catch (d2Error: D2Error) {
            Timber.e(d2Error)
        }
        return d2
            .eventModule()
            .events()
            .uid(event?.uid())
            .blockingGet()!!
    }

    override fun completeEnrollment(enrollmentUid: String): Flowable<Enrollment> =
        Flowable.fromCallable {
            d2
                .enrollmentModule()
                .enrollments()
                .uid(enrollmentUid)
                .setStatus(EnrollmentStatus.COMPLETED)
            d2
                .enrollmentModule()
                .enrollments()
                .uid(enrollmentUid)
                .blockingGet()
        }

    override fun displayGenerateEvent(eventUid: String?): Observable<ProgramStage> =
        d2
            .eventModule()
            .events()
            .uid(eventUid)
            .get()
            .map<String> { obj: Event -> obj.programStage() }
            .flatMap { stageUid: String ->
                d2
                    .programModule()
                    .programStages()
                    .uid(stageUid)
                    .get()
            }.map { it }
            .toObservable()

    override fun relationshipsForTeiType(teType: String): Observable<List<Pair<RelationshipType?, String>>> {
        return d2
            .systemInfoModule()
            .systemInfo()
            .get()
            .toObservable()
            .map<String?>(Function { obj: SystemInfo -> obj.version() })
            .flatMap { version: String? ->
                return@flatMap if (version == "2.29") {
                    d2
                        .relationshipModule()
                        .relationshipTypes()
                        .get()
                        .toObservable()
                        .flatMapIterable<RelationshipType?> { list: List<RelationshipType?>? -> list }
                        .map { relationshipType: RelationshipType? ->
                            Pair<RelationshipType?, String>(
                                relationshipType!!,
                                teType,
                            )
                        }.toList()
                        .toObservable()
                } else {
                    d2
                        .relationshipModule()
                        .relationshipTypes()
                        .withConstraints()
                        .get()
                        .map<List<Pair<RelationshipType?, String>>> { relationshipTypes: List<RelationshipType> ->
                            val relTypeList = mapRelationShipTypes(relationshipTypes, teType)
                            relTypeList.toList()
                        }.toObservable()
                }
            }
    }

    override fun catOptionCombo(catComboUid: String?): CategoryOptionCombo =
        d2
            .categoryModule()
            .categoryOptionCombos()
            .uid(catComboUid)
            .blockingGet()!!

    override fun getTrackedEntityInstance(teiUid: String): Observable<TrackedEntityInstance> =
        Observable.fromCallable {
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .byUid()
                .eq(teiUid)
                .one()
                .blockingGet()
        }

    override fun getProgramTrackedEntityAttributes(programUid: String?): Observable<List<ProgramTrackedEntityAttribute>> =
        if (programUid != null) {
            d2
                .programModule()
                .programTrackedEntityAttributes()
                .byProgram()
                .eq(programUid)
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                .get()
                .toObservable()
        } else {
            Observable
                .fromCallable {
                    d2
                        .trackedEntityModule()
                        .trackedEntityAttributes()
                        .byDisplayInListNoProgram()
                        .eq(true)
                        .blockingGet()
                }.map { trackedEntityAttributes: List<TrackedEntityAttribute> ->
                    val programs =
                        d2.programModule().programs().blockingGet()
                    val teaUids = getUidsList(trackedEntityAttributes)
                    val programTrackedEntityAttributes: MutableList<ProgramTrackedEntityAttribute> =
                        java.util.ArrayList()
                    for (program in programs) {
                        val attributeList =
                            d2
                                .programModule()
                                .programTrackedEntityAttributes()
                                .byProgram()
                                .eq(program.uid())
                                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                                .blockingGet()
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

    override fun getTeiOrgUnits(
        teiUid: String,
        programUid: String?,
    ): Observable<List<OrganisationUnit>> {
        var enrollmentRepo =
            d2
                .enrollmentModule()
                .enrollments()
                .byTrackedEntityInstance()
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
            d2
                .organisationUnitModule()
                .organisationUnits()
                .byUid()
                .`in`(orgUnitIds.toList())
                .blockingGet()
        }
    }

    private fun getOwnerOrgUnit(teiUid: String): OrganisationUnit? =
        d2
            .trackedEntityModule()
            .trackedEntityInstances()
            .withProgramOwners()
            .uid(teiUid)
            .blockingGet()
            ?.programOwners()
            ?.firstOrNull { it.trackedEntityInstance() == teiUid }
            ?.ownerOrgUnit()
            ?.let { orgUnitUid ->
                d2
                    .organisationUnitModule()
                    .organisationUnits()
                    .uid(orgUnitUid)
                    .blockingGet()
            }

    override fun getDashboardModel(): DashboardModel =
        if (programUid.isNullOrEmpty()) {
            DashboardTEIModel(
                getTEIEnrollments(teiUid).blockingFirst(),
                getTrackedEntityInstance(teiUid).blockingFirst(),
                getTEIAttributeValues(null, teiUid).blockingFirst(),
                getTeiActivePrograms(teiUid, true).blockingFirst(),
                getTeiOrgUnits(teiUid, null).blockingFirst(),
                getTeiHeader(),
                getTeiProfilePath(),
                getOwnerOrgUnit(teiUid),
            )
        } else {
            DashboardEnrollmentModel(
                getEnrollment().blockingFirst(),
                getProgramStages(programUid).blockingFirst(),
                getTrackedEntityInstance(teiUid).blockingFirst(),
                getAttributesMap(programUid, teiUid).blockingFirst(),
                getTEIAttributeValues(programUid, teiUid).blockingFirst(),
                getTeiActivePrograms(teiUid, false).blockingFirst(),
                getTeiOrgUnits(teiUid, programUid).blockingFirst(),
                getTeiHeader(),
                getTeiProfilePath(),
                getOwnerOrgUnit(teiUid),
                getQuickActions(programUid),
            )
        }

    private fun getQuickActions(programUid: String): List<String> =
        programConfigurationRepository
            .getConfigurationByProgram(programUid)
            ?.quickActions()
            ?.map { it.actionId() }
            ?: emptyList()

    override fun getTeiActivePrograms(
        teiUid: String,
        showOnlyActive: Boolean,
    ): Observable<List<kotlin.Pair<Program, MetadataIconData>>> {
        val enrollmentRepo =
            d2
                .enrollmentModule()
                .enrollments()
                .byTrackedEntityInstance()
                .eq(teiUid)
                .byDeleted()
                .eq(false)
        if (showOnlyActive) enrollmentRepo.byStatus().eq(EnrollmentStatus.ACTIVE)

        return enrollmentRepo
            .get()
            .toObservable()
            .flatMapIterable { enrollments: List<Enrollment>? -> enrollments }
            .map { obj: Enrollment -> obj.program() }
            .toList()
            .toObservable()
            .map { programUids ->
                d2
                    .programModule()
                    .programs()
                    .byUid()
                    .`in`(programUids.filterNotNull())
                    .blockingGet()
                    .map {
                        Pair(it, metadataIconProvider(it.style(), SurfaceColor.Primary))
                    }
            }
    }

    override fun getTEIEnrollments(teiUid: String): Observable<List<Enrollment>> =
        d2
            .enrollmentModule()
            .enrollments()
            .byTrackedEntityInstance()
            .eq(teiUid)
            .byDeleted()
            .eq(false)
            .get()
            .toObservable()

    override fun saveCatOption(
        eventUid: String?,
        catOptionComboUid: String?,
    ) {
        try {
            d2
                .eventModule()
                .events()
                .uid(eventUid)
                .setAttributeOptionComboUid(catOptionComboUid)
        } catch (d2Error: D2Error) {
            Timber.e(d2Error)
        }
    }

    override fun checkIfDeleteTeiIsPossible(): Boolean {
        val local =
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .uid(teiUid)
                .blockingGet()
                ?.aggregatedSyncState() == State.TO_POST
        val hasAuthority =
            d2
                .userModule()
                .authorities()
                .byName()
                .eq("F_TEI_CASCADE_DELETE")
                .one()
                .blockingExists()

        return local || hasAuthority
    }

    override fun deleteTei(): Single<Boolean> =
        d2
            .trackedEntityModule()
            .trackedEntityInstances()
            .uid(teiUid)
            .delete()
            .andThen(Single.fromCallable { true })

    override fun checkIfDeleteEnrollmentIsPossible(enrollmentUid: String): Boolean {
        val local =
            d2
                .enrollmentModule()
                .enrollments()
                .uid(enrollmentUid)
                .blockingGet()!!
                .aggregatedSyncState() == State.TO_POST
        val hasAuthority =
            d2
                .userModule()
                .authorities()
                .byName()
                .eq("F_ENROLLMENT_CASCADE_DELETE")
                .one()
                .blockingExists()

        return local || hasAuthority
    }

    override fun deleteEnrollment(enrollmentUid: String): Single<Boolean> =
        Single.fromCallable {
            val enrollmentObjectRepository =
                d2
                    .enrollmentModule()
                    .enrollments()
                    .uid(enrollmentUid)
            enrollmentObjectRepository.setStatus(
                enrollmentObjectRepository.blockingGet()!!.status()!!,
            )
            enrollmentObjectRepository.blockingDelete()
            d2
                .enrollmentModule()
                .enrollments()
                .byTrackedEntityInstance()
                .eq(teiUid)
                .byDeleted()
                .isFalse
                .byStatus()
                .eq(EnrollmentStatus.ACTIVE)
                .blockingGet()
                .isNotEmpty()
        }

    override fun getNoteCount(): Single<Int> =
        d2
            .enrollmentModule()
            .enrollments()
            .withNotes()
            .uid(enrollmentUid)
            .get()
            .map(Function { enrollment: Enrollment -> if (enrollment.notes() != null) enrollment.notes()!!.size else 0 })

    override fun getEnrollmentStatus(enrollmentUid: String?): EnrollmentStatus? =
        d2
            .enrollmentModule()
            .enrollments()
            .uid(enrollmentUid)
            .blockingGet()!!
            .status()

    override fun updateEnrollmentStatus(
        enrollmentUid: String,
        status: EnrollmentStatus,
    ): Observable<StatusChangeResultCode> =
        try {
            if (d2
                    .programModule()
                    .programs()
                    .uid(programUid)
                    .blockingGet()!!
                    .access()
                    .data()
                    .write()
            ) {
                if (reopenCheck(status)) {
                    d2
                        .enrollmentModule()
                        .enrollments()
                        .uid(enrollmentUid)
                        .setStatus(status)
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

    private fun reopenCheck(status: EnrollmentStatus): Boolean =
        status != EnrollmentStatus.ACTIVE ||
            d2
                .enrollmentModule()
                .enrollments()
                .byProgram()
                .eq(programUid)
                .byTrackedEntityInstance()
                .eq(teiUid)
                .byStatus()
                .eq(EnrollmentStatus.ACTIVE)
                .blockingIsEmpty()

    override fun programHasRelationships(): Boolean =
        if (!programUid.isNullOrEmpty()) {
            val teiTypeUid =
                d2
                    .programModule()
                    .programs()
                    .uid(programUid)
                    .blockingGet()
                    ?.trackedEntityType()
                    ?.uid()
            teiTypeUid?.let { relationshipsForTeiType(it) }!!.blockingFirst().isNotEmpty()
        } else {
            false
        }

    override fun programHasAnalytics(): Boolean =
        if (!programUid.isNullOrEmpty()) {
            val enrollmentScopeRulesUids =
                d2
                    .programModule()
                    .programRules()
                    .byProgramUid()
                    .eq(programUid)
                    .byProgramStageUid()
                    .isNull
                    .blockingGetUids()
            val hasDisplayRuleActions =
                !d2
                    .programModule()
                    .programRuleActions()
                    .byProgramRuleUid()
                    .`in`(enrollmentScopeRulesUids)
                    .byProgramRuleActionType()
                    .`in`(ProgramRuleActionType.DISPLAYKEYVALUEPAIR, ProgramRuleActionType.DISPLAYTEXT)
                    .blockingIsEmpty()
            val hasProgramIndicator =
                !d2
                    .programModule()
                    .programIndicators()
                    .byProgramUid()
                    .eq(programUid)
                    .blockingIsEmpty()
            val hasCharts =
                enrollmentUid?.let { charts.geEnrollmentCharts(enrollmentUid).isNotEmpty() }
                    ?: false
            hasDisplayRuleActions || hasProgramIndicator || hasCharts
        } else {
            false
        }

    override fun getGrouping(): Boolean = getGroupingOptions().getOrDefault(programUid, true)

    override fun setGrouping(groupEvent: Boolean) {
        val groups = getGroupingOptions()
        programUid?.let { groups[programUid] = groupEvent }
        preferenceProvider.saveAsJson<Map<String, Boolean>>(
            Preference.GROUPING,
            groups,
        )
    }

    override fun transferTei(newOrgUnitId: String) {
        d2
            .trackedEntityModule()
            .ownershipManager()
            .blockingTransfer(teiUid, programUid!!, newOrgUnitId)
    }

    override fun teiCanBeTransferred(): Boolean {
        if (programUid == null) {
            return false
        }

        val orgUnits =
            d2
                .organisationUnitModule()
                .organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                .byProgramUids(listOf(programUid))
                .blockingGet()

        if (orgUnits.isEmpty()) {
            return false
        }

        return orgUnits.size > 1 ||
            orgUnits.first().uid() != getOwnerOrgUnit(teiUid)?.uid()
    }

    override fun enrollmentHasWriteAccess(): Boolean =
        programUid?.let {
            d2.enrollmentModule().enrollmentService().blockingGetEnrollmentAccess(
                teiUid,
                it,
            )
        } == EnrollmentAccess.WRITE_ACCESS

    private fun getGroupingOptions(): HashMap<String, Boolean> {
        val typeToken: TypeToken<HashMap<String, Boolean>> =
            object : TypeToken<HashMap<String, Boolean>>() {}
        val grouping =
            preferenceProvider.getObjectFromJson(
                Preference.GROUPING,
                typeToken,
                HashMap(),
            )
        return grouping
    }

    override fun getTETypeName(): String? =
        getTrackedEntityInstance(teiUid)
            .flatMap { tei: TrackedEntityInstance ->
                d2
                    .trackedEntityModule()
                    .trackedEntityTypes()
                    .uid(tei.trackedEntityType())
                    .get()
                    .toObservable()
            }.blockingFirst()
            ?.displayName()

    override fun getAttributesMap(
        programUid: String,
        teiUid: String,
    ): Observable<List<kotlin.Pair<TrackedEntityAttribute, TrackedEntityAttributeValue>>> =
        teiAttributesProvider
            .getProgramTrackedEntityAttributesByProgram(programUid, teiUid)
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
                                attribute
                                    .optionSet()!!
                                    .uid()
                            } else {
                                null
                            },
                        )
                    } else {
                        TrackedEntityAttributeValue
                            .builder()
                            .trackedEntityAttribute(attribute!!.uid())
                            .trackedEntityInstance(teiUid)
                            .value("")
                            .build()
                    }
                Pair(
                    attribute,
                    formattedAttributeValue,
                )
            }.toList()
            .toObservable()
}
