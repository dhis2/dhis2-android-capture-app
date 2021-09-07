package org.dhis2.usescases.orgunitselector

interface OUTreeView {
    fun setOrgUnits(organisationUnits: List<TreeNode>)
    fun getCurrentList(): List<TreeNode>
}
