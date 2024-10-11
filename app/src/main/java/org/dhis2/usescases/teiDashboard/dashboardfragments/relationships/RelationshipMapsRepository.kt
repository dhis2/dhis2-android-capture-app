package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import org.dhis2.commons.data.RelationshipOwnerType
import org.dhis2.maps.model.RelatedInfo

interface RelationshipMapsRepository {
    fun getEventProgram(eventUid: String?): String
    fun getRelatedInfo(
        ownerType: RelationshipOwnerType,
        ownerUid: String,
    ): RelatedInfo?
}
