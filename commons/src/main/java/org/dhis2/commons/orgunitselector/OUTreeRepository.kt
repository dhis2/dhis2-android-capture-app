package org.dhis2.commons.orgunitselector

import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class OUTreeRepository(
    private val orgUnitRepositoryConfiguration: OURepositoryConfiguration,
) {
    private var availableOrgUnits: List<OrganisationUnit> = emptyList()

    fun orgUnits(name: String? = null): List<OrganisationUnit> {
        availableOrgUnits = orgUnitRepositoryConfiguration.orgUnitRepository(name)
        return availableOrgUnits.withParents().sortedBy { it.displayNamePath()?.joinToString(" ") }
    }

    fun childrenOrgUnits(parentUid: String): List<OrganisationUnit> = availableOrgUnits
        .filter { it.uid() != parentUid && it.path()?.contains(parentUid) == true }
        .sortedBy { it.displayNamePath()?.joinToString(" ") }
    fun orgUnit(uid: String): OrganisationUnit? = availableOrgUnits.firstOrNull { it.uid() == uid }

    fun canBeSelected(orgUnitUid: String): Boolean =
        availableOrgUnits.any { it.uid() == orgUnitUid }

    fun orgUnitHasChildren(uid: String): Boolean =
        availableOrgUnits.filter { it.uid() != uid }.any { it.path()?.contains(uid) == true }

    fun countSelectedChildren(parentOrgUnitUid: String, selectedOrgUnits: List<String>): Int {
        return orgUnitRepositoryConfiguration.countChildren(
            parentOrgUnitUid,
            selectedOrgUnits,
        )
    }
    private fun List<OrganisationUnit>.withParents(): List<OrganisationUnit> {
        val listWithParents = this.toMutableList()
        this.forEach { organisationUnit ->
            organisationUnit.path()?.split("/")?.filter { it.isNotEmpty() }?.forEach { parentUid ->
                if (!listWithParents.any { it.uid() == parentUid }) {
                    orgUnitRepositoryConfiguration.orgUnit(parentUid)
                        ?.let { listWithParents.add(it) }
                }
            }
        }
        return listWithParents
    }
}
