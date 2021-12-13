package org.dhis2.maps.mapper

import org.dhis2.commons.data.RelationshipViewModel
import org.dhis2.maps.model.RelationshipDirection
import org.dhis2.maps.model.RelationshipUiComponentModel
import org.dhis2.maps.model.TeiMap

class MapRelationshipToRelationshipMapModel {

    fun mapList(
        relationshipViewModels: List<RelationshipViewModel>
    ): List<RelationshipUiComponentModel> {
        return relationshipViewModels
            .filter { it.toGeometry != null && it.fromGeometry != null }.mapNotNull { map(it) }
    }

    private fun map(relationshipViewModel: RelationshipViewModel): RelationshipUiComponentModel? {
        val relationshipUid = relationshipViewModel.relationship.uid()!!
        val displayName = relationshipViewModel.relationshipType.displayName()
        val typeUid = relationshipViewModel.relationshipType.uid()
        val bidirectional = relationshipViewModel.relationshipType.bidirectional()
        val direction =
            if (relationshipViewModel.isFrom()) {
                RelationshipDirection.FROM
            } else {
                RelationshipDirection.TO
            }

        val teiFromUid = relationshipViewModel.relationship.from()?.trackedEntityInstance()
            ?.trackedEntityInstance()
        val teiToUid = relationshipViewModel.relationship.to()?.trackedEntityInstance()
            ?.trackedEntityInstance()

        val fromAttr =
            relationshipViewModel.fromValues.firstOrNull()?.let { it.second ?: "-" } ?: "-"
        val toAttr =
            relationshipViewModel.toValues.firstOrNull()?.let { it.second ?: "-" } ?: "-"

        val teiFrom = TeiMap(
            teiFromUid,
            relationshipViewModel.fromGeometry,
            relationshipViewModel.fromImage,
            relationshipViewModel.fromDefaultImageResource,
            fromAttr
        )
        val teiTo = TeiMap(
            teiToUid,
            relationshipViewModel.toGeometry,
            relationshipViewModel.toImage,
            relationshipViewModel.toDefaultImageResource,
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
