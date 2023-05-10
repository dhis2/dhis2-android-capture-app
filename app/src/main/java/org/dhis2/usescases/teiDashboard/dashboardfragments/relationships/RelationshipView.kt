package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import org.dhis2.commons.data.RelationshipViewModel
import org.dhis2.commons.data.tuples.Trio
import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.relationship.RelationshipType

interface RelationshipView : AbstractActivityContracts.View {

    fun setRelationships(relationships: List<RelationshipViewModel>)
    fun goToAddRelationship(teiUid: String, teiTypeUidToAdd: String)
    fun showPermissionError()
    fun openDashboardFor(teiUid: String)
    fun showTeiWithoutEnrollmentError(teiTypeName: String)
    fun showRelationshipNotFoundError(teiTypeName: String)
    fun initFab(relationshipTypes: MutableList<Trio<RelationshipType, String, Int>>)
    fun setFeatureCollection(
        currentTei: String?,
        relationshipsMapModels: List<org.dhis2.maps.model.RelationshipUiComponentModel>,
        map: Pair<Map<String, FeatureCollection>, BoundingBox>
    )

    fun openEventFor(eventUid: String, programUid: String)
}
