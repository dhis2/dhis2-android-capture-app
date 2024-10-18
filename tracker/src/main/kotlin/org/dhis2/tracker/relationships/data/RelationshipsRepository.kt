package org.dhis2.tracker.relationships.data

import kotlinx.coroutines.flow.Flow
import org.dhis2.bindings.userFriendlyValue
import org.dhis2.commons.date.toUi
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.tracker.R
import org.dhis2.tracker.relationships.model.RelationshipModel
import org.dhis2.tracker.relationships.model.RelationshipOwnerType
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.relationship.RelationshipConstraint
import org.hisp.dhis.android.core.relationship.RelationshipType
import org.hisp.dhis.android.core.systeminfo.DHISVersion
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import java.util.Date

typealias value = String
typealias label = String

abstract class RelationshipsRepository(
    private val d2: D2,
    private val resources: ResourceManager,
) {
    abstract fun getRelationshipTypes(): Flow<List<Pair<RelationshipType, String?>>>
    abstract fun getRelationships(): Flow<List<RelationshipModel>>
    abstract fun getRelationshipTitle(relationshipType: RelationshipType): String

    protected fun orgUnitInScope(orgUnitUid: String?): Boolean {
        return orgUnitUid?.let {
            val inCaptureScope = d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .uid(orgUnitUid)
                .blockingExists()
            val inSearchScope = d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                .uid(orgUnitUid)
                .blockingExists()
            inCaptureScope || inSearchScope
        } ?: false
    }

    protected fun getTeiAttributesForRelationship(
        teiUid: String?,
        relationshipConstraint: RelationshipConstraint?,
        relationshipCreationDate: Date?,
    ): List<Pair<label, value>> {

        //Get list of ordered attributes uids
        val trackedEntityAttributesUids = when {

            //When there are  attributes defined in the relationship constraint
            relationshipConstraint?.trackerDataView()?.attributes()?.isNotEmpty() == true -> {
                relationshipConstraint.trackerDataView()?.attributes()
            }

            //If not and server version is less than 38, we check the trackedEntity type attributes
            relationshipConstraint?.trackedEntityType()?.uid() != null &&
                    isServerVersionLessThan38() -> {
                val teiTypeUid = relationshipConstraint.trackedEntityType()?.uid()
                d2.trackedEntityModule().trackedEntityTypeAttributes()
                    .byTrackedEntityTypeUid().eq(teiTypeUid)
                    .byDisplayInList().isTrue.blockingGet().mapNotNull {
                        it.trackedEntityAttribute()?.uid()
                    }
            }

            else -> {
                emptyList()
            }

        }

        //Get a list of Pair<label, value>
        val attributes = trackedEntityAttributesUids?.mapNotNull { attributeUid ->
            val fieldName = d2.trackedEntityModule().trackedEntityAttributes()
                .uid(attributeUid).blockingGet()
                ?.displayFormName()

            val value = d2.trackedEntityModule().trackedEntityAttributeValues()
                .value(attributeUid, teiUid!!).blockingGet()?.userFriendlyValue(d2)
            if (fieldName != null && value != null) {
                Pair(fieldName, value)
            } else {
                null
            }
        } ?: emptyList()

        return attributes.ifEmpty {
            val teiTypeUid = relationshipConstraint?.trackedEntityType()?.uid()
            val teiTypeName = d2.trackedEntityModule().trackedEntityTypes()
                .uid(teiTypeUid).blockingGet()?.name() ?: ""
            listOf(
                Pair(teiTypeName, ""),
                Pair(
                    resources.getString(R.string.relation_creation_date),
                    relationshipCreationDate?.toUi() ?: ""
                )
            )
        }
    }

    protected fun getEventValuesForRelationship(
        eventUid: String?,
        relationshipConstraint: RelationshipConstraint?,
        relationshipCreationDate: Date?,
    ): List<Pair<label, value>> {

        //Get list of ordered data elements uids
        val dataElementUids = when {

            //When there are  data elements defined in the relationship constraint
            relationshipConstraint?.trackerDataView()?.dataElements()?.isNotEmpty() == true -> {
                relationshipConstraint.trackerDataView()?.dataElements()
            }

            //If not and server version is less than 38, we check the program stage data elements
            relationshipConstraint?.programStage() != null && isServerVersionLessThan38() -> {
                val programStageUid = relationshipConstraint.programStage()?.uid()
                d2.programModule().programStageDataElements()
                    .byProgramStage().eq(programStageUid)
                    .byDisplayInReports().isTrue.blockingGetUids()
            }

            else -> {
                emptyList()
            }

        }

        val event =
            d2.eventModule().events().withTrackedEntityDataValues().uid(eventUid).blockingGet()

        val dataElements = dataElementUids?.mapNotNull { dataElementUid ->
            val formName = d2.dataElementModule().dataElements()
                .uid(dataElementUid).blockingGet()
                ?.displayName()
            val value =
                event?.trackedEntityDataValues()?.find { it.dataElement() == dataElementUid }
                    .userFriendlyValue(d2)
            if (formName != null && value != null) {
                Pair(formName, value)
            } else {
                null
            }
        } ?: emptyList()

        return dataElements.ifEmpty {
            val stage = d2.programModule().programStages().uid(event?.programStage()).blockingGet()
            listOf(
                Pair(
                    stage?.displayName() ?: event?.uid() ?: "",
                    ""
                ),
                Pair(
                    resources.getString(R.string.relation_creation_date),
                    relationshipCreationDate?.toUi() ?: ""
                )
            )
        }
    }

    protected fun getTeiDefaultRes(tei: TrackedEntityInstance?): Int {
        val teiType =
            d2.trackedEntityModule().trackedEntityTypes()
                .uid(tei?.trackedEntityType())
                .blockingGet()
        return getTeiTypeDefaultRes(teiType?.uid())
    }

    private fun getTeiTypeDefaultRes(teiTypeUid: String?): Int {
        val teiType =
            d2.trackedEntityModule().trackedEntityTypes().uid(teiTypeUid).blockingGet()
        return resources.getObjectStyleDrawableResource(
            teiType?.style()?.icon(),
            R.drawable.photo_temp_gray,
        )
    }

    protected fun getEventDefaultRes(event: Event?): Int {
        val stage = d2.programModule().programStages().uid(event?.programStage()).blockingGet()
        val program = d2.programModule().programs().uid(event?.program()).blockingGet()
        return resources.getObjectStyleDrawableResource(
            stage?.style()?.icon() ?: program?.style()?.icon(),
            R.drawable.photo_temp_gray,
        )
    }

    protected fun getOwnerStyle(
        uid: String,
        relationshipOwnerType: RelationshipOwnerType,
    ): ObjectStyle? {
        return when (relationshipOwnerType) {
            RelationshipOwnerType.EVENT -> {
                val event = d2.eventModule().events().uid(uid).blockingGet()
                val program = d2.programModule().programs().uid(event?.program()).blockingGet()
                if (program?.programType() == ProgramType.WITHOUT_REGISTRATION) {
                    program.style()
                } else {
                    val programStage =
                        d2.programModule().programStages().uid(event?.programStage()).blockingGet()
                    programStage?.style()
                }
            }

            RelationshipOwnerType.TEI -> {
                val tei = d2.trackedEntityModule().trackedEntityInstances()
                    .uid(uid).blockingGet()
                val teType = d2.trackedEntityModule().trackedEntityTypes()
                    .uid(tei?.trackedEntityType()).blockingGet()
                teType?.style()
            }
        }
    }

    protected fun getStage(programStageUid: String): ProgramStage? {
        return d2.programModule().programStages()
            .uid(programStageUid)
            .blockingGet()
    }

    private fun isServerVersionLessThan38(): Boolean {
        return !d2.systemInfoModule().versionManager().isGreaterOrEqualThan(DHISVersion.V2_38)
    }

    fun deleteRelationship(relationshipUid: String) {
        d2.relationshipModule()
            .relationships()
            .withItems()
            .uid(relationshipUid)
            .blockingDelete()
    }
}
