package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import org.dhis2.usescases.general.AbstractActivityContracts

interface RelationshipView : AbstractActivityContracts.View {

    fun goToAddRelationship(teiUid: String, teiTypeUidToAdd: String)
    fun showPermissionError()
    fun openDashboardFor(teiUid: String)
    fun showTeiWithoutEnrollmentError(teiTypeName: String)
    fun showRelationshipNotFoundError(teiTypeName: String)
    fun setFeatureCollection(
        currentTei: String?,
        relationshipsMapModels: List<org.dhis2.maps.model.RelationshipUiComponentModel>,
        map: Pair<Map<String, FeatureCollection>, BoundingBox>,
    )

    fun openEventFor(eventUid: String, programUid: String)
}
