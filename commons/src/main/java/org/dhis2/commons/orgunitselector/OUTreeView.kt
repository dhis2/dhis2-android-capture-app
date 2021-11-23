package org.dhis2.commons.orgunitselector

interface OUTreeView {
    fun setOrgUnits(organisationUnits: List<TreeNode>)
    fun getCurrentList(): List<TreeNode>
}
