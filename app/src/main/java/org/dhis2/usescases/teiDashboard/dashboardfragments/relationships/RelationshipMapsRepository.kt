package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import io.reactivex.Single
import org.dhis2.maps.model.MapItemModel

interface RelationshipMapsRepository {
    fun mapRelationships(): Single<List<MapItemModel>>
    fun getTeiTypeDefaultRes(teiTypeUid: String?): Int
    fun getEventProgram(eventUid: String?): String
}
