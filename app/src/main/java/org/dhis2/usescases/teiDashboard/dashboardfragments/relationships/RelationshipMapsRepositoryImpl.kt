package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import org.dhis2.commons.bindings.event
import org.dhis2.commons.bindings.program
import org.dhis2.maps.model.MapItemModel
import org.dhis2.maps.model.RelatedInfo
import org.dhis2.tracker.relationships.model.RelationshipOwnerType
import org.dhis2.usescases.events.EventInfoProvider
import org.dhis2.usescases.tracker.TrackedEntityInstanceInfoProvider
import org.hisp.dhis.android.core.D2

class RelationshipMapsRepositoryImpl(
    private val d2: D2,
    private val config: RelationshipConfiguration,
    private val trackedEntityInfoProvider: TrackedEntityInstanceInfoProvider,
    private val eventInfoProvider: EventInfoProvider,
) : RelationshipMapsRepository {
    override fun getRelatedInfo(
        ownerType: RelationshipOwnerType,
        ownerUid: String,
    ): RelatedInfo? {
        return when (ownerType) {
            RelationshipOwnerType.EVENT -> {
                val event = d2.event(ownerUid)
                return event?.let {
                    eventInfoProvider.getRelatedInfo(it)
                }
            }

            RelationshipOwnerType.TEI -> {
                val tei =
                    d2
                        .trackedEntityModule()
                        .trackedEntityInstances()
                        .uid(ownerUid)
                        .blockingGet()

                val searchItem =
                    d2
                        .trackedEntityModule()
                        .trackedEntitySearch()
                        .byTrackedEntityType()
                        .eq(tei?.trackedEntityType())
                        .uid(ownerUid)
                        .blockingGet()

                searchItem?.let {
                    if (config is TrackerRelationshipConfiguration) {
                        val programUid =
                            d2
                                .enrollmentModule()
                                .enrollments()
                                .uid(config.enrollmentUid)
                                .blockingGet()
                                ?.program()
                        trackedEntityInfoProvider.getRelatedInfo(
                            searchItem = searchItem,
                            selectedProgram = programUid?.let { d2.program(programUid) },
                        )
                    } else {
                        null
                    }
                }
            }
        }
    }

    override fun addRelationshipInfo(
        mapItem: MapItemModel,
        relationshipUid: String,
    ): MapItemModel {
        val relationship =
            d2
                .relationshipModule()
                .relationships()
                .uid(relationshipUid)
                .blockingGet()
        requireNotNull(relationship)
        return trackedEntityInfoProvider.updateRelationshipInfo(mapItem, relationship)
    }

    override fun getEventProgram(eventUid: String?): String =
        d2
            .eventModule()
            .events()
            .uid(eventUid)
            .blockingGet()
            ?.program() ?: ""
}

sealed class RelationshipConfiguration

data class TrackerRelationshipConfiguration(
    val enrollmentUid: String,
    val teiUid: String,
) : RelationshipConfiguration()

data class EventRelationshipConfiguration(
    val eventUid: String,
) : RelationshipConfiguration()
