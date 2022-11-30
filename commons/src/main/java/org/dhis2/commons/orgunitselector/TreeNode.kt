package org.dhis2.commons.orgunitselector

import java.util.UUID
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

data class TreeNode(
    var content: OrganisationUnit,
    var isOpen: Boolean = false,
    var hasChild: Boolean = false,
    var isChecked: Boolean = false,
    var level: Int = 0,
    var selectedChildrenCount: Int = 0
) {
    val id: String = UUID.randomUUID().toString()
    fun displayName(): String? {
        return if (selectedChildrenCount == 0) {
            content.displayName()
        } else {
            "${content.displayName()} ($selectedChildrenCount)"
        }
    }
}
