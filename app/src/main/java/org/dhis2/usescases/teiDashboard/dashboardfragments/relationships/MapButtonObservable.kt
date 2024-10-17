package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import androidx.lifecycle.LiveData
import org.dhis2.tracker.relationships.model.RelationshipTopBarIconState

interface MapButtonObservable {
    fun relationshipMap(): LiveData<Boolean>
    fun onRelationshipMapLoaded()
    fun updateRelationshipsTopBarIconState(topBarIconState: RelationshipTopBarIconState)
}
