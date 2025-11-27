package org.dhis2.tracker.relationships.data

import org.dhis2.bindings.userFriendlyValue
import org.dhis2.commons.date.toUi
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.mobile.tracker.R
import org.dhis2.mobile.tracker.resources.Res
import org.dhis2.mobile.tracker.resources.relation_creation_date
import org.dhis2.mobile.tracker.resources.relationship
import org.dhis2.tracker.relationships.model.RelationshipConstraintSide
import org.dhis2.tracker.relationships.model.RelationshipModel
import org.dhis2.tracker.relationships.model.RelationshipOwnerType
import org.dhis2.tracker.relationships.model.RelationshipSection
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.relationship.Relationship
import org.hisp.dhis.android.core.relationship.RelationshipConstraint
import org.hisp.dhis.android.core.relationship.RelationshipConstraintType
import org.hisp.dhis.android.core.relationship.RelationshipType
import org.hisp.dhis.android.core.systeminfo.DHISVersion
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.jetbrains.compose.resources.getString
import java.util.Date

typealias value = String
typealias label = String

abstract class RelationshipsRepository(
    private val d2: D2,
    private val resources: ResourceManager,
) : RelationshipsRepositoryActions {
    abstract override suspend fun getRelationshipTypes(): List<RelationshipSection>

    abstract override suspend fun getRelationshipsGroupedByTypeAndSide(relationshipSection: RelationshipSection): RelationshipSection

    abstract override suspend fun getRelationships(): List<RelationshipModel>

    abstract fun createRelationship(
        selectedTeiUid: String,
        relationshipTypeUid: String,
        relationshipSide: RelationshipConstraintSide,
    ): Relationship

    protected fun orgUnitInScope(orgUnitUid: String?): Boolean =
        orgUnitUid?.let {
            val inCaptureScope =
                d2
                    .organisationUnitModule()
                    .organisationUnits()
                    .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                    .uid(orgUnitUid)
                    .blockingExists()
            val inSearchScope =
                d2
                    .organisationUnitModule()
                    .organisationUnits()
                    .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                    .uid(orgUnitUid)
                    .blockingExists()
            inCaptureScope || inSearchScope
        } ?: false

    protected suspend fun getTeiAttributesForRelationship(
        teiUid: String?,
        relationshipConstraint: RelationshipConstraint?,
        relationshipCreationDate: Date?,
    ): List<Pair<label, value>> {
        // Get list of ordered attributes uids
        val trackedEntityAttributesUids =
            when {
                // When there are  attributes defined in the relationship constraint
                relationshipConstraint?.trackerDataView()?.attributes()?.isNotEmpty() == true -> {
                    relationshipConstraint.trackerDataView()?.attributes()
                }

                // If not and server version is less than 38, we check the trackedEntity type attributes
                relationshipConstraint?.trackedEntityType()?.uid() != null &&
                    isServerVersionLessThan38() -> {
                    val teiTypeUid = relationshipConstraint.trackedEntityType()?.uid()
                    d2
                        .trackedEntityModule()
                        .trackedEntityTypeAttributes()
                        .byTrackedEntityTypeUid()
                        .eq(teiTypeUid)
                        .byDisplayInList()
                        .isTrue
                        .blockingGet()
                        .mapNotNull {
                            it.trackedEntityAttribute()?.uid()
                        }
                }

                else -> {
                    emptyList()
                }
            }

        // Get a list of Pair<label, value>
        val attributes =
            trackedEntityAttributesUids?.mapNotNull { attributeUid ->
                val fieldName =
                    d2
                        .trackedEntityModule()
                        .trackedEntityAttributes()
                        .uid(attributeUid)
                        .blockingGet()
                        ?.displayFormName()

                val value =
                    d2
                        .trackedEntityModule()
                        .trackedEntityAttributeValues()
                        .value(attributeUid, teiUid!!)
                        .blockingGet()
                        ?.userFriendlyValue(d2)
                if (fieldName != null && value != null) {
                    Pair(fieldName, value)
                } else {
                    null
                }
            } ?: emptyList()

        return attributes.ifEmpty {
            val teiTypeUid = relationshipConstraint?.trackedEntityType()?.uid()
            val teiTypeName =
                d2
                    .trackedEntityModule()
                    .trackedEntityTypes()
                    .uid(teiTypeUid)
                    .blockingGet()
                    ?.name() ?: ""
            listOf(
                Pair(teiTypeName, ""),
                Pair(
                    getString(Res.string.relation_creation_date),
                    relationshipCreationDate?.toUi() ?: "",
                ),
            )
        }
    }

    protected suspend fun getEventValuesForRelationship(
        eventUid: String?,
        relationshipConstraint: RelationshipConstraint?,
        relationshipCreationDate: Date?,
    ): List<Pair<label, value>> {
        // Get list of ordered data elements uids
        val dataElementUids =
            when {
                // When there are  data elements defined in the relationship constraint
                relationshipConstraint?.trackerDataView()?.dataElements()?.isNotEmpty() == true -> {
                    relationshipConstraint.trackerDataView()?.dataElements()
                }

                // If not and server version is less than 38, we check the program stage data elements
                relationshipConstraint?.programStage() != null && isServerVersionLessThan38() -> {
                    val programStageUid = relationshipConstraint.programStage()?.uid()
                    d2
                        .programModule()
                        .programStageDataElements()
                        .byProgramStage()
                        .eq(programStageUid)
                        .byDisplayInReports()
                        .isTrue
                        .blockingGetUids()
                }

                else -> {
                    emptyList()
                }
            }

        val event =
            d2
                .eventModule()
                .events()
                .withTrackedEntityDataValues()
                .uid(eventUid)
                .blockingGet()

        val dataElements =
            dataElementUids?.mapNotNull { dataElementUid ->
                val formName =
                    d2
                        .dataElementModule()
                        .dataElements()
                        .uid(dataElementUid)
                        .blockingGet()
                        ?.displayName()
                val value =
                    event
                        ?.trackedEntityDataValues()
                        ?.find { it.dataElement() == dataElementUid }
                        .userFriendlyValue(d2)
                if (formName != null && value != null) {
                    Pair(formName, value)
                } else {
                    null
                }
            } ?: emptyList()

        return dataElements.ifEmpty {
            val stage =
                d2
                    .programModule()
                    .programStages()
                    .uid(event?.programStage())
                    .blockingGet()
            listOf(
                Pair(
                    stage?.displayName() ?: event?.uid() ?: "",
                    "",
                ),
                Pair(
                    getString(Res.string.relation_creation_date),
                    relationshipCreationDate?.toUi() ?: "",
                ),
            )
        }
    }

    protected fun getTeiDefaultRes(tei: TrackedEntityInstance?): Int {
        val teiType =
            d2
                .trackedEntityModule()
                .trackedEntityTypes()
                .uid(tei?.trackedEntityType())
                .blockingGet()
        return getTeiTypeDefaultRes(teiType?.uid())
    }

    private fun getTeiTypeDefaultRes(teiTypeUid: String?): Int {
        val teiType =
            d2
                .trackedEntityModule()
                .trackedEntityTypes()
                .uid(teiTypeUid)
                .blockingGet()
        return resources.getObjectStyleDrawableResource(
            teiType?.style()?.icon(),
            R.drawable.photo_temp_gray,
        )
    }

    protected fun getEventDefaultRes(event: Event?): Int {
        val stage =
            d2
                .programModule()
                .programStages()
                .uid(event?.programStage())
                .blockingGet()
        val program =
            d2
                .programModule()
                .programs()
                .uid(event?.program())
                .blockingGet()
        return resources.getObjectStyleDrawableResource(
            stage?.style()?.icon() ?: program?.style()?.icon(),
            R.drawable.photo_temp_gray,
        )
    }

    protected fun getOwnerStyle(
        uid: String,
        relationshipOwnerType: RelationshipOwnerType,
    ): ObjectStyle? =
        when (relationshipOwnerType) {
            RelationshipOwnerType.EVENT -> {
                val event =
                    d2
                        .eventModule()
                        .events()
                        .uid(uid)
                        .blockingGet()
                val program =
                    d2
                        .programModule()
                        .programs()
                        .uid(event?.program())
                        .blockingGet()
                if (program?.programType() == ProgramType.WITHOUT_REGISTRATION) {
                    program.style()
                } else {
                    val programStage =
                        d2
                            .programModule()
                            .programStages()
                            .uid(event?.programStage())
                            .blockingGet()
                    programStage?.style()
                }
            }

            RelationshipOwnerType.TEI -> {
                val tei =
                    d2
                        .trackedEntityModule()
                        .trackedEntityInstances()
                        .uid(uid)
                        .blockingGet()
                val teType =
                    d2
                        .trackedEntityModule()
                        .trackedEntityTypes()
                        .uid(tei?.trackedEntityType())
                        .blockingGet()
                teType?.style()
            }
        }

    protected fun getStage(programStageUid: String): ProgramStage? =
        d2
            .programModule()
            .programStages()
            .uid(programStageUid)
            .blockingGet()

    private fun isServerVersionLessThan38(): Boolean = !d2.systemInfoModule().versionManager().isGreaterOrEqualThan(DHISVersion.V2_38)

    override suspend fun deleteRelationship(relationshipUid: String) {
        d2
            .relationshipModule()
            .relationships()
            .withItems()
            .uid(relationshipUid)
            .blockingDelete()
    }

    override suspend fun addRelationship(
        selectedTeiUid: String,
        relationshipTypeUid: String,
        relationshipSide: RelationshipConstraintSide,
    ): Result<String> =
        try {
            val relationship =
                createRelationship(
                    selectedTeiUid,
                    relationshipTypeUid,
                    relationshipSide,
                )
            val relationshipUid = d2.relationshipModule().relationships().blockingAdd(relationship)
            Result.success(relationshipUid)
        } catch (error: D2Error) {
            Result.failure(error)
        }

    protected fun getRelationshipTypeByUid(relationshipTypeUid: String?) =
        d2
            .relationshipModule()
            .relationshipTypes()
            .withConstraints()
            .uid(relationshipTypeUid)
            .blockingGet()

    protected suspend fun getRelationshipTitle(
        relationshipType: RelationshipType,
        entitySide: RelationshipConstraintType,
    ): String =
        when (entitySide) {
            RelationshipConstraintType.FROM -> {
                relationshipType.fromToName() ?: relationshipType.displayName()
                    ?: getString(Res.string.relationship)
            }

            RelationshipConstraintType.TO -> {
                relationshipType.toFromName() ?: relationshipType.displayName()
                    ?: getString(Res.string.relationship)
            }
        }

    override fun hasWritePermission(relationshipTypeUid: String): Boolean =
        getRelationshipTypeByUid(relationshipTypeUid)?.let { relationshipType ->
            d2.relationshipModule().relationshipService().hasAccessPermission(relationshipType)
        } ?: false
}
