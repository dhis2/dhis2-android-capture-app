package org.dhis2.usescases.org_unit_selector

import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import java.util.*

data class TreeNode(
        var content: OrganisationUnit,
        var isOpen: Boolean = false,
        var hasChild: Boolean = false,
        var isChecked: Boolean = false,
        var level: Int = 0
) {
    val id: String = UUID.randomUUID().toString()
}