package org.dhis2.commons.orgunitselector

import org.dhis2.commons.bindings.addIf
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class OUTreeRepository(
    private val orgUnitRepositoryConfiguration: OURepositoryConfiguration
) {
    private var cachedOrgUnits: List<String> = emptyList()
    private var availableOrgUnits: List<String> = emptyList()
    private val parentOrgUnits: MutableList<String> = mutableListOf()

    fun orgUnits(name: String? = null): List<String> {
        val orgUnits =
            orgUnitRepositoryConfiguration.orgUnitRepository(name)
                .sortedWith(compareBy({ it.level() }, { it.parent()?.uid() }, { it.displayName() }))

        val orderedList = mutableListOf<String>()
        availableOrgUnits = orgUnits.map { it.uid() }

        orderList(
            orgUnits,
            orderedList
        )
        cachedOrgUnits = orderedList

        return cachedOrgUnits
    }

    private fun orderList(
        orgUnitToOrder: List<OrganisationUnit>,
        orderedList: MutableList<String>
    ) {
        orgUnitToOrder.forEach { org ->
            val parentPath = org.path()?.split("/")
            parentPath?.filter { it.isNotEmpty() }
                ?.forEach { str ->
                    if (str != org.uid()) {
                        parentOrgUnits.add(str)
                    }
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

    fun orgUnit(uid: String): OrganisationUnit? = orgUnitRepositoryConfiguration.orgUnit(uid)

    fun canBeSelected(orgUnitUid: String): Boolean = availableOrgUnits.any { it == orgUnitUid }

    fun orgUnitHasChildren(uid: String): Boolean {
        return parentOrgUnits.contains(uid) or orgUnitRepositoryConfiguration.hasChildren(
            uid,
            availableOrgUnits.contains(uid)
        )
    }

    fun countSelectedChildren(parentOrgUnitUid: String, selectedOrgUnits: List<String>): Int {
        return orgUnitRepositoryConfiguration.countChildren(
            parentOrgUnitUid,
            selectedOrgUnits
        )
    }
}
