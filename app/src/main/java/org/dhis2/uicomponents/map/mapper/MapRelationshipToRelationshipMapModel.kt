package org.dhis2.uicomponents.map.mapper

import org.dhis2.uicomponents.map.model.RelationshipDirection
import org.dhis2.uicomponents.map.model.RelationshipMapModel
import org.dhis2.uicomponents.map.model.TeiMap
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipViewModel

class MapRelationshipToRelationshipMapModel {

    fun map(relationshipViewModel: RelationshipViewModel) : RelationshipMapModel? {
        if (relationshipViewModel.fromGeometry() == null ||
            relationshipViewModel.toGeometry() == null){
            return null
        }
        val displayName = relationshipViewModel.relationshipType().displayName()
        val typeUid = relationshipViewModel.relationshipType().uid()
        val bidirectional = relationshipViewModel.relationshipType().bidirectional()
        val direction = if (relationshipViewModel.relationshipDirection() == RelationshipViewModel.RelationshipDirection.TO){
            RelationshipDirection.TO
        } else {
            RelationshipDirection.FROM
        }

        val teiFromUid = relationshipViewModel.relationship().from()?.trackedEntityInstance()?.trackedEntityInstance()
        val teiToUid = relationshipViewModel.relationship().to()?.trackedEntityInstance()?.trackedEntityInstance()

        val teiFrom = TeiMap(teiFromUid, relationshipViewModel.fromGeometry())
        val teiTo = TeiMap(teiToUid, relationshipViewModel.toGeometry())

        return RelationshipMapModel(displayName, typeUid, direction, bidirectional, teiFrom, teiTo)
    }
}
