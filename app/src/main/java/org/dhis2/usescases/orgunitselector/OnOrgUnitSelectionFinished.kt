package org.dhis2.usescases.orgunitselector

import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

interface OnOrgUnitSelectionFinished {
    fun onSelectionFinished(selectedOrgUnits: List<OrganisationUnit>)
}
