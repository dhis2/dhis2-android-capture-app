package org.dhis2.uicomponents.map.mapper

import org.dhis2.uicomponents.map.model.RelationshipDirection
import org.dhis2.uicomponents.map.model.RelationshipUiComponentModel
import org.dhis2.uicomponents.map.model.TeiMap
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipViewModel

class MapRelationshipToRelationshipMapModel {

    fun mapList(
        relationshipViewModels: List<RelationshipViewModel>
    ): List<RelationshipUiComponentModel> {
        return relationshipViewModels
            .filter { it.toGeometry() != null && it.fromGeometry() != null }.mapNotNull { map(it) }
    }

    private fun map(relationshipViewModel: RelationshipViewModel): RelationshipUiComponentModel? {
        val relationshipUid = relationshipViewModel.relationship().uid()!!
        val displayName = relationshipViewModel.relationshipType().displayName()
        val typeUid = relationshipViewModel.relationshipType().uid()
        val bidirectional = relationshipViewModel.relationshipType().bidirectional()
        val direction =
            if (relationshipViewModel.relationshipDirection()
                == RelationshipViewModel.RelationshipDirection.TO
            ) {
                RelationshipDirection.TO
            } else {
                RelationshipDirection.FROM
            }

        val teiFromUid = relationshipViewModel.relationship().from()?.trackedEntityInstance()
            ?.trackedEntityInstance()
        val teiToUid = relationshipViewModel.relationship().to()?.trackedEntityInstance()
            ?.trackedEntityInstance()

        val fromAttr =
            relationshipViewModel.fromAttributes().firstOrNull()?.let { it.value() ?: "" } ?: ""
        val toAttr =
            relationshipViewModel.toAttributes().firstOrNull()?.let { it.value() ?: "" } ?: ""

        val teiFrom = TeiMap(
            teiFromUid,
            relationshipViewModel.fromGeometry(),
            relationshipViewModel.fromImage(),
            relationshipViewModel.fromDefaultImageResource(),
            fromAttr
        )
        val teiTo = TeiMap(
            teiToUid,
            relationshipViewModel.toGeometry(),
            relationshipViewModel.toImage(),
            relationshipViewModel.toDefaultImageResource(),
            toAttr
        )

        return RelationshipUiComponentModel(
            displayName,
            relationshipUid,
            typeUid,
            direction,
            bidirectional,
            teiFrom,
            teiTo
        )
    }
}
