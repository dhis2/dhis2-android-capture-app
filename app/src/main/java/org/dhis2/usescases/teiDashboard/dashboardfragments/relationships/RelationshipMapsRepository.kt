package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import org.dhis2.maps.model.MapItemModel
import org.dhis2.maps.model.RelatedInfo
import org.dhis2.tracker.relationships.model.RelationshipOwnerType
import org.hisp.dhis.android.core.relationship.Relationship

interface RelationshipMapsRepository {
    fun getEventProgram(eventUid: String?): String
    fun getRelatedInfo(
        ownerType: RelationshipOwnerType,
        ownerUid: String,
    ): RelatedInfo?

    fun addRelationshipInfo(mapItem: MapItemModel, relationship: Relationship): MapItemModel
}
