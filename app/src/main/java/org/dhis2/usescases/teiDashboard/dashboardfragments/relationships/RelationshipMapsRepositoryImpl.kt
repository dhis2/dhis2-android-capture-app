package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import io.reactivex.Single
import org.dhis2.R
import org.dhis2.commons.bindings.event
import org.dhis2.commons.bindings.program
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.maps.model.MapItemModel
import org.dhis2.usescases.events.EventInfoProvider
import org.dhis2.usescases.tracker.TrackedEntityInstanceInfoProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.relationship.Relationship
import org.hisp.dhis.android.core.relationship.RelationshipItem
import org.hisp.dhis.android.core.relationship.RelationshipItemEvent
import org.hisp.dhis.android.core.relationship.RelationshipItemTrackedEntityInstance

class RelationshipMapsRepositoryImpl(
    private val d2: D2,
    private val config: RelationshipConfiguration,
    private val resources: ResourceManager,
    private val trackedEntityInfoProvider: TrackedEntityInstanceInfoProvider,
    private val eventInfoProvider: EventInfoProvider,
) : RelationshipMapsRepository {

    override fun mapRelationships(): Single<List<MapItemModel>> {
        return when (config) {
            is EventRelationshipConfiguration -> Single.just(eventMapRelationships())
            is TrackerRelationshipConfiguration -> Single.just(enrollmentMapRelationships())
        }
    }

    private fun eventMapRelationships(): List<MapItemModel> {
        return buildList {
            d2.relationshipModule().relationships().withItems().getByItem(
                RelationshipItem.builder()
                    .event(
                        RelationshipItemEvent.builder()
                            .event((config as EventRelationshipConfiguration).eventUid)
                            .build(),
                    )
                    .build(),
            ).forEach { relationship ->
                mapEventRelationshipItemToMapItemModel(
                    relationship.from()!!.elementUid(),
                    relationship,
                )?.let {
                    add(it)
                }
                when {
                    relationship.to()?.event() != null ->
                        mapEventRelationshipItemToMapItemModel(
                            relationship.to()!!.elementUid(),
                            relationship,
                        )?.let { add(it) }

                    relationship.to()?.trackedEntityInstance() != null ->
                        mapTeiRelationshipItemToMapItemModel(
                            relationship.to()!!.elementUid(),
                            relationship,
                            null,
                        )?.let { add(it) }
                }
            }
        }
    }

    private fun enrollmentMapRelationships(): List<MapItemModel> {
        val programUid = d2.enrollmentModule().enrollments()
            .uid((config as TrackerRelationshipConfiguration).enrollmentUid).blockingGet()
            ?.program()

        return buildList {
            d2.relationshipModule().relationships().withItems().getByItem(
                RelationshipItem.builder()
                    .trackedEntityInstance(
                        RelationshipItemTrackedEntityInstance.builder()
                            .trackedEntityInstance(config.teiUid)
                            .build(),
                    )
                    .build(),
            ).filter { it.to()?.trackedEntityInstance() != null }.forEach { relationship ->

                relationship.from()?.trackedEntityInstance()?.trackedEntityInstance()?.let { tei ->
                    mapTeiRelationshipItemToMapItemModel(
                        tei,
                        relationship,
                        programUid,
                    )?.let {
                        add(it)
                    }
                }
                relationship.to()?.trackedEntityInstance()?.trackedEntityInstance()?.let { tei ->
                    mapTeiRelationshipItemToMapItemModel(
                        tei,
                        relationship,
                        programUid,
                    )?.let {
                        add(it)
                    }
                }
            }
        }
    }

    private fun mapEventRelationshipItemToMapItemModel(
        eventUid: String,
        relationship: Relationship,
    ): MapItemModel? {
        val event = d2.event(eventUid)
        return event?.let {
            MapItemModel(
                uid = it.uid(),
                avatarProviderConfiguration = eventInfoProvider.getAvatar(it),
                title = eventInfoProvider.getEventTitle(it),
                description = eventInfoProvider.getEventDescription(it),
                lastUpdated = eventInfoProvider.getEventLastUpdated(it),
                additionalInfoList = eventInfoProvider.getAdditionInfoList(it),
                isOnline = false,
                geometry = it.geometry(),
                relatedInfo = eventInfoProvider.getRelatedInfo(it),
                state = relationship.syncState() ?: State.SYNCED,
            )
        }
    }

    private fun mapTeiRelationshipItemToMapItemModel(
        teiUid: String,
        relationship: Relationship,
        programUid: String?,
    ): MapItemModel? {
        val searchTei = d2.trackedEntityModule().trackedEntityInstances()
            .uid(teiUid)
            .blockingGet()

        val searchItem = d2.trackedEntityModule().trackedEntitySearch()
            .byTrackedEntityType().eq(searchTei?.trackedEntityType())
            .uid(teiUid)
            .blockingGet()

        return searchItem?.let {
            val attributeValues = trackedEntityInfoProvider.getTeiAdditionalInfoList(
                searchItem.attributeValues ?: emptyList(),
            )

            val model = MapItemModel(
                uid = searchItem.uid,
                avatarProviderConfiguration = trackedEntityInfoProvider.getAvatar(
                    searchTei!!,
                    programUid,
                    attributeValues.firstOrNull(),
                ),
                title = trackedEntityInfoProvider.getTeiTitle(
                    header = searchItem.header,
                    attributeValues = attributeValues,
                ),
                description = null,
                lastUpdated = trackedEntityInfoProvider.getTeiLastUpdated(searchItem),
                additionalInfoList = attributeValues,
                isOnline = false,
                geometry = searchItem.geometry,
                relatedInfo = trackedEntityInfoProvider.getRelatedInfo(
                    searchItem = searchItem,
                    selectedProgram = programUid?.let { d2.program(programUid) },
                ),
                state = relationship.syncState() ?: State.SYNCED,
            )
            trackedEntityInfoProvider.updateRelationshipInfo(model, relationship)
        }
    }

    override fun getTeiTypeDefaultRes(teiTypeUid: String?): Int {
        val teiType =
            d2.trackedEntityModule().trackedEntityTypes().uid(teiTypeUid).blockingGet()
        return resources.getObjectStyleDrawableResource(
            teiType?.style()?.icon(),
            R.drawable.photo_temp_gray,
        )
    }

    override fun getEventProgram(eventUid: String?): String {
        return d2.eventModule().events().uid(eventUid).blockingGet()?.program() ?: ""
    }
}

sealed class RelationshipConfiguration
data class TrackerRelationshipConfiguration(
    val enrollmentUid: String,
    val teiUid: String,
) : RelationshipConfiguration()

data class EventRelationshipConfiguration(
    val eventUid: String,
) : RelationshipConfiguration()
