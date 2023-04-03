package org.dhis2.commons.orgunitselector

import org.dhis2.commons.bindings.addIf
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class OUTreeRepository(
    private val orgUnitRepositoryConfiguration: OURepositoryConfiguration
) {
    private var cachedOrgUnits: List<String> = emptyList()
    private var availableOrgUnits: List<String> = emptyList()

    fun orgUnits(name: String? = null): List<String> {
        val orgUnits =
            orgUnitRepositoryConfiguration.orgUnitRepository(name)

        val orderedList = mutableListOf<String>()
        val minLevel = orgUnits.minOfOrNull { it.level() ?: 0 }
        availableOrgUnits = orgUnits.map { it.uid() }
        cachedOrgUnits = availableOrgUnits
        orderList(
            orgUnits.filter { it.level() == minLevel },
            orderedList
        )

        return orderedList
    }

    private fun orderList(
        orgUnitToOrder: List<OrganisationUnit>,
        orderedList: MutableList<String>
    ) {
        orgUnitToOrder.forEach { org ->
            org.path()?.split("/")
                ?.filter { it.isNotEmpty() }
                ?.forEach { str ->
                    orderedList.addIf(!orderedList.contains(str), str)
                }
            if (orgUnitHasChildren(org.uid())) {
                orderList(childrenOrgUnits(org.uid()), orderedList)
            }
        }
    }

    fun childrenOrgUnits(parentUid: String? = null): List<OrganisationUnit> {
        val childrenOrgUnits = orgUnitRepositoryConfiguration.childrenOrgUnits(parentUid)
        return childrenOrgUnits.filter {
            cachedOrgUnits.contains(it.uid())
        }
    }

    fun orgUnit(uid: String): OrganisationUnit? =
        orgUnitRepositoryConfiguration.orgUnit(uid)

    fun canBeSelected(orgUnitUid: String): Boolean =
        availableOrgUnits.any { it == orgUnitUid }

    fun orgUnitHasChildren(uid: String): Boolean {
        return orgUnitRepositoryConfiguration.hasChildren(
            uid,
            availableOrgUnits.contains(uid)
        )
    }

    fun countSelectedChildren(
        parentOrgUnitUid: String,
        selectedOrgUnits: List<String>
    ): Int {
        return orgUnitRepositoryConfiguration.countChildren(
            parentOrgUnitUid,
            selectedOrgUnits
        )
    }
}
