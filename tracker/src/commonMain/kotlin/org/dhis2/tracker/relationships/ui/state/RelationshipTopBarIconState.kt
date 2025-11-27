package org.dhis2.tracker.relationships.ui.state

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Map
import androidx.compose.ui.graphics.vector.ImageVector

sealed class RelationshipTopBarIconState(
    val icon: ImageVector,
) {
    class List :
        RelationshipTopBarIconState(
            icon = Icons.Outlined.Map,
        )

    class Map :
        RelationshipTopBarIconState(
            icon = Icons.AutoMirrored.Outlined.List,
        )

    data class Selecting(
        val onClickListener: () -> Unit,
    ) : RelationshipTopBarIconState(
            icon = Icons.Outlined.DeleteOutline,
        )
}
