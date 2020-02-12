package org.dhis2.usescases.orgunitselector

import org.dhis2.usescases.general.AbstractActivityContracts

interface OUTreeView : AbstractActivityContracts.View {

    fun setOrgUnits(organisationUnits: List<TreeNode>)
    fun addOrgUnits(location: Int, organisationUnits: List<TreeNode>)
}
