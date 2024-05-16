package org.dhis2.maps.model

import org.dhis2.commons.data.CarouselItemModel
import org.dhis2.commons.data.RelationshipOwnerType
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry

data class RelationshipUiComponentModel(
    val displayName: String?,
    val relationshipUid: String,
    val relationshipTypeUid: String,
    val direction: RelationshipDirection,
    val bidirectional: Boolean?,
    val from: TeiMap,
    val to: TeiMap,
    val relationshipOwner: RelationshipOwnerType,
    val canBeDeleted: Boolean? = true,
) : CarouselItemModel {
    override fun uid(): String = relationshipUid
    fun shouldShowNavigationButton(): Boolean {
        return to.geometry?.type() == FeatureType.POINT
    }
}

data class TeiMap(
    val teiUid: String?,
    val geometry: Geometry?,
    val image: String?,
    val defaultImage: Int,
    val mainAttribute: String?,
)

enum class RelationshipDirection { FROM, TO }
