package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import io.reactivex.Single
import org.dhis2.commons.data.RelationshipViewModel
import org.dhis2.maps.model.MapItemModel

interface RelationshipRepository {
    fun relationships(): Single<List<RelationshipViewModel>>
    fun mapRelationships(): Single<List<MapItemModel>>
    fun getTeiTypeDefaultRes(teiTypeUid: String?): Int
    fun getEventProgram(eventUid: String?): String
}
