package org.dhis2.tracker.relationships.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Map
import androidx.compose.ui.graphics.vector.ImageVector

sealed class RelationshipTopBarIconState(
    val icon: ImageVector,
) {
    class List : RelationshipTopBarIconState(
        icon = Icons.AutoMirrored.Outlined.List,
    )

    class Map : RelationshipTopBarIconState(
        icon = Icons.Outlined.Map,
    )

    data class Selecting(val onClickListener: () -> Unit) : RelationshipTopBarIconState(
        icon = Icons.Outlined.DeleteOutline,
    )
}