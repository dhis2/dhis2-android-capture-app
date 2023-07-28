package org.dhis2.ui.dialogs.orgunit

data class OrgUnitTreeItem(
    val uid: String,
    val label: String,
    var isOpen: Boolean = false,
    val hasChildren: Boolean = false,
    val selected: Boolean = false,
    val level: Int = 0,
    val selectedChildrenCount: Int = 0,
    val canBeSelected: Boolean = true
) {
    fun formattedLabel() = if (selectedChildrenCount == 0) {
        label
    } else {
        "$label ($selectedChildrenCount)"
    }
}
