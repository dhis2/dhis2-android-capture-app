package org.dhis2.commons.orgunitselector

import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class OUTreeRepository(
    private val orgUnitRepositoryConfiguration: OURepositoryConfiguration,
) {
    private var availableOrgUnits: List<OrganisationUnit> = emptyList()

    fun orgUnits(name: String? = null): List<OrganisationUnit> {
        availableOrgUnits = orgUnitRepositoryConfiguration.orgUnitRepository(name)
        return availableOrgUnits.order().sortedBy { it.displayNamePath()?.joinToString(" ") }
    }

    fun childrenOrgUnits(parentUid: String): List<OrganisationUnit> =
        availableOrgUnits
            .filter { it.uid() != parentUid && it.path()?.contains(parentUid) == true }
            .sortedBy { it.displayNamePath()?.joinToString(" ") }

    fun orgUnit(uid: String): OrganisationUnit? = availableOrgUnits.firstOrNull { it.uid() == uid }

    fun canBeSelected(orgUnitUid: String): Boolean = availableOrgUnits.any { it.uid() == orgUnitUid }

    fun orgUnitHasChildren(uid: String): Boolean = availableOrgUnits.filter { it.uid() != uid }.any { it.path()?.contains(uid) == true }

    fun countSelectedChildren(
        parentOrgUnitUid: String,
        selectedOrgUnits: List<String>,
    ): Int =
        orgUnitRepositoryConfiguration.countChildren(
            parentOrgUnitUid,
            selectedOrgUnits,
        )

    private fun List<OrganisationUnit>.order(): List<OrganisationUnit> {
        val listWithParents = this.toMutableList()
        val minLevel = minOfOrNull { it.level() ?: 0 }
        this.forEach { organisationUnit ->
            var isParentInParentList = false
            organisationUnit.path()?.split("/")?.filter { it.isNotEmpty() && it != organisationUnit.uid() }?.forEach { parentUid ->
                if (listWithParents.any { it.uid() == parentUid }) {
                    isParentInParentList = true
                }
            }
            if (!isParentInParentList && listWithParents.indexOf(organisationUnit) != 0) {
                listWithParents.remove(organisationUnit)
                listWithParents.add(0, organisationUnit.toBuilder().level(minLevel).build())
            }
        }
        return listWithParents
    }
}
